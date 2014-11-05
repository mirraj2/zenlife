package zenlife.sentinel;

import java.util.Map;
import java.util.Set;
import org.simpleframework.http.Cookie;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import zenlife.db.SessionDB;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;

public class Sentinel {

  private static final Set<String> excluded = ImmutableSet.of("js", "css", "png", "jpg", "gif", "ico", "woff", "json");

  private final SessionDB sessionDB = new SessionDB();

  private final Map<String, DataFile> map = Maps.newConcurrentMap();

  public void handle(Request req, Response resp) {
    try {
      String extension = req.getPath().getExtension();
      if (excluded.contains(extension)) {
        return;
      }

      String token = getSessionToken(req, resp);

      String path = req.getPath().getPath().toLowerCase();
      if (path.contains("enroll")) {
        sessionDB.markEnrollVisited(token);
      } else if (path.contains("purchase")) {
        sessionDB.markPurchaseVisited(token);
      }

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
      resp.setCookie(sessionCookie);
    }
    return sessionCookie.getValue();
  }

  private String openSession(Request req) {
    String sessionId = sessionDB.createSession(req);

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
