import {
  StyleSheet,
  Text,
  TextInput,
  TouchableOpacity,
  View,
} from 'react-native';
import { JsiWebSockets } from 'react-native-websockets';
import { useState } from 'react';
import React from 'react';

const endpoint = `wss://s14895.nyc1.piesocket.com/v3/1?api_key=bwAl6zRukYfTEizi7FTYnaJkGI25GIrIEED9d7vS&notify_self=1`;

export default function App() {
  const [value, setValue] = useState('');
  const [ws] = useState(() => new JsiWebSockets());

  React.useEffect(() => {
    ws.setOnEvent((event) => {
      switch (event.type) {
        case 'onOpen':
          console.log('[😀App.onOpen]');
          break;
        case 'onMessage':
          console.log('[😀App.onMessage]', event.message);
          break;
        case 'onClose':
          console.log('[😀App.onClose]');
          break;
        case 'onError':
          console.log('[😀App.onError]', event.error);
          break;
      }
    });

    return () => {
      ws.close();
      ws.unsubscribeAll();
    };
  }, [ws]);

  return (
    <View style={styles.container}>
      <TouchableOpacity style={styles.button} onPress={() => ws.close()}>
        <Text>Close</Text>
      </TouchableOpacity>

      <TouchableOpacity
        style={styles.button}
        onPress={() => ws.connect({ endpoint })}
      >
        <Text>Open</Text>
      </TouchableOpacity>

      <TouchableOpacity
        style={styles.button}
        onPress={() => console.log('[App.]', ws.state())}
      >
        <Text>State</Text>
      </TouchableOpacity>

      <TouchableOpacity
        style={styles.button}
        onPress={() => console.log('[App.]', ws.sendMessage(value))}
      >
        <Text>Send test</Text>
      </TouchableOpacity>

      <TextInput
        value={value}
        onChangeText={setValue}
        style={{ width: '100%', height: 50, backgroundColor: 'red' }}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    paddingTop: 56,
    flex: 1,
    flexDirection: 'row',
    flexWrap: 'wrap',
    paddingHorizontal: 16,
  },
  button: {
    height: 32,
    backgroundColor: 'gray',
    justifyContent: 'center',
    paddingHorizontal: 8,
    borderRadius: 4,
    marginEnd: 16,
    marginBottom: 16,
  },
});
