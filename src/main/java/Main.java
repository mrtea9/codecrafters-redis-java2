import client.SocketClient;
import configuration.Configuration;
import redis.Redis;
import store.Storage;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ThreadFactory;

public class Main {

  public static void main(String[] args) throws IOException {

    final ThreadFactory threadFactory = Thread.ofVirtual().factory();
    final Storage storage = new Storage();
    final Configuration configuration = new Configuration();

    final Redis redis = new Redis(storage, configuration);

    final int port = configuration.port().argument(0, Integer.class).get();
    System.out.println("port: %s".formatted(port));

    try (final ServerSocket serverSocket = new ServerSocket(port)) {
      serverSocket.setReuseAddress(true);

      while (true) {
        final Socket socket = serverSocket.accept();
        final var client = new SocketClient(socket, redis);

        final Thread thread = threadFactory.newThread(client);
        thread.start();
      }
    }
  }
}
