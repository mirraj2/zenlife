package zenlife.sentinel;

import jasonlib.Config;
import jasonlib.IO;
import jasonlib.OS;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.List;
import org.simpleframework.http.Request;
import com.google.common.base.Throwables;

public class DataFile {

  private static final File dir = new File(OS.getLocalAppFolder("zenlife"), "sentinel");
  private static final boolean buffer;
  static {
    dir.mkdirs();
    buffer = Config.load("zenlife").getBoolean("sentinel-buffer", true);
  }

  private static final int TIME = 0, HEADERS = 1, REQUEST = 2;

  private final File file;
  private final DataOutputStream out;
  private final long last = System.currentTimeMillis();

  public DataFile(String sessionId) {
    file = new File(dir, sessionId);
    try {
      OutputStream os = new FileOutputStream(file, true);
      if (buffer) {
        os = new BufferedOutputStream(os);
      }
      out = new DataOutputStream(os);
      out.write(TIME);
      out.writeLong(last);
    } catch (Exception e) {
      throw Throwables.propagate(e);
    }
  }

  public synchronized void writeHeaders(Request req) {
    try {
      out.writeByte(HEADERS);

      String ip = req.getClientAddress().getAddress().getHostAddress();
      out.writeBytes(ip);

      List<String> keys = req.getNames();
      out.write(keys.size());
      for (String header : req.getNames()) {
        out.writeBytes(header);
        out.writeBytes(req.getValue(header));
      }
    } catch (Exception e) {
      throw Throwables.propagate(e);
    }
  }

  public synchronized void record(Request req) {
    try {
      out.writeByte(REQUEST);
      out.writeBytes(req.getMethod());
      out.writeBytes(req.getPath().getPath());
    } catch (Exception e) {
      throw Throwables.propagate(e);
    }
  }

  public synchronized void exit() {
    IO.close(out);
  }

}
