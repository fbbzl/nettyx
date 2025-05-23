package escape;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.util.HashMap;
import java.util.Map;

public class AIEscape {

    // 转义映射表
    private final Map<byte[], byte[]> escapeMappings;
    // 反转义映射表
    private final Map<byte[], byte[]> unescapeMappings;

    public AIEscape(Map<byte[], byte[]> escapeMappings) {
        this.escapeMappings = escapeMappings;
        this.unescapeMappings = new HashMap<>();
        for (Map.Entry<byte[], byte[]> entry : escapeMappings.entrySet()) {
            this.unescapeMappings.put(entry.getValue(), entry.getKey());
        }
    }

    // 转义方法
    public ByteBuf escape(ByteBuf message) {
        ByteBuf output = Unpooled.buffer(message.readableBytes() * 2); // 最坏情况下，每个字节都可能被转义
        int readerIndex = message.readerIndex();
        int writerIndex = message.writerIndex();

        for (int i = readerIndex; i < writerIndex; i++) {
            byte currentByte = message.getByte(i);
            boolean matched = false;

            for (Map.Entry<byte[], byte[]> entry : escapeMappings.entrySet()) {
                byte[] key = entry.getKey();
                if (message.getByte(i) == key[0] && key.length == 1) {
                    output.writeBytes(entry.getValue());
                    matched = true;
                    break;
                } else if (message.getByte(i) == key[0] && i + key.length <= writerIndex) {
                    boolean fullMatch = true;
                    for (int j = 1; j < key.length; j++) {
                        if (message.getByte(i + j) != key[j]) {
                            fullMatch = false;
                            break;
                        }
                    }
                    if (fullMatch) {
                        output.writeBytes(entry.getValue());
                        i += key.length - 1; // 跳过已处理的字节
                        matched = true;
                        break;
                    }
                }
            }

            if (!matched) {
                output.writeByte(currentByte); // 写入原始字节
            }
        }

        return output;
    }

    // 反转义方法
    public ByteBuf unescape(ByteBuf message) {
        ByteBuf output = Unpooled.buffer(message.readableBytes());
        int readerIndex = message.readerIndex();
        int writerIndex = message.writerIndex();

        for (int i = readerIndex; i < writerIndex; i++) {
            byte currentByte = message.getByte(i);
            boolean matched = false;

            for (Map.Entry<byte[], byte[]> entry : unescapeMappings.entrySet()) {
                byte[] key = entry.getKey();
                if (message.getByte(i) == key[0] && key.length == 1) {
                    output.writeBytes(entry.getValue());
                    matched = true;
                    break;
                } else if (message.getByte(i) == key[0] && i + key.length <= writerIndex) {
                    boolean fullMatch = true;
                    for (int j = 1; j < key.length; j++) {
                        if (message.getByte(i + j) != key[j]) {
                            fullMatch = false;
                            break;
                        }
                    }
                    if (fullMatch) {
                        output.writeBytes(entry.getValue());
                        i += key.length - 1; // 跳过已处理的字节
                        matched = true;
                        break;
                    }
                }
            }

            if (!matched) {
                output.writeByte(currentByte); // 写入原始字节
            }
        }

        return output;
    }

    public static void main(String[] args) {
        // 定义转义映射
        Map<byte[], byte[]> escapeMappings = new HashMap<>();
        escapeMappings.put(new byte[]{0x7E}, new byte[]{0x7E, 0x7E}); // 将0x7E转义为0x7E 0x7E
        escapeMappings.put(new byte[]{0x7D, 0x5E}, new byte[]{0x7E, 0x7D, 0x5E}); // 将0x7D 0x5E转义为0x7E 0x7D 0x5E

        // 创建转义工具实例
        AIEscape escapeTool = new AIEscape(escapeMappings);

        // 生成测试消息
        ByteBuf testMessage = Unpooled.copiedBuffer(new byte[]{0x01, 0x02, 0x7E, 0x04, 0x7D, 0x5E, 0x06, 0x07});

        // 开始性能测试
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < 5_000_000; i++) {
            ByteBuf escapedMessage = escapeTool.escape(testMessage.duplicate());

            escapedMessage.release();
        }

        long endTime = System.currentTimeMillis();

        System.out.println("Total time for 50,000,000 iterations: " + (endTime - startTime) + " ms");

        // 释放资源
        testMessage.release();
    }
}