package highlowcardgame.game;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

// test illegal initialization arguments
public class CardErrorsTest {
  @Test
  public void testCardZeroValue() {
    try {
      Card card = new Card(Card.Suit.CLUBS, 0);
      Assertions.fail("Expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      // Expected: test successful
    }
  }

  @Test
  public void testCardNegativeValue() {
    try {
      Card card = new Card(Card.Suit.HEARTS, -1);
      Assertions.fail("Expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      // Expected: test successful
    }
  }

  @Test
  public void testCardTooLargeValue() {
    try {
      Card card = new Card(Card.Suit.SPADES, 14);
      Assertions.fail("Expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      // Expected: test successful
    }
  }
}
