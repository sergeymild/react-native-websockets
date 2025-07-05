import type { TurboModule } from 'react-native';
import { TurboModuleRegistry } from 'react-native';
import type { UnsafeObject } from 'react-native/Libraries/Types/CodegenTypes';

export interface Spec extends TurboModule {
  connect: (data: { endpoint: string; headers?: UnsafeObject }) => void;
  close: () => void;
  state: () => 'CONNECTING' | 'OPEN' | 'CLOSING' | 'CLOSED';
  sendMessage: (message: string) => boolean;

  // Events
  addListener(eventName: string): void;
  removeListeners(count: number): void;
}

export default TurboModuleRegistry.getEnforcing<Spec>('Websockets');
