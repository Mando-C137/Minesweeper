package api;

import java.awt.Point;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import org.sat4j.core.VecInt;
import org.sat4j.pb.SolverFactory;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IProblem;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.TimeoutException;
import domain.Square;

/**
 * Das soll der Agent werden, der das Minesweeperfeld löst
 * 
 * @author paulh
 *
 */
public class SatAgent extends MSAgent {

  Queue<Point> safeSquare = new ArrayDeque<Point>();

  public final static int NoBomb = 0;

  public final static int Bomb = 2;

  public final static int possibleBomb = 1;

  // public final static int NoBomb =;

  private HashMap<Integer, Square> myMap;

  private boolean displayActivated;

  private Square[][] arr;


  public SatAgent(MSField field) {
    super(field);
    displayActivated = false;
    arr = new Square[this.field.getNumOfRows()][this.field.getNumOfCols()];
    this.initArrs();
    initHashMap();



  }

  private void initHashMap() {
    this.myMap = new HashMap<Integer, Square>();

    for (Square[] row : this.arr) {
      for (Square s : row) {
        myMap.put(s.getID(), s);
      }
    }
  }

  @Override
  public boolean solve() {

    boolean firstDecision = true;
    int x = 0, y = 0, feedback;

    do {
      if (displayActivated) {
        System.out.println(field);
      }
      if (firstDecision) {
        x = 0;
        y = 0;
        firstDecision = false;
      } else {
        // x and y must be computed here
        Point p = this.getSafeSquare();

        x = (int) p.getX();
        y = (int) p.getY();
      }

      if (displayActivated) {
        System.out.println("Uncovering (" + x + "," + y + ")");
      }
      feedback = field.uncover(x, y);

      if (feedback >= 0) {
        this.arr[x][y].uncover(feedback);
      } else {
        // game endet eh
      }

    } while (feedback >= 0 && !field.solved());

    if (field.solved()) {
      if (displayActivated) {
        System.out.println("Solved the field");
      }
      return true;
    } else {
      if (displayActivated) {
        System.out.println("BOOM!");
      }
      return false;
    }

  }

  @Override
  public void activateDisplay() {
    this.displayActivated = true;

  }

  @Override
  public void deactivateDisplay() {
    this.displayActivated = false;
  }

  private void initArrs() {
    for (int i = 0; i < this.arr.length; i++) {
      for (int j = 0; j < this.arr[i].length; j++) {
        this.arr[i][j] = new Square(new Point(i, j));
      }
    }
  }

  private boolean inBounds(int x, int y) {
    return x >= 0 && x < this.field.getNumOfRows() && y >= 0 && y < this.field.getNumOfCols();
  }


  private Point getSafeSquare() {

    ISolver solver = SolverFactory.newDefault();
    solver.newVar(this.arr.length * this.arr[0].length);
    // solver.setExpectedNumberOfClauses(31);

    for (int i = 0; i < this.arr.length; i++) {
      for (int j = 0; j < this.arr[i].length; j++) {
        if (this.arr[i][j].isCovered()) {
          // wenn es covered ist, kann man nix wissen
        } else {
          try {
            for (int[] clause : createClause(i, j)) {
              solver.addClause(new VecInt(clause));

            }
            solver.addClause(new VecInt(new int[] {-this.arr[i][j].getID()}));

          } catch (ContradictionException e) {
            System.out.println("contradiction ");
          }
        }
      }
    }
    IProblem problem = solver;
    /*
     * Das Problem wird geloest.
     */
    try {
      if (problem.isSatisfiable()) {
        /*
         * Ausgabe einer möglichen Loesung
         */
        System.out.println("Problem ist loesbar mit folgender Loesung:");
        // System.out.println(Arrays.toString(problem.findModel()));

        int[] ls = null;
        System.out.println(Arrays.toString((ls = problem.findModel())));


        for (int i = 0; i < ls.length; i++) {
          if (ls[i] < 0 && myMap.get(Math.abs(ls[i])).isCovered()) {
            System.out.println("Das Square " + myMap.get(Math.abs(ls[i])).getPoint().toString()
                + "wird gewaehlt und das covered ist " + myMap.get(Math.abs(ls[i])).isCovered());
            return myMap.get(Math.abs(ls[i])).getPoint();
          }
        }



      } else {
        System.out.println("Problem ist unloesbar");
        return new Point(-1, -1);
      }
    } catch (TimeoutException e) {
      System.out.println("TimeOutF");
    }

    return new Point(-1, -1);


  }


  private LinkedList<int[]> createClause(int x, int y) {

    LinkedList<int[]> clauses = new LinkedList<int[]>();

    switch (this.arr[x][y].getNeighbourBombs()) {

      case 0:
        clauses.addAll(zeroNeighbours(x, y));
        break;
      case 1:
        break;
      case 2:
        break;
      case 3:
        break;
      case 4:
        break;
      case 5:
        break;
      case 6:
        break;
      case 7:
        break;
      case 8:
        break;
    }



    return clauses;

  }

  private LinkedList<int[]> zeroNeighbours(int x, int y) {

    LinkedList<int[]> clauses = new LinkedList<int[]>();

    for (int i = -1; i <= 1; i++) {
      for (int j = -1; j <= 1; j++) {
        if (inBounds(x + i, y + j)) {
          clauses.add(new int[] {-this.arr[x + i][y + j].getID()});
        }
      }
    }

    return clauses;
  }


}
