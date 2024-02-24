# nettyx

#### Introduce
Based on [netty4.1.X. Final], ultra-lightweight packaging has been carried out, providing some tools and basic templates, as well as additional serial communication templates to help you quickly build netty-based server/client applications and serial port based applications

#### Installation Tutorial
1. Add Maven Dependency：
```xml
<dependency>
    <groupId>io.github.fbbzl</groupId>
    <artifactId>nettyx</artifactId>
    <version>2.1.2-RELEASE</version>
</dependency>
```
## api
```
action                              Contains sufficient functional interfaces to support nettyx functional programming 
  ---Actions                        Action Generic util                                            
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
codec                               Provided some basic codecs
  ---DelimiterBasedFrameCodec          Based on the delimiter codec
  ---EscapeCodec                       Protocol sensitive word replacement, such as escape
  ---StartEndFlagFrameCodec            Start End Flag Codec, use to decoe message based on start and end flag
  ---StringMessageCodec                String Codec
endpoint
  serial
      jsc                            A Simple Implementation of Java Serial Communication Based on JSC
         ---JSerialCommClient           simple jsc client    
  tcp                                    TCP encapsulation
     client                              Provide client side basic implementation
       ---TcpClient                      TCP encapsulation
       ---MultiTcpChannelClient          Client with multiple channels, using key to retrieve corresponding channels
       ---SingleTcpChannelClient         Single Channel Client
     server
       ---Server                          Provide server-side basic implementation
  ---Client                            Top-level client abstraction  
envet                                Provide support for netty events
  ---ChannelEvent                      Channel event object, recommended to be used in conjunction with Spring container events
  ---ChannelEvents                     Channel Event Object Tool
exception                           Abnormal extension
  ---ClosingChannelException           In conjunction with channel advice, the channel can be closed by throwing the abnormal subclass
  ---HandlerException
  ---NoSuchPortException
  ---ParameterizedTypeException
  ---SerializeException
  ---TooLessBytesException
  ---TypeJudgmentException
handler                             Provided some basic channel handler implementations
  actionable
     ---ActionableIdleStateHandler     Actionable Idle State Handler
     ---ActionableReadTimeoutHandler   Actionable ReadTimeout Handler
     ---ActionableWriteTimeoutHandler  Actionable WriteTimeout Handler
  advice
     ---InboundAdvice                  Inbound Advice
     ---OutboundAdvice                 Outbound Advice
  interceptor
     ---ChannelInterceptor                Channel interceptor, suitable for pre operation such as handshake before communication
     ---ChannelInterceptors               Channel interceptor utils
  ---AdvisableChannelInitializer       Channel advice initializer
  ---ExceptionHandler                  exception handler
  ---HeartBeater                       TCP heartbeat device
  ---LoggerHandler                     Entry and exit message log
  ---MessageStealer                    use to discard message
listener
  ---ActionableChannelFutureListener   Actionable channel future listener
serializer                             Serialization tool
  struct
     annotation
        ---Ignore                      ignore this field when serializing
        ---Struct                      In serialization, the domain type needs to be annotated, something like @Entity in JPA
        ---ToArray                     Array serializer
        ---ToArrayList                 ArrayList serializer
        ---ToLinkedList                ToLinkedList serializer
        ---ToNamedEnum                 Named Enum serializer
        ---ToString                    String serializer
     basic
        c                              internal c basic types
        cpp                            internal cpp basic types
        ---Basic                       basic type
     ---PropertyHandler                top property handler interface
     ---StructSerializer               core struct serializer tool
     ---StructUtils                    struct serializer tool
     ---TypeRefer                      struct serializer type util
  xml
     ---XmlSerializer
  yml
     ---YmlSerializer
  ---Serializer.java                   top level serializer interface
ssl
  ---OpenSslContextFactory           OpenSSL context factory
  ---SslContextFactory               SSL context factory
util                              Basic tool class
  ---ChannelStorage                  Storage channel, internally using KV pairs for storage
  ---HexBins                         Hexadecimal tool
  ---Bins                            binary util
  ---BytesKit                        bytes tool           
  ---CommPorts                       commport util
  ---Exceptions                      exceptioin util
  ---Throws                          assert util
  ---Try                             lambda exception
  
```
