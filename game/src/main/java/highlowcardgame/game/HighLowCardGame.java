package highlowcardgame.game;

import highlowcardgame.game.Deck.NoNextCardException;
import highlowcardgame.game.observable.Observable;
import highlowcardgame.game.observable.Observer;
import highlowcardgame.game.observable.ObserverSupport;
import java.util.List;
import java.util.Map;

/** The high-low card game logic */
public class HighLowCardGame implements Observable {

  private final ObserverSupport observerSupport = new ObserverSupport();

  static final Map<Guess, Integer> GUESS_SCORE =
      Map.of(
          HighLowCardGame.Guess.HIGH,
          1,
          HighLowCardGame.Guess.LOW,
          1,
          HighLowCardGame.Guess.EQUAL,
          25);

  /** Represents different types of guess */
  public enum Guess {
    HIGH,
    LOW,
    EQUAL
  }

  private volatile GameState state;

  /** Creates a new HighLowCardGame */
  public HighLowCardGame(Deck deck) {
    state = new GameState(deck);
  }

  public void start() throws NoNextCardException {
    state = state.nextRound(state.getDeck().getNextCard());
    notifyAboutState(state);
  }

  public void addPlayer(Player player) {
    if (!isValidPlayerName(player.getName())) {
      throw new AssertionError(
          "An invalid player name reached the game logic. This should be handled before. Name: "
              + player.getName());
    }
    synchronized (this) {
      state = state.addPlayer(player);
      observerSupport.subscribe(player);
      notifyAboutNewPlayer(player.getName(), state);
    }
  }

  private boolean isValidPlayerName(String playerName) {
    if (playerName == null || playerName.isBlank()) {
      return false;
    }
    for (Player p : state.getPlayers()) {
      if (p.getName().equals(playerName)) {
        return false;
      }
    }
    return true;
  }

  public void removePlayer(Player player) {
    synchronized (this) {
      state = state.removePlayer(player);
      observerSupport.unsubscribe(player);
      notifyAboutRemovedPlayer(player.getName(), state);
    }
  }

  public void guess(Player player, Guess guess) throws NoNextCardException {
    synchronized (this) {
      state = state.addGuess(player, guess);
      if (hasEveryoneGuessed()) {
        nextRound();
      }
      notifyAboutState(state);
    }
  }

  private boolean hasEveryoneGuessed() {
    final Map<Player, Guess> guesses = state.getGuesses();
    final List<Player> players = state.getPlayers();
    // we use '>=' because players may first guess and then leave the game before the next round
    // starts
    return guesses.size() >= players.size();
  }

  /** Evaluates the correctness of the guess */
  boolean isGuessCorrect(Guess guess, Card firstCard, Card secondCard) {
    return guess == getCorrectGuess(firstCard, secondCard);
  }

  private Guess getCorrectGuess(Card firstCard, Card secondCard) {
    int comparison = firstCard.compareTo(secondCard);
    if (comparison == 0) { // current == next
      return Guess.EQUAL;
    } else if (comparison > 0) { // current > next
      return Guess.LOW;
    } else { // current < next
      assert comparison < 0;
      return Guess.HIGH;
    }
  }

  /**
   * Starts a new round, replaces the current card with the next card, and generates a new next
   * card.
   */
  private void nextRound() throws NoNextCardException {
    Card currentCard = state.getCurrentCard();
    Card nextCard = state.getDeck().getNextCard();
    distributeScores(currentCard, nextCard);
    state = state.nextRound(nextCard);
    notifyAboutState(state);
  }

  private void distributeScores(Card firstCard, Card secondCard) {
    assert hasEveryoneGuessed();

    final Map<Player, Guess> guesses = state.getGuesses();
    for (var e : guesses.entrySet()) {
      final Player player = e.getKey();
      final Guess guess = e.getValue();
      if (isGuessCorrect(guess, firstCard, secondCard)) {
        state = state.incrementScore(player, GUESS_SCORE.get(guess));
      }
    }
  }

  @Override
  public void subscribe(Observer obsv) {
    observerSupport.subscribe(obsv);
  }

  @Override
  public void unsubscribe(Observer obsv) {
    observerSupport.unsubscribe(obsv);
  }

  @Override
  public void notifyAboutState(GameState newState) {
    observerSupport.notifyAboutState(newState);
  }

  @Override
  public void notifyAboutNewPlayer(String playerName, GameState newState) {
    observerSupport.notifyAboutNewPlayer(playerName, newState);
  }

  @Override
  public void notifyAboutRemovedPlayer(String playerName, GameState newState) {
    observerSupport.notifyAboutRemovedPlayer(playerName, newState);
  }

  public GameState getState() {
    return state;
  }
}
