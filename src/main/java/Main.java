import java.util.concurrent.ThreadFactory;

public class Main {

  public static void main(String[] args) {

    final ThreadFactory threadFactory = Thread.ofVirtual().factory();

    for (int index = 0; index < args.length; index++) {
        final String key = args[index].substring(2);

        System.out.println(key);
    }
  }
}
