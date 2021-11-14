package api;

import static java.util.stream.Collectors.toList;
import java.awt.Point;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import org.sat4j.core.VecInt;
import org.sat4j.pb.SolverFactory;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IProblem;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.TimeoutException;
import org.sat4j.tools.ModelIterator;
import domain.Clause;
import domain.Square;

/**
 * Das soll der Agent werden, der das Minesweeperfeld löst
 * 
 * @author paulh
 *
 */
public class SatAgent extends MSAgent {

  /**
   * unopened but safe Squares
   */
  private Queue<Square> safeSquares;

  /**
   * opened and thus, safe Squares
   */
  private HashSet<Square> openedSquares;

  /**
   * Squares that are uncertain to be safe
   */
  private HashSet<Square> unknownSquares;

  private HashSet<Square> bombSquares;

  /**
   * map that maps the IDs of the Squares to themselves
   */
  private HashMap<Integer, Square> myMap;

  private boolean displayActivated;

  /**
   * the 2x2 arr of Squares
   */
  private Square[][] arr;

  /**
   * A non-duplicate list of the currentClauses of a state
   */
  private HashSet<Clause> currentClauses;


  public SatAgent(MSField field) {
    super(field);
    this.displayActivated = false;
    this.arr = new Square[this.field.getNumOfCols()][this.field.getNumOfRows()];
    this.initArrs();
    this.initNeighbours();
    this.initHashMap();
    this.currentClauses = new HashSet<Clause>();

    initSets();


  }

  private void initSets() {
    this.safeSquares = new ArrayDeque<Square>();
    this.openedSquares = new HashSet<Square>();
    this.unknownSquares = new HashSet<Square>();
    this.bombSquares = new HashSet<Square>();
    for (int i = 0; i < this.field.getNumOfCols(); i++) {
      for (int j = 0; j < this.field.getNumOfRows(); j++) {
        unknownSquares.add(this.arr[i][j]);
      }
    }
  }

  private void initHashMap() {
    this.myMap = new HashMap<Integer, Square>();

    for (Square[] row : this.arr) {
      for (Square s : row) {
        myMap.put(s.getID(), s);
      }
    }
  }

  private void initNeighbours() {
    for (Square[] row : this.arr) {
      for (Square s : row) {
        s.initNeighbours(this);
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
        if (this.safeSquares.isEmpty()) {
          calcKNF();
          makeSAT();
        }
        Point p;



        if (this.safeSquares.isEmpty()) {
          System.out.println("Guessing now");

          List<Square> ls = this.unknownSquares.stream().collect(toList());
          // System.out.println(ls.size());
          Square s = ls.get((int) (Math.random() * ls.size()));
          this.unknownSquares.remove(s);
          this.safeSquares.add(s);
        }


        p = this.safeSquares.poll().getPoint();


        x = (int) p.x;
        y = (int) p.y;
      }

      if (displayActivated) {
        System.out.println("Uncovering (" + x + "," + y + ")");
      }
      feedback = field.uncover(x, y);
      this.arr[x][y].uncover(feedback);
      this.safeSquares.remove(this.arr[x][y]);
      this.openedSquares.add(this.arr[x][y]);

      // this.printInformation();

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
    for (int i = 0; i < this.field.getNumOfCols(); i++) {
      for (int j = 0; j < this.field.getNumOfRows(); j++) {

        this.arr[i][j] = new Square(new Point(i, j));
      }
    }
  }

  public boolean inBounds(int x, int y) {
    return x >= 0 && x < this.field.getNumOfCols() && y >= 0 && y < this.field.getNumOfRows();
  }

  /**
   * berechnet Punkte, die safe keine Bomben sind und fügt sie zu einer Queue hinzu.
   */
  private void calcKNF() {

    currentClauses.clear();

    // aufgedeckte Squares sind safe
    for (Square s : this.openedSquares) {
      currentClauses.add(Clause.UnitClause(s));
    }

    // die sicheren Bomben Squares sind obviously sichere Bomben
    for (Square s : this.bombSquares) {
      currentClauses.add(Clause.BombClause(s));
    }



    // solver.setExpectedNumberOfClauses(31);
    for (int i = 0; i < this.field.getNumOfCols(); i++) {
      for (int j = 0; j < this.field.getNumOfRows(); j++) {

        if (!this.arr[i][j].isCovered()) {


          // hinzufügen der KNF für nachbarbezoehungen
          if (this.arr[i][j].getNeighbourBombs() == 0) {
            zeroNeighbours(i, j);
            // currentClauses.addAll(this.arr[i][j].neighbourClauses());
          } else {
            currentClauses.addAll(this.arr[i][j].neighbourClauses());
          }



        }
      }
    }

    // if (displayActivated)
    //
    // System.out.println("size of clauses: " + currentClauses.size());
    //
    // for (Clause c : currentClauses) {
    // System.out.println(Arrays.toString(c.getClause()));
    // }


  }



  private void makeSAT() {

    for (Square s : this.unknownSquares) {
      s.setBomb(true);
      s.setSafe(true);
    }

    try {
      ISolver solver = SolverFactory.newDefault();

      solver = new ModelIterator(solver);

      solver.newVar(this.arr.length * this.arr[0].length);

      for (Clause a : currentClauses) {
        solver.addClause(new VecInt(a.getClause()));
        // System.out.println(Arrays.toString(a.getClause()));
      }

      IProblem problem = solver;

      HashSet<Square> possibleOnes = new HashSet<Square>(unknownSquares);

      while (problem.isSatisfiable()) {

        int[] loesung = problem.model();

        // System.out.println(Arrays.toString(loesung));

        for (int i = 0; i < loesung.length; i++) {

          Square s = this.myMap.get(Math.abs(loesung[i]));

          if (this.unknownSquares.contains(s)) {
            if (loesung[i] > 0) {

              s.setSafe(false);
            }
            if (loesung[i] < 0) {
              s.setBomb(false);

            }
          }


        }

      }

      for (Square s : possibleOnes) {

        if (s.isCovered() && s.isBomb() && !s.isSafe()) {
          this.unknownSquares.remove(s);
          this.bombSquares.add(s);
        } else if (s.isCovered() && !s.isBomb() && s.isSafe()) {
          this.unknownSquares.remove(s);
          this.safeSquares.add(s);
          Point p = s.getPoint();
          // if (displayActivated)
          // System.out.println("Das Square (" + p.x + "|" + p.y + ") wird zur Queue hinzugefuegt");
        }

      }



    } catch (ContradictionException e) {
      System.out.println("contradiction");
    } catch (TimeoutException ie) {
      System.out.println("timeout");
    }


  }



  // fügt zu CurrentClauses die Klausel hinzu, dass alle nachbarn keine Minen sind
  private void zeroNeighbours(int x, int y) {


    for (int i = -1; i <= 1; i++) {
      for (int j = -1; j <= 1; j++) {
        if (inBounds(x + i, y + j)) {
          currentClauses.add(Clause.UnitClause(this.arr[x + i][y + j]));
        }
      }
    }


  }


  public void printInformation() {

    if (displayActivated) {
      System.out.println("-------------------");
      System.out.println("meine Info ist ");


      StringBuffer sb = new StringBuffer("");
      for (int y = 0; y < this.field.getNumOfRows(); y++) {
        for (int x = 0; x < this.field.getNumOfCols(); x++) {


          if (!this.arr[x][y].isCovered())
            sb.append(this.arr[x][y].getNeighbourBombs());
          else {
            sb.append("-");
          }
          sb.append(" ");
        }
        sb.append("\n");
      }

      System.out.println(sb.toString());
      System.out.println("-------------------");
    }

  }

  public Square[][] getArr() {
    return this.arr;
  }


}
