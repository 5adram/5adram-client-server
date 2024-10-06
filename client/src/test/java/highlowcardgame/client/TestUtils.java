package highlowcardgame.client;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;

final class TestUtils {

  static PrintStream getDummyPrintStream() {
    return new PrintStream(
        new OutputStream() {
          @Override
          public void write(int b) {}
        });
  }

  static InputStream stringToInputStream(String str) {
    return new ByteArrayInputStream(str.getBytes());
  }
}
