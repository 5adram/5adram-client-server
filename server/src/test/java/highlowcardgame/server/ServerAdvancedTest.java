package highlowcardgame.server;

import static com.google.common.truth.Truth.assertThat;
import static highlowcardgame.server.TestUtils.getNetworkIn;
import static highlowcardgame.server.TestUtils.getNetworkOut;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

@Timeout(7)
public class ServerAdvancedTest {

  private static final String USER1 = "User_1";
  private static final String USER2 = "User_2";

  private void assertThatContainsNKeyValuePairs(String message, int numberOfPairsExpected) {
    assertThat(message).matches("[^:]*:[^:]*".repeat(numberOfPairsExpected));
  }

  @Test
  public void testServer_receivesGuessRequest_sendsPlayerGuessed()
      throws IOException, InterruptedException {
    String joinRequest1 = "{\"messageType\":\"JoinGameRequest\",\"playerName\":\"" + USER1 + "\"}";
    String joinRequest2 = "{\"messageType\":\"JoinGameRequest\",\"playerName\":\"" + USER2 + "\"}";
    String guessRequest2 =
        "{\"messageType\":\"GuessRequest\",\"guess\":\"HIGH\",\"playerName\":\"" + USER2 + "\"}";
    MockInputStream networkIn1 = getNetworkIn(joinRequest1);
    OutputStream networkOut1 = getNetworkOut();
    MockInputStream networkIn2 =
        getNetworkIn(joinRequest2 + System.lineSeparator() + guessRequest2);
    ByteArrayOutputStream networkOut2 = getNetworkOut();
    MockSocket mockSocket1 = new MockSocket(networkIn1, networkOut1);
    MockSocket mockSocket2 = new MockSocket(networkIn2, networkOut2);
    List<MockInputStream> inputs = List.of(networkIn1, networkIn2);
    MockServerSocket serverSocket = new MockServerSocket(List.of(mockSocket1, mockSocket2));

    TestUtils.startServer(serverSocket);
    do {
      Thread.sleep(10);
    } while (!TestUtils.areDone(inputs));
    Thread.sleep(Sleeps.SLEEP_BEFORE_TESTING);

    String sent = networkOut2.toString(StandardCharsets.UTF_8);
    String[] jsonMessages = sent.split(System.lineSeparator());
    for (String message : jsonMessages) {
      if (message.matches(".*\"messageType\"\\s*:\\s*\"PlayerGuessedNotification\".*")
          && message.matches(".*\"playerGuessed\"\\s*:\\s*\"" + USER2 + "\".*")) {
        assertThat(message).matches(".*\"numNotGuessedPlayers\"\\s*:\\s*1.*");
        assertThatContainsNKeyValuePairs(message, 3);
        return;
      }
    }
    fail("No PlayerGuessedNotification for " + USER2);
  }

  @Test
  public void testServer_clientUnreachable_sendsPlayerLeft()
      throws IOException, InterruptedException {
    String joinRequest1 = "{\"messageType\":\"JoinGameRequest\",\"playerName\":\"" + USER1 + "\"}";
    String joinRequest2 = "{\"messageType\":\"JoinGameRequest\",\"playerName\":\"" + USER2 + "\"}";
    MockInputStream networkIn1 = getNetworkIn(joinRequest1);
    ByteArrayOutputStream networkOut1 = getNetworkOut();
    MockInputStream networkIn2 = getNetworkIn(joinRequest2);
    OutputStream networkOut2 = getNetworkOut();
    MockSocket mockSocket1 = new MockSocket(networkIn1, networkOut1);
    MockSocket mockSocket2 = new MockSocket(networkIn2, networkOut2);
    List<MockInputStream> inputs = List.of(networkIn1, networkIn2);
    MockServerSocket serverSocket = new MockServerSocket(List.of(mockSocket1, mockSocket2));

    TestUtils.startServer(serverSocket);
    do {
      Thread.sleep(10);
    } while (!TestUtils.areDone(inputs));
    // give server enough time to actually try to read from networkIn2 after registering player
    Thread.sleep(Sleeps.SLEEP_BEFORE_TESTING);
    networkIn2.finish();
    Thread.sleep(Sleeps.SLEEP_BEFORE_TESTING);

    String sent = networkOut1.toString();
    String[] jsonMessages = sent.split(System.lineSeparator());
    for (String message : jsonMessages) {
      if (message.matches(".*\"messageType\"\\s*:\\s*\"PlayerLeftNotification\".*")
          && message.matches(".*\"playerName\"\\s*:\\s*\"" + USER2 + "\".*")) {
        assertThat(message).matches(".*\"numPlayers\"\\s*:\\s*1.*");
        assertThatContainsNKeyValuePairs(message, 3);
        return;
      }
    }
    fail("No PlayerLeftNotification for " + USER2);
  }

  @Test
  public void testServer_multipleClientConnections() throws IOException, InterruptedException {
    final int numUsers = 20;
    List<MockSocket> sockets = new ArrayList<>(numUsers);
    List<ByteArrayOutputStream> networkOutputs = new ArrayList<>(numUsers);
    List<MockInputStream> networkInputs = new ArrayList<>(numUsers);
    for (int i = 1; i <= numUsers; i++) {
      String user = "U" + i;
      String joinRequest = "{\"messageType\":\"JoinGameRequest\",\"playerName\":\"" + user + "\"}";
      MockInputStream networkIn = getNetworkIn(joinRequest);
      ByteArrayOutputStream networkOut = getNetworkOut();
      networkOutputs.add(networkOut);
      networkInputs.add(networkIn);
      sockets.add(new MockSocket(networkIn, networkOut));
    }
    MockServerSocket serverSocket = new MockServerSocket(sockets);

    TestUtils.startServer(serverSocket);
    do {
      Thread.sleep(10);
    } while (!TestUtils.areDone(networkInputs));
    Thread.sleep(Sleeps.SLEEP_BEFORE_TESTING);

    for (int i = 1; i <= networkOutputs.size(); i++) {
      boolean found = false;
      String sent = networkOutputs.get(i - 1).toString(StandardCharsets.UTF_8);
      String[] jsonMessages = sent.split(System.lineSeparator());
      for (String message : jsonMessages) {
        if (message.matches(".*\"messageType\"\\s*:\\s*\"PlayerJoinedNotification\".*")
            && message.matches(
                ".*\"newPlayerName\"\\s*:\\s*\"U" + networkOutputs.size() + "\".*")) {
          assertThat(message).matches(".*\"numPlayers\"\\s*:\\s*[0-9]+.*");
          assertThatContainsNKeyValuePairs(message, 3);
          found = true;
          break;
        }
      }
      Assertions.assertTrue(
          found,
          "User U"
              + i
              + " did not receive PlayerJoinedNotification for user U"
              + networkOutputs.size());
    }
  }
}
