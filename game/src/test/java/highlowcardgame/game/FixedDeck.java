package highlowcardgame.game;

import java.util.List;

// for easier testing
class FixedDeck implements Deck {
  private int count;
  private List<Card> cardList;

  public FixedDeck(List<Card> cardList) {
    this.cardList = cardList;
    count = 0;
  }

  @Override
  public Card getNextCard() throws NoNextCardException {
    if (!hasNextCard()) {
      throw new NoNextCardException("The deck is empty");
    }

    return cardList.get(count++);
  }

  @Override
  public boolean hasNextCard() {
    return count < cardList.size();
  }
}
