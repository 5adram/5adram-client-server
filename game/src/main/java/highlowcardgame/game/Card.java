package highlowcardgame.game;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/** A poker card. */
public final class Card implements Comparable<Card> {

  /** Suit of a poker card. */
  public enum Suit {
    CLUBS("C"),
    DIAMONDS("D"),
    HEARTS("H"),
    SPADES("S");

    private final String codeSuit;

    Suit(String codeSuit) {
      this.codeSuit = codeSuit;
    }

    /** Returns the string representing the suit. */
    public String getCodeSuit() {
      return codeSuit;
    }
  }

  private static final int CARD_VALUE_MIN = 1;
  private static final int CARD_VALUE_MAX = 13;

  private final Suit suit;
  private final int value;

  /** Returns all valid, distinct playing cards. */
  public static Collection<Card> getAllValidCards() {
    List<Card> cards = new ArrayList<>();
    for (Suit suit : Suit.values()) {
      for (int value = CARD_VALUE_MIN; value <= CARD_VALUE_MAX; ++value) {
        cards.add(new Card(suit, value));
      }
    }
    return cards;
  }

  /**
   * Creates a new card object.
   *
   * @param suit the suit of the card
   * @param value the value of the card, must be between 1 and 13.
   * @throws IllegalArgumentException if the value is not in the range of 1~13
   */
  public Card(Suit suit, int value) {
    if (value < CARD_VALUE_MIN || value > CARD_VALUE_MAX) {
      throw new IllegalArgumentException("A card cannot have a value of " + value);
    }

    this.suit = suit;
    this.value = value;
  }

  /** Returns the suit of the card. */
  public Suit getSuit() {
    return suit;
  }

  /** Returns the value of the card. */
  public int getValue() {
    return value;
  }

  private int getAbsoluteValue() {
    return value * 4 + suit.ordinal();
  }

  @Override
  public int compareTo(Card card) {
    int thisScore = getAbsoluteValue();
    int thatScore = card.getAbsoluteValue();
    return thisScore - thatScore;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Card card = (Card) o;
    return value == card.value && suit == card.suit;
  }

  @Override
  public int hashCode() {
    return Objects.hash(suit, value);
  }

  @Override
  public String toString() {
    return suit.getCodeSuit() + String.format("%02d", value);
  }
}
