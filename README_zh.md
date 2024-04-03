# Nettyx

## 鸣谢
 首先谢谢家人, 给了我充足的时间专注在此项目上, 然后感谢JetBrain赠送的Ultimate Edition版的Idea, 最后谢谢自己.
 希望此框架能够为大家节省哪怕一分钟的开发时间

## 链接
JetBrain官网: https://www.jetbrains.com
Github地址: https://github.com/fbbzl/nettyx
Gitee地址: https://gitee.com/fbbzl/nettyx
更多使用案例: https://blog.csdn.net/fbbwht

### 介绍
基于[netty4.1.X.Final]进行了超轻量级的封装, 提供了一些工具和基础模板, 并额外提供串口通信模板, 帮助你快速搭建基于netty的服务端/客户端应用 及 基于串口的应用

#### 安装教程
1. 在项目添加以下依赖包：
```xml

<dependency>
    <groupId>io.github.fbbzl</groupId>
    <artifactId>nettyx</artifactId>
    <version>2.2.16-RELEASE</version>
</dependency>
```
## api
```
action                              包含足够的功能接口来支持nettyx函数式编程
  ---Actions                        操作泛型实用程序                                           
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
codec                              提供了一些基本的编解码器
  ---DelimiterBasedFrameCodec          基于分隔符编解码器
  ---EscapeCodec                       协议敏感字替换，例如转义
  ---StartEndFlagFrameCodec            Start End Flag 编解码器，用于根据开始和结束标志对消息进行解码
  ---StringMessageCodec                字符串编解码器
endpoint
  client
     jsc
       support                         jsc对netty的支持实现
       ---MultiJscChannelClient        多jsc通道客户端 
       ---SingleJscChannelClient       单jsc通道客户端
     rxtx
       support                        重写了RxtxChannel, 提供了异步读取的方法, 读取将不会在阻塞发送
       ---MultiRxtxChannelClient        多rxtx通道客户端 
       ---SingleRxtxChannelClient       单rxtx通道客户端
     tcp
       ---MultiTcpChannelClient         多tcp通道客户端 
       ---SingleTcpChannelClient        单tcp通道客户端
     ---AbstractMultiChannelClient      多通道客户端的抽象父类    
     ---AbstractSingleChannelClient     单通道客户端的抽象父类
     ---Client                          客户端顶级抽象父类
  server
     ---TcpServer
envet                                为网络事件提供支持
  ---ChannelEvent                     Channel 事件对象，建议与 Spring 容器事件结合使用
  ---ChannelEvents                    通道事件对象工具
exception                           异常扩展
  ---HandlerException
  ---NoSuchPortException
  ---ParameterizedTypeException
  ---SerializeException
  ---TooLessBytesException
  ---TypeJudgmentException
handler                             提供了一些基本的通道处理程序实现
  interceptor
     ---ChannelInterceptor                信道拦截器，适用于通信前握手等预操作
     ---ChannelInterceptors               通道拦截器实用程序
  ---ActionIdleStateHandler            可操作的空闲状态处理程序
  ---ActioneReadTimeoutHandler         可操作的 读取超时 处理程序
  ---ActionWriteTimeoutHandler         可操作的 写超时 处理程序
  ---ChannelAdvice                     包含入站建言和出站建言
  ---IdledHeartBeater                  闲置后的心跳器
  ---LoggerHandler                     进入和退出消息日志
  ---MessageStealer                    用于丢弃消息
listener
  ---ActionableChannelFutureListener   可操作的频道未来侦听器
serializer                             序列化工具
  struct
     annotation
        ---Ignore                      序列化时忽略此字段
        ---Struct                      在序列化中，需要对域类型进行注释，类似于 JPA 中的 @Entity
        ---ToArray                     数组序列化器
        ---ToArrayList                 ArrayList序列化器
        ---ToLinkedList                ToLinkedList序列化器
        ---ToNamedEnum                 Named Enum序列化器
        ---ToString                    String序列化器
     basic
        c                              内置的C基础类型
        cpp                            内置的Cpp基础类型
        ---Basic                       基础类型顶级父类
     ---PropertyHandler                字段处理器
     ---StructSerializer               核心结构体序列化器
     ---StructUtils                    序列化工具
     ---TypeRefer                      结构泛型类型应用
  xml
     dtd
        ---Dtd                         xml序列化器定义
     handler
        ---EnumHandler                 
        ---NumberHandler             
        ---PropHandler                 顶级的xml prop处理器
        ---PropTypeHandler             处理prop-type的处理器
        ---StringHandler             
        ---SwitchHandler             
     ---XmlSerializer                  从xml中读取配置, 然后序列化成LinkedMap
     ---XmlSerializerContext           xml序列化器的上下文
     ---XmlUtils        
  ---Serializer.java                    顶级序列化器接口
ssl
  ---OpenSslContextFactory           OpenSSL 上下文工厂
  ---SslContextFactory               SSL 上下文工厂
util                                 基础工具
  ---Bins                            二进制工具
  ---ChannelStorage                  存储通道，内部使用 KV 对进行存储
  ---CommPorts                       串口工具
  ---EndianKit                       字节工具  
  ---Exceptions                      异常工具
  ---HexKit                          16进制工具
  ---Throws                          建言工具
  ---Try                             lambda受检异常工具
  
```

