package domain;

import java.awt.Point;

public class Square {

  public static int count = 1;

  private boolean covered;
  private int neighbourBombs;
  private int isBomb;

  public final static int SAFE = 0;
  public final static int UNKNOWN = -1;
  public final static int BOMB = 1;

  private int ID;

  private Point p;

  public Square(Point p) {
    this.p = p;
    this.ID = count++;
    covered = true;
    neighbourBombs = -1;
    isBomb = UNKNOWN;
  }

  public void uncover(int neighbours) {
    this.covered = false;
    this.neighbourBombs = neighbours;
    this.isBomb = SAFE;

  }

  public boolean isCovered() {
    return covered;
  }

  public int getNeighbourBombs() {
    return neighbourBombs;
  }

  public int isBomb() {
    return isBomb;
  }

  public void safeNoBomb() {
    this.isBomb = SAFE;
  }

  public void MarkBomb() {
    this.isBomb = BOMB;


  }


  public int getID() {
    return this.ID;
  }

  public Point getPoint() {
    return this.p;
  }

  public static void resetCounter() {
    count = 1;
  }

  @Override
  public boolean equals(Object obj) {
    Square temp = (Square) obj;

    return this.getID() == temp.getID();
  }



}
