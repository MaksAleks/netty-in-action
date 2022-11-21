# This repo is dedicated to learning Netty framework by "Netty in Action" book.

## Main Netty's building blocks

- **Channels**: Represent open connection to socket. It's just like a vehicle for incoming and outgoing data.

- **Events and handlers**: Netty uses events to inform out business logic about  
    some action happened: a connection was established, a new inbound message has come, a connection was closed etc.  
    To react to this events Netty provide the mechanism to register callbacks using `interface ChannelHalder` abstraction.  
    There are different types of events, hence different types of `ChannelHandler`s  
  
- **Futures** `ChannelFuture`: Represent future result of an operation
  - You can register multiple listeners: `ChannelFutureListener` to invoke some logic when future result is ready

