package highlowcardgame.game;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

// test illegal initialization arguments
public class CardComparisonTest {
  @Test
  public void testIsComparable() {
    Card card = new Card(Card.Suit.DIAMONDS, 3);
    assertTrue(card instanceof Comparable);
  }

  @Test
  public void testCompareToLargerValue() {
    Card card1 = new Card(Card.Suit.DIAMONDS, 4);
    Card card2 = new Card(Card.Suit.DIAMONDS, 5);
    Card card3 = new Card(Card.Suit.SPADES, 5);
    assertTrue(card1.compareTo(card2) < 0);
    assertTrue(card1.compareTo(card3) < 0);
  }

  @Test
  public void testCompareToLargerSuit() {
    Card card1 = new Card(Card.Suit.CLUBS, 7);
    Card card2 = new Card(Card.Suit.DIAMONDS, 7);
    Card card3 = new Card(Card.Suit.HEARTS, 7);
    Card card4 = new Card(Card.Suit.SPADES, 7);
    assertTrue(card1.compareTo(card2) < 0);
    assertTrue(card2.compareTo(card3) < 0);
    assertTrue(card3.compareTo(card4) < 0);
  }

  @Test
  public void testCompareToSmallerValue() {
    Card card1 = new Card(Card.Suit.HEARTS, 13);
    Card card2 = new Card(Card.Suit.HEARTS, 10);
    Card card3 = new Card(Card.Suit.CLUBS, 10);
    assertTrue(card1.compareTo(card2) > 0);
    assertTrue(card1.compareTo(card3) > 0);
  }

  @Test
  public void testCompareToSmallerSuit() {
    Card card1 = new Card(Card.Suit.CLUBS, 11);
    Card card2 = new Card(Card.Suit.DIAMONDS, 11);
    Card card3 = new Card(Card.Suit.HEARTS, 11);
    Card card4 = new Card(Card.Suit.SPADES, 11);
    assertTrue(card4.compareTo(card3) > 0);
    assertTrue(card3.compareTo(card2) > 0);
    assertTrue(card2.compareTo(card1) > 0);
  }

  @Test
  public void testCompareToEqual() {
    Card card1 = new Card(Card.Suit.CLUBS, 8);
    Card card2 = new Card(Card.Suit.CLUBS, 8);
    assertTrue(card1.compareTo(card2) == 0);
    assertTrue(card2.compareTo(card1) == 0);
  }
}
