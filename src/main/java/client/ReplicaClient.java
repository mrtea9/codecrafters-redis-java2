package client;

import lombok.SneakyThrows;
import rdb.RdbLoader;
import redis.Redis;
import serial.Deserializer;
import serial.Serializer;
import type.RArray;
import type.RBlob;
import type.RString;
import type.RValue;
import util.TrackedInputStream;
import util.TrackedOutputStream;

import java.io.IOException;
import java.net.Socket;

public class ReplicaClient implements Client, Runnable {

    private final Socket socket;
    private final Redis redis;

    private final TrackedInputStream inputStream;
    private final Deserializer deserializer;
    private final Serializer serializer;

    public ReplicaClient(Socket socket, Redis redis) throws IOException {
        this.socket = socket;
        this.redis = redis;

        inputStream = new TrackedInputStream(socket.getInputStream());
        final var outputStream = socket.getOutputStream();

        deserializer = new Deserializer(inputStream);
        serializer = new Serializer((TrackedOutputStream) outputStream);
    }

    @SneakyThrows
    @Override
    public void run() {
        try (socket) {
            handshake(deserializer, serializer);

            while (true) {
                inputStream.begin();

                final var request = deserializer.read();
                if (request == null) break;

                final var read = inputStream.count();

                Redis.log("replica: received (%s): %s".formatted(read, request));
                final var response = redis.evaluate(this, request, read);

                if (response == null) {
                    Redis.log("replica: no response");
                    continue;
                } else {
                    Redis.log("replica: responding: %s".formatted(response));

                    if (!response.ignorableByReplica()) {
                        serializer.write(response.value());
                    }
                }

                serializer.flush();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Redis.log("replica: disconnected");
    }

    @SneakyThrows
    private void handshake(Deserializer deserializer, Serializer serializer) throws IOException {
        send(RArray.of(
                RString.bulk("PING")
        ));

        final var port = redis.getConfiguration().port().argument(0, Integer.class).get();

        send(RArray.of(
                RString.bulk("REPLCONF"),
                RString.bulk("listening-port"),
                RString.bulk(String.valueOf(port))
        ));

        send(RArray.of(
                RString.bulk("REPLCONF"),
                RString.bulk("capa"),
                RString.bulk("psync2")
        ));

        send(RArray.of(
                RString.bulk("PSYNC"),
                RString.bulk("?"),
                RString.bulk("-1")
        ));

        final var rdb = (RBlob) deserializer.read(true);
        redis.getStorage().clear();
        RdbLoader.load(rdb.inputStream(), redis.getStorage());
    }

    @SneakyThrows
    public Object send(RArray<RValue> command) throws IOException {
        Redis.log("replica: sending: %s".formatted(command));
        serializer.write(command);

        final var answer = deserializer.read();
        Redis.log("replica: received: %s".formatted(answer));

        return answer;
    }
}
