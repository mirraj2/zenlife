package zenlife;

import static java.lang.Double.parseDouble;
import jasonlib.IO;
import jasonlib.Json;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import au.com.bytecode.opencsv.CSVReader;
import com.google.common.base.Throwables;
import com.google.common.collect.Maps;

public class RiskRatios {
  
  private static final Map<String, Double> lowCoverageMap = Maps.newHashMap();

  public RiskRatios() {
    for (String[] row : readCSV("data/risk_ratios/face_on_age_on_gender.csv")) {
      lowCoverageMap.put(row[1] + row[2], parseDouble(row[3]));
    }
  }

  public double compute(Json json, boolean male, int age) {
    double ret = 1.0;

    ret *= lowCoverage(json, male, age);

    return ret;
  }

  private double lowCoverage(Json json, boolean male, int age) {
    int coverage = json.getInt("coverage");
    if (coverage > 100000) {
      return 1.0;
    }
    
    Double d = lowCoverageMap.get((male ? "male" : "female") + age);
    
    return d == null ? 1.0 : d;
  }

  private static List<String[]> readCSV(String path) {
    CSVReader reader = new CSVReader(new InputStreamReader(RiskRatios.class.getResourceAsStream(path)));
    try {
      reader.readNext(); // header
      return reader.readAll();
    } catch (Exception e) {
      throw Throwables.propagate(e);
    } finally {
      IO.close(reader);
    }
  }

}
