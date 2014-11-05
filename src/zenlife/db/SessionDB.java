package zenlife.db;

import jasonlib.Log;
import java.time.LocalDateTime;
import java.util.UUID;
import org.simpleframework.http.Request;
import ez.Row;
import ez.Table;

public class SessionDB extends ZenDB {

  public SessionDB() {
    if (!db.hasTable("session")) {
      db.addTable(new Table("session")
          .primary("id", UUID.class)
          .column("date", LocalDateTime.class)
          .column("ip", String.class)
          .varchar("user_agent", 1024)
          .column("enroll_visits", Integer.class)
          .column("purchase_visits", Integer.class)
          );
    }
  }

  public String createSession(Request req) {
    String ret = UUID.randomUUID().toString();
    String ip = req.getClientAddress().getAddress().getHostAddress();

    Log.info("Opening new session: " + ret);

    db.insert("session", new Row()
        .with("id", ret)
        .with("date", LocalDateTime.now())
        .with("ip", ip)
        .with("user_agent", req.getValue("User-Agent"))
        .with("enroll_visits", 0)
        .with("purchase_visits", 0)
        );

    return ret;
  }

  public void markEnrollVisited(String token) {
    db.update("UPDATE session SET enroll_visits = enroll_visits + 1 WHERE id = ?", token);
  }

  public void markPurchaseVisited(String token) {
    db.update("UPDATE session SET purchase_visits = purchase_visits + 1 WHERE id = ?", token);
  }

}
