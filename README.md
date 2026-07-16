# Nettyx

<p align="center">
  <img src="https://img.shields.io/maven-central/v/io.github.fbbzl/nettyx?style=flat-square&label=Maven%20Central" alt="Maven Central">
  <img src="https://img.shields.io/github/license/fbbzl/nettyx?style=flat-square" alt="License">
  <img src="https://img.shields.io/badge/JDK-21+-orange?style=flat-square" alt="JDK">
  <img src="https://img.shields.io/badge/Netty-4.2.x-009688?style=flat-square" alt="Netty">
</p>

<p align="center">
  <b>馃殌 Ultra-lightweight Netty framework</b><br>
  <sub>Lightweight, fast, and ready for production 鈥?for TCP, Serial, and Bluetooth</sub>
</p>

---

## 馃専 Highlights

| | Feature | Description |
|---|---------|-------------|
| 鈿?| **Lightweight** | Ultra-thin wrapper over Netty 4.2.x, zero extra overhead |
| 馃攲 | **Multi-Protocol** | TCP 路 Serial (Rxtx/Jsc) 路 Bluetooth 鈥?unified template API |
| 馃З | **Struct Serializer** | Declarative binary protocol 鈥?annotate your POJO, done |
| 馃敡 | **Function-first** | Functional handlers, interceptors, heartbeats 鈥?less boilerplate |
| 馃摗 | **Bluetooth Ready** | OIO-based Bluetooth server/client for embedded devices |

---

## 馃摝 Install

```xml
<dependency>
    <groupId>io.github.fbbzl</groupId>
    <artifactId>nettyx</artifactId>
    <version>2.6.27</version>
</dependency>
```

```groovy
implementation 'io.github.fbbzl:nettyx:2.6.25'
```

---

## 馃Л Quick Start

### TCP Server 鈥?3 lines

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

### Struct Serializer 鈥?Declare your protocol

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

### Serial Port 鈥?RXTX

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

### Bluetooth Server

```java
BtServerTemplate btServer = new BtServerTemplate("0000110100001000800000805f9b34fb", "MyBtServer") {
    @Override
    protected ChannelInitializer<BtAcceptedChannel> childChannelInitializer() {
        return ch -> ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
            @Override
            public void channelActive(ChannelHandlerContext ctx) {
                System.out.println("Bt client connected: " + ctx.channel().remoteAddress());
            }
        });
    }
};
```

---

## 馃摎 Modules

```
action                               Functional interfaces & utilities
channel                              Channel extensions
  鈹溾攢鈹€ bluetooth                      Bluetooth OIO (client/server)
  鈹溾攢鈹€ enhanced                       Optimized OIO byte stream
  鈹斺攢鈹€ serial                         Rxtx / Jsc serial channels
codec                                Codecs
  鈹溾攢鈹€ ByteArrayCodec                 Byte array 鈫?ByteBuf
  鈹溾攢鈹€ DelimiterBasedFrameCodec       Delimiter-based framing
  鈹溾攢鈹€ EscapeCodec                    Escape / sensitive-word replacement
  鈹溾攢鈹€ StartEndFlagFrameCodec         Start/end flag delimiter
  鈹溾攢鈹€ StringMessageCodec             String codec
  鈹斺攢鈹€ StructCodec                    Struct serializer codec
event                                Netty event utilities
exception                            Custom runtime exceptions
handler                              Pipeline handlers
  鈹溾攢鈹€ ChannelInterceptor             Pre-read/write interception
  鈹溾攢鈹€ ActionIdleStateHandler         Parameterized idle-state handler
  鈹溾攢鈹€ ActionReadTimeoutHandler       Parameterized read timeout
  鈹溾攢鈹€ ActionWriteTimeoutHandler      Parameterized write timeout
  鈹溾攢鈹€ ChannelAdvice                  Inbound / outbound advice
  鈹溾攢鈹€ IdledHeartBeater               Auto heartbeat on idle
  鈹斺攢鈹€ MessageFilter                  Message content filter
serializer                           Serialization
  鈹斺攢鈹€ struct                         Binary struct serializer (annotation-driven)
template                             Application templates
  鈹溾攢鈹€ serial/jsc                     Multi/single Jsc channel client
  鈹溾攢鈹€ serial/rxtx                    Multi/single Rxtx channel client
  鈹溾攢鈹€ tcp/client                     Multi/single TCP client + server detector
  鈹溾攢鈹€ tcp/server                     TCP server
  鈹斺攢鈹€ bluetooth/server               Bluetooth server
util                                 Utilities
  鈹溾攢鈹€ Bins                           Binary bit/array tools
  鈹溾攢鈹€ BtFinder                       Bluetooth device scanner
  鈹溾攢鈹€ CommPorts                      Serial port utility
  鈹溾攢鈹€ EndianKit                      Endian conversion
  鈹溾攢鈹€ HexKit                         Hex encode/decode
  鈹斺攢鈹€ ...
```

---

## 馃И Build

```bash
mvn clean install -DskipTests
```

> 鈩癸笍 Tests require hardware (serial ports, Bluetooth adapters). Run with caution.

---

## 馃敆 Links

| | |
|---|------|
| 馃寪 | [GitHub](https://github.com/fbbzl/nettyx) |
| 馃嚚馃嚦 | [Gitee](https://gitee.com/fbbzl/nettyx) |
| 馃摉 | [Blog / Examples](https://blog.csdn.net/fbbwht) |
| 馃洜锔?| [JetBrains IDEA](https://www.jetbrains.com) 鈥?Ultimate license sponsored |

---

## 馃檹 Gratitude

> First of all, I would like to thank my family for giving me enough time to focus on this project, then I would like to thank JetBrains for giving me the Ultimate Edition of the IDEA, and finally I would like to thank myself.
>
> Hopefully, this framework will save you even a minute of development time.
