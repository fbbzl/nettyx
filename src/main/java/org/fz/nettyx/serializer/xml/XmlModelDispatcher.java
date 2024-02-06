package org.fz.nettyx.serializer.xml;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.dom4j.Document;
import org.fz.nettyx.serializer.xml.element.Model;


/**
 * install this handler into you channel pipeline It will dispatch messages according to the XML configuration
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2024 /2/6 19:12
 */
@Slf4j
public abstract class XmlModelDispatcher extends MessageToMessageCodec<ByteBuf, String> {

    /**
     * Gets dispatch key.
     *
     * @param buf the buf
     * @return the dispatch key
     */
    public abstract String getDispatchKey(ByteBuf buf);

    /**
     * Gets message body.
     *
     * @param buf the buf
     * @return the message body
     */
    public ByteBuf getMessageBody(ByteBuf buf) {
        /* default is no nothing */
        return buf;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
        String dispatchKey = this.getDispatchKey(msg);

        Model model = XmlSerializerContext.findModel(dispatchKey);
        if (model == null) {
            return;
        }

        Document read = XmlSerializer.read(getMessageBody(msg), model);

        // TODO deal the document
        System.err.println(read);
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, String msg, List<Object> out) throws Exception {

    }
}
