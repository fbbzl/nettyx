package escape;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class AiKimiEscape {

    // 定义分隔符（多字节）
    private static final byte[] DELIMITER = new byte[]{0x01, 0x02};
    // 定义转义字符（多字节）
    private static final byte[] ESCAPE_CHAR = new byte[]{0x02, 0x03};

    // 检查当前索引位置是否匹配指定的字节序列
    private static boolean matches(ByteBuf buffer, int index, byte[] sequence) {
        if (buffer.readableBytes() < index + sequence.length) {
            return false;
        }
        for (int i = 0; i < sequence.length; i++) {
            if (buffer.getByte(index + i) != sequence[i]) {
                return false;
            }
        }
        return true;
    }

    // 转义方法
    public static ByteBuf escape(ByteBuf message) {
        ByteBuf output = Unpooled.buffer();
        int readerIndex = message.readerIndex();
        int writerIndex = message.writerIndex();

        for (int i = readerIndex; i < writerIndex; i++) {
            if (matches(message, i, ESCAPE_CHAR)) {
                // 转义转义字符本身
                output.writeBytes(ESCAPE_CHAR);
                output.writeBytes(ESCAPE_CHAR);
                i += ESCAPE_CHAR.length - 1; // 跳过已处理的字节
            } else if (matches(message, i, DELIMITER)) {
                // 转义分隔符
                output.writeBytes(ESCAPE_CHAR);
                output.writeBytes(DELIMITER);
                i += DELIMITER.length - 1; // 跳过已处理的字节
            } else {
                output.writeByte(message.getByte(i));
            }
        }

        return output;
    }

    // 反转义方法
    public static ByteBuf unescape(ByteBuf message) {
        ByteBuf output = Unpooled.buffer();
        int readerIndex = message.readerIndex();
        int writerIndex = message.writerIndex();

        boolean escapeNext = false;
        for (int i = readerIndex; i < writerIndex; i++) {
            if (escapeNext) {
                // 写入当前字节和下一个字节序列
                byte[] sequence = new byte[DELIMITER.length];
                message.getBytes(i, sequence);
                output.writeBytes(sequence);
                i += DELIMITER.length - 1; // 跳过已处理的字节
                escapeNext = false;
            } else if (matches(message, i, ESCAPE_CHAR)) {
                escapeNext = true;
                i += ESCAPE_CHAR.length - 1; // 跳过已处理的字节
            } else {
                output.writeByte(message.getByte(i));
            }
        }

        return output;
    }

    public static void main(String[] args) {
        // 生成 200 字节的测试消息
        byte[] testMessageBytes = new byte[200];
        for (int i = 0; i < testMessageBytes.length; i++) {
            testMessageBytes[i] = (byte) (i % 256); // 随机填充数据
        }

        // 插入一些分隔符和转义字符
        System.arraycopy(DELIMITER, 0, testMessageBytes, 50, DELIMITER.length);
        System.arraycopy(ESCAPE_CHAR, 0, testMessageBytes, 100, ESCAPE_CHAR.length);

        ByteBuf testMessage = Unpooled.copiedBuffer(testMessageBytes);

        // 测试 100 万次转义和反转义的耗时
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < 1_000_000; i++) {
            ByteBuf escapedMessage = escape(testMessage.duplicate());
            ByteBuf unescapedMessage = unescape(escapedMessage);
            unescapedMessage.release();
            escapedMessage.release();
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        System.out.println("Total time for 1,000,000 iterations: " + duration + " ms");


        // 释放 ByteBuf
        testMessage.release();
    }
}