package zenlife;

import static com.google.common.base.Preconditions.checkState;
import static java.lang.Integer.parseInt;
import jasonlib.IO;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Map;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import au.com.bytecode.opencsv.CSVReader;
import com.google.common.base.Throwables;
import com.google.common.collect.Maps;

public class EnrollController {

  Map<String, double[]> ratesTable = Maps.newHashMap();

  public EnrollController() {
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

  public void handle(Request req, Response resp) throws Exception {
    String s = req.getPath().toString();

    checkState(s.equals("/getRates"));

    int sex = parseInt(req.getParameter("sex"));
    int age = parseInt(req.getParameter("age"));
    int smoking = parseInt(req.getParameter("smoking"));

    boolean male = sex == 0;
    boolean smoker = smoking != 1;

    String key = age + (male ? "M" : "F") + (smoker ? "Y" : "N");

    double[] rates = ratesTable.get(key);

    IO.from(Arrays.toString(rates)).to(resp.getOutputStream());
  }

}
