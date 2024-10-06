package highlowcardgame.client;

import static com.google.common.truth.Truth.assertThat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

@Timeout(value = 5)
public class ClientAdvancedTest {

  private static final String USERNAME = "DummyUser";
  private static final String OTHER_USER = "Villain 1";
  private ByteArrayOutputStream out;

  @BeforeEach
  public void setUp() {
    out = new ByteArrayOutputStream();
    System.setOut(new PrintStream(out));
  }

  @AfterEach
  public void tearDown() throws IOException {
    out.close();
  }

  private void feedInput(String input) {
    System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
  }

  private String getOutput() {
    return out.toString();
  }

  private OutputStream getNetworkOut() {
    return new ByteArrayOutputStream();
  }

  private InputStream getNetworkIn(String input) {
    return new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
  }

  @Test
  public void testReceivePlayerGuess_otherPlayer_prints() throws InterruptedException {
    Client client = new Client();
    String playerJoined1 =
        "{\"messageType\":\"PlayerJoinedNotification\",\"newPlayerName\":\""
            + USERNAME
            + "\",\"numPlayers\":1}";
    String playerJoined2 =
        "{\"messageType\":\"PlayerJoinedNotification\",\"newPlayerName\":\""
            + OTHER_USER
            + "\",\"numPlayers\":2}";
    String playerGuess =
        "{\"messageType\":\"PlayerGuessedNotification\",\"numNotGuessedPlayers\":1,\"numRounds\":1,\"playerGuessed\":\""
            + OTHER_USER
            + "\"}";
    InputStream networkIn =
        getNetworkIn(
            playerJoined1
                + System.lineSeparator()
                + playerJoined2
                + System.lineSeparator()
                + playerGuess);
    OutputStream networkOut = getNetworkOut();
    MockSocket mockSocket = new MockSocket(networkIn, networkOut);

    try {
      client.start(USERNAME, mockSocket);
    } catch (IOException e) {
      // expected, because there's no more network messages
    }

    // wait in case some thread is still working
    Thread.sleep(500);
    String shellOutput = getOutput();
    assertThat(shellOutput)
        .endsWith(
            "<<<<<<<<< PlayerGuessedNotification <<<<<<<<<"
                + System.lineSeparator()
                + "<<< Player "
                + OTHER_USER
                + " just made his/her guess."
                + System.lineSeparator()
                + "<<< Waiting for the remaining 1 players to make their guesses."
                + System.lineSeparator());
  }

  @Test
  public void testReceivePlayerGuess_own_prints() throws InterruptedException {
    Client client = new Client();
    String playerJoined1 =
        "{\"messageType\":\"PlayerJoinedNotification\",\"newPlayerName\":\""
            + USERNAME
            + "\",\"numPlayers\":1}";
    String playerJoined2 =
        "{\"messageType\":\"PlayerJoinedNotification\",\"newPlayerName\":\""
            + OTHER_USER
            + "\",\"numPlayers\":2}";
    String playerGuess =
        "{\"messageType\":\"PlayerGuessedNotification\",\"numNotGuessedPlayers\":1,\"numRounds\":1,\"playerGuessed\":\""
            + USERNAME
            + "\"}";
    InputStream networkIn =
        getNetworkIn(
            playerJoined1
                + System.lineSeparator()
                + playerJoined2
                + System.lineSeparator()
                + playerGuess);
    OutputStream networkOut = getNetworkOut();
    MockSocket mockSocket = new MockSocket(networkIn, networkOut);

    try {
      client.start(USERNAME, mockSocket);
    } catch (IOException e) {
      // expected, because there's no more network messages
    }

    // wait in case some thread is still working
    Thread.sleep(500);
    String shellOutput = getOutput();
    assertThat(shellOutput)
        .endsWith(
            "<<<<<<<<< PlayerGuessedNotification <<<<<<<<<"
                + System.lineSeparator()
                + "<<< Player "
                + USERNAME
                + " just made his/her guess."
                + System.lineSeparator()
                + "<<< Waiting for the remaining 1 players to make their guesses."
                + System.lineSeparator());
  }

  @Test
  public void testReceivePlayerLeft_prints() throws InterruptedException {
    Client client = new Client();
    String playerJoined1 =
        "{\"messageType\":\"PlayerJoinedNotification\",\"newPlayerName\":\""
            + USERNAME
            + "\",\"numPlayers\":1}";
    String playerJoined2 =
        "{\"messageType\":\"PlayerJoinedNotification\",\"newPlayerName\":\""
            + OTHER_USER
            + "\",\"numPlayers\":2}";
    String playerLeft =
        "{\"messageType\":\"PlayerLeftNotification\",\"numPlayers\":1,\"playerName\":\""
            + OTHER_USER
            + "\"}";
    InputStream networkIn =
        getNetworkIn(
            playerJoined1
                + System.lineSeparator()
                + playerJoined2
                + System.lineSeparator()
                + playerLeft);
    OutputStream networkOut = getNetworkOut();
    MockSocket mockSocket = new MockSocket(networkIn, networkOut);

    try {
      client.start(USERNAME, mockSocket);
    } catch (IOException e) {
      // expected, because there's no more network messages
    }

    // wait in case some thread is still working
    Thread.sleep(500);
    String shellOutput = getOutput();
    assertThat(shellOutput)
        .endsWith(
            "<<<<<<<<< PlayerLeftNotification <<<<<<<<<"
                + System.lineSeparator()
                + "<<< Player "
                + OTHER_USER
                + " just left the game."
                + System.lineSeparator()
                + "<<< There are currently 1 active players."
                + System.lineSeparator());
  }
}
