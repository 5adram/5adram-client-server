package highlowcardgame.game.observable;

import highlowcardgame.game.GameState;

public interface Observer {

  /** Notify this observer of an update in the game state. */
  void updateState(GameState state);

  void updateNewPlayer(String playerName, GameState state);

  void updateRemovedPlayer(String playerName, GameState state);
}
