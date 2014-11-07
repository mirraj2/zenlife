package zenlife;

import jasonlib.IO;
import jasonlib.Log;
import jasonlib.OS;
import java.io.File;

public class RService {

  private static final String envName = "zenenv.rdata";
  private static final String scriptName = "get_rate_soa.r";

  private final File folder, envFile, scriptFile;

  public RService() {
    folder = OS.getLocalAppFolder("zenlife");
    envFile = new File(folder, envName);
    scriptFile = new File(folder, scriptName);

    Log.info("extracting " + envFile);
    IO.from(getClass(), "data/zenenv.rdata").to(envFile);

    Log.info("extracting " + scriptFile);
    IO.from(getClass(), "data/" + scriptName).to(scriptFile);
  }

  private static int round(int riskRatio, boolean smoker, boolean roundUp) {
    int increment = smoker ? 25 : 10;
    int min = smoker ? 75 : 70;

    if (riskRatio < min) {
      return min;
    }

    if (riskRatio % increment == 0) {
      return riskRatio;
    }

    int ret = riskRatio / increment * increment;

    if (roundUp) {
      ret += increment;
    }

    return ret;
  }

  public double query(int age, int term, int coverage, boolean male, boolean smoker, double riskRatio) throws Exception {
    riskRatio = Math.round(riskRatio * 100);

    int risk1 = round((int) riskRatio, smoker, false);
    int risk2 = round((int) riskRatio, smoker, true);

    double result1 = query2(age, term, coverage, male, smoker, risk1);
    if (risk1 == risk2) {
      return result1;
    }

    double result2 = query2(age, term, coverage, male, smoker, risk2);
    double p = (riskRatio - risk1) / (risk2 - risk1);

    return result1 + p * (result2 - result1);
  }

  public double query2(int age, int term, int coverage, boolean male, boolean smoker, int riskRatio)
      throws Exception {
    StringBuilder distribution = new StringBuilder()
        .append("alb_")
        .append(riskRatio).append("_")
        .append(male ? "male_" : "female_")
        .append(smoker ? "smoker_" : "nonsmoker_")
        .append("soa");

    StringBuilder sb = new StringBuilder("Rscript")
        .append(" --slave ").append(scriptFile)
        .append(" --age ").append(age)
        .append(" --term ").append(term)
        .append(" --face ").append(coverage)
        .append(" --distribution ").append(distribution)
        .append(" --working_directory ").append(folder)
        .append(" --environment ").append(envFile);

    Log.debug(sb.toString());

    Process p = Runtime.getRuntime().exec(sb.toString());

    String output = IO.from(p.getInputStream()).toString();

    int i = output.indexOf(" ");

    return Double.parseDouble(output.substring(i + 1)) / 12.0;
  }

  public static void main(String[] args) throws Exception {
    double price = new RService().query(23, 10, 1000000, false, false, 1.20);
    Log.debug("$%.2f / month", price);
  }

}
