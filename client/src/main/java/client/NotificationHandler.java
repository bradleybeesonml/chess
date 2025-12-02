package client;

import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;

public interface NotificationHandler {
    void onLoadGame(LoadGameMessage message);
    void onNotification(NotificationMessage message);
    void onError(ErrorMessage message);
}