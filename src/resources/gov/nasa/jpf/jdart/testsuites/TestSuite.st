package <package>;

import junit.framework.*;
import junit.textui.*;

public class <class> extends TestCase {

  public static void main(String[] args) {
    TestRunner runner = new TestRunner();
    TestResult result = runner.doRun(suite(args), false);
    if (! result.wasSuccessful()) {
      System.exit(1);
    \}
  \}

  public <class>(String name) {
    super(name);
  \}

  public static Test suite(String[] args) {
    int min = -1;
    int max = -1;
    if (args.length == 2) {
      min = Integer.parseInt(args[0]);
      max = Integer.parseInt(args[1]);
    }
    TestSuite result = new TestSuite();

    int c = 0;
    int x = 0;
    <parts:{p | 
    if (min \< 0 || max \< 0 || (c >= min && c \<= max)) {
      result.addTest(new TestSuite(<p>.class));
      x++;
    \}
    c++;
    }> 

    System.out.println("Running " + x + " of " + c + " sub suites: " +
        "[" + min + ", " + max + "]");

    return result;
  \}

\}
