package highlowcardgame.game;

import highlowcardgame.game.observable.Observer;

public interface Player extends Observer {
  String getName();
}
