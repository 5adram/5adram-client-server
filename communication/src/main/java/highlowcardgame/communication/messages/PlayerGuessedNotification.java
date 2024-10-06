package highlowcardgame.communication.messages;

public final class PlayerGuessedNotification implements Message {

  public String getPlayerName() {
    return "";
  }

  public int getNumNotGuessedPlayers() {
    return 0;
  }
}
