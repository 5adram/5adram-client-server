package highlowcardgame.communication.messages;

import highlowcardgame.game.HighLowCardGame.Guess;

public final class GuessRequest implements Message {

  private final String messageType = "GuessRequest";
  private final Guess guess;
  private final String playerName;

  public GuessRequest(Guess guess, String playerName) {

    this.guess = guess;
    this.playerName = playerName;
  }

  public String getPlayerName() {
    return playerName;
  }

  public Guess getGuess() {
    return guess;
  }

  public String getMessageType() {
    return messageType;
  }
}
