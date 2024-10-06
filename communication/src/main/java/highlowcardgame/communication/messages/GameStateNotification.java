package highlowcardgame.communication.messages;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import highlowcardgame.game.Card;
import org.json.JSONObject;

public final class GameStateNotification implements Message {

  private final String messageType = "GameStateNotification";
  private final String playerName;
  private final int numRounds;
  private final Card currentCard;
  private final int score;
//  private final Card previousCard;
//  private final boolean isCorrectGuess;
//  private final boolean isGuess;

  public GameStateNotification(String playerName, int numRounds, Card currentCard, int score) {
    this.playerName = playerName;
    this.numRounds = numRounds;
    this.currentCard = currentCard;
    this.score = score;
    //this.isCorrectGuess = isCorrectGuess;
//    this.previousCard = previousCard;
//    this.isGuess = isGuess;
  }

  public String getMessageType() {
    return messageType;
  }

  public String getPlayerName() {
    return playerName;
  }

  public int getNumRounds() {
    return numRounds;
  }

  public Card getCurrentCard() {
    return currentCard;
  }

  public int getScore() {
    return score;
  }

  public JSONObject toJSON() {
    Moshi moshi = new Moshi.Builder().build();
    JsonAdapter<GameStateNotification> jsonAdapter = moshi.adapter(GameStateNotification.class);
    try {
      String jsonString = jsonAdapter.toJson(this);
      return new JSONObject(jsonString);
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

//  public synchronized String notificationMessage() {
//    StringBuilder message = new StringBuilder();
//    String previousCardRepresentation = (previousCard != null) ? previousCard.getSuit().getCodeSuit() + String.format("%02d", previousCard.getValue()) : "N/A";
//    String currentCardRepresentation = (currentCard != null) ? currentCard.getSuit().getCodeSuit() + String.format("%02d", currentCard.getValue()) : "N/A";
//
//    message.append("<<<<<<<<< GameStateNotification <<<<<<<<<\n");
//
//    if (isGuess) {
//      message.append("<<< The previous card is ").append(previousCardRepresentation)
//              .append(", and the current card is ").append(currentCardRepresentation).append(".\n");
//      if (isCorrectGuess) {
//        message.append("<<< Congratulations Player ").append(playerName).append(", your guess was correct!\n");
//      } else {
//        message.append("<<< Bad luck, Player ").append(playerName).append("! Your guess was incorrect.\n");
//      }
//      message.append("<<< Now you have ").append(score).append(" points.\n");
//    }
//
//    message.append("<<< Round ").append(numRounds).append(" just started.\n")
//            .append("<<< Please guess if the next card is higher/lower than or equal to the current card ").append(currentCardRepresentation).append(".\n")
//            .append(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>\n")
//            .append(">>> Please input your guess: (H/L/E)");
//
//    return message.toString().trim();
//  }
//
//  public static GameStateNotification fromJSON(JSONObject json) {
//    Moshi moshi = new Moshi.Builder().build();
//    JsonAdapter<GameStateNotification> jsonAdapter = moshi.adapter(GameStateNotification.class);
//    try {
//      return jsonAdapter.fromJson(json.toString());
//    } catch (Exception e) {
//      e.printStackTrace();
//      return null;
//    }
//  }
}
/*
  private static Card parseCardFromString(String cardString) {
    String suitCode = cardString.substring(0, 1);
    int value = Integer.parseInt(cardString.substring(1));
    Card.Suit suit = null;
    switch (suitCode) {
      case "C":
        suit = Card.Suit.CLUBS;
        break;
      case "D":
        suit = Card.Suit.DIAMONDS;
        break;
      case "H":
        suit = Card.Suit.HEARTS;
        break;
      case "S":
        suit = Card.Suit.SPADES;
        break;
      default:
        throw new IllegalArgumentException("Invalid suit code: " + suitCode);
    }
    return new Card(suit, value);
  }

   */
