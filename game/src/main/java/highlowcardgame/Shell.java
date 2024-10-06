package highlowcardgame;

import highlowcardgame.game.Card;
import highlowcardgame.game.Deck;
import highlowcardgame.game.GameState;
import highlowcardgame.game.HighLowCardGame;
import highlowcardgame.game.InfiniteShuffledDeck;
import highlowcardgame.game.Player;
import highlowcardgame.game.Score;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Shell implements Player {

  private HighLowCardGame game;
  private int lastSeenRound = 0;
  private int currentOutputLines = 1;
  private BufferedReader inputReader;

  public static void main(String[] args) throws Deck.NoNextCardException {
    new Shell().run();
  }

  public void run() throws Deck.NoNextCardException {
    inputReader = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
    game = new HighLowCardGame(new InfiniteShuffledDeck(Card.getAllValidCards()));
    game.start();
    game.addPlayer(this);
  }

  @Override
  public void updateState(GameState state) {
    print(state);
    int round = state.getRound();
    if (round > lastSeenRound) {
      print("Round " + round + " started");
    }
    lastSeenRound = round;
  }

  @Override
  public void updateNewPlayer(String playerName, GameState state) {
    updateState(state);
    print("Player joined: " + playerName);
  }

  @Override
  public void updateRemovedPlayer(String playerName, GameState state) {
    updateState(state);
    print("Player left: " + playerName);
  }

  private void print(String message) {
    int numberOfLines = message.split("\n").length;
    currentOutputLines += numberOfLines + 1;
    System.out.println(message);
  }

  private void print(GameState state) {
    List<Player> players = state.getPlayers().stream().sorted().collect(Collectors.toList());
    int round = state.getRound();
    Map<Player, HighLowCardGame.Guess> g = state.getGuesses();
    Map<Player, Score> scores = state.getScores();
    Card currentCard = state.getCurrentCard();

    StringBuilder msgBuilder = new StringBuilder();
    msgBuilder.append(String.format("Round: %8d%n", round));
    msgBuilder.append("Scores: {");
    for (Player p : players) {
      final Score playerScore = scores.get(p);
      msgBuilder.append(p.getName()).append(": ").append(playerScore.get());
    }
    msgBuilder.append("}\n----------\n");
    msgBuilder.append("Current card: ").append(cardtoString(currentCard));
    msgBuilder.append("\n");
    msgBuilder.append("Player guesses: { ");
    for (Player p : players) {
      msgBuilder.append(p.getName()).append(": ").append(g.get(p));
    }
    msgBuilder.append(" }\n");

    final String msg = msgBuilder.toString();
    resetOutput();
    print(msg);

    if (!g.containsKey(this)) {
      promptForGuess();
    }
  }

  private void resetOutput() {
    for (int i = 0; i < currentOutputLines; i++) {
      System.out.print("\033[1A\033[2K");
    }
    currentOutputLines = 1;
  }

  private void promptForGuess() {
    print("Enter Guess H/L/E:");
    HighLowCardGame.Guess guess = null;
    while (guess == null) {
      try {
        String input = inputReader.readLine();
        if (input == null) {
          throw new IllegalStateException("Game stopped");
        }

        switch (input) {
          case "H":
            guess = HighLowCardGame.Guess.HIGH;
            break;
          case "L":
            guess = HighLowCardGame.Guess.LOW;
            break;
          case "E":
            guess = HighLowCardGame.Guess.EQUAL;
            break;
          default:
            print("Invalid input. Enter one of H/L/E.");
            break;
        }

      } catch (IOException e) {
        throw new IllegalStateException(e);
      }
    }
    try {
      game.guess(this, guess);
    } catch (Deck.NoNextCardException e) {
      throw new AssertionError(e);
    }
  }

  private String cardtoString(Card currentCard) {
    return currentCard.getSuit().getCodeSuit() + String.format("%02d", currentCard.getValue());
  }

  @Override
  public String getName() {
    return "ShellPlayer";
  }
}
