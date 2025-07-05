import Websockets from './NativeWebsockets';
import { type EmitterSubscription, NativeEventEmitter } from 'react-native';

export type JsiWebSocketState = 'CONNECTING' | 'OPEN' | 'CLOSING' | 'CLOSED';
export type JsiWebSocketEventType =
  | 'onOpen'
  | 'onMessage'
  | 'onStateChange'
  | 'onClose'
  | 'onError';
export type JsiWebSocketError = string;

export type JsiWebSocketCallback =
  | { type: 'onOpen' }
  | {
      type: 'onMessage';
      message: string;
    }
  | {
      type: 'onStateChange';
      state: JsiWebSocketState;
    }
  | { type: 'onClose' }
  | {
      type: 'onError';
      error: JsiWebSocketError;
    };

export type ConnectParams = {
  endpoint: string;
  headers?: Record<string, string>;
};

class _ixWebSocket {
  private events: Array<EmitterSubscription> = [];
  private eventEmitter = new NativeEventEmitter(Websockets);
  private baseCallback: (event: JsiWebSocketCallback) => void = () => {};

  private onNativeEvent = (event: JsiWebSocketCallback) => {
    this.baseCallback(event);
  };

  setOnEvent(callback: (event: JsiWebSocketCallback) => void) {
    this.baseCallback = callback;
  }

  connect(params: ConnectParams) {
    this.events.push(
      this.eventEmitter.addListener('onNativeMessage', this.onNativeEvent)
    );
    Websockets.connect(params);
  }

  close() {
    Websockets.close();
  }

  state(): JsiWebSocketState {
    return Websockets.state();
  }

  sendMessage(message: string): boolean {
    return Websockets.sendMessage(message);
  }

  unsubscribeAll() {
    this.events.forEach((e) => e.remove());
    this.baseCallback = () => {};
  }
}

export { _ixWebSocket as JsiWebSockets };
