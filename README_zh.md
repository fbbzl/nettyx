# Nettyx

## 鸣谢
 首先谢谢家人, 给了我充足的时间专注在此项目上, 然后感谢JetBrain赠送的Ultimate Edition版的Idea, 最后谢谢自己.
 希望此框架能够为大家节省哪怕一分钟的开发时间

## 链接
JetBrain官网: https://www.jetbrains.com<br>
Github地址: https://github.com/fbbzl/nettyx<br>
Gitee地址: https://gitee.com/fbbzl/nettyx<br>
更多使用案例: https://blog.csdn.net/fbbwht<br>

### 介绍
基于[netty4.1.X.Final]进行了超轻量级的封装, 提供了一些工具和基础模板, 并额外提供串口通信模板, 帮助你快速搭建基于netty的服务端/客户端应用 及 基于串口的应用

#### 安装教程
1. 在项目添加以下依赖包：
```xml

<dependency>
    <groupId>io.github.fbbzl</groupId>
    <artifactId>nettyx</artifactId>
    <version>2.5.2-RELEASE</version>
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
channel                               channel扩展
  ---bluetooth
    ---client
      ---BtChannel                    蓝牙channel 
      ---BtChannelConfig              蓝牙channel 配置
      ---BtChannelOption              蓝牙channel 配置选项
      ---BtDeviceAddress              蓝牙设备地址     
  ---enhanced
    ---EnhancedOioByteStreamChannel     优化读任务阻塞   
  ---serial
    ---jsc                              jsc
      ---JscChannel                     jsc 串口channel
      ---JscChannelConfig               jsc 串口channel 配置
      ---JscChannelOption               jsc 串口channel 配置选项        
    ---rxtx                             rxtx 串口channel
      ---RxtxChannel                    rxtx 串口channel
      ---RxtxChannelConfig              rxtx 串口channel 配置
      ---RxtxChannelOption              rxtx 串口channel 配置选项
    SerialCommChannel                   通用串口通道
codec                              提供了一些基本的编解码器
  ---DelimiterBasedFrameCodec          基于分隔符编解码器
  ---EscapeCodec                       协议敏感字替换，例如转义
  ---StartEndFlagFrameCodec            Start End Flag 编解码器，用于根据开始和结束标志对消息进行解码
  ---StringMessageCodec                字符串编解码器
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
  ---ChannelInterceptor                channel拦截器，适用于通信前握手等预操作
  ---ActionIdleStateHandler            空闲状态处理程序,lambda增强
  ---ActioneReadTimeoutHandler         可操作的 读取超时 处理器
  ---ActionWriteTimeoutHandler         可操作的 写超时 处理器
  ---ChannelAdvice                     包含入站建言(InboundAdvice)和出站建言(OutboundAdvice)
  ---IdledHeartBeater                  闲置心跳器,读闲置,写闲置,读写闲置
  ---MessageFilter                     消息过滤器
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
     ---StructDefinition               结构体定义
     ---StructFieldHandler             字段处理器
     ---StructSerializer               核心结构体序列化器
     ---StructSerializerContext        struct 序列化器的上下文，包含 handler-instance、type cache等等
     ---StructHepler                   序列化工具
  ---Serializer.java                    顶级序列化器接口
ssl
  ---OpenSslContextFactory           OpenSSL 上下文工厂
  ---SslContextFactory               SSL 上下文工厂
template
  ---serial
    ---jsc
      ---MultiJscChannelTemplate        多通道java serial comm客户端模板
      ---SingleJscChannelTemplate       单通道java serial comm客户端模板
    ---rxtx
      ---MultiRxtxChannelTemplate       多通道rxtx客户端模板
      ---SingleRxtxChannelCTemplate     单通道rxtx客户端模板
  ---tcp
    ---client
      ---MultiTcpChannelTemplate        多通道tcp客户端模板
      ---ServerDetector                 远程服务探测器
      ---SingleTcpChannelCTemplate      单通道tcp客户端模板
    ---server
      ---TcpServer                      服务单模板
  ---AbstractMultiChannelTemplate      抽象多通道模板       
  ---AbstractSingleChannelTemplate     抽象单通道模板
util                                 基础工具
  ---Bins                            二进制工具
  ---CommPorts                       串口工具
  ---EndianKit                       字节工具  
  ---Exceptions                      异常工具
  ---HexKit                          16进制工具
  ---TypeRefer                      结构泛型类型应用
  
```

