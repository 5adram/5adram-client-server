package highlowcardgame.server;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class MockInputStream extends InputStream {

  private ByteArrayInputStream toRead;
  private boolean done = false;

  public MockInputStream(String toRead) {
    this.toRead = new ByteArrayInputStream(toRead.getBytes(StandardCharsets.UTF_8));
  }

  public boolean isDone() {
    return done;
  }

  public void finish() {
    synchronized (this) {
      notifyAll();
    }
  }

  @Override
  public int read() throws IOException {
    if (done) {
      synchronized (this) {
        try {
          wait();
          System.out.println("Continue");
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    }
    int ret = toRead.read();
    if (ret < 0) {
      done = true;
    }
    return ret;
  }

  @Override
  public int available() throws IOException {
    return toRead.available();
  }
}
