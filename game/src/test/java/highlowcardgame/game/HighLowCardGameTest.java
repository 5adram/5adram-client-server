package highlowcardgame.game;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import highlowcardgame.game.Card.Suit;
import highlowcardgame.game.Deck.NoNextCardException;
import highlowcardgame.game.HighLowCardGame.Guess;
import java.util.List;
import org.junit.jupiter.api.Test;

// test guess correctness
public class HighLowCardGameTest {
  private void assertIsHigh(HighLowCardGame game, Card first, Card second) {
    assertTrue(game.isGuessCorrect(Guess.HIGH, first, second));
    assertFalse(game.isGuessCorrect(Guess.LOW, first, second));
    assertFalse(game.isGuessCorrect(Guess.EQUAL, first, second));
  }

  private void assertIsLow(HighLowCardGame game, Card first, Card second) {
    assertFalse(game.isGuessCorrect(Guess.HIGH, first, second));
    assertTrue(game.isGuessCorrect(Guess.LOW, first, second));
    assertFalse(game.isGuessCorrect(Guess.EQUAL, first, second));
  }

  private void assertIsEqual(HighLowCardGame game, Card first, Card second) {
    assertFalse(game.isGuessCorrect(Guess.HIGH, first, second));
    assertFalse(game.isGuessCorrect(Guess.LOW, first, second));
    assertTrue(game.isGuessCorrect(Guess.EQUAL, first, second));
  }

  @Test
  public void testCorrectGuess_differentNumbers_sameSuit() {
    Card cardHigh = new Card(Suit.CLUBS, 12);
    Card cardLow = new Card(Suit.CLUBS, 11);
    List<Card> cards = List.of(cardLow, cardHigh);
    Deck deck = new FixedDeck(cards);
    HighLowCardGame game = new HighLowCardGame(deck);

    assertIsHigh(game, cardLow, cardHigh);
    assertIsLow(game, cardHigh, cardLow);
  }

  @Test
  public void testCorrectGuess_sameNumbers_differentSuit() {
    // order should be Clubs < Diamonds < Hearts < Spades
    Card clubs = new Card(Suit.CLUBS, 12);
    Card diamonds = new Card(Suit.DIAMONDS, 12);
    Card hearts = new Card(Suit.HEARTS, 12);
    Card spades = new Card(Suit.SPADES, 12);
    List<Card> cards = List.of(clubs, spades, hearts, diamonds);
    Deck deck = new FixedDeck(cards);
    HighLowCardGame game = new HighLowCardGame(deck);

    for (Card higher : List.of(diamonds, hearts, spades)) {
      assertIsHigh(game, clubs, higher);
      assertIsLow(game, higher, clubs);
    }

    for (Card higher : List.of(hearts, spades)) {
      assertIsHigh(game, diamonds, higher);
      assertIsLow(game, higher, diamonds);
    }

    assertIsHigh(game, hearts, spades);
    assertIsLow(game, spades, hearts);
  }

  @Test
  public void testCorrectGuess_same() throws NoNextCardException {
    List<Card> cardList = List.of(new Card(Suit.DIAMONDS, 3), new Card(Suit.DIAMONDS, 3));
    Deck deck = new FixedDeck(cardList);
    HighLowCardGame game = new HighLowCardGame(deck);

    assertIsEqual(game, cardList.get(0), cardList.get(1));
  }
}
