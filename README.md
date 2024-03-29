# Nettyx

## Express gratitude
First of all, I would like to thank my family for giving me enough time to focus on this project, then I would like to thank JetBrain for giving me the Ultimate Edition of the Idea, and finally I would like to thank myself.
Hopefully, this framework will save you even a minute of development time

## Links
JetBrain Website: https://www.jetbrains.com
Github address: https://github.com/fbbzl/nettyx
Gitee address: https://gitee.com/fbbzl/nettyx
for more use cases please refer to: https://blog.csdn.net/fbbwht

#### Introduce
Based on [netty4.1.X. Final], ultra-lightweight packaging has been carried out, providing some tools and basic templates, as well as additional serial communication templates to help you quickly build netty-based server/client applications and serial port based applications

#### Installation Tutorial
1. Add Maven Dependency：
```xml
<dependency>
    <groupId>io.github.fbbzl</groupId>
    <artifactId>nettyx</artifactId>
    <version>2.2.15-RELEASE</version>
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
  client
     jsc
       support                         rewrite jsc-netty support
       ---MultiJscChannelClient        Client with multiple channels, using key to retrieve corresponding channels    
       ---SingleJscChannelClient       Single Channel Client
     rxtx
       support                          rewrite rxtx-netty support, using async comm-read
       ---MultiRxtxChannelClient        Client with multiple channels, using key to retrieve corresponding channels
       ---SingleRxtxChannelClient       Single Channel Client
     tcp
       ---MultiTcpChannelClient         Client with multiple channels, using key to retrieve corresponding channels
       ---SingleTcpChannelClient        Single Channel Client
     ---AbstractMultiChannelClient      Abstract parent class for multi-channel clients       
     ---AbstractSingleChannelClient     Abstract parent class for single channel clients
     ---Client                          Client top-level abstract parent class
  server
     ---TcpServer
envet                                Provide support for netty events
  ---ChannelEvent                      Channel event object, recommended to be used in conjunction with Spring container events
  ---ChannelEvents                     Channel Event Object Tool
exception                           Abnormal extension
  ---HandlerException
  ---NoSuchPortException
  ---ParameterizedTypeException
  ---SerializeException
  ---TooLessBytesException
  ---TypeJudgmentException
handler                             Provided some basic channel handler implementations
  interceptor
     ---ChannelInterceptor                Channel interceptor, suitable for pre operation such as handshake before communication
     ---ChannelInterceptors               Channel interceptor utils
  ---ActionIdleStateHandler            Actionable Idle State Handler
  ---ActionReadTimeoutHandler          Actionable ReadTimeout Handler
  ---ActionWriteTimeoutHandler         Actionable WriteTimeout Handler
  ---ChannelAdvice                     contains inbound-advice and outbound-advice
  ---IdledHeartBeater                  will do heartbeat after idle
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
     ---StructFieldHandler             top struct field handler interface
     ---StructSerializer               core struct serializer tool
     ---StructSerializerContext        the context of struct serializer, contains handler-instance, type cache, etc
     ---StructUtils                    struct serializer tool
     ---TypeRefer                      struct serializer generic-type util
  xml
     dtd
        ---Dtd                         xml serializer defination
     handler
        ---EnumHandler                 
        ---NumberHandler             
        ---PropHandler                 top level xml prop handler
        ---PropTypeHandler             handle xlm prop type 
        ---StringHandler             
        ---SwitchHandler             
     ---XmlSerializer                  parse from xml config file
     ---XmlSerializerContext           the xml serializer context. 
     ---XmlUtils                      
  ---Serializer.java                   top level serializer interface
ssl
  ---OpenSslContextFactory           OpenSSL context factory
  ---SslContextFactory               SSL context factory
util                              nettyx tool class
  ---Bins                            binary util
  ---ChannelStorage                  Storage channel, internally using KV pairs for storage
  ---CommPorts                       commport util
  ---EndianKit                       bytes tool
  ---Exceptions                      exceptioin util
  ---HexKit                          Hexadecimal tool
  ---Throws                          assert util
  ---Try                             lambda exception
  
```
