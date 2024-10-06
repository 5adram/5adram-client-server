package highlowcardgame.game;

/** Interface to generate a sequence of cards. */
public interface Deck {

  /** Exception that signals that no further cards exist in a {@link Deck}. */
  class NoNextCardException extends Exception {
    /** Creates a new NoNextCardException with the given message. */
    public NoNextCardException(String errorMessage) {
      super(errorMessage);
    }
  }

  /**
   * Gets the next card in the sequence.
   *
   * @return the next card
   * @throws NoNextCardException if there is no next card
   */
  Card getNextCard() throws NoNextCardException;

  /** Returns whether there is a next card in the sequence. */
  boolean hasNextCard();
}
