#import "Websockets.h"

@interface Websockets() {
  NSURLSessionWebSocketTask *wsTask;
  NSURLSession *openedSession;
  NSString* wsState;
}

@end

@interface Websockets () <NativeWebsocketsSpec, NSURLSessionWebSocketDelegate>
@end

@implementation Websockets
RCT_EXPORT_MODULE()

- (NSArray<NSString *> *)supportedEvents {
  return @[@"onNativeMessage"];
}

- (void)connect:(JS::NativeWebsockets::SpecConnectData &)data {
  if (openedSession != NULL) return;
  
  NSDictionary *headers = [[NSMutableDictionary alloc] init];
  
  if (data.headers()) {
    headers = (NSDictionary*) data.headers();
  }
  
  
    auto config = [NSURLSessionConfiguration defaultSessionConfiguration];
    [config setHTTPAdditionalHeaders:headers];
    auto queue = [[NSOperationQueue alloc]init];
    
    openedSession = [NSURLSession sessionWithConfiguration:config delegate:self delegateQueue: queue];
    @try {
      wsTask = [openedSession webSocketTaskWithURL:[[NSURL alloc]initWithString:data.endpoint()]];
      wsState = @"CONNECTING";
      [self sendMessageToJs:@"onStateChange" with:wsState];
      [wsTask resume];
    } @catch(id anException) {
      [self sendMessageToJs:@"onError" with:@"anException"];
    }
}

- (NSString *)state {
  return wsState;
}

- (NSNumber *)sendMessage:(NSString *) message {
  return [self wsSendMessage:message] == 1 ? @1 : @0;
}

- (void)close { 
  if (wsTask != nil) {
      [wsTask cancelWithCloseCode:NSURLSessionWebSocketCloseCodeNormalClosure reason:nil];
    }
}

-(BOOL)wsSendMessage:(NSString*)message {
  if (wsTask == nil || ![wsState  isEqual: @"OPEN"]) return false;

  auto socketMessage = [[NSURLSessionWebSocketMessage alloc] initWithString:message];
  [wsTask sendMessage:socketMessage completionHandler:^(NSError * _Nullable error) {}];
  return true;
}

-(void) sendMessageToJs:(NSString *)messageType with:(NSString *) message {
  [self sendEventWithName:@"onNativeMessage" body:@{@"type": messageType, @"message": message}];
}

-(void)gracefulClose {
  wsState = @"CLOSE";
  [self sendMessageToJs:@"onStateChange" with:wsState];
  wsTask = nil;
  openedSession = nil;
  [openedSession finishTasksAndInvalidate];
}

-(void)startReceiveMessage {
  if (wsTask == nil || ![wsState  isEqual: @"OPEN"]) return;
  
  [wsTask receiveMessageWithCompletionHandler:^(NSURLSessionWebSocketMessage * _Nullable message, NSError * _Nullable error) {
    if (error != nil) {
      [self sendMessageToJs:@"onError" with:error.domain.description];
    } else if (message != nil) {
      if ([message type] == NSURLSessionWebSocketMessageTypeData) {
        NSString *m = [[NSString alloc] initWithData:[message data] encoding:NSUTF8StringEncoding];
        [self sendMessageToJs:@"onMessage" with:m];
      } else {
        [self sendMessageToJs:@"onMessage" with:[message string]];
      }
    }
    [self startReceiveMessage];
  }];
}

- (void)URLSession:(NSURLSession *)session webSocketTask:(NSURLSessionWebSocketTask *)webSocketTask didOpenWithProtocol:(NSString *)protocol {
  
  wsState = @"OPEN";
  [self sendMessageToJs:@"onStateChange" with:wsState];
  [self sendMessageToJs:@"onOpen" with:@""];
  [self startReceiveMessage];
}

- (void)URLSession:(NSURLSession *)session webSocketTask:(NSURLSessionWebSocketTask *)webSocketTask didCloseWithCode:(NSURLSessionWebSocketCloseCode)closeCode reason:(NSData *)reason {
  [self gracefulClose];
  [self sendMessageToJs:@"onClose" with:@""];
}

- (void)URLSession:(NSURLSession *)session
              task:(NSURLSessionTask *)task
didCompleteWithError:(NSError *)error {
  NSLog(@"didCompleteWithError %@", error.description);
  if (error.domain != NULL) {
    [self sendMessageToJs:@"onError" with:error.domain];
    [self gracefulClose];
  }
}


- (std::shared_ptr<facebook::react::TurboModule>)getTurboModule:
(const facebook::react::ObjCTurboModule::InitParams &)params
{
  return std::make_shared<facebook::react::NativeWebsocketsSpecJSI>(params);
}

@end
