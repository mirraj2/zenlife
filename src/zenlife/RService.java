package zenlife;

import jasonlib.IO;
import jasonlib.Log;
import jasonlib.OS;
import java.io.File;

public class RService {

  private static final String envName = "20141031105048.zenenv";
  private static final String scriptName = "get_rate_ss.r";

  private final File folder, envFile, scriptFile;

  public RService() {
    folder = OS.getLocalAppFolder("zenlife");
    envFile = new File(folder, envName);
    scriptFile = new File(folder, scriptName);

    if (!envFile.exists()) {
      Log.info("creating file: " + envFile);
      IO.from(getClass(), "data/" + envName).to(envFile);
    }
    if (!scriptFile.exists()) {
      Log.info("creating file: " + scriptFile);
      IO.from(getClass(), "data/" + scriptName).to(scriptFile);
    }
  }

  public double query(int age, int term, int coverage, boolean male, boolean smoker) throws Exception {
    String distribution = male ? "male_" : "female_";
    distribution += smoker ? "smoker" : "nonsmoker";
    distribution += "_ss";

    StringBuilder sb = new StringBuilder("Rscript");
    sb.append(" --slave ").append(scriptFile);
    sb.append(" --age ").append(age);
    sb.append(" --term ").append(term);
    sb.append(" --face ").append(coverage);
    sb.append(" --distribution ").append(distribution);
    sb.append(" --working_directory ").append(folder);

    Log.debug(sb.toString());

    Process p = Runtime.getRuntime().exec(sb.toString());

    String output = IO.from(p.getInputStream()).toString();

    int i = output.indexOf(" ");

    return Double.parseDouble(output.substring(i + 1)) / 12.0;
  }

  public static void main(String[] args) throws Exception {
    double price = new RService().query(23, 10, 100000, false, false);
    Log.debug("$%.2f / month", price);
  }

}
