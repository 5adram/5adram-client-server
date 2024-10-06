package highlowcardgame.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketAddress;

class MockSocket extends Socket {

  private final InputStream input;
  private final OutputStream output;
  private boolean isClosed = false;

  MockSocket(InputStream input, OutputStream output) {
    this.input = input;
    this.output = output;
  }

  @Override
  public void connect(SocketAddress endpoint) {}

  @Override
  public void connect(SocketAddress endpoint, int timeout) {}

  @Override
  public InputStream getInputStream() throws IOException {
    return input;
  }

  @Override
  public OutputStream getOutputStream() throws IOException {
    return output;
  }

  @Override
  public void close() {
    isClosed = true;
  }

  @Override
  public boolean isConnected() {
    return !isClosed;
  }

  @Override
  public boolean isBound() {
    return true;
  }

  @Override
  public boolean isClosed() {
    return isClosed;
  }
}
