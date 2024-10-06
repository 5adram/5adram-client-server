package highlowcardgame.server;

import static com.google.common.truth.Truth.assertThat;
import static highlowcardgame.server.TestUtils.getNetworkIn;
import static highlowcardgame.server.TestUtils.getNetworkOut;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

@Timeout(7)
public class ServerRequiredTest {

  private static final String USER1 = "User_1";

  private void assertThatContainsNKeyValuePairs(String message, int numberOfPairsExpected) {
    assertThat(message).matches("[^:]*:[^:]*".repeat(numberOfPairsExpected));
  }

  @Test
  public void testServer1_receivesJoinRequest_sendsPlayerJoined()
      throws IOException, InterruptedException {
    MockInputStream networkIn =
        getNetworkIn("{\"messageType\":\"JoinGameRequest\",\"playerName\":\"" + USER1 + "\"}");
    ByteArrayOutputStream networkOut = getNetworkOut();
    MockSocket mockSocket = new MockSocket(networkIn, networkOut);
    MockServerSocket serverSocket = new MockServerSocket(List.of(mockSocket));

    TestUtils.startServer(serverSocket);
    do {
      Thread.sleep(10);
    } while (!networkIn.isDone());
    Thread.sleep(Sleeps.SLEEP_BEFORE_TESTING);

    String sent = networkOut.toString(StandardCharsets.UTF_8);
    String[] jsonMessages = sent.split(System.lineSeparator());
    for (String message : jsonMessages) {
      if (message.matches(".*\"messageType\"\\s*:\\s*\"PlayerJoinedNotification\".*")
          && message.matches(".*\"newPlayerName\"\\s*:\\s*\"" + USER1 + "\".*")) {
        assertThat(message).matches(".*\"numPlayers\"\\s*:\\s*1.*");
        assertThatContainsNKeyValuePairs(message, 3);
        return;
      }
    }
    Assertions.fail("No PlayerJoinedNotification for player " + USER1);
  }

  @Test
  public void testServer2_receivesGuessRequestH_handlesGuessAndSendsGameUpdate()
      throws IOException, InterruptedException {
    String joinRequest = "{\"messageType\":\"JoinGameRequest\",\"playerName\":\"" + USER1 + "\"}";
    String guessRequest =
        "{\"messageType\":\"GuessRequest\",\"guess\":\"HIGH\",\"playerName\":\"" + USER1 + "\"}";
    MockInputStream networkIn = getNetworkIn(joinRequest + System.lineSeparator() + guessRequest);
    ByteArrayOutputStream networkOut = getNetworkOut();
    MockSocket mockSocket = new MockSocket(networkIn, networkOut);
    MockServerSocket serverSocket = new MockServerSocket(List.of(mockSocket));

    TestUtils.startServer(serverSocket);
    do {
      Thread.sleep(10);
    } while (!networkIn.isDone());
    Thread.sleep(Sleeps.SLEEP_BEFORE_TESTING);

    String sent = networkOut.toString(StandardCharsets.UTF_8);
    String[] jsonMessages = sent.split(System.lineSeparator());
    for (String message : jsonMessages) {
      if (message.matches(".*\"messageType\"\\s*:\\s*\"GameStateNotification\".*")
          && message.matches(".*\"playerName\"\\s*:\\s*\"" + USER1 + "\".*")
          && message.matches(".*\"numRounds\"\\s*:\\s*2.*")) {
        assertThat(message).matches(".*\"messageType\"\\s*:\\s*\"GameStateNotification\".*");
        assertThat(message).matches(".*\"playerName\"\\s*:\\s*\"" + USER1 + "\".*");
        assertThat(message).matches(".*\"currentCard\"\\s*:\\s*\\{[^}]+}.*");
        assertThat(message).matches(".*\"suit\"\\s*:\\s*\"(CLUBS|DIAMONDS|HEARTS|SPADES)\".*");
        assertThat(message).matches(".*\"value\"\\s*:\\s*([0-9]|1[0-3]).*");
        assertThat(message).matches(".*\"numRounds\"\\s*:\\s*2.*");
        assertThat(message).matches(".*\"score\"\\s*:\\s*(0|1|25).*");
        assertThatContainsNKeyValuePairs(message, 7);
        return;
      }
    }
    Assertions.fail("No GameStateNotification for player " + USER1 + " and round 2");
  }

  @Test
  public void testServer2_receivesGuessRequestL_handlesGuessAndSendsGameUpdate()
      throws IOException, InterruptedException {
    String joinRequest = "{\"messageType\":\"JoinGameRequest\",\"playerName\":\"" + USER1 + "\"}";
    String guessRequest =
        "{\"messageType\":\"GuessRequest\",\"guess\":\"LOW\",\"playerName\":\"" + USER1 + "\"}";
    MockInputStream networkIn = getNetworkIn(joinRequest + System.lineSeparator() + guessRequest);
    ByteArrayOutputStream networkOut = getNetworkOut();
    MockSocket mockSocket = new MockSocket(networkIn, networkOut);
    MockServerSocket serverSocket = new MockServerSocket(List.of(mockSocket));

    TestUtils.startServer(serverSocket);
    do {
      Thread.sleep(10);
    } while (!networkIn.isDone());
    Thread.sleep(Sleeps.SLEEP_BEFORE_TESTING);

    String sent = networkOut.toString(StandardCharsets.UTF_8);
    String[] jsonMessages = sent.split(System.lineSeparator());
    for (String message : jsonMessages) {
      if (message.matches(".*\"messageType\"\\s*:\\s*\"GameStateNotification\".*")
          && message.matches(".*\"playerName\"\\s*:\\s*\"" + USER1 + "\".*")
          && message.matches(".*\"numRounds\"\\s*:\\s*2.*")) {
        assertThat(message).matches(".*\"messageType\"\\s*:\\s*\"GameStateNotification\".*");
        assertThat(message).matches(".*\"playerName\"\\s*:\\s*\"" + USER1 + "\".*");
        assertThat(message).matches(".*\"currentCard\"\\s*:\\s*\\{[^}]+}.*");
        assertThat(message).matches(".*\"suit\"\\s*:\\s*\"(CLUBS|DIAMONDS|HEARTS|SPADES)\".*");
        assertThat(message).matches(".*\"value\"\\s*:\\s*([0-9]|1[0-3]).*");
        assertThat(message).matches(".*\"numRounds\"\\s*:\\s*2.*");
        assertThat(message).matches(".*\"score\"\\s*:\\s*[01].*");
        assertThatContainsNKeyValuePairs(message, 7);
        return;
      }
    }
    Assertions.fail("No GameStateNotification for player " + USER1 + " and round 2");
  }

  @Test
  public void testServer2_receivesGuessRequestE_handlesGuessAndSendsGameUpdate()
      throws IOException, InterruptedException {

    String joinRequest = "{\"messageType\":\"JoinGameRequest\",\"playerName\":\"" + USER1 + "\"}";
    String guessRequest =
        "{\"messageType\":\"GuessRequest\",\"guess\":\"EQUAL\",\"playerName\":\"" + USER1 + "\"}";
    MockInputStream networkIn = getNetworkIn(joinRequest + System.lineSeparator() + guessRequest);
    ByteArrayOutputStream networkOut = getNetworkOut();
    MockSocket mockSocket = new MockSocket(networkIn, networkOut);
    MockServerSocket serverSocket = new MockServerSocket(List.of(mockSocket));

    TestUtils.startServer(serverSocket);
    do {
      Thread.sleep(10);
    } while (!networkIn.isDone());
    Thread.sleep(Sleeps.SLEEP_BEFORE_TESTING);

    String sent = networkOut.toString(StandardCharsets.UTF_8);
    String[] jsonMessages = sent.split(System.lineSeparator());
    for (String message : jsonMessages) {
      if (message.matches(".*\"messageType\"\\s*:\\s*\"GameStateNotification\".*")
          && message.matches(".*\"playerName\"\\s*:\\s*\"" + USER1 + "\".*")
          && message.matches(".*\"numRounds\"\\s*:\\s*2.*")) {
        assertThat(message).matches(".*\"messageType\"\\s*:\\s*\"GameStateNotification\".*");
        assertThat(message).matches(".*\"playerName\"\\s*:\\s*\"" + USER1 + "\".*");
        assertThat(message).matches(".*\"currentCard\"\\s*:\\s*\\{[^}]+}.*");
        assertThat(message).matches(".*\"suit\"\\s*:\\s*\"(CLUBS|DIAMONDS|HEARTS|SPADES)\".*");
        assertThat(message).matches(".*\"value\"\\s*:\\s*([0-9]|1[0-3]).*");
        assertThat(message).matches(".*\"numRounds\"\\s*:\\s*2.*");
        assertThat(message).matches(".*\"score\"\\s*:\\s*[01].*");
        assertThatContainsNKeyValuePairs(message, 7);
        return;
      }
    }
    Assertions.fail("No GameStateNotification for player " + USER1 + " and round 2");
  }
}
