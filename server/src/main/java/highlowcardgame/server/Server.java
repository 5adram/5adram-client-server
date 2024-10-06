package highlowcardgame.server;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory;
import highlowcardgame.Shell;
import highlowcardgame.communication.messages.GameStateNotification;
import highlowcardgame.communication.messages.JoinGameRequest;
import highlowcardgame.communication.messages.Message;
import highlowcardgame.communication.messages.PlayerJoinedNotification;
import highlowcardgame.game.GameState;
import highlowcardgame.game.Player;
import highlowcardgame.game.Deck;
import highlowcardgame.game.Card;
import highlowcardgame.game.HighLowCardGame;
import highlowcardgame.game.Score;
import highlowcardgame.game.observable.Observer;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Main class for the game server. The class starts the server sockets and delegates connection and
 * game handling to {@link ConnectionManager}.
 */
public class Server {
  private static final int DEFAULT_PORT = 4441;
  private GameState gameState;
  private final ConcurrentHashMap<String, ClientHandler> clients = new ConcurrentHashMap<>();
  private final HighLowCardGame game;
  private Card previousCard;

  /**
   * Main method for the server.
   *
   * @param args Commandline arguments
   */
  public static void main(final String[] args) {
    int port = DEFAULT_PORT;
    for (int i = 0; i < args.length; ++i) {
      switch (args[i]) {
        case "--port":
          {
            if (isLastArgument(i, args)) {
              printErrorMessage("Please specify the port number.");
              return;
            }
            try {
              i++;
              port = Integer.parseInt(args[i]);
            } catch (NumberFormatException e) {
              printErrorMessage("Invalid port number: " + args[i]);
              return;
            }
            if (!isValidPort(port)) {
              printErrorMessage("The port number should be in the range of 1024~65535.");
              return;
            }
            break;
          }
        case "--help":
        default:
          {
            printHelpMessage();
            return;
          }
      }
    }

    try (ServerSocket socket = new ServerSocket(port)) {
      Server server = new Server();
      server.start(socket);
    } catch (IOException e) {
      System.out.println("Connection lost. Shutting down: " + e.getMessage());
    }
  }

  private static boolean isLastArgument(int i, final String[] args) {
    return i == args.length - 1;
  }

  private static boolean isValidPort(int port) {
    return port >= 1024 && port <= 65535;
  }

  private static void printHelpMessage() {
    System.out.println("java Server [--port <int>] [--help]");
  }

  private static void printErrorMessage(String str) {

    System.out.println("Error! " + str);
  }

  /**
   * Default constructor for the Server class.
   */
  public Server() {
    Deck deck = new StandardDeck();
    this.game = new HighLowCardGame(deck);
    game.subscribe(new Observer() {
      @Override
      public void updateState(GameState state) {
        // Update logic
      }

      @Override
      public void updateNewPlayer(String playerName, GameState state) {
        // Update logic
      }

      @Override
      public void updateRemovedPlayer(String playerName, GameState state) {
        // Update logic
      }
    });
  }


  /**
   * Start method for running the server.
   * @param socket a socket which make the connection possible.
   * @throws IOException throws an Exception in the case of in or out put failures.
   */
  public void start(ServerSocket socket) throws IOException {
    try {
      while (true) {
        Socket s = socket.accept();
        ClientHandler handler = new ClientHandler(s, this);
        clients.put(s.getRemoteSocketAddress().toString(), handler);
        new Thread(handler).start();
      }
    } finally {
      socket.close();
    }
  }

  private void broadcastMessage(String message) throws IOException {
    for (ClientHandler handler : clients.values()) {
      handler.sendMessage(message);
    }
  }

  private class ClientHandler implements Runnable {
    private final Socket socket;
    private final Server server;
    private BufferedReader in;
    private OutputStreamWriter out;
    private Player player;
    private GameState currentState;

    public ClientHandler(Socket socket, Server server) {
      this.socket = socket;
      this.server = server;
      this.currentState = server.game.getState();
    }

    @Override
    public void run() {
      try {
        in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        out = new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8);

        // Read initial player info
        String initMessage = in.readLine();
        if (initMessage == null) {
          System.err.println("Received null initial message");
          return;
        }
        System.out.println("Received initial message: " + initMessage);
        processMessage(initMessage);

      } catch (IOException | JSONException e) {
        e.printStackTrace();
      } catch (Deck.NoNextCardException e) {
          throw new RuntimeException(e);
      } finally {
        try {
          socket.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
        server.game.removePlayer(player);
        server.clients.remove(socket.getRemoteSocketAddress().toString());
      }
    }

    private void processMessage(String message) throws JSONException, IOException, Deck.NoNextCardException {
      JSONObject jsonObject = new JSONObject(message);
      String messageType = jsonObject.getString("messageType");

      switch (messageType) {
        case "GuessRequest":
          handleGuess(jsonObject);
          String initMessage = in.readLine();
          processMessage(initMessage);
          break;
        case "JoinGameRequest":
          handleJoinGameRequest(message);
          String initMessage1 = in.readLine();
          processMessage(initMessage1);
          break;
        default:
          System.err.println("Unknown message type: " + messageType);
      }
    }

    private void handleJoinGameRequest(String message) throws IOException, Deck.NoNextCardException {
      Moshi moshi = new Moshi.Builder()
              .add(PolymorphicJsonAdapterFactory.of(Message.class, "messageType")
                      .withSubtype(JoinGameRequest.class, "JoinGameRequest"))
              .build();
      JsonAdapter<JoinGameRequest> joinGameRequestJsonAdapter = moshi.adapter(JoinGameRequest.class);
      JoinGameRequest joinGameRequest = joinGameRequestJsonAdapter.fromJson(message);
      if (joinGameRequest != null) {
        String playerName = joinGameRequest.getPlayerName();
        player = new SimplePlayer(playerName);

        server.game.addPlayer(player);

        if (server.game.getState().getPlayers().size() == 1) {
          server.game.start();
        }
        broadcastPlayerJoined(playerName, server.game.getState().getPlayers().size());
        sendGameState(server.game.getState());
      }
    }

    private void handleGuess(JSONObject jsonObject) throws JSONException, IOException, Deck.NoNextCardException {
      String guessStr = jsonObject.getString("guess");
      HighLowCardGame.Guess guess = HighLowCardGame.Guess.valueOf(guessStr);
      Card previousCard = server.previousCard;
      Card currentCard = game.getState().getCurrentCard();
      boolean isCorrectGuess = false;

      if (previousCard != null) {
        isCorrectGuess = isGuessCorrect(guess, previousCard, currentCard);
      }

      server.previousCard = currentCard;

      if (isCorrectGuess) {
        server.game.getState().getScores().get(player).increment(1);
      }

      game.guess(player, guess);


      System.out.println("Guess: " + guessStr);
      sendGameState(server.game.getState());
    }

    private boolean isGuessCorrect(HighLowCardGame.Guess guess, Card firstCard, Card secondCard) {
      return guess == getCorrectGuess(firstCard, secondCard);
    }

    private HighLowCardGame.Guess getCorrectGuess(Card firstCard, Card secondCard) {
      int comparison = firstCard.compareTo(secondCard);
      if (comparison == 0) {
        return HighLowCardGame.Guess.EQUAL;
      } else if (comparison > 0) {
        return HighLowCardGame.Guess.LOW;
      } else {
        return HighLowCardGame.Guess.HIGH;
      }
    }

    private void broadcastPlayerJoined(String playerName, int numPlayers) throws IOException {
      PlayerJoinedNotification notification = new PlayerJoinedNotification(playerName, numPlayers);
      JSONObject json = notification.toJSON();
      server.broadcastMessage(json.toString());
    }

    public void sendGameState(GameState state) throws IOException {
      for (Player player : state.getPlayers()) {
        String playerName = player.getName();
        int score = state.getScores().get(player).get();
        Card currentCard = state.getCurrentCard();
        int numRounds = state.getRound();

        GameStateNotification notification = new GameStateNotification(playerName, numRounds, currentCard, score);
        JSONObject json = notification.toJSON();

        sendMessage(json.toString());
      }
    }

    public void sendMessage(String message) throws IOException {
      out.write(message + System.lineSeparator());
      out.flush();
      System.out.println(message);
    }

    private class SimplePlayer implements Player {
      private final String name;

      public SimplePlayer(String name) {
        this.name = name;
      }

      @Override
      public String getName() {
        return name;
      }

      @Override
      public void updateState(GameState state) {
      }

      @Override
      public void updateNewPlayer(String playerName, GameState state) {
      }

      @Override
      public void updateRemovedPlayer(String playerName, GameState state) {
      }
    }
  }

  private class StandardDeck implements Deck {
    private List<Card> cards;
    private int currentIndex;

    public StandardDeck() {
      cards = new ArrayList<>(Card.getAllValidCards());
      Collections.shuffle(cards);
      currentIndex = 0;
    }

    @Override
    public Card getNextCard() throws NoNextCardException {
      if (!hasNextCard()) {
        throw new NoNextCardException("No more cards in the deck");
      }
      return cards.get(currentIndex++);
    }

    @Override
    public boolean hasNextCard() {
      return currentIndex < cards.size();
    }
  }
}