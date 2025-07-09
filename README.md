# React Native WebSockets

A high-performance WebSocket client for React Native applications, implemented using JSI (JavaScript Interface) for efficient native-to-JavaScript communication.

## Features

- Fast and efficient WebSocket implementation using React Native's TurboModule system
- Simple and intuitive API
- Support for text and binary messages
- TypeScript support
- Event-based communication
- Cross-platform (iOS and Android)

## Installation

```sh
"react-native-jsi-websockets":"sergeymild/react-native-jsi-websockets#1.0.2"
```

## Usage

Here's a basic example of how to use the library:

```jsx
import { JsiWebSockets } from 'react-native-websockets';
import React, { useState, useEffect } from 'react';

function WebSocketExample() {
  // Create a WebSocket instance
  const [ws] = useState(() => new JsiWebSockets());

  useEffect(() => {
    // Set up event handlers
    ws.setOnEvent((event) => {
      switch (event.type) {
        case 'onOpen':
          console.log('WebSocket connection opened');
          break;
        case 'onMessage':
          console.log('Received message:', event.message);
          break;
        case 'onClose':
          console.log('WebSocket connection closed');
          break;
        case 'onError':
          console.log('WebSocket error:', event.error);
          break;
        case 'onStateChange':
          console.log('WebSocket state changed to:', event.state);
          break;
      }
    });

    // Connect to a WebSocket server
    ws.connect({ 
      endpoint: 'wss://example.com/socket',
      headers: { 'Authorization': 'Bearer token123' } // Optional headers
    });

    // Clean up on unmount
    return () => {
      ws.close();
      ws.unsubscribeAll();
    };
  }, [ws]);

  // Example of sending a message
  const sendMessage = (message) => {
    ws.sendMessage(message);
  };

  // Example of checking connection state
  const checkState = () => {
    const state = ws.state(); // Returns: 'CONNECTING', 'OPEN', 'CLOSING', or 'CLOSED'
    console.log('Current WebSocket state:', state);
  };

  return (
    // Your component UI
  );
}
```

## API Reference

### `JsiWebSockets`

The main class for WebSocket functionality.

#### Methods

##### `connect(params: ConnectParams): void`

Connects to a WebSocket server.

Parameters:
- `params.endpoint` (string): The WebSocket server URL (starts with ws:// or wss://)
- `params.headers` (optional Record<string, string>): HTTP headers to include in the connection request

##### `close(): void`

Closes the WebSocket connection.

##### `state(): JsiWebSocketState`

Returns the current state of the WebSocket connection.

Return value: One of `'CONNECTING'`, `'OPEN'`, `'CLOSING'`, or `'CLOSED'`

##### `sendMessage(message: string): boolean`

Sends a text message through the WebSocket connection.

Parameters:
- `message` (string): The message to send

Return value: `true` if the message was sent successfully, `false` otherwise

##### `setOnEvent(callback: (event: JsiWebSocketCallback) => void): void`

Sets a callback function to handle WebSocket events.

Parameters:
- `callback`: A function that will be called when WebSocket events occur

##### `unsubscribeAll(): void`

Removes all event listeners and cleans up resources.

### Types

#### `JsiWebSocketState`

Represents the state of the WebSocket connection:
- `'CONNECTING'`: The connection is being established
- `'OPEN'`: The connection is open and ready to communicate
- `'CLOSING'`: The connection is in the process of closing
- `'CLOSED'`: The connection is closed or couldn't be opened

#### `JsiWebSocketEventType`

Types of events that can be emitted by the WebSocket:
- `'onOpen'`: Connection established
- `'onMessage'`: Message received
- `'onStateChange'`: Connection state changed
- `'onClose'`: Connection closed
- `'onError'`: Error occurred

#### `JsiWebSocketCallback`

The structure of the event object passed to the callback function:

```typescript
type JsiWebSocketCallback =
  | { type: 'onOpen' }
  | { type: 'onMessage'; message: string }
  | { type: 'onStateChange'; state: JsiWebSocketState }
  | { type: 'onClose' }
  | { type: 'onError'; error: string };
```

## Example

See the [example app](./example) for a complete implementation.

## License

MIT
