# This repo is dedicated to learning Netty framework by "Netty in Action" book.

## Main Netty's building blocks

- **Channels**: Represent open connection to socket. It's just like a vehicle for incoming and outgoing data.

- **Events and handlers**: Netty uses events to inform out business logic about  
    some action happened: a connection was established, a new inbound message has come, a connection was closed etc.  
    To react to this events Netty provide the mechanism to register callbacks using `interface ChannelHalder` abstraction.  
    There are different types of events, hence different types of `ChannelHandler`s  
  
- **Futures** `ChannelFuture`: Represent future result of an operation
  - You can register multiple listeners: `ChannelFutureListener` to invoke some logic when future result is ready


### Event Loop and Event Loop Group

- `EventLoop` is a Netty's abstraction for handling events that occur during a connection lifetime.  
- `EventLoopGroup` consists of one or several `EventLoop`s.
- Each `EventLoop` is bound to a one single `Thread`.
- All I/O events processed by `EventLoop` are handled on its dedicated `Thread`.
- A `Channel` is registered for its lifetime with a single `EventLoop`
- One `EventLoop` may be assigned to one or more `Channel`

### Channel Handlers and Channel Pipeline

- `ChannelHandler` is the container where your application business logic will reside.  
- `ChannelHandler` is triggered by network events (here word "event" is used very broadly -  
it can mean inbound or outbound data, connection establishment or closing connection...).
- `ChannelHandler`s must be registered for each new connection in order to react to events arising  
at this connection.
- `ChannelPipeline` provides a container for a chain of `ChannelHandlers`
- each `Channel` is assigned its own `ChannelPipeline`
- When event arises it flows through each `ChannelHandler`. The order in which they are executed
is determined by the order in which they were added.
- You can divide `ChannelHandler`s to two specific types:
  - `ChannelInboundHandler`
  - `ChannelOutboutHandler`

```plantuml
skinparam groupInheritance 2
skinparam linetype ortho

interface ChannelHandler
interface ChannelInboundHandler
interface ChannelOutboundHandler

ChannelInboundHandler -up-|> ChannelHandler
ChannelOutboundHandler -up-|> ChannelHandler
```

- From a client's point of view an **even is outbound** - if it moves from client to a server,
and **event is inbound** - if it moves from a server to the client.

An event can be forwarded to the next handler in the current chain  
by using the `ChannelHandlerContext` that’s supplied as an argument to each method.  
Because you’ll sometimes ignore uninteresting events, Netty provides
the abstract base classes `ChannelInboundHandlerAdapter` and `ChannelOutboundHandlerAdapter`.  
Each provides method implementations that simply pass the event to the next handler
by calling the corresponding method on the `ChannelHandlerContext`.  
You can then extend the class by overriding the methods that interest you.

### Sending messages

There are two ways of sending messages in Netty:
- write directly to the `Channel` - the message to start from the tail of the `ChannelPipeline`
- write to a ChannelHandlerContext object -  the message to start from the _next_ handler in the `ChannelPipeline` 


### Adapters

Netty provides default implementations of `ChannelHandler`, called adapters, where all methods have implemented
with default behaviour.  
These are the adapters you’ll call most often when creating your custom handlers:
- `ChannelHandlerAdapter`
- `ChannelInboundHandlerAdapter`
- `ChannelOutboundHandlerAdapter`
- `ChannelDuplexHandlerAdapter`

### Encoders and Decoders

They are simply other types of `ChannelHandler`.  
Encoders convert a message from an application convenient format to bytes.  
Decoders do reverse job.  

Given this, encoders implement `ChannelOutboundAdapter`,  
and decoders implement `ChannelInboundAdapter`.

There are different types of encoders and decoders implemented in Netty.  
If you need custom encoders/decoders, there are corresponding base classes:  
`MessageToByteEncoder<T>` and `ByteToMessageDecoder`;

### SimpleChannelInboundHandler<T>

Most frequently your application will employ a handler that receives a decoded message  
and applies business logic to the data. To create such a `ChannelHandler`,  
you need only extend the base class `SimpleChannelInboundHandler<T>`,  
where T is the Java type of the message you want to process.  

In this handler you’ll override one or more methods of the base class  
and obtain a reference to the `ChannelHandlerContext`,
which is passed as an input argument to all the handler methods.  

The most important method in a handler of this type is `channelRead0(ChannelHandlerContext,T)`.  
The implementation is entirely up to you, except for the __requirement that the current I/O thread not be blocked__.  

### Bootstrapping

Bootstrap classes provide containers for configuring network layer, which involves:
 - binding to a specific port: `ServerBootstrap` (server side)
 - connecting to a specific `host:port`: `Bootstrap` (client side)

 - `Bootstrap` (client) requires only one `EventLoopGroup`,
 - `ServerBootstrap` - two `EventLoopGroup`s

A server needs two distinct sets of channels:
 - First set will contain a single `ServerChannel`, bound to a local port
 - Second set will contain new `Channel`s, representing new incoming connections

![Different EventLoopGroups for Server and Client bootstraps](./resources/Bootstrapping.png))
