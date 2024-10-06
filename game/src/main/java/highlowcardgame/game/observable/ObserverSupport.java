package highlowcardgame.game.observable;

import highlowcardgame.game.GameState;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

public final class ObserverSupport implements Observable {

  private ConcurrentLinkedQueue<Observer> observers = new ConcurrentLinkedQueue<>();

  @Override
  public void subscribe(Observer obsv) {
    if (observers.contains(obsv)) {
      throw new AssertionError("Observer " + obsv + " already part of observers");
    }
    observers.add(obsv);
  }

  @Override
  public void unsubscribe(Observer obsv) {
    observers.remove(obsv);
    if (observers.contains(obsv)) {
      throw new AssertionError("Observer " + obsv + " still part of observers");
    }
  }

  @Override
  public void notifyAboutState(GameState state) {
    updateAll(o -> o.updateState(state));
  }

  @Override
  public void notifyAboutNewPlayer(String playerName, GameState newState) {
    updateAll(o -> o.updateNewPlayer(playerName, newState));
  }

  @Override
  public void notifyAboutRemovedPlayer(String playerName, GameState newState) {
    updateAll(o -> o.updateRemovedPlayer(playerName, newState));
  }

  private void updateAll(Consumer<Observer> toCall) {
    for (Observer o : observers) {
      toCall.accept(o);
    }
  }
}
