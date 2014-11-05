package zenlife.sentinel;

import jasonlib.Log;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.simpleframework.http.Cookie;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;

public class Sentinel {

  private static final Set<String> excluded = ImmutableSet.of("js", "css", "png", "jpg", "gif", "ico", "woff");

  private final Map<String, DataFile> map = Maps.newConcurrentMap();

  public void handle(Request req, Response resp) {
    try {
      String extension = req.getPath().getExtension();
      if (excluded.contains(extension)) {
        return;
      }

      String token = getSessionToken(req, resp);
      DataFile file = getFile(token);
      file.record(req);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private String getSessionToken(Request req, Response resp) {
    Cookie sessionCookie = req.getCookie("session");
    if (sessionCookie == null) {
      String sessionId = openSession(req);
      sessionCookie = new Cookie("session", sessionId);
      final int secondsPerDay = 60 * 60 * 24;
      sessionCookie.setExpiry(secondsPerDay * 30);
      Log.debug(sessionCookie.getExpiry());
      resp.setCookie(sessionCookie);
    }
    return sessionCookie.getValue();
  }

  private String openSession(Request req) {
    String sessionId = UUID.randomUUID().toString();
    Log.info("Opening new session: " + sessionId);

    DataFile file = getFile(sessionId);

    file.writeHeaders(req);

    return sessionId;
  }

  private DataFile getFile(String token) {
    DataFile ret = map.get(token);
    if (ret == null) {
      synchronized (map) {
        ret = map.get(token);
        if (ret == null) {
          ret = new DataFile(token);
          map.put(token, ret);
        }
      }
    }
    return ret;
  }

  public void exit() {
    for (DataFile file : map.values()) {
      file.exit();
    }
  }

}
