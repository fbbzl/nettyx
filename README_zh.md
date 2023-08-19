# nettyx

#### 介绍
基于[netty4.1.90.Final]进行了超轻量级的封装, 提供了一些工具和基础模板, 并额外提供串口通信模板, 帮助你快速搭建基于netty的服务端/客户端应用 及 基于串口的应用

#### 安装教程
1. 在项目添加以下依赖包：
```xml
截止2023/8/19为止, 最新版本为[2.0.1-RELEASE]
<dependency>
    <groupId>io.github.fbbzl</groupId>
    <artifactId>nettyx</artifactId>
    <version>2.0.2-RELEASE</version>
</dependency>
```
## api
```
annotation
  ---FieldHandler                     在序列化字段时，可以指定字段序列化/反序列化逻辑
  ---Ignore                           序列化时忽略此字段
  ---Length                           在序列化/反序列化时，数组类型的字段必须使用此注释来指定长度
  ---Struct                           在序列化中，需要对域类型进行注释，就像JPA中的@Entity一样
endpoint
  client                              提供client端基础实现
    ---Client                            顶级Client抽象  
    ---rxtx                           串口封装
       ---RxtxClient                     串口通信顶级父类
       ---MultiRxtxChannelClient         多channel串口通信, 使用key检索对应channel
       ---SingleRxtxChannelClient        单channel串口通信
    ---tcp                            tcp封装
       ---TcpClient                      tcp通信顶级父类
       ---MultiTcpChannelClient          多Channel的Client, 使用key检索对应channel
       ---SingleTcpChannelClient         单Channel的Client
    ---upd 未完成
    ---jsc                               简单的基于jsc的java串口通信实现
  server
    ---Server                          提供server端基础实现
codec                               提供了一些基础的编解码器
  ---DelimiterBasedFrameCodec          基于分隔符codec
  ---EscapeCodec                       协议敏感字替换
  ---StartEndFlagFrameCodec            起止符codec
  ---StringMessageCodec                字符串Codec
envet                                对netty事件提供支持
  ---ChannelEvent                      Channel事件对象, 建议配合Spring容器事件使用
  ---ChannelEvents                     Channel事件对象工具
exception                           异常扩展
  ---ClosingChannelException           配合channel advice, 可以通过抛出该异常子类来关闭channel
function                            包含了充足的函数式接口, 为nettyx函数式编程提供支持                     
  ---Action                            
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
handler                             提供了一些基础的channel handler实现
  actionable
     ---ActionableIdleStateHandler     可操作闲置处理器
     ---ActionableReadTimeoutHandler   可操作读取超时处理器
     ---ActionableWriteTimeoutHandler  可操作写超时处理器
  ---AdvisableChannelInitializer       channel建言初始化器
  ---ChannelInterceptor                channel拦截器, 适用于通信前的握手动作等前置操作
  ---ExceptionHandler                  异常处理器
  ---HeartBeater                       tcp心跳器
  ---LoggerHandler                     出入站消息日志
listener
  ---ActionableChannelFutureListener   可操作channel future监听器
serializer                             序列化工具
  ---offset
    ---AnnotatedOffsetByteBufSerializer   基于注释的序列化器
    ---OffsetByteBufSerializer            基于偏移量的序列化器
    ---YmlOffsetByteBufSerializer         基于yml配置文件的序列化器
  ---typed
    ---Basic                              序列化时的基类型by type
    ---TypedByteBufSerializer             基于类型的序列化器
  ---ByteBufSerializer                    序列化/反序列化顶级抽象
  ---Serializers.java                     通用序列化工具
ssl
  ---OpenSslContextFactory           openssl工厂
  ---SslContextFactory               ssl context工厂
util                              基础工具类
  ---ChannelStorage                  存储channel, 内部使用KV对存储
  ---HexBins                         16进制工具
  
```
