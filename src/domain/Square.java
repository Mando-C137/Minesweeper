package domain;

import java.awt.Point;
import java.util.LinkedList;
import java.util.List;
import api.SatAgent;

public class Square {

  public final static int SAFE = 0;

  public final static int BOMB = 1;

  public final static int UNKNOWN = 2;

  public static int count = 1;

  private boolean covered;

  private int neighbourBombs;

  private List<Square> neighbours;

  private int ID;

  private Point p;

  private int isBomb;

  private boolean possibleBomb = false;

  public boolean isPossibleBomb() {
    return this.possibleBomb;
  }

  public void change(boolean ch) {
    this.possibleBomb = ch;
  }


  public Square(Point p) {
    this.p = new Point(p);
    this.ID = count++;
    covered = true;
    neighbourBombs = 8;
    this.neighbours = new LinkedList<Square>();
    this.isBomb = UNKNOWN;

  }

  public void uncover(int neighbours) {
    this.covered = false;
    this.neighbourBombs = neighbours;


  }

  public boolean isCovered() {
    return covered;
  }

  public int getNeighbourBombs() {
    return neighbourBombs;
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


  public void initNeighbours(SatAgent s) {

    for (int i = -1; i <= 1; i++) {
      for (int j = -1; j <= 1; j++) {
        if (s.inBounds(this.p.x + i, this.p.y + j) && (i != 0 || j != 0)) {

          this.neighbours.add(s.getArr()[this.p.x + i][this.p.y + j]);

        }
      }
    }

  }


  public LinkedList<Clause> neighbourClauses() {

    LinkedList<Clause> ls = new LinkedList<Clause>();

    // System.out.println(Math.pow(2, this.neighbours.size()));

    for (int i = 0; i < (int) Math.pow(2, this.neighbours.size()); i++) {

      LinkedList<Integer> temp = new LinkedList<Integer>();

      if (Integer.bitCount(i) != this.neighbourBombs) {
        String a = Integer.toBinaryString(i);
        while (a.length() < this.neighbours.size()) {
          a = "0" + a;
        }


        for (int c = 0; c < this.neighbours.size(); c++) {

          int id = this.neighbours.get(c).ID;

          if (String.valueOf(a.charAt(c)).equals("1")) {
            temp.add(-id);
          } else {
            temp.add(id);
          }
        }

        ls.add(Clause.ListToClause(temp));

      }



    }
    // System.out.println("--------------");
    // for (Clause a : ls) {
    // System.out.println(Arrays.toString(a.getClause()));
    // }
    // System.out.println("--------------");

    return ls;

  }

  @Override
  public int hashCode() {

    return Integer.hashCode(this.ID);
  }

  @Override
  public boolean equals(Object obj) {
    if (this.getClass().equals(obj.getClass())) {
      Square temp = (Square) obj;
      return this.ID == temp.ID;
    }
    return false;
  }

  public void setBomb(int i) {
    if (i < 0 || i > 2) {
      throw new IllegalArgumentException("muss 0 oder 1 oder 2 sein");
    }

    this.isBomb = i;
  }

  public int isBomb() {
    return this.isBomb;
  }



}
