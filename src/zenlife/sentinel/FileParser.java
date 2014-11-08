package zenlife.sentinel;

import static com.google.common.base.Preconditions.checkState;
import jasonlib.Log;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;

public class FileParser {

  private static final File dir = new File("/users/Jason/.zenlife/sentinel/");

  public void run() throws Exception {
    for (File f : dir.listFiles()) {
      run(f.getName());
    }
  }

  public void run(String session) throws Exception {
    File file = new File(dir, session);
    checkState(file.exists());

    DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
    while (true) {
      int b = in.read();
      if (b == -1) {
        break;
      }
      if (b == DataFile.TIME) {
        long time = in.readLong();
        Log.debug("time = " + time);
      } else if (b == DataFile.HEADERS) {
        String ip = in.readUTF();
        Log.debug("ip = " + ip);
        int numKeys = in.read();
        for (int i = 0; i < numKeys; i++) {
          Log.debug(in.readUTF() + " = " + in.readUTF());
        }
      } else if (b == DataFile.REQUEST) {
        String method = in.readUTF();
        String path = in.readUTF();
        Log.debug(method + " " + path);
      } else if (b == 'x' || b == 'q') {
        String path = in.readUTF();
        Log.debug("path: " + path);
      } else if (b == 'm' || b == 'd' || b == 'u') {
        int time = in.readInt();
        int x = in.readShort();
        int y = in.readShort();
        Log.debug(((char) b) + " " + time + " " + x + " " + y);
      } else if (b == 'a') {
        int time = in.readInt();
        String answer = in.readUTF();
        Log.debug("changed answer: " + answer);
      }
      else {
        Log.debug("UNKNOWN CHAR: " + (char) b);
        break;
      }
    }

    in.close();
  }

  public static void main(String[] args) throws Exception {
    new FileParser().run();
  }

}
