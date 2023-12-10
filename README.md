# nettyx

#### Introduce
Based on [netty4.1.101. Final], ultra-lightweight packaging has been carried out, providing some tools and basic templates, as well as additional serial communication templates to help you quickly build netty-based server/client applications and serial port based applications

#### Installation Tutorial
1. Add Maven Dependency：
```xml

<dependency>
    <groupId>io.github.fbbzl</groupId>
    <artifactId>nettyx</artifactId>
    <version>2.0.6-RELEASE</version>
</dependency>
```
## api
```
annotation
  ---FieldHandler                     When serializing fields, you can specify field serialization/deserialization logic
  ---Ignore                           ignore this field when serializing
  ---Length                           When serialized/deserialized, fields of array type must use this annotation to specify the length
  ---Struct                           In serialization, the domain type needs to be annotated, something like @Entity in JPA
endpoint
  client                              Provide client side basic implementation
    ---Client                            Top-level client abstraction  
    ---rxtx                           Serial rxtx packaging
       ---RxtxClient                     Serial communication top-level parent class
       ---MultiRxtxChannelClient         Multi channel serial communication, using key to retrieve corresponding channels
       ---SingleRxtxChannelClient        Single channel serial communication
    ---tcp                            TCP encapsulation
       ---TcpClient                      TCP encapsulation
       ---MultiTcpChannelClient          Client with multiple channels, using key to retrieve corresponding channels
       ---SingleTcpChannelClient         Single Channel Client
    ---upd (incomplete)
    ---jsc                               A Simple Implementation of Java Serial Communication Based on JSC
  server
    ---Server                          Provide server-side basic implementation
codec                               Provided some basic codecs
  ---DelimiterBasedFrameCodec          Based on the delimiter codec
  ---EscapeCodec                       Protocol sensitive word replacement, such as escape
  ---StartEndFlagFrameCodec            Start End Flag Codec, use to decoe message based on start and end flag
  ---StringMessageCodec                String Codec
envet                                Provide support for netty events
  ---ChannelEvent                      Channel event object, recommended to be used in conjunction with Spring container events
  ---ChannelEvents                     Channel Event Object Tool
exception                           Abnormal extension
  ---ClosingChannelException           In conjunction with channel advice, the channel can be closed by throwing the abnormal subclass
function                            Contains sufficient functional interfaces to support nettyx functional programming                                             
  ---ChannelAction                      
  ---ChannelBindAction
  ---ChannelConnectAction
  ---ChannelEventAction
  ---ChannelExceptionAction
  ---ChannelFutureAction
  ---ChannelHandlerContextAction
  ---ChannelPromiseAction
  ---ChannelReadAction
  ---ChannelWriteAction
handler                             Provided some basic channel handler implementations
  actionable
     ---ActionableIdleStateHandler     Actionable Idle State Handler
     ---ActionableReadTimeoutHandler   Actionable ReadTimeout Handler
     ---ActionableWriteTimeoutHandler  Actionable WriteTimeout Handler
  ---AdvisableChannelInitializer       Channel advice initializer
  ---ChannelInterceptor                Channel interceptor, suitable for pre operation such as handshake before communication
  ---ExceptionHandler                  exception handler
  ---HeartBeater                       TCP heartbeat device
  ---LoggerHandler                     Entry and exit message log
  ---MessageStealer                    use to discard message
  ---ReadHandler                       use to extends read bytebuf 
  ---ReadWriteHandler                  use to extends read-write bytebuf 
  ---WriteHandler                      use to extends write bytebuf 
listener
  ---ActionableChannelFutureListener   Actionable channel future listener
serializer                             Serialization tool
  ---typed
    ---Basic                              base type when serialized deserialized by type
    ---TypedByteBufSerializer             type based serializer
  ---ByteBufSerializer                    serialize deserialize top level abstractions
  ---Serializers.java                     universal serialization tool
ssl
  ---OpenSslContextFactory           OpenSSL context factory
  ---SslContextFactory               SSL context factory
util                              Basic tool class
  ---ChannelStorage                  Storage channel, internally using KV pairs for storage
  ---HexBins                         Hexadecimal tool
  
```
