package highlowcardgame.game;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

/** This class generates card sequence randomly. The sequence can be arbitrarily long. */
public class InfiniteShuffledDeck implements Deck {
  private final List<Card> possibleCards;
  private final Random random;

  public InfiniteShuffledDeck(Collection<Card> possibleCards) {
    this.possibleCards = new ArrayList<>(possibleCards);
    random = new Random();
  }

  public InfiniteShuffledDeck(Collection<Card> possibleCards, long seed) {
    this.possibleCards = new ArrayList<>(possibleCards);
    random = new Random(seed);
  }

  /** Returns a random card from the possible card collection */
  @Override
  public Card getNextCard() {
    return possibleCards.get(random.nextInt(possibleCards.size()));
  }

  /** Always returns true, as the card sequence is of infinite length */
  @Override
  public boolean hasNextCard() {
    return true;
  }
}
