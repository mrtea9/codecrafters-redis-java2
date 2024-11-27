package client;

import lombok.SneakyThrows;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicInteger;

public class SocketClient implements Client, Runnable {

    private static final AtomicInteger ID_INCREMENT = new AtomicInteger();

    private final int id;
    private boolean connected;
    private final Socket socket;

    public SocketClient(Socket socket) throws IOException {
        this.id = ID_INCREMENT.incrementAndGet();
        this.socket = socket;
    }

    @SneakyThrows
    @Override
    public void run() {
        connected = true;
        System.out.println("este");
    }
}
