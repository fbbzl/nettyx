# nettyx

#### 介绍
基于[netty4.1.72.Final]进行了超轻量级的封装, 提供了一些工具和基础模板, 并额外提供串口通信模板, 帮助你快速搭建基于netty的服务端/客户端应用 及 基于串口的应用

#### 安装教程
1. 安装极其简单, 先添加gitee库
```xml
<repositories>
  <repository>
    <id>fz</id>
    <url>https://gitee.com/fbb9290/maven-repository</url>
  </repository>
</repositories>
```

2. 然后在项目添加以下依赖包：
```xml
<dependency>
  <groupId>org.fz</groupId>
  <artifactId>nettyx</artifactId>
  <version>1.2</version>
</dependency>
```
## api
```
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
  ---ExceptionHandler                  异常处理器
  ---ChannelInterceptor                channel拦截器, 适用于通信前的握手动作等前置操作
server
  ---Server                          提供server端基础实现
ssl
  ---SslContextFactory               ssl context工厂
support                              基础工具类
  ---ActionableChannelFutureListener 可操作channel future监听器
  ---ChannelStorage                  存储channel, 内部使用KV对存储
  ---HexBins                         16进制工具
  
```
#### 参与贡献

1.  Fork 本仓库
2.  新建 Feat_xxx 分支
3.  提交代码
4.  新建 Pull Request


#### 特技

1.  使用 Readme\_XXX.md 来支持不同的语言，例如 Readme\_en.md, Readme\_zh.md
2.  Gitee 官方博客 [blog.gitee.com](https://blog.gitee.com)
3.  你可以 [https://gitee.com/explore](https://gitee.com/explore) 这个地址来了解 Gitee 上的优秀开源项目
4.  [GVP](https://gitee.com/gvp) 全称是 Gitee 最有价值开源项目，是综合评定出的优秀开源项目
5.  Gitee 官方提供的使用手册 [https://gitee.com/help](https://gitee.com/help)
6.  Gitee 封面人物是一档用来展示 Gitee 会员风采的栏目 [https://gitee.com/gitee-stars/](https://gitee.com/gitee-stars/)
