package highlowcardgame.client;

import static org.junit.jupiter.api.Assertions.assertEquals;

import highlowcardgame.game.HighLowCardGame.Guess;
import java.util.NoSuchElementException;
import java.util.Scanner;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/** Test if the shell returns the correct guess under different input sequences */
public class ShellInputTest {
  private Guess extractGuessFromString(String str) {
    Scanner scanner = new Scanner(str);
    Shell shell = new Shell(scanner, TestUtils.getDummyPrintStream());
    return shell.getUserInputGuess();
  }

  @Test
  public void testInputGuessHigh() {
    String inputs = "a\nb\n\n\ncc\nh\nHH\nH";
    Guess guess = extractGuessFromString(inputs);
    assertEquals(Guess.HIGH, guess);
  }

  @Test
  public void testInputGuessLow() {
    String inputs = "ddd\n\n\ne\nf\nl\nLLL\nL";
    Guess guess = extractGuessFromString(inputs);
    assertEquals(Guess.LOW, guess);
  }

  @Test
  public void testInputGuessEqual() {
    String inputs = "ii\n\njk\nl\nmnop\nEE\ne\nE";
    Guess guess = extractGuessFromString(inputs);
    assertEquals(Guess.EQUAL, guess);
  }

  @Test
  public void testInvalidInputGuess() {
    String inputs = "s\nEE\nHHH\nLl\n\n\nx\nyz";
    try {
      Guess guess = extractGuessFromString(inputs);
      Assertions.fail("Expected NoSuchElementException");
    } catch (NoSuchElementException e) {
      // Expected: test successful
    }
  }
}
