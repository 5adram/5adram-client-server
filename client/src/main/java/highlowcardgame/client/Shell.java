package highlowcardgame.client;

import highlowcardgame.communication.messages.GameStateNotification;
import highlowcardgame.communication.messages.Message;
import highlowcardgame.communication.messages.PlayerGuessedNotification;
import highlowcardgame.communication.messages.PlayerJoinedNotification;
import highlowcardgame.communication.messages.PlayerLeftNotification;
import highlowcardgame.game.Card;
import highlowcardgame.game.HighLowCardGame.Guess;
import java.io.PrintStream;
import java.util.Scanner;

/** Class for handling user input and writing server messages to output. */
final class Shell {
  private static final String CLIENT_PROMPT = ">>>";
  private static final String SERVER_PROMPT = "<<<";

  private final Scanner scanner;
  private final PrintStream out;

  private int lastScore = 0;
  private Card lastSeenCard = null;

  /**
   * Creates a new Shell object with the given input- and output-streams.
   *
   * @param scanner the Scanner object to read the user's input
   * @param out the PrintStream stream to print the output to
   */
  Shell(Scanner scanner, PrintStream out) {
    this.scanner = scanner;
    this.out = out;
  }

  private void printServerBanner(Class<?> messageClass) {
    String str =
        SERVER_PROMPT.repeat(3)
            + " "
            + messageClass.getSimpleName()
            + " "
            + SERVER_PROMPT.repeat(3);
    out.println(str);
  }

  private void printClientBanner() {
    out.println(CLIENT_PROMPT.repeat(12));
  }

  private void printServerString(String str) {
    out.println(SERVER_PROMPT + " " + str);
  }

  private void printClientString(String str) {
    out.println(CLIENT_PROMPT + " " + str);
  }

  private void showInputGuessMessage() {
    printClientBanner();
    printClientString("Please input your guess: (H/L/E)");
  }

  private void showInvalidGuessMessage() {
    printClientBanner();
    printClientString("Invalid input! Please try again.");
  }

  /** Displays information contained in the given message. */
  void showServerMessage(Message message) {
    printServerBanner(message.getClass());
    if (message instanceof PlayerJoinedNotification) {
      printPlayerJoinedNotification((PlayerJoinedNotification) message);
    } else if (message instanceof PlayerLeftNotification) {
      printPlayerLeftNotification((PlayerLeftNotification) message);
    } else if (message instanceof PlayerGuessedNotification) {
      printPlayerGuessedNotification((PlayerGuessedNotification) message);
    } else if (message instanceof GameStateNotification) {
      printNewRoundNotification((GameStateNotification) message);
      showInputGuessMessage();
    } else {
      throw new AssertionError("Unknown message type!");
    }
  }

  private void printPlayerJoinedNotification(PlayerJoinedNotification notification) {
    printServerString("Player " + notification.getNewPlayerName() + " just joined the game.");
    printServerString("There are currently " + notification.getNumPlayers() + " active player(s).");
  }

  private void printPlayerLeftNotification(PlayerLeftNotification notification) {
    printServerString("Player " + notification.getPlayerName() + " just left the game.");
    printServerString("There are currently " + notification.getNumPlayers() + " active players.");
  }

  private void printPlayerGuessedNotification(PlayerGuessedNotification notification) {
    printServerString("Player " + notification.getPlayerName() + " just made his/her guess.");
    printServerString(
        "Waiting for the remaining "
            + notification.getNumNotGuessedPlayers()
            + " players to make their guesses.");
  }

  private void printNewRoundNotification(GameStateNotification notification) {
    if (notification.getNumRounds() != 1) {
      printRoundResult(notification);
    }
    lastSeenCard = notification.getCurrentCard();

    printServerString("Round " + notification.getNumRounds() + " just started.");
    printServerString(
        "Please guess if the next card is higher/lower than or equal to the current card "
            + notification.getCurrentCard()
            + ".");
  }

  private void printRoundResult(GameStateNotification notification) {
    String playerName = notification.getPlayerName();
    Card currentCard = notification.getCurrentCard();
    int newScore = notification.getScore();
    if (lastSeenCard != null) {
      printServerString(
          "The previous card is "
              + lastSeenCard
              + ", and the current card is "
              + currentCard
              + ".");

      if (newScore > lastScore) {
        printServerString("Congratulations Player " + playerName + ", your guess was correct!");
      } else {
        printServerString("Bad luck, Player " + playerName + "! Your guess was incorrect.");
      }
      printServerString("Now you have " + newScore + " points.");
      printServerString("");
    }
    lastScore = newScore;
  }

  /** Prompts the user for the High/Low/Equal guess. */
  Guess getUserInputGuess() {
    while (true) {
      showInputGuessMessage();

      String inputGuess = scanner.nextLine();
      switch (inputGuess) {
        case "H":
          {
            return Guess.HIGH;
          }
        case "L":
          {
            return Guess.LOW;
          }
        case "E":
          {
            return Guess.EQUAL;
          }
        default:
          showInputGuessMessage();
      }
    }
  }
}
