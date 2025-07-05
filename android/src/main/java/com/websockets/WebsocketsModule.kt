package com.websockets

import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.module.annotations.ReactModule

@ReactModule(name = WebsocketsModule.NAME)
class WebsocketsModule(reactContext: ReactApplicationContext) :
  NativeWebsocketsSpec(reactContext) {
    private val jsiWebsockets = JsiWebsockets(reactApplicationContext)

  override fun getName(): String {
    return NAME
  }


  override fun connect(data: ReadableMap?) {
    data ?: return
    var headers = mutableMapOf<String, String>()
    if (data.hasKey("headers")) {
      headers = data.getMap("headers")!!.toHashMap() as MutableMap<String, String>
    }
    jsiWebsockets.connect(data.getString("endpoint")!!, headers)
  }

  override fun close() {
    jsiWebsockets.close()
  }

  override fun state(): String? {
    return jsiWebsockets.state.toString()
  }

  override fun sendMessage(message: String?): Boolean {
    message ?: return false
    return jsiWebsockets.sendMessage(message)
  }

  override fun addListener(eventName: String?) {
    // IOS only
  }

  override fun removeListeners(count: Double) {
    // IOS only
  }

  companion object {
    const val NAME = "Websockets"
  }
}
