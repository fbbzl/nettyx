# Nettyx

## Express gratitude
First of all, I would like to thank my family for giving me enough time to focus on this project, then I would like to thank JetBrain for giving me the Ultimate Edition of the Idea, and finally I would like to thank myself.
Hopefully, this framework will save you even a minute of development time

## Links
JetBrain Website: https://www.jetbrains.com<br>
Github address: https://github.com/fbbzl/nettyx<br>
Gitee address: https://gitee.com/fbbzl/nettyx<br>
for more use cases please refer to: https://blog.csdn.net/fbbwht

#### Introduce
Based on [netty4.1.X. Final], ultra-lightweight packaging has been carried out, providing some tools and basic templates, as well as additional serial communication templates to help you quickly build netty-based server/client applications and serial port based applications

#### Installation Tutorial
1. Add Maven Dependency：
```xml
<dependency>
    <groupId>io.github.fbbzl</groupId>
    <artifactId>nettyx</artifactId>
    <version>2.3.20-RELEASE</version>
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
channel                               channel extention
  ---jsc                              java serial comm channel
    ---JscChannel
    ---JscChannelConfig
    ---JscChannelOption                       
  ---rxtx                             java rxtx channel
    ---RxtxChannel
    ---RxtxChannelConfig
    ---RxtxChannelOption
  EnhancedOioByteStreamChannel         non blocking read Oio
  SerialCommChannel                    abstract serial comm channel
codec                               Provided some basic codecs
  ---DelimiterBasedFrameCodec          Based on the delimiter codec
  ---EscapeCodec                       Protocol sensitive word replacement, such as escape
  ---StartEndFlagFrameCodec            Start End Flag Codec, use to decoe message based on start and end flag
  ---StringMessageCodec                String Codec
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
  ---ChannelInterceptor                Channel interceptor, suitable for pre operation such as handshake before communication   
  ---ActionIdleStateHandler            Actionable Idle State Handler
  ---ActionReadTimeoutHandler          Actionable ReadTimeout Handler
  ---ActionWriteTimeoutHandler         Actionable WriteTimeout Handler
  ---ChannelAdvice                     contains inbound-advice and outbound-advice
  ---IdledHeartBeater                  will do heartbeat after idle
  ---MessageFilter                     use to filter message

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
  ---Serializer.java                   top level serializer interface
ssl
  ---OpenSslContextFactory           OpenSSL context factory
  ---SslContextFactory               SSL context factory
template
  ---serial
    ---jsc
      ---MultiJscChannelTemplate       Client with multiple channels, using key to retrieve corresponding channels    
      ---SingleJscChannelTemplate      Single Channel Client
    ---rxtx
      ---MultiRxtxChannelTemplate       Client with multiple channels, using key to retrieve corresponding channels
      ---SingleRxtxChannelCTemplate     Single Channel Client
  ---tcp
    ---client
      ---MultiTcpChannelTemplate        Client with multiple channels, using key to retrieve corresponding channels
      ---ServerDetector                 remote server detector     
      ---SingleTcpChannelCTemplate      Single Channel Client
    ---server
      ---TcpServer
  ---AbstractMultiChannelTemplate      Abstract parent class for multi-channel client       
  ---AbstractSingleChannelTemplate     Abstract parent class for single channel client
util                              nettyx tool class
  ---Bins                            binary util
  ---BtFinder                        bluetooth device search util. [Sorry, im not your Lou, Sam]
  ---ChannelStorage                  Storage channel, internally using KV pairs for storage
  ---CommPorts                       commport util
  ---EndianKit                       bytes tool
  ---Exceptions                      exceptioin util
  ---HexKit                          Hexadecimal tool
  ---Throws                          assert util
  ---Try                             lambda exception
  
```
