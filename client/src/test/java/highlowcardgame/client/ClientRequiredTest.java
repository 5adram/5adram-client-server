package highlowcardgame.client;

import static com.google.common.truth.Truth.assertThat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

@Timeout(value = 5)
public class ClientRequiredTest {

  private static final String USERNAME = "DummyUser";
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

  private void assertThatContainsNKeyValuePairs(String message, int numberOfPairsExpected) {
    assertThat(message).matches("[^:]*:[^:]*".repeat(numberOfPairsExpected));
  }

  @Test
  public void testClient1_start_sendsJoinRequest() throws InterruptedException {
    Client client = new Client();
    InputStream networkIn = getNetworkIn("");
    OutputStream networkOut = getNetworkOut();
    MockSocket mockSocket = new MockSocket(networkIn, networkOut);

    try {
      client.start(USERNAME, mockSocket);
    } catch (IOException e) {
      // expected, because networkIn will be exhausted quickly.
    }

    // wait in case some thread is still working
    Thread.sleep(500);
    String sent = networkOut.toString();
    String[] jsonMessages = sent.split(System.lineSeparator());
    String lastMessage = jsonMessages[jsonMessages.length - 1];
    assertThat(lastMessage).matches(".*\"messageType\"\\s*:\\s*\"JoinGameRequest\".*");
    assertThat(lastMessage).matches(".*\"playerName\"\\s*:\\s*\"" + USERNAME + "\".*");
    // Check that last message only contains two
    assertThatContainsNKeyValuePairs(lastMessage, 2);
  }

  @Test
  public void testClient2_receiveGameStateNotification_printsState()
      throws IOException, InterruptedException {
    Client client = new Client();
    String playerJoined =
        "{\"messageType\":\"PlayerJoinedNotification\",\"newPlayerName\":\""
            + USERNAME
            + "\",\"numPlayers\":1}";
    String stateUpdate =
        "{\"messageType\":\"GameStateNotification\",\"currentCard\":{\"suit\":\"DIAMONDS\", \"value\":1},\"numRounds\":1,\"playerName\":\""
            + USERNAME
            + "\",\"score\":0}";
    InputStream networkIn = getNetworkIn(playerJoined + System.lineSeparator() + stateUpdate);
    OutputStream networkOut = getNetworkOut();
    MockSocket mockSocket = new MockSocket(networkIn, networkOut);

    try {
      client.start(USERNAME, mockSocket);
    } catch (NoSuchElementException e) {
      // expected, because there's no user input.
    }

    // wait in case some thread is still working
    Thread.sleep(500);
    String shellOutput = getOutput();
    assertThat(shellOutput)
        .endsWith(
            "<<<<<<<<< GameStateNotification <<<<<<<<<"
                + System.lineSeparator()
                + "<<< Round 1 just started."
                + System.lineSeparator()
                + "<<< Please guess if the next card is higher/lower than or equal to the current card D01."
                + System.lineSeparator()
                + ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"
                + System.lineSeparator()
                + ">>> Please input your guess: (H/L/E)"
                + System.lineSeparator());
  }

  @Test
  public void testClient3_guessInputH_sendsGuessRequest() throws InterruptedException {
    feedInput("H");
    Client client = new Client();
    String playerJoined =
        "{\"messageType\":\"PlayerJoinedNotification\",\"newPlayerName\":\""
            + USERNAME
            + "\",\"numPlayers\":1}";
    String stateUpdate =
        "{\"messageType\":\"GameStateNotification\",\"currentCard\":{\"suit\":\"DIAMONDS\", \"value\":1},\"numRounds\":1,\"playerName\":\""
            + USERNAME
            + "\",\"score\":0}";
    InputStream networkIn = getNetworkIn(playerJoined + System.lineSeparator() + stateUpdate);
    OutputStream networkOut = getNetworkOut();
    MockSocket mockSocket = new MockSocket(networkIn, networkOut);

    try {
      client.start(USERNAME, mockSocket);
    } catch (IOException e) {
      // expected, because networkIn will be exhausted quickly.
    }

    // wait in case some thread is still working
    Thread.sleep(500);
    String sent = networkOut.toString();
    String[] jsonMessages = sent.split(System.lineSeparator());
    String lastMessage = jsonMessages[jsonMessages.length - 1];
    assertThat(lastMessage).matches(".*\"messageType\"\\s*:\\s*\"GuessRequest\".*");
    assertThat(lastMessage).matches(".*\"playerName\"\\s*:\\s*\"" + USERNAME + "\".*");
    assertThat(lastMessage).matches(".*\"guess\"\\s*:\\s*\"HIGH\".*");
    assertThatContainsNKeyValuePairs(lastMessage, 3);
  }

  @Test
  public void testClient3_guessInputL_sendsGuessRequest() throws InterruptedException {
    feedInput("L");
    Client client = new Client();
    String playerJoined =
        "{\"messageType\":\"PlayerJoinedNotification\",\"newPlayerName\":\""
            + USERNAME
            + "\",\"numPlayers\":1}";
    String stateUpdate =
        "{\"messageType\":\"GameStateNotification\",\"currentCard\":{\"suit\":\"DIAMONDS\", \"value\":1},\"numRounds\":1,\"playerName\":\""
            + USERNAME
            + "\",\"score\":0}";
    InputStream networkIn = getNetworkIn(playerJoined + System.lineSeparator() + stateUpdate);
    OutputStream networkOut = getNetworkOut();
    MockSocket mockSocket = new MockSocket(networkIn, networkOut);

    try {
      client.start(USERNAME, mockSocket);
    } catch (IOException e) {
      // expected, because networkIn will be exhausted quickly.
    }
    // wait in case some thread is still working
    Thread.sleep(500);
    String sent = networkOut.toString();
    String[] jsonMessages = sent.split(System.lineSeparator());
    String lastMessage = jsonMessages[jsonMessages.length - 1];
    assertThat(lastMessage).matches(".*\"messageType\"\\s*:\\s*\"GuessRequest\".*");
    assertThat(lastMessage).matches(".*\"playerName\"\\s*:\\s*\"" + USERNAME + "\".*");
    assertThat(lastMessage).matches(".*\"guess\"\\s*:\\s*\"LOW\".*");
    assertThatContainsNKeyValuePairs(lastMessage, 3);
  }

  @Test
  public void testClient3_guessInputE_sendsGuessRequest() throws InterruptedException {
    feedInput("E");
    Client client = new Client();
    String playerJoined =
        "{\"messageType\":\"PlayerJoinedNotification\",\"newPlayerName\":\""
            + USERNAME
            + "\",\"numPlayers\":1}";
    String stateUpdate =
        "{\"messageType\":\"GameStateNotification\",\"currentCard\":{\"suit\":\"DIAMONDS\", \"value\":1},\"numRounds\":1,\"playerName\":\""
            + USERNAME
            + "\",\"score\":0}";
    InputStream networkIn = getNetworkIn(playerJoined + System.lineSeparator() + stateUpdate);
    OutputStream networkOut = getNetworkOut();
    MockSocket mockSocket = new MockSocket(networkIn, networkOut);

    try {
      client.start(USERNAME, mockSocket);
    } catch (IOException e) {
      // expected, because networkIn will be exhausted quickly.
    }

    // wait in case some thread is still working
    Thread.sleep(500);
    String sent = networkOut.toString();
    String[] jsonMessages = sent.split(System.lineSeparator());
    String lastMessage = jsonMessages[jsonMessages.length - 1];
    assertThat(lastMessage).matches(".*\"messageType\"\\s*:\\s*\"GuessRequest\".*");
    assertThat(lastMessage).matches(".*\"playerName\"\\s*:\\s*\"" + USERNAME + "\".*");
    assertThat(lastMessage).matches(".*\"guess\"\\s*:\\s*\"EQUAL\".*");
    assertThatContainsNKeyValuePairs(lastMessage, 3);
  }

  @Test
  public void testClient4_receiveLaterGameStateNotification_printsState()
      throws IOException, InterruptedException {
    Client client = new Client();
    String playerJoined =
        "{\"messageType\":\"PlayerJoinedNotification\",\"newPlayerName\":\""
            + USERNAME
            + "\",\"numPlayers\":1}";
    String stateUpdate =
        "{\"messageType\":\"GameStateNotification\",\"currentCard\":{\"suit\":\"DIAMONDS\", \"value\":1},\"numRounds\":4,\"playerName\":\""
            + USERNAME
            + "\",\"score\":0}";
    InputStream networkIn = getNetworkIn(playerJoined + System.lineSeparator() + stateUpdate);
    OutputStream networkOut = getNetworkOut();
    MockSocket mockSocket = new MockSocket(networkIn, networkOut);

    try {
      client.start(USERNAME, mockSocket);
    } catch (NoSuchElementException e) {
      // expected, because there's no user input.
    }

    // wait in case some thread is still working
    Thread.sleep(500);
    String shellOutput = getOutput();
    assertThat(shellOutput)
        .endsWith(
            "<<<<<<<<< GameStateNotification <<<<<<<<<"
                + System.lineSeparator()
                + "<<< Round 4 just started."
                + System.lineSeparator()
                + "<<< Please guess if the next card is higher/lower than or equal to the current card D01."
                + System.lineSeparator()
                + ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"
                + System.lineSeparator()
                + ">>> Please input your guess: (H/L/E)"
                + System.lineSeparator());
  }

  @Test
  public void testClient5_receiveNewRoundGameStateNotificationLoss_printsState()
      throws IOException, InterruptedException {
    feedInput("L");
    Client client = new Client();
    String playerJoined =
        "{\"messageType\":\"PlayerJoinedNotification\",\"newPlayerName\":\""
            + USERNAME
            + "\",\"numPlayers\":1}";
    String stateUpdate1 =
        "{\"messageType\":\"GameStateNotification\",\"currentCard\":{\"suit\":\"DIAMONDS\", \"value\":1},\"numRounds\":1,\"playerName\":\""
            + USERNAME
            + "\",\"score\":0}";
    String stateUpdate2 =
        "{\"messageType\":\"GameStateNotification\",\"currentCard\":{\"suit\":\"HEARTS\", \"value\":13},\"numRounds\":2,\"playerName\":\""
            + USERNAME
            + "\",\"score\":0}";
    InputStream networkIn =
        getNetworkIn(
            playerJoined
                + System.lineSeparator()
                + stateUpdate1
                + System.lineSeparator()
                + stateUpdate2);
    OutputStream networkOut = getNetworkOut();
    MockSocket mockSocket = new MockSocket(networkIn, networkOut);

    try {
      client.start(USERNAME, mockSocket);
    } catch (NoSuchElementException e) {
      // expected, because there's no user input.
    }

    // wait in case some thread is still working
    Thread.sleep(500);
    String shellOutput = getOutput();
    assertThat(shellOutput)
        .endsWith(
            "<<<<<<<<< GameStateNotification <<<<<<<<<"
                + System.lineSeparator()
                + "<<< The previous card is D01, and the current card is H13."
                + System.lineSeparator()
                + "<<< Bad luck, Player DummyUser! Your guess was incorrect."
                + System.lineSeparator()
                + "<<< Now you have 0 points."
                + System.lineSeparator()
                + "<<< "
                + System.lineSeparator()
                + "<<< Round 2 just started."
                + System.lineSeparator()
                + "<<< Please guess if the next card is higher/lower than or equal to the current card H13."
                + System.lineSeparator()
                + ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"
                + System.lineSeparator()
                + ">>> Please input your guess: (H/L/E)"
                + System.lineSeparator());
  }

  @Test
  public void testClient5_receiveNewRoundGameStateNotificationWin_printsState()
      throws IOException, InterruptedException {
    feedInput("H");
    Client client = new Client();
    String playerJoined =
        "{\"messageType\":\"PlayerJoinedNotification\",\"newPlayerName\":\""
            + USERNAME
            + "\",\"numPlayers\":1}";
    String stateUpdate1 =
        "{\"messageType\":\"GameStateNotification\",\"currentCard\":{\"suit\":\"DIAMONDS\", \"value\":1},\"numRounds\":1,\"playerName\":\""
            + USERNAME
            + "\",\"score\":0}";
    String stateUpdate2 =
        "{\"messageType\":\"GameStateNotification\",\"currentCard\":{\"suit\":\"HEARTS\", \"value\":13},\"numRounds\":2,\"playerName\":\""
            + USERNAME
            + "\",\"score\":1}";
    InputStream networkIn =
        getNetworkIn(
            playerJoined
                + System.lineSeparator()
                + stateUpdate1
                + System.lineSeparator()
                + stateUpdate2);
    OutputStream networkOut = getNetworkOut();
    MockSocket mockSocket = new MockSocket(networkIn, networkOut);

    try {
      client.start(USERNAME, mockSocket);
    } catch (NoSuchElementException e) {
      // expected, because there's no user input.
    }

    // wait in case some thread is still working
    Thread.sleep(500);
    String shellOutput = getOutput();
    assertThat(shellOutput)
        .endsWith(
            "<<<<<<<<< GameStateNotification <<<<<<<<<"
                + System.lineSeparator()
                + "<<< The previous card is D01, and the current card is H13."
                + System.lineSeparator()
                + "<<< Congratulations Player DummyUser, your guess was correct!"
                + System.lineSeparator()
                + "<<< Now you have 1 points."
                + System.lineSeparator()
                + "<<< "
                + System.lineSeparator()
                + "<<< Round 2 just started."
                + System.lineSeparator()
                + "<<< Please guess if the next card is higher/lower than or equal to the current card H13."
                + System.lineSeparator()
                + ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"
                + System.lineSeparator()
                + ">>> Please input your guess: (H/L/E)"
                + System.lineSeparator());
  }
}
