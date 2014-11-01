package zenlife;

import jasonlib.Config;
import jasonlib.IO;
import jasonlib.Log;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Map.Entry;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.Status;
import org.simpleframework.http.core.Container;
import org.simpleframework.http.core.ContainerServer;
import org.simpleframework.transport.Server;
import org.simpleframework.transport.connect.Connection;
import org.simpleframework.transport.connect.SocketConnection;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;

public class ZenlifeServer implements Container {

  private final EnrollController enrollController = new EnrollController();

  @Override
  public void handle(Request req, Response resp) {
    String s = req.getPath().toString();
    Log.debug("Handling: " + s + " (" + req.getClientAddress().getAddress().getHostAddress() + ")");

    try {
      if (s.equals("/")) {
        s += "index";
      }

      if (s.startsWith("/getRates")) {
        enrollController.getRates(req, resp);
        return;
      } else if (s.startsWith("/getFinalRates")) {
        enrollController.getFinalRates(req, resp);
        return;
      }

      if (!s.contains(".")) {
        s += ".html";
      }

      serve(s, resp);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void serve(String s, Response resp) {
    serve(s, resp, ImmutableMap.<String, String> of());
  }

  private void serve(String s, Response resp, Map<String, String> variables) {
    try {
      if (s.contains("*") || s.contains("..") || s.contains("$")) {
        Log.warn("weird request: " + s);
        throw new RuntimeException("Bad request: " + s);
      }

      if (s.startsWith("/")) {
        s = s.substring(1);
      }
      if (s.endsWith(".html")) {
        s = "html/" + s;
      }

      if (s.endsWith(".html")) {
        String data = IO.from(getClass(), s).toString();
        for (Entry<String, String> variable : variables.entrySet()) {
          data = data.replace(variable.getKey(), variable.getValue());
        }
        IO.from(data).to(resp.getOutputStream());
      } else {
        IO.from(getClass(), s).to(resp.getOutputStream());
      }

      resp.setStatus(Status.OK);
    } catch (Exception e) {
      Log.error("Could not find: " + s);
      resp.setStatus(Status.NOT_FOUND);
    } finally {
      try {
        resp.close();
      } catch (IOException e) {
        throw Throwables.propagate(e);
      }
    }
  }

  @SuppressWarnings("resource")
  public static void main(String[] args) throws Exception {
    int port = Config.load("zenlife").getInt("port", 80);
    
    Server server = new ContainerServer(new ZenlifeServer());
    Connection connection = new SocketConnection(server);
    connection.connect(new InetSocketAddress(port));
    Log.debug("Server started on port " + port);
  }

}
