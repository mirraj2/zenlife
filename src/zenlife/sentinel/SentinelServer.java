package zenlife.sentinel;

import static com.google.common.base.Preconditions.checkState;
import jasonlib.Log;
import java.net.InetSocketAddress;
import java.util.Map;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import com.google.common.collect.Maps;

public class SentinelServer extends WebSocketServer {

  private final Sentinel sentinel;
  private final Map<WebSocket, DataFile> socketFiles = Maps.newConcurrentMap();

  public SentinelServer(int port, Sentinel sentinel) {
    super(new InetSocketAddress(port));

    this.sentinel = sentinel;
  }

  @Override
  public void onOpen(WebSocket conn, ClientHandshake handshake) {
    Log.debug("socket open");
  }

  @Override
  public void onClose(WebSocket conn, int code, String reason, boolean remote) {
    DataFile file = socketFiles.remove(conn);
    Log.debug("socket close " + code + " " + reason + " " + remote);

    if (file != null) {
      file.flush();
    }
  }

  @Override
  public void onMessage(WebSocket conn, String message) {
    // Log.debug(message);
    DataFile file = socketFiles.get(conn);
    if (file == null) {
      checkState(message.startsWith("s"), "first message needs to be the session!");
      file = sentinel.getFile(message.substring(1));
      file.recordTime();
      socketFiles.put(conn, file);
    }
    file.recordWebSocketMessage(message);
  }

  @Override
  public void onError(WebSocket conn, Exception e) {
    Log.debug("socket error");
    e.printStackTrace();
  }

}
