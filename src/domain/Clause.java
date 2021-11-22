package domain;

import java.util.Arrays;
import java.util.List;

public class Clause {

  private int[] clause;

  @Override
  public boolean equals(Object obj) {

    Clause a = null;

    if (obj instanceof Clause) {
      a = (Clause) obj;
    }

    return Arrays.equals(this.clause, a.clause);
  }

  @Override
  public int hashCode() {

    return Arrays.hashCode(this.clause);
  }

  public Clause(int[] clause) {
    this.clause = clause;
  }

  public static Clause UnitClause(Square a) {
    return new Clause(new int[] {-a.getID()});
  }

  public static Clause ListToClause(List<Integer> ls) {
    return new Clause(ls.stream().mapToInt(num -> num).toArray());
  }

  public int[] getClause() {
    return this.clause;
  }

  public static Clause BombClause(Square s) {

    return new Clause(new int[] {s.getID()});
  }



}
