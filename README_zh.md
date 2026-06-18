# Nettyx

<p align="center">
  <img src="https://img.shields.io/maven-central/v/io.github.fbbzl/nettyx?style=flat-square&label=Maven%20Central" alt="Maven Central">
  <img src="https://img.shields.io/github/license/fbbzl/nettyx?style=flat-square" alt="License">
  <img src="https://img.shields.io/badge/JDK-21+-orange?style=flat-square" alt="JDK">
  <img src="https://img.shields.io/badge/Netty-4.2.x-009688?style=flat-square" alt="Netty">
</p>

<p align="center">
  <b>🚀 基于 Netty 的极简开发框架</b><br>
  <sub>轻量 · 高效 · 开箱即用 — 支持 TCP、串口、蓝牙</sub>
</p>

---

## 🌟 特性

| | 特性 | 说明 |
|---|------|------|
| ⚡ | **超轻量** | Netty 4.2.x 超薄封装，零额外开销 |
| 🔌 | **多协议** | TCP · 串口(Rxtx/Jsc) · 蓝牙 — 统一模板 API |
| 🧩 | **结构体序列化** | 声明式二进制协议 — 注解标注 POJO，一步到位 |
| 🔧 | **函数式优先** | 函数式处理器、拦截器、心跳 — 告别样板代码 |
| 📡 | **蓝牙就绪** | 基于 OIO 的蓝牙服务端/客户端，嵌入式设备友好 |

---

## 📦 安装

```xml
<dependency>
    <groupId>io.github.fbbzl</groupId>
    <artifactId>nettyx</artifactId>
    <version>2.6.25</version>
</dependency>
```

```groovy
implementation 'io.github.fbbzl:nettyx:2.6.25'
```

---

## 🧭 快速开始

### TCP 服务端 — 3 行代码

```java
ServerTemplate server = new ServerTemplate(8080) {
    @Override
    protected ChannelInitializer<NioSocketChannel> childChannelInitializer() {
        return ch -> ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
            @Override
            public void channelRead(ChannelHandlerContext ctx, Object msg) {
                ctx.writeAndFlush(Unpooled.wrappedBuffer("Hello\n".getBytes()));
            }
        });
    }
};
server.bind();
```

### 结构体序列化 — 声明式协议定义

```java
@Struct
public class Login {
    @ToCharSequence(bufferLength = 32) String  username;
    @ToArray(10)                       byte[]  password;
    @Chunk(length = 8)                 byte[]  reserved;
}
```

```java
Login login = StructSerializer.toStruct(buf, Login.class);
byte[] bytes = StructSerializer.toBytes(login);
```

### 串口 — RXTX

```java
SingleRxtxChannelTemplate serial = new SingleRxtxChannelTemplate("COM1") {
    @Override
    protected ChannelInitializer<RxtxChannel> channelInitializer() {
        return ch -> ch.pipeline().addLast(new StringCodec());
    }
};
serial.connect();
serial.writeAndFlush("Hello");
```

### 蓝牙服务端

```java
BtServerTemplate btServer = new BtServerTemplate("0000110100001000800000805f9b34fb", "MyBtServer") {
    @Override
    protected ChannelInitializer<BtAcceptedChannel> childChannelInitializer() {
        return ch -> ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
            @Override
            public void channelActive(ChannelHandlerContext ctx) {
                System.out.println("蓝牙客户端已连接: " + ctx.channel().remoteAddress());
            }
        });
    }
};
```

---

## 📚 模块说明

```
action                               功能接口 & 工具类
channel                              Channel 扩展
  ├── bluetooth                      蓝牙 OIO（客户端/服务端）
  ├── enhanced                       优化的 OIO 字节流
  └── serial                         Rxtx / Jsc 串口通道
codec                                编解码器
  ├── ByteArrayCodec                 字节数组 ↔ ByteBuf
  ├── DelimiterBasedFrameCodec       基于分隔符的编解码
  ├── EscapeCodec                    转义 / 敏感词替换
  ├── StartEndFlagFrameCodec         起始/结束标志编解码
  ├── StringMessageCodec             字符串编解码
  └── StructCodec                    结构体编解码
event                                Netty 事件工具
exception                            自定义运行时异常
handler                              管道处理器
  ├── ChannelInterceptor             读写拦截器
  ├── ActionIdleStateHandler         可参数化空闲状态处理器
  ├── ActionReadTimeoutHandler       可参数化读超时处理器
  ├── ActionWriteTimeoutHandler      可参数化写超时处理器
  ├── ChannelAdvice                  入站/出站通知
  ├── IdledHeartBeater               自动心跳
  └── MessageFilter                  消息过滤器
serializer                           序列化
  └── struct                         二进制结构体序列化（注解驱动）
template                             应用模板
  ├── serial/jsc                     Jsc 多/单通道客户端
  ├── serial/rxtx                    Rxtx 多/单通道客户端
  ├── tcp/client                     TCP 多/单通道客户端 + 服务探测
  ├── tcp/server                     TCP 服务端
  └── bluetooth/server               蓝牙服务端
util                                 工具类
  ├── Bins                           二进制位/数组工具
  ├── BtFinder                       蓝牙设备扫描
  ├── CommPorts                      串口工具
  ├── EndianKit                      大小端转换
  ├── HexKit                         十六进制编解码
  └── ...
```

---

## 🧪 构建

```bash
mvn clean install -DskipTests
```

> ℹ️ 部分测试依赖硬件（串口、蓝牙适配器），请按需运行。

---

## 🔗 链接

| | |
|---|------|
| 🌐 | [GitHub](https://github.com/fbbzl/nettyx) |
| 🇨🇳 | [Gitee](https://gitee.com/fbbzl/nettyx) |
| 📖 | [使用案例](https://blog.csdn.net/fbbwht) |
| 🛠️ | [JetBrains IDEA](https://www.jetbrains.com) — 授权赞助 |

---

## 🙏 鸣谢

> 首先谢谢家人，给了我充足的时间专注在此项目上；然后感谢 JetBrains 赠送的 Ultimate Edition 版 IDEA；最后谢谢自己。
>
> 希望此框架能够为大家节省哪怕一分钟的开发时间。
