package api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.regex.Pattern;
import domain.Square;

/**
 * An example of how to use a MSAgent to solve the game. You can do whatever you want with this
 * class.
 */
public class Main {

  /**
   * Array containing the names of all fields. If you want to iterate over all of them, this might
   * help
   */
  public static final String[] fields = {"baby1-3x3-0.txt", "baby2-3x3-1.txt", "baby3-5x5-1.txt",
      "baby4-5x5-3.txt", "baby5-5x5-5.txt", "baby6-7x7-1.txt", "baby7-7x7-3.txt", "baby8-7x7-5.txt",
      "baby9-7x7-10.txt", "anfaenger1-9x9-10.txt", "anfaenger2-9x9-10.txt", "anfaenger3-9x9-10.txt",
      "anfaenger4-9x9-10.txt", "anfaenger5-9x9-10.txt", "fortgeschrittene1-16x16-40.txt",
      "fortgeschrittene2-16x16-40.txt", "fortgeschrittene3-16x16-40.txt",
      "fortgeschrittene4-16x16-40.txt", "fortgeschrittene5-16x16-40.txt", "profi1-30x16-99.txt",
      "profi2-30x16-99.txt", "profi3-30x16-99.txt", "profi4-30x16-99.txt", "profi5-30x16-99.txt"};

  public static void main(String[] args) {

    for (int i = 0; i < fields.length; i++) {
      System.out.println(i + ": " + fields[i]);
    }

    try {
      System.out.println("Enter Aufgabenindex");
      int aufg = getInput();
      if (aufg != -1)
        Main.execute(aufg);
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }


  }

  private static int getInput() throws IOException {
    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    int result = 0;
    try {
      String line = br.readLine();
      br.close();
      if (line == null) {
        throw new IOException("no input");
      }

      Pattern pattern = Pattern.compile("\\d+");

      if (!pattern.matcher(line).matches()) {
        throw new IOException("wrong input");
      }

      result = Integer.parseInt(line);

      if (result < 0 || result > 23) {
        throw new IOException("wrong input");
      }


      return result;

    } catch (IOException e) {

      e.printStackTrace();
    }
    System.out.println("falsche eingabe");
    return -1;
  }


  private static void execute(int aufgabe) {

    // use smaller numbers for larger fields
    int iterations = 100;


    double sum = 0;

    int success = 0;
    for (int i = 1; i <= iterations; i++) {

      double iterationTime = System.currentTimeMillis();

      System.out.print("Iteration : " + i);
      MSField f = new MSField("fields/" + fields[aufgabe]);
      MSAgent agent = new SatAgent(f);
      // to see what happens in the first iteration
      if (i == 1) {
        agent.activateDisplay();
      } else {
        agent.deactivateDisplay();
      }

      boolean solved = agent.solve();
      if (solved) {
        success++;
        System.out.println("-- win");
        sum += (System.currentTimeMillis() - iterationTime);
      } else {
        iterationTime = 0;
        System.out.println("-- loose");

      }

      Square.resetCounter();

    }
    String rate = new DecimalFormat("#0.0").format(100 * (double) success / (double) iterations);

    double timeInSeconds = sum / 1000;

    String avgTime = new DecimalFormat("#0.000").format(timeInSeconds / success);

    System.out.printf("%s \t| %s | %s | %ss", fields[aufgabe], "" + iterations, rate, avgTime);



  }



}
