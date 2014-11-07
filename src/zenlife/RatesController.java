package zenlife;

import static com.google.common.base.Preconditions.checkState;
import static java.lang.Integer.parseInt;
import jasonlib.IO;
import jasonlib.Json;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Map;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import au.com.bytecode.opencsv.CSVReader;
import com.google.common.base.Throwables;
import com.google.common.collect.Maps;

public class RatesController {

  private final RService rService = new RService();
  private final Map<String, double[]> ratesTable = Maps.newHashMap();
  private final RiskRatios ratios = new RiskRatios();

  public RatesController() {
    initRatesTable();
  }

  public void getFinalRates(Request req, Response resp) throws Exception {
    Json json = new Json(req.getContent());

    boolean male = json.getJson("-6").getInt(0) == 0;
    int age = parseInt(json.get("-5"));
    boolean smoker = json.getJson("-4").getInt(0) != 1;

    double[] starterRates = getRates(age, male, smoker);
    int selectedProtection = json.getInt("coverage");
    double starterRate = -1;

    for (int i = 0; i < starterRates.length; i += 2) {
      if (starterRates[i] == selectedProtection) {
        starterRate = starterRates[i + 1];
        break;
      }
    }

    checkState(starterRate != -1, "Could not find coverage amount: " + selectedProtection);

    double rate = rService.query(age, 10, selectedProtection, male, smoker, ratios.compute(json, male, age));

    Json ret = Json.object();
    ret.with("benchmark_rate", starterRate)
        .with("rate", rate);

    IO.from(ret).to(resp.getOutputStream());
  }

  public void getRates(Request req, Response resp) throws Exception {
    String s = req.getPath().toString();

    checkState(s.equals("/getRates"));

    int sex = parseInt(req.getParameter("sex"));
    int age = parseInt(req.getParameter("age"));
    int smoking = parseInt(req.getParameter("smoking"));

    boolean male = sex == 0;
    boolean smoker = smoking != 1;

    double[] rates = getRates(age, male, smoker);

    IO.from(Arrays.toString(rates)).to(resp.getOutputStream());
  }

  private double[] getRates(int age, boolean male, boolean smoker) {
    String key = age + (male ? "M" : "F") + (smoker ? "Y" : "N");
    return ratesTable.get(key);
  }

  private void initRatesTable() {
    CSVReader reader = new CSVReader(new InputStreamReader(
        RatesController.class.getResourceAsStream("data/rates-table.csv")));

    try {
      reader.readNext();

      double[] buffer = new double[24];
      int c = 0;
      for (String[] row : reader.readAll()) {
        String key = row[0] + row[1] + row[2];
        buffer[c++] = parseInt(row[3]);
        buffer[c++] = Double.parseDouble(row[4]);
        if (c == buffer.length) {
          ratesTable.put(key, buffer);
          c = 0;
          buffer = new double[24];
        }
      }
    } catch (Exception e) {
      throw Throwables.propagate(e);
    } finally {
      IO.close(reader);
    }
  }

}
