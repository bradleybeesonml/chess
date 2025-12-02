package server;

import io.javalin.websocket.WsContext;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionManager {
    private final Map<Integer, Set<WsContext>> gameConnections = new ConcurrentHashMap<>();

    public void addConnection(int gameID, WsContext session) {
        gameConnections.computeIfAbsent(gameID, k -> ConcurrentHashMap.newKeySet())
                .add(session);
    }

    public void removeConnection(int gameID, WsContext session) {
        Set<WsContext> connections = gameConnections.get(gameID);
        if (connections != null) {
            connections.remove(session);
            if (connections.isEmpty()) {
                gameConnections.remove(gameID);
            }
        }
    }

    public void broadcast(int gameID, String message, WsContext excludeSession) {
        Set<WsContext> connections = gameConnections.get(gameID);
        if (connections != null) {
            for (WsContext ctx : connections) {
                if (ctx != excludeSession && ctx.session.isOpen()) {
                    ctx.send(message);
                }
            }
        }
    }

    public void broadcastToAll(int gameID, String message) {
        broadcast(gameID, message, null);
    }
}