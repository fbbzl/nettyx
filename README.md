# nettyx

#### 介绍
基于netty4.x进行了超轻量级的封装, 提供了一些工具和基础模板

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
  <version>1.1.6</version>
</dependency>
```
## api
```
client                              提供一些抽象客户端基础实现
  ---Client                            顶级Client抽象
  ---MultiChannelClient                多Channel的Client 
  ---SingleChannelClient               单Channel的Client
codec                               提供了一些基础的编解码器
  ---DelimiterBasedFrameCodec          基于分隔符codec
  ---StartEndFlagFrameCodec            基于起止符codec
  ---StringMessageCodec                字符串Codec
envet
  ---ChannelEvent                      Channel事件对象, 建议配合Spring容器事件使用
  ---ChannelEvents                     Channel事件对象工具
function                            包含了充足的函数式接口, 为函数式编程提供支持                         
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
  ---ChannelAdvice                     提供了channel事件谏言  
  ---ChannelInterceptor                channel拦截器, 适用于协议握手等场景
server
  ---Server                         暂未提供server基础实现, 后续更新
support                             基础工具类
  ---ChannelStorage                 存储channel, 内部使用KV存储
  ---ConnectListener                轻度封装后的连接监听器
  ---Logs                           日志工具
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
