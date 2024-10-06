package highlowcardgame.client;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import highlowcardgame.communication.messages.*;
import highlowcardgame.game.HighLowCardGame;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;


import static java.lang.System.out;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Network client to play a {@link HighLowCardGame}.
 */

public final class Client {
  private static final int DEFAULT_PORT = 4441;
  private static final String DEFAULT_ADDRESS = "localhost";
  private static final String DEFAULT_USERNAME = System.getProperty("user.name");
 // private BufferedReader userInputReader = new BufferedReader(new InputStreamReader(System.in));
  private Scanner userInputReader = new Scanner(System.in);
  private HighLowCardGame.Guess guess;


  /**
   * Default constructor for the Client class.
   */
  public Client() {
    // Default constructor
  }

  /**
   * Entry to <code>Client</code>.
   *
   * @param args command-line arguments
   */
  public static void main(String[] args) {
    // parse arguments
    String username = DEFAULT_USERNAME;
    String serverAddress = DEFAULT_ADDRESS;
    int port = DEFAULT_PORT;
    for (int i = 0; i < args.length; ++i) {
      switch (args[i]) {
        case "--username": {
          if (isLastArgument(i, args)) {
            printErrorMessage("Please specify the username.");
            return;
          }
          ++i;
          username = args[i];
          break;
        }
        case "--address": {
          if (isLastArgument(i, args)) {
            printErrorMessage("Please specify the server address.");
            return;
          }
          ++i;
          serverAddress = args[i];
          break;
        }
        case "--port": {
          if (isLastArgument(i, args)) {
            printErrorMessage("Please specify the port number.");
            return;
          }
          try {
            ++i;
            port = Integer.parseInt(args[i]);
          } catch (NumberFormatException e) {
            printErrorMessage("Invalid port number: " + args[i]);
            return;
          }
          break;
        }
        case "--help":
        default: {
          printHelpMessage();
          return;
        }
      }
    }

    // check validity
    if (!isValidName(username)) {
      printErrorMessage("Invalid username: " + username);
      return;
    }

    InetAddress inetAddress = null;
    try {
      inetAddress = InetAddress.getByName(serverAddress);
    } catch (UnknownHostException e) {
      printErrorMessage("Invalid server address: " + serverAddress);
      return;
    }
    assert inetAddress != null;

    if (!isValidPort(port)) {
      printErrorMessage("The port number should be in the range of 1024~65535.");
      return;
    }

    // start a client
    InetSocketAddress address = new InetSocketAddress(inetAddress, port);

    Client client = new Client();
    try (Socket socket = new Socket(address.getAddress(), address.getPort())) {
      client.start(username, socket);
    } catch (IOException e) {
      out.println("Connection lost. Shutting down: " + e.getMessage());
    }
  }

  private static boolean isLastArgument(int i, final String[] args) {
    return i == args.length - 1;
  }

  private static boolean isValidPort(int port) {
    return port >= 1024 && port <= 65535;
  }

  private static boolean isValidName(String username) {
    return username != null && !username.isBlank();
  }
  /**
   * Prints the help message for the Client application.
   */
  public static void printHelpMessage() {
    out.println(
            "java Client [--username <String>] [--address <String>] [--port <int>] [--help]");
  }

  private static void printErrorMessage(String str) {
    out.println("Error! " + str);
  }

  /**
   * A method that starts the client.
   * @param username the username of player
   * @param socket the socket connection
   * @throws IOException throw an Exception in the case of input and output problem.
   */
  public void start(String username, Socket socket) throws IOException {

    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), UTF_8));
    OutputStreamWriter out = new OutputStreamWriter(socket.getOutputStream(), UTF_8);


    // Create a JoinGameRequest object
    JoinGameRequest joinGameRequest = new JoinGameRequest(username);

    // Set up Moshi for serialization
    Moshi moshi = new Moshi.Builder().build();
    JsonAdapter<JoinGameRequest> joinGameRequestJsonAdapter = moshi.adapter(JoinGameRequest.class);

    // Serialize JoinGameRequest to JSON
    String json = joinGameRequestJsonAdapter.toJson(joinGameRequest);

    // Add messageType to the JSON object
    JSONObject jsonObject = new JSONObject(json);
    jsonObject.put("messageType", "JoinGameRequest");

    // send message to server
    out.write(jsonObject.toString() + System.lineSeparator());
    out.flush();

    Shell shell = new Shell(userInputReader, System.out);
    // waiting for the response from server


    String line = in.readLine();
    while (line != null) {
      processMessage(line, moshi, out, username,shell);
      line = in.readLine();
    }

    socket.close();
  }


    private void processMessage (String line, Moshi moshi, OutputStreamWriter out, String username,Shell shell) throws IOException {
      JSONObject jsonObject = new JSONObject(line);
      String messageType = jsonObject.getString("messageType");

      switch (messageType) {
        case "PlayerJoinedNotification":
          JsonAdapter<PlayerJoinedNotification> playerJoinedAdapter = moshi.adapter(PlayerJoinedNotification.class);
          PlayerJoinedNotification playerJoinedNotification = playerJoinedAdapter.fromJson(line);
          shell.showServerMessage(playerJoinedNotification);
          break;

        case "GameStateNotification":
          JsonAdapter<GameStateNotification> gameStateAdapter = moshi.adapter(GameStateNotification.class);
          GameStateNotification gameStateNotification = gameStateAdapter.fromJson(line);
          shell.showServerMessage(gameStateNotification);

          handleGuess(gameStateNotification, out, moshi, username);
          break;

        // Handle other message types...
        case "PlayerGuessedNotification":
          JsonAdapter<PlayerGuessedNotification> playerGuessedAdapter = moshi.adapter(PlayerGuessedNotification.class);
          PlayerGuessedNotification playerGuessedNotification = playerGuessedAdapter.fromJson(line);
          shell.showServerMessage(playerGuessedNotification);
          break;

        case "PlayerLeftNotification":
          JsonAdapter<PlayerLeftNotification> playerLeftAdapter = moshi.adapter(PlayerLeftNotification.class);
          PlayerLeftNotification playerLeftNotification = playerLeftAdapter.fromJson(line);
          shell.showServerMessage(playerLeftNotification);
          break;

        default:
          System.err.println("Unknown message type: " + messageType);
          break;
      }
    }

  private void handleGuess(GameStateNotification notification, OutputStreamWriter out, Moshi moshi, String username) throws IOException {
    String guessInput = userInputReader.nextLine().trim().toUpperCase();

    while (!(guessInput.equals("H") || guessInput.equals("L") || guessInput.equals("E"))) {
      System.err.println("Invalid guess. Please enter H, L, or E.");
      guessInput = userInputReader.nextLine().trim().toUpperCase();
    }
    if (guessInput.equals("H")) {
      guess = HighLowCardGame.Guess.HIGH;
    } else if (guessInput.equals("L")) {
      guess = HighLowCardGame.Guess.LOW;
    } else if (guessInput.equals("E")) {
      guess = HighLowCardGame.Guess.EQUAL;
    }

    GuessRequest guessRequest = new GuessRequest(guess, username);
    JsonAdapter<GuessRequest> guessRequestJsonAdapter = moshi.adapter(GuessRequest.class);
    String guessJson = guessRequestJsonAdapter.toJson(guessRequest);
    JSONObject guessJsonObject = new JSONObject(guessJson);
    guessJsonObject.put("messageType", "GuessRequest");

    out.write(guessJsonObject.toString() + System.lineSeparator());
    out.flush();

    System.out.println("Sent guess to server: " + guessJsonObject.toString());
  }
}