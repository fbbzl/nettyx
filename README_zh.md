# Nettyx

<p align="center">
  <img src="https://img.shields.io/maven-central/v/io.github.fbbzl/nettyx?style=flat-square&label=Maven%20Central" alt="Maven Central">
  <img src="https://img.shields.io/github/license/fbbzl/nettyx?style=flat-square" alt="License">
  <img src="https://img.shields.io/badge/JDK-21+-orange?style=flat-square" alt="JDK">
  <img src="https://img.shields.io/badge/Netty-4.2.x-009688?style=flat-square" alt="Netty">
</p>

<p align="center">
  <b>🚀 A minimal development framework built on Netty</b><br>
  <sub>Lightweight · Efficient · Ready to use - TCP, serial ports and Bluetooth</sub>
</p>

---

## 🌟 Features

| | Feature | Description |
|---|------|------|
| ⚡ | **Lightweight** | A thin wrapper around Netty 4.2.x with no additional overhead |
| 🔌 | **Multiple protocols** | TCP · Serial (Rxtx/Jsc) · Bluetooth with a shared template API |
| 🧩 | **Struct serialization** | Declarative binary protocols defined through annotated POJOs |
| 🔧 | **Functional APIs** | Functional handlers, interceptors and heartbeats reduce boilerplate |
| 📡 | **Bluetooth support** | OIO Bluetooth servers and clients for embedded devices |

---

## 📦 Installation

```xml
<dependency>
    <groupId>io.github.fbbzl</groupId>
    <artifactId>nettyx</artifactId>
    <version>2.6.33</version>
</dependency>
```

```groovy
implementation 'io.github.fbbzl:nettyx:2.6.29'
```

---

## 🧭 Quick start

### TCP server - 3 lines of code

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

### Struct serialization - declarative protocol definitions

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

### Serial ports - RXTX

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

### Bluetooth server

```java
BtServerTemplate btServer = new BtServerTemplate("0000110100001000800000805f9b34fb", "MyBtServer") {
    @Override
    protected ChannelInitializer<BtAcceptedChannel> childChannelInitializer() {
        return ch -> ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
            @Override
            public void channelActive(ChannelHandlerContext ctx) {
                System.out.println("Bluetooth client connected: " + ctx.channel().remoteAddress());
            }
        });
    }
};
```

---

## 📚 Modules

```
action                               Functional interfaces and utilities
channel                              Channel extensions
  ├── bluetooth                      Bluetooth OIO (client/server)
  ├── enhanced                       Optimized OIO byte streams
  └── serial                         Rxtx / Jsc serial channels
codec                                Codecs
  ├── ByteArrayCodec                 Byte arrays ↔ ByteBuf
  ├── DelimiterBasedFrameCodec       Delimiter-based framing
  ├── EscapeCodec                    Escaping / sensitive-word replacement
  ├── StartEndFlagFrameCodec         Start/end marker framing
  ├── StringMessageCodec             String encoding and decoding
  └── StructCodec                    Struct encoding and decoding
event                                Netty event utilities
exception                            Custom runtime exceptions
handler                              Pipeline handlers
  ├── ChannelInterceptor             Read/write interceptors
  ├── ActionIdleStateHandler         Configurable idle-state handler
  ├── ActionReadTimeoutHandler       Configurable read-timeout handler
  ├── ActionWriteTimeoutHandler      Configurable write-timeout handler
  ├── ChannelAdvice                  Inbound/outbound advice
  ├── IdledHeartBeater               Automatic heartbeats
  └── MessageFilter                  Message filters
serializer                           Serialization
  └── struct                         Binary struct serialization (annotation-driven)
template                             Application templates
  ├── serial/jsc                     Jsc multi/single-channel clients
  ├── serial/rxtx                    Rxtx multi/single-channel clients
  ├── tcp/client                     TCP multi/single-channel clients and server detection
  ├── tcp/server                     TCP servers
  └── bluetooth/server               Bluetooth servers
util                                 Utilities
  ├── Bins                           Bit and binary-array utilities
  ├── BtFinder                       Bluetooth device discovery
  ├── CommPorts                      Serial-port utilities
  ├── EndianKit                      Byte-order conversion
  ├── HexKit                         Hexadecimal encoding and decoding
  └── ...
```

---

## 🧪 Build

```bash
mvn clean install -DskipTests
```

> ℹ️ Some tests require hardware (serial ports or Bluetooth adapters). Run them as needed.

---

## 🔗 Links

| | |
|---|------|
| 🌐 | [GitHub](https://github.com/fbbzl/nettyx) |
| 🇨🇳 | [Gitee](https://gitee.com/fbbzl/nettyx) |
| 📖 | [Examples](https://blog.csdn.net/fbbwht) |
| 🛠️ | [JetBrains IDEA](https://www.jetbrains.com) — License sponsor |

---

## 🙏 Acknowledgments

> Thanks to my family for giving me time to focus on this project, to JetBrains for providing an IntelliJ IDEA Ultimate license, and finally to myself.
>
> I hope this framework saves you even a minute of development time.
