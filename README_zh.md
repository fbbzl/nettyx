# Nettyx

<p align="center">
  <img src="https://img.shields.io/maven-central/v/io.github.fbbzl/nettyx?style=flat-square&label=Maven%20Central" alt="Maven Central">
  <img src="https://img.shields.io/github/license/fbbzl/nettyx?style=flat-square" alt="License">
  <img src="https://img.shields.io/badge/JDK-21+-orange?style=flat-square" alt="JDK">
  <img src="https://img.shields.io/badge/Netty-4.2.x-009688?style=flat-square" alt="Netty">
</p>

<p align="center">
  <b>馃殌 鍩轰簬 Netty 鐨勬瀬绠€寮€鍙戞鏋?/b><br>
  <sub>杞婚噺 路 楂樻晥 路 寮€绠卞嵆鐢?鈥?鏀寔 TCP銆佷覆鍙ｃ€佽摑鐗?/sub>
</p>

---

## 馃専 鐗规€?

| | 鐗规€?| 璇存槑 |
|---|------|------|
| 鈿?| **瓒呰交閲?* | Netty 4.2.x 瓒呰杽灏佽锛岄浂棰濆寮€閿€ |
| 馃攲 | **澶氬崗璁?* | TCP 路 涓插彛(Rxtx/Jsc) 路 钃濈墮 鈥?缁熶竴妯℃澘 API |
| 馃З | **缁撴瀯浣撳簭鍒楀寲** | 澹版槑寮忎簩杩涘埗鍗忚 鈥?娉ㄨВ鏍囨敞 POJO锛屼竴姝ュ埌浣?|
| 馃敡 | **鍑芥暟寮忎紭鍏?* | 鍑芥暟寮忓鐞嗗櫒銆佹嫤鎴櫒銆佸績璺?鈥?鍛婂埆鏍锋澘浠ｇ爜 |
| 馃摗 | **钃濈墮灏辩华** | 鍩轰簬 OIO 鐨勮摑鐗欐湇鍔＄/瀹㈡埛绔紝宓屽叆寮忚澶囧弸濂?|

---

## 馃摝 瀹夎

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

## 馃Л 蹇€熷紑濮?

### TCP 鏈嶅姟绔?鈥?3 琛屼唬鐮?

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

### 缁撴瀯浣撳簭鍒楀寲 鈥?澹版槑寮忓崗璁畾涔?

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

### 涓插彛 鈥?RXTX

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

### 钃濈墮鏈嶅姟绔?

```java
BtServerTemplate btServer = new BtServerTemplate("0000110100001000800000805f9b34fb", "MyBtServer") {
    @Override
    protected ChannelInitializer<BtAcceptedChannel> childChannelInitializer() {
        return ch -> ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
            @Override
            public void channelActive(ChannelHandlerContext ctx) {
                System.out.println("钃濈墮瀹㈡埛绔凡杩炴帴: " + ctx.channel().remoteAddress());
            }
        });
    }
};
```

---

## 馃摎 妯″潡璇存槑

```
action                               鍔熻兘鎺ュ彛 & 宸ュ叿绫?
channel                              Channel 鎵╁睍
  鈹溾攢鈹€ bluetooth                      钃濈墮 OIO锛堝鎴风/鏈嶅姟绔級
  鈹溾攢鈹€ enhanced                       浼樺寲鐨?OIO 瀛楄妭娴?
  鈹斺攢鈹€ serial                         Rxtx / Jsc 涓插彛閫氶亾
codec                                缂栬В鐮佸櫒
  鈹溾攢鈹€ ByteArrayCodec                 瀛楄妭鏁扮粍 鈫?ByteBuf
  鈹溾攢鈹€ DelimiterBasedFrameCodec       鍩轰簬鍒嗛殧绗︾殑缂栬В鐮?
  鈹溾攢鈹€ EscapeCodec                    杞箟 / 鏁忔劅璇嶆浛鎹?
  鈹溾攢鈹€ StartEndFlagFrameCodec         璧峰/缁撴潫鏍囧織缂栬В鐮?
  鈹溾攢鈹€ StringMessageCodec             瀛楃涓茬紪瑙ｇ爜
  鈹斺攢鈹€ StructCodec                    缁撴瀯浣撶紪瑙ｇ爜
event                                Netty 浜嬩欢宸ュ叿
exception                            鑷畾涔夎繍琛屾椂寮傚父
handler                              绠￠亾澶勭悊鍣?
  鈹溾攢鈹€ ChannelInterceptor             璇诲啓鎷︽埅鍣?
  鈹溾攢鈹€ ActionIdleStateHandler         鍙弬鏁板寲绌洪棽鐘舵€佸鐞嗗櫒
  鈹溾攢鈹€ ActionReadTimeoutHandler       鍙弬鏁板寲璇昏秴鏃跺鐞嗗櫒
  鈹溾攢鈹€ ActionWriteTimeoutHandler      鍙弬鏁板寲鍐欒秴鏃跺鐞嗗櫒
  鈹溾攢鈹€ ChannelAdvice                  鍏ョ珯/鍑虹珯閫氱煡
  鈹溾攢鈹€ IdledHeartBeater               鑷姩蹇冭烦
  鈹斺攢鈹€ MessageFilter                  娑堟伅杩囨护鍣?
serializer                           搴忓垪鍖?
  鈹斺攢鈹€ struct                         浜岃繘鍒剁粨鏋勪綋搴忓垪鍖栵紙娉ㄨВ椹卞姩锛?
template                             搴旂敤妯℃澘
  鈹溾攢鈹€ serial/jsc                     Jsc 澶?鍗曢€氶亾瀹㈡埛绔?
  鈹溾攢鈹€ serial/rxtx                    Rxtx 澶?鍗曢€氶亾瀹㈡埛绔?
  鈹溾攢鈹€ tcp/client                     TCP 澶?鍗曢€氶亾瀹㈡埛绔?+ 鏈嶅姟鎺㈡祴
  鈹溾攢鈹€ tcp/server                     TCP 鏈嶅姟绔?
  鈹斺攢鈹€ bluetooth/server               钃濈墮鏈嶅姟绔?
util                                 宸ュ叿绫?
  鈹溾攢鈹€ Bins                           浜岃繘鍒朵綅/鏁扮粍宸ュ叿
  鈹溾攢鈹€ BtFinder                       钃濈墮璁惧鎵弿
  鈹溾攢鈹€ CommPorts                      涓插彛宸ュ叿
  鈹溾攢鈹€ EndianKit                      澶у皬绔浆鎹?
  鈹溾攢鈹€ HexKit                         鍗佸叚杩涘埗缂栬В鐮?
  鈹斺攢鈹€ ...
```

---

## 馃И 鏋勫缓

```bash
mvn clean install -DskipTests
```

> 鈩癸笍 閮ㄥ垎娴嬭瘯渚濊禆纭欢锛堜覆鍙ｃ€佽摑鐗欓€傞厤鍣級锛岃鎸夐渶杩愯銆?

---

## 馃敆 閾炬帴

| | |
|---|------|
| 馃寪 | [GitHub](https://github.com/fbbzl/nettyx) |
| 馃嚚馃嚦 | [Gitee](https://gitee.com/fbbzl/nettyx) |
| 馃摉 | [浣跨敤妗堜緥](https://blog.csdn.net/fbbwht) |
| 馃洜锔?| [JetBrains IDEA](https://www.jetbrains.com) 鈥?鎺堟潈璧炲姪 |

---

## 馃檹 楦ｈ阿

> 棣栧厛璋㈣阿瀹朵汉锛岀粰浜嗘垜鍏呰冻鐨勬椂闂翠笓娉ㄥ湪姝ら」鐩笂锛涚劧鍚庢劅璋?JetBrains 璧犻€佺殑 Ultimate Edition 鐗?IDEA锛涙渶鍚庤阿璋㈣嚜宸便€?
>
> 甯屾湜姝ゆ鏋惰兘澶熶负澶у鑺傜渷鍝€曚竴鍒嗛挓鐨勫紑鍙戞椂闂淬€?
