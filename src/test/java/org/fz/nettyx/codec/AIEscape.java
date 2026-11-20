package org.fz.nettyx.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.util.HashMap;
import java.util.Map;

/**
 * Demonstrates replacing matched byte sequences using a configured escape map.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2025-05-23
 */
public class AIEscape {

    // Escape mappings
    private final Map<byte[], byte[]> escapeMappings;
    // Unescape mappings
    private final Map<byte[], byte[]> unescapeMappings;

    public AIEscape(Map<byte[], byte[]> escapeMappings) {
        this.escapeMappings = escapeMappings;
        this.unescapeMappings = new HashMap<>();
        for (Map.Entry<byte[], byte[]> entry : escapeMappings.entrySet()) {
            this.unescapeMappings.put(entry.getValue(), entry.getKey());
        }
    }

    // Escape a message
    public ByteBuf escape(ByteBuf message) {
        ByteBuf output = Unpooled.buffer(message.readableBytes() * 2); // Reserve initial capacity for escaping every byte
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
                        i += key.length - 1; // Skip bytes already matched
                        matched = true;
                        break;
                    }
                }
            }

            if (!matched) {
                output.writeByte(currentByte); // Preserve unmatched bytes
            }
        }

        return output;
    }

    public static void main(String[] args) {
        // Define escape mappings
        Map<byte[], byte[]> escapeMappings = new HashMap<>();
        escapeMappings.put(new byte[]{0x7E}, new byte[]{0x7E, 0x7E}); // Escape 0x7E as 0x7E 0x7E
        escapeMappings.put(new byte[]{0x7D, 0x5E}, new byte[]{0x7E, 0x7D, 0x5E}); // Escape 0x7D 0x5E as 0x7E 0x7D 0x5E

        // Create the escape utility
        AIEscape escapeTool = new AIEscape(escapeMappings);

        // Create the test message
        ByteBuf testMessage = Unpooled.copiedBuffer(new byte[]{0x01, 0x02, 0x7E, 0x04, 0x7D, 0x5E, 0x06, 0x07});

        // Start the benchmark
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < 10_000_000; i++) {
            ByteBuf escapedMessage = escapeTool.escape(testMessage.duplicate());
            escapedMessage.release();
        }

        long endTime = System.currentTimeMillis();

        System.out.println("Total time for 50,000,000 iterations: " + (endTime - startTime) + " ms");

        // Release resources
        testMessage.release();
    }
}
