package highlowcardgame.game;

public class Score {
  private volatile int score = 0;

  public synchronized void increment(int inc) {
    score += inc;
  }

  public int get() {
    return score;
  }
}
