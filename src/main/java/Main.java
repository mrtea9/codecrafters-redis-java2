import configuration.Configuration;

import java.util.concurrent.ThreadFactory;

public class Main {

  public static void main(String[] args) {

    final ThreadFactory threadFactory = Thread.ofVirtual().factory();
    final Configuration configuration = new Configuration();

    final int port = configuration.port().argument(0, Integer.class).get();

    System.out.println(port);
  }
}
