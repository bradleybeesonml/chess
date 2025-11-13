package client;

import com.google.gson.Gson;
import model.AuthData;
import model.GameData;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

public class ServerFacade {
    private final HttpClient client = HttpClient.newHttpClient();
    private final String serverUrl;
    private final Gson gson = new Gson();

    public ServerFacade(int port) {
        this.serverUrl = "http://localhost:" + port;
    }

    public ServerFacade(String url) {
        this.serverUrl = url;
    }

    public void clear() throws ResponseException {
        var request = buildRequest("DELETE", "/db", null, null);
        var response = sendRequest(request);
        handleResponse(response, null);
    }

    public AuthData register(String username, String password, String email) throws ResponseException {
        var body = Map.of("username", username, "password", password, "email", email);
        var request = buildRequest("POST", "/user", body, null);
        var response = sendRequest(request);
        return handleResponse(response, AuthData.class);
    }

    public AuthData login(String username, String password) throws ResponseException {
        var body = Map.of("username", username, "password", password);
        var request = buildRequest("POST", "/session", body, null);
        var response = sendRequest(request);
        return handleResponse(response, AuthData.class);
    }

    public void logout(String authToken) throws ResponseException {
        var request = buildRequest("DELETE", "/session", null, authToken);
        var response = sendRequest(request);
        handleResponse(response, null);
    }

    public int createGame(String authToken, String gameName) throws ResponseException {
        var body = Map.of("gameName", gameName);
        var request = buildRequest("POST", "/game", body, authToken);
        var response = sendRequest(request);
        var result = handleResponse(response, Map.class);
        assert result != null; //intellij wanted me to add this
        return ((Double) result.get("gameID")).intValue();
    }

    
    private HttpRequest buildRequest(String method, String path, Object body, String authToken) {
        var builder = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + path))
                .method(method, makeRequestBody(body));
        
        if (authToken != null) {
            builder.header("authorization", authToken);
        }
        
        if (body != null) {
            builder.header("Content-Type", "application/json");
        }
        
        return builder.build();
    }

    private HttpRequest.BodyPublisher makeRequestBody(Object request) {
        if (request != null) {
            return HttpRequest.BodyPublishers.ofString(gson.toJson(request));
        } else {
            return HttpRequest.BodyPublishers.noBody();
        }
    }

    private HttpResponse<String> sendRequest(HttpRequest request) throws ResponseException {
        try {
            return client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception ex) {
            throw new ResponseException(500, "Network error: " + ex.getMessage());
        }
    }

    private <T> T handleResponse(HttpResponse<String> response, Class<T> responseClass) throws ResponseException {
        int status = response.statusCode();
        
        if (!isSuccessful(status)) {
            String body = response.body();
            if (body != null && !body.isEmpty()) {
                try {
                    var errorResponse = gson.fromJson(body, Map.class);
                    String message = (String) errorResponse.get("message");
                    throw new ResponseException(status, message != null ? message : "Request failed");
                } catch (Exception e) {
                    throw new ResponseException(status, "Request failed with status: " + status);
                }
            }
            throw new ResponseException(status, "Request failed with status: " + status);
        }

        if (responseClass != null) {
            return gson.fromJson(response.body(), responseClass);
        }

        return null;
    }

    private boolean isSuccessful(int status) {
        return status / 100 == 2;
    }
}

