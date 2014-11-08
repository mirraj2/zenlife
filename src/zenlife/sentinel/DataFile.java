package zenlife.sentinel;

import static java.lang.Integer.parseInt;
import jasonlib.Config;
import jasonlib.IO;
import jasonlib.Log;
import jasonlib.OS;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;
import org.simpleframework.http.Request;
import com.google.common.base.Splitter;
import com.google.common.base.Throwables;

public class DataFile {

  private static final File dir = new File(OS.getLocalAppFolder("zenlife"), "sentinel");
  private static final boolean buffer;
  static {
    dir.mkdirs();
    buffer = Config.load("zenlife").getBoolean("sentinel-buffer", true);
  }

  public static final int TIME = 0, HEADERS = 1, REQUEST = 2;

  private final File file;
  private final DataOutputStream out;
  private long last;

  public DataFile(String sessionId) {
    file = new File(dir, sessionId);
    try {
      OutputStream os = new FileOutputStream(file, true);
      if (buffer) {
        os = new BufferedOutputStream(os);
      }
      out = new DataOutputStream(os);
      recordTime();
    } catch (Exception e) {
      throw Throwables.propagate(e);
    }
  }

  public synchronized void flush() {
    try {
      out.flush();
    } catch (IOException e) {
      throw Throwables.propagate(e);
    }
  }

  public synchronized void writeHeaders(Request req) {
    try {
      out.writeByte(HEADERS);

      String ip = req.getClientAddress().getAddress().getHostAddress();
      out.writeUTF(ip);

      List<String> keys = req.getNames();
      out.write(keys.size());
      for (String header : req.getNames()) {
        out.writeUTF(header);
        out.writeUTF(req.getValue(header));
      }
    } catch (Exception e) {
      throw Throwables.propagate(e);
    }
  }

  public synchronized void record(Request req) {
    try {
      out.writeByte(REQUEST);
      out.writeUTF(req.getMethod());
      out.writeUTF(req.getPath().getPath());
    } catch (Exception e) {
      throw Throwables.propagate(e);
    }
  }

  public synchronized void recordTime() {
    try {
      last = System.currentTimeMillis();
      out.write(TIME);
      out.writeLong(last);
    } catch (Exception e) {
      throw Throwables.propagate(e);
    }
  }

  public synchronized void recordWebSocketMessage(String message) {
    try {
      char c = message.charAt(0);

      if (c == 's') {
        return;
      }

      out.write(c);

      if (c == 'm' || c == 'd' || c == 'u') {
        Iterator<String> iter = Splitter.on(' ').split(message).iterator();
        out.writeInt(parseInt(iter.next().substring(1)));
        out.writeShort(parseInt(iter.next()));
        out.writeShort(parseInt(iter.next()));
      } else if (c == 'k') {
        Iterator<String> iter = Splitter.on(' ').split(message).iterator();
        out.writeInt(parseInt(iter.next().substring(1)));
        out.writeShort(parseInt(iter.next()));
      } else if (c == 'a') {
        int i = message.indexOf(' ');
        out.writeInt(parseInt(message.substring(1, i)));
        out.writeUTF(message.substring(i + 1));
      } else if (c == 'q' || c == 'x') {
        out.writeUTF(message.substring(1));
      } else {
        Log.error("Unknown type: " + c);
      }
    } catch (Exception e) {
      throw Throwables.propagate(e);
    }
    // sdumkaqx
  }

  public synchronized void exit() {
    IO.close(out);
  }

}
