package highlowcardgame.game;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** State of the game. */
public class GameState {

  private final Deck deck;
  private final Card currentCard;

  private final int round;
  private final Map<String, Player> players;
  private final Map<Player, Score> scores;

  private final Map<Player, HighLowCardGame.Guess> currentGuesses;

  GameState(Deck deck) {
    this(deck, null, 0, Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap());
  }

  private GameState(
      Deck deck,
      Card card,
      int round,
      Map<String, Player> players,
      Map<Player, Score> scores,
      Map<Player, HighLowCardGame.Guess> guesses) {
    this.deck = deck;
    currentCard = card;
    this.round = round;
    this.players = Map.copyOf(players);
    this.scores = Map.copyOf(scores);
    currentGuesses = Map.copyOf(guesses);
  }

  public synchronized GameState addPlayer(Player player) {
    String playerName = player.getName();
    Map<String, Player> newPlayers = new HashMap<>(players);
    newPlayers.put(playerName, player);
    Map<Player, Score> newScores = new HashMap<>(scores);
    newScores.put(player, new Score());
    GameState state =
        new GameState(deck, currentCard, round, newPlayers, newScores, currentGuesses);
    return state;
  }

  public synchronized GameState removePlayer(Player player) {
    String playerName = player.getName();
    Map<String, Player> newMap = new HashMap<>(players);
    newMap.remove(playerName);
    GameState state = new GameState(deck, currentCard, round, newMap, scores, currentGuesses);
    return state;
  }

  public synchronized GameState addGuess(Player guesser, HighLowCardGame.Guess guess) {
    Map<Player, HighLowCardGame.Guess> newGuesses = new HashMap<>(currentGuesses);
    newGuesses.put(guesser, guess);
    return new GameState(deck, currentCard, round, players, scores, newGuesses);
  }

  Deck getDeck() {
    return deck;
  }

  synchronized GameState nextRound(Card newCard) {
    GameState newState =
        new GameState(deck, newCard, round + 1, players, scores, Collections.emptyMap());
    return newState;
  }

  public Map<Player, HighLowCardGame.Guess> getGuesses() {
    return currentGuesses;
  }

  public int getRound() {
    return round;
  }

  /** Gets the current card of the game */
  public Card getCurrentCard() {
    return currentCard;
  }

  public List<Player> getPlayers() {
    return List.copyOf(players.values());
  }

  public Map<Player, Score> getScores() {
    return scores;
  }

  public synchronized GameState incrementScore(Player player, int integer) {
    scores.get(player).increment(integer);
    return this;
  }
}
