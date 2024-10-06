package highlowcardgame.communication.messages;

import com.squareup.moshi.Json;

public final class JoinGameRequest implements Message {
  private final String playerName;

  public JoinGameRequest(String playerName) {
    this.playerName = playerName;
  }

  public String getPlayerName() {
    return playerName;
  }
}
