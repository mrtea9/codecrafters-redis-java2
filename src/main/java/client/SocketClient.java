package client;

import command.CommandParser;
import command.CommandResponse;
import command.ParsedCommand;
import lombok.SneakyThrows;
import redis.Redis;
import serial.Deserializer;
import serial.Serializer;
import type.RBlob;
import type.RValue;
import util.TrackedInputStream;
import util.TrackedOutputStream;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class SocketClient implements Client, Runnable {

    private static final AtomicInteger ID_INCREMENT = new AtomicInteger();

    private final int id;
    private final Socket socket;
    private final Redis evaluator;
    private boolean connected;
    private Consumer<SocketClient> disconnectListener;
    private boolean replicate;
    private long offset = 0;
    private final BlockingQueue<CommandResponse> pendingCommands = new ArrayBlockingQueue<>(128, true);
    private Consumer<Object> replicateConsumer;
    private List<ParsedCommand> queuedCommands;

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

            while (!replicate) {
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

            if (replicate) {
                Thread.ofVirtual().start(new Runnable() {

                    @Override
                    @SneakyThrows
                    public void run() {
                        while (socket.isConnected()) {
                            RValue request = null;
                            try {
                                request = deserializer.read();
                            } catch (IOException e) {
                                System.out.println(e.getMessage());
                            }
                            if (request == null) break;

                            final var consumer = replicateConsumer;
                            if (consumer != null) consumer.accept(request);
                        }
                    }
                });
            }

            while (replicate && socket.isConnected()) {
                final var command = pendingCommands.poll(1, TimeUnit.MINUTES);
                if (command == null) {
                    continue;
                }

                Redis.log("%d: send command: %s".formatted(id, command));

                outputStream.begin();
                serializer.write(command.value());
                serializer.flush();

                if (command.value() instanceof RBlob) {
                    offset = 0;
                    Redis.log("%d: reset offset".formatted(id));
                } else {
                    offset += outputStream.count();
                    Redis.log("%d: offset: %d".formatted(id, offset));
                }
            }

        } catch (Exception exception) {
            Redis.error("%d: returned an error: %s".formatted(id, exception.getMessage()));

            final var writer = new StringWriter();
            exception.printStackTrace(new PrintWriter(writer));

            for (final var line : writer.getBuffer().toString().split("\n")) {
                Redis.error("%d:   %s".formatted(id, line.replace("\r", "")));
            }
        }
        Redis.log("%d: disconnected".formatted(id));

        synchronized (this) {
            connected = false;

            if (disconnectListener != null) disconnectListener.accept(this);
        }
    }

    public void command(CommandResponse value) {
        final var inserted = pendingCommands.offer(value);
        Redis.log("%d: queue command: %s - inserted?=%s newSize=%s".formatted(id, value, inserted, pendingCommands.size()));

        if (!inserted) {
            Redis.log("%d: retry queue command: %s".formatted(id, value));
            pendingCommands.add(value);
        }
    }

    public boolean onDisconnect(Consumer<SocketClient> listener) {
        synchronized (this) {
            if (!connected) return false;

            if (disconnectListener != null) return false;

            disconnectListener = listener;
            return true;
        }
    }

    public boolean isInTransaction() {
        return queuedCommands != null;
    }

    public void beginTransaction() {
        queuedCommands = new ArrayList<>();
    }

    public List<ParsedCommand> discardTransaction() {
        final var commands = queuedCommands;

        queuedCommands = null;

        return commands;
    }

    public boolean queueCommand(ParsedCommand command) {
        if (isInTransaction()) {
            queuedCommands.add(command);
            return true;
        }

        return false;
    }

    public static SocketClient cast(Client client) {
        if (client instanceof SocketClient socketClient) {
            return socketClient;
        }

        throw new UnsupportedOperationException("client must be a SocketClient");
    }

    public void setReplicate(boolean replicate) {
        this.replicate = replicate;
    }

    public long getOffset() {
        return offset;
    }

    public void setReplicateConsumer(Consumer<Object> replicateConsumer) {
        this.replicateConsumer = replicateConsumer;
    }
}
