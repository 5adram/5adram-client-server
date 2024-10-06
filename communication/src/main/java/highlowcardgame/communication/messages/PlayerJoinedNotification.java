package highlowcardgame.communication.messages;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import org.json.JSONObject;

public final class PlayerJoinedNotification implements Message {
  private final String newPlayerName;
  private final int numPlayers;
  private final String messageType = "PlayerJoinedNotification";

  public PlayerJoinedNotification(String playerName, int numPlayers) {

    this.newPlayerName = playerName;
    this.numPlayers = numPlayers;
  }

  public String getNewPlayerName() {
    return newPlayerName;
  }

  public int getNumPlayers() {
    return numPlayers;
  }

  public String getMessageType() {
    return messageType;
  }

  public JSONObject toJSON() {
    Moshi moshi = new Moshi.Builder().build();
    JsonAdapter<PlayerJoinedNotification> jsonAdapter = moshi.adapter(PlayerJoinedNotification.class);
    System.out.println("PlayerJoinedNotification: Converting to JSON");
    try {
      String jsonString = jsonAdapter.toJson(this);
      return new JSONObject(jsonString);
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  public static PlayerJoinedNotification fromJSON(JSONObject json) {
    Moshi moshi = new Moshi.Builder().build();
    JsonAdapter<PlayerJoinedNotification> jsonAdapter = moshi.adapter(PlayerJoinedNotification.class);
    try {
      return jsonAdapter.fromJson(json.toString());
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }
}
