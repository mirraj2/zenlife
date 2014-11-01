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

public class EnrollController {

  private final RService rService = new RService();
  private final Map<String, double[]> ratesTable = Maps.newHashMap();

  public EnrollController() {
    initRatesTable();
  }

  public void getFinalRates(Request req, Response resp) throws Exception {
    boolean male = req.getParameter("-6").equals("0");
    int age = parseInt(req.getParameter("-5"));
    boolean smoker = !req.getParameter("-4").equals("1");

    double[] starterRates = getRates(age, male, smoker);
    int selectedProtection = parseInt(req.getParameter("0"));
    double starterRate = -1;

    for (int i = 0; i < starterRates.length; i += 2) {
      if (starterRates[i] == selectedProtection) {
        starterRate = starterRates[i + 1];
        break;
      }
    }

    checkState(starterRate != -1, "Could not find coverage amount: " + selectedProtection);

    double rate = rService.query(age, 10, selectedProtection, male, smoker);

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
        EnrollController.class.getResourceAsStream("data/rates-table.csv")));

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
