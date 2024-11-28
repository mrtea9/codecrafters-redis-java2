import client.SocketClient;
import configuration.Configuration;
import redis.Redis;
import store.Storage;
import rdb.RdbLoader;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.ThreadFactory;
import java.util.stream.Collectors;

public class Main {

    public static void main(String[] args) throws IOException {

        final ThreadFactory threadFactory = Thread.ofVirtual().factory();
        final Storage storage = new Storage();
        final Configuration configuration = new Configuration();

        for (int index = 0; index < args.length; index++) {
            final var key = args[index].substring(2);

            final var option = configuration.option(key);
            if (option == null) {
                System.err.println("unknown property: %s".formatted(key));
                continue;
            }

            final var argumentsCount = option.argumentsCount();
            for (int jndex = 0; jndex < argumentsCount; jndex++) {
                final var argumentValue = args[index + 1 + jndex];
                final var argument = option.argument(jndex);

                argument.set(argumentValue);
            }

            index += argumentsCount;
        }

        for (final var option : configuration.options()) {
            final var arguments = option.arguments()
                    .stream()
                    .map((argument -> "%s='%s'".formatted(argument.name(), argument.get())))
                    .collect(Collectors.joining(", "));

            System.out.println("configuration: %s(%s)".formatted(option.name(), arguments));
        }

        final Redis redis = new Redis(storage, configuration);

        final var directory = configuration.directory().pathArgument();
        final var databaseFilename = configuration.databaseFilename().pathArgument();

        System.out.println(directory.get());
        System.out.println(databaseFilename.get());
        if (directory.isSet() && databaseFilename.isSet()) {
          final var stringPath = directory.get() + "/" + databaseFilename.get();
          System.out.println(stringPath);
          final var path = Paths.get(directory.get(), databaseFilename.get());
          System.out.println(path.toString());

          System.out.println(Files.exists(path));
          if (Files.exists(path)) {
              System.out.println("este");
              RdbLoader.load(path, storage);
          }
        }

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
