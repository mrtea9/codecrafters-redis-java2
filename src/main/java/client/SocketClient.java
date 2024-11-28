package client;

import lombok.SneakyThrows;
import redis.Redis;
import serial.Deserializer;
import serial.Serializer;
import util.TrackedInputStream;
import util.TrackedOutputStream;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicInteger;

public class SocketClient implements Client, Runnable {

    private static final AtomicInteger ID_INCREMENT = new AtomicInteger();

    private final int id;
    private boolean connected;
    private final Socket socket;
    private final Redis evaluator;

    public SocketClient(Socket socket, Redis evaluator) throws IOException {
        this.id = ID_INCREMENT.incrementAndGet();
        this.socket = socket;
        this.evaluator = evaluator;
    }

    @SneakyThrows
    @Override
    public void run() {
        connected = true;
        Redis.log("%d: connected".formatted(id));

        try (socket) {
            final var inputStream = new TrackedInputStream(socket.getInputStream());
            final var outputStream = new TrackedOutputStream(socket.getOutputStream());

            final var deserializer = new Deserializer(inputStream);
            final var serializer = new Serializer(outputStream);

            while (true) {
                inputStream.begin();

                final var request = deserializer.read();
                if (request == null) return;

                final var read = inputStream.count();

                Redis.log("%d: received (%d): %s".formatted(id, read, request));
                final var response = evaluator.evaluate(this, request, read);

                if (response == null) {
                    Redis.log("%d: no response".formatted(id));
                } else {
                    Redis.log("%d: responding: %s".formatted(id, response));
                    serializer.write(response.value());
                }

                outputStream.flush();
            }

        } catch (Exception exception) {
            Redis.error("%d: returned an error: %s".formatted(id, exception.getMessage()));

            final var writer = new StringWriter();
            exception.printStackTrace(new PrintWriter(writer));

            for (final var line : writer.getBuffer().toString().split("\n")) {
                Redis.error("%d:   %s".formatted(id, line.replace("\r", "")));
            }
        }
    }
}
