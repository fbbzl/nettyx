# Nettyx

<p align="center">
  <img src="https://img.shields.io/maven-central/v/io.github.fbbzl/nettyx?style=flat-square&label=Maven%20Central" alt="Maven Central">
  <img src="https://img.shields.io/github/license/fbbzl/nettyx?style=flat-square" alt="License">
  <img src="https://img.shields.io/badge/JDK-21+-orange?style=flat-square" alt="JDK">
  <img src="https://img.shields.io/badge/Netty-4.2.x-009688?style=flat-square" alt="Netty">
</p>

<p align="center">
  <b>🚀 基于 Netty 的极简开发框架</b><br>
  <sub>Lightweight, fast, and ready for production — for TCP, Serial, and Bluetooth</sub>
</p>

---

## 🌟 Highlights

| | Feature | Description |
|---|---------|-------------|
| ⚡ | **Lightweight** | Ultra-thin wrapper over Netty 4.2.x, zero extra overhead |
| 🔌 | **Multi-Protocol** | TCP · Serial (Rxtx/Jsc) · Bluetooth — unified template API |
| 🧩 | **Struct Serializer** | Declarative binary protocol — annotate your POJO, done |
| 🔧 | **Function-first** | Functional handlers, interceptors, heartbeats — less boilerplate |
| 📡 | **Bluetooth Ready** | OIO-based Bluetooth server/client for embedded devices |

---

## 📦 Install

```xml
<dependency>
    <groupId>io.github.fbbzl</groupId>
    <artifactId>nettyx</artifactId>
    <version>2.6.12</version>
</dependency>
```

```groovy
implementation 'io.github.fbbzl:nettyx:2.6.12'
```

---

## 🧭 Quick Start

### TCP Server — 3 lines

```java
new TcpServer(8080, ctx -> {
    ctx.writeAndFlush(Unpooled.wrappedBuffer("Hello\n".getBytes()));
    return ctx.executor().newPromise();
}).start();
```

### Struct Serializer — Declare your protocol

```java
@Struct
public class Login {
    @ToString     String  username;
    @ToArray(10)  byte[]  password;
    @Chunk        byte[]  reserved;
}
```

```java
Login login = StructSerializer.toStruct(buf, Login.class);
byte[] bytes = StructSerializer.toBytes(login);
```

### Bluetooth Server

```java
new BtServerTemplate("localhost", 1, channel -> {
    System.out.println("Bt client connected: " + channel.remoteAddress());
});
```

---

## 📚 Modules

```
action                               Functional interfaces & utilities
channel                              Channel extensions
  ├── bluetooth                      Bluetooth OIO (client/server)
  ├── enhanced                       Optimized OIO byte stream
  └── serial                         Rxtx / Jsc serial channels
codec                                Codecs
  ├── ByteArrayCodec                 Byte array ↔ ByteBuf
  ├── DelimiterBasedFrameCodec       Delimiter-based framing
  ├── EscapeCodec                    Escape / sensitive-word replacement
  ├── StartEndFlagFrameCodec         Start/end flag delimiter
  ├── StringMessageCodec             String codec
  └── StructCodec                    Struct serializer codec
event                                Netty event utilities
exception                            Custom runtime exceptions
handler                              Pipeline handlers
  ├── ChannelInterceptor             Pre-read/write interception
  ├── ActionIdleStateHandler         Parameterized idle-state handler
  ├── ActionReadTimeoutHandler       Parameterized read timeout
  ├── ActionWriteTimeoutHandler      Parameterized write timeout
  ├── ChannelAdvice                  Inbound / outbound advice
  ├── IdledHeartBeater               Auto heartbeat on idle
  └── MessageFilter                  Message content filter
serializer                           Serialization
  └── struct                         Binary struct serializer (annotation-driven)
template                             Application templates
  ├── serial/jsc                     Multi/single Jsc channel client
  ├── serial/rxtx                    Multi/single Rxtx channel client
  ├── tcp/client                     Multi/single TCP client + server detector
  ├── tcp/server                     TCP server
  └── bluetooth/server               Bluetooth server
util                                 Utilities
  ├── Bins                           Binary bit/array tools
  ├── BtFinder                       Bluetooth device scanner
  ├── CommPorts                      Serial port utility
  ├── EndianKit                      Endian conversion
  ├── HexKit                         Hex encode/decode
  └── ...
```

---

## 🧪 Build

```bash
mvn clean install -DskipTests
```

> ℹ️ Tests require hardware (serial ports, Bluetooth adapters). Run with caution.

---

## 🔗 Links

| | |
|---|------|
| 🌐 | [GitHub](https://github.com/fbbzl/nettyx) |
| 🇨🇳 | [Gitee](https://gitee.com/fbbzl/nettyx) |
| 📖 | [Blog / Examples](https://blog.csdn.net/fbbwht) |
| 🛠️ | [JetBrains IDEA](https://www.jetbrains.com) — Ultimate license sponsored |

---

## 🙏 Gratitude

> First of all, I would like to thank my family for giving me enough time to focus on this project, then I would like to thank JetBrains for giving me the Ultimate Edition of the IDEA, and finally I would like to thank myself.
>
> Hopefully, this framework will save you even a minute of development time.
