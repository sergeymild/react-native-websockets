package com.websockets;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.facebook.common.internal.DoNotStrip;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.WritableMap;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.EOFException;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Map;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class JsiWebsockets extends WebSocketListener {

  private final ReactApplicationContext context;
  @Nullable
  private WebSocket webSocket;
  WsState state = WsState.CLOSED;

  public JsiWebsockets(ReactApplicationContext context) {
    this.context = context;
  }

  private void sendEvent(String eventName, String message) {
    if (!context.hasActiveReactInstance()) return;
    WritableMap map = Arguments.createMap();
    map.putString("type", eventName);
    map.putString("message", message);
    context.emitDeviceEvent("onNativeMessage", map);
  }

  @DoNotStrip
  void connect(String endpoint, Map<String, String> headers) throws JSONException {
    if (webSocket != null && state != WsState.CLOSED) return;
    Request.Builder builder = new Request.Builder().url(endpoint);
    for (Map.Entry<String, String> entry : headers.entrySet()) {
      builder.addHeader(entry.getKey(), entry.getValue());
    }
    webSocket = new OkHttpClient().newWebSocket(builder.build(), this);
    state = WsState.CONNECTING;
    sendEvent("onStateChange", state.toString());
  }

  @DoNotStrip
  void close() {
    if (webSocket == null || state == WsState.CLOSED) return;
    webSocket.close(1000, null);
  }

  @DoNotStrip
  void sendMessage(String message) {
    if (webSocket == null || state == WsState.CLOSED) return;
    webSocket.send(message);
  }

  @Override
  public void onClosed(@NonNull WebSocket webSocket, int code, @NonNull String reason) {
    System.out.println("AndroidWS.closed");
    super.onClosed(webSocket, code, reason);
    state = WsState.CLOSED;
    webSocket = null;
    sendEvent("onStateChange", state.toString());
    sendEvent("onClose", "");
  }

  @Override
  public void onClosing(@NonNull WebSocket webSocket, int code, @NonNull String reason) {
    System.out.println("AndroidWS.onClosing");
    super.onClosing(webSocket, code, reason);
    state = WsState.CLOSING;
    sendEvent("onStateChange", state.toString());
  }

  @Override
  public void onFailure(@NonNull WebSocket webSocket, @NonNull Throwable t, @Nullable Response response) {
    System.out.println("AndroidWS.onFailure");
    super.onFailure(webSocket, t, response);
    state = WsState.CLOSED;
    webSocket = null;
    if (t instanceof EOFException) {
      sendEvent("onStateChange", state.toString());
      sendEvent("onClose", "");
      return;
    }
    sendEvent("onStateChange", state.toString());
    sendEvent("onError", t.getMessage());
  }

  @Override
  public void onMessage(@NonNull WebSocket webSocket, @NonNull String text) {
    super.onMessage(webSocket, text);
    sendEvent("onMessage", text);
  }

  @Override
  public void onMessage(@NonNull WebSocket webSocket, @NonNull ByteString bytes) {
    super.onMessage(webSocket, bytes);
    sendEvent("onMessage", bytes.string(StandardCharsets.UTF_8));
  }

  @Override
  public void onOpen(@NonNull WebSocket webSocket, @NonNull Response response) {
    System.out.println("AndroidWS.onOpen");
    super.onOpen(webSocket, response);
    state = WsState.OPEN;
    sendEvent("onStateChange", state.toString());
    sendEvent("onOpen", "");
  }

  private enum WsState {
    CONNECTING,
    OPEN,
    CLOSING,
    CLOSED
  }
}
