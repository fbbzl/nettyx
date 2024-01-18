package org.fz.nettyx.handler.interceptor;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * tool class used with ChannelInterceptor
 *
 * @see ChannelInterceptor
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ChannelInterceptors {

    /**
     * Gets interceptors.
     *
     * @param <T>     the type parameter
     * @param channel the channel
     * @return the interceptors
     */
    public static <T extends ChannelInterceptor> List<T> getInterceptors(Channel channel) {
        return getInterceptors(channel.pipeline());
    }

    /**
     * Gets interceptors.
     *
     * @param <T> the type parameter
     * @param ctx the ctx
     * @return the interceptors
     */
    public static <T extends ChannelInterceptor> List<T> getInterceptors(ChannelHandlerContext ctx) {
        return getInterceptors(ctx.pipeline());
    }

    /**
     * Gets interceptors.
     *
     * @param <T>      the type parameter
     * @param pipeline the pipeline
     * @return the interceptors
     */
    @SuppressWarnings("unchecked")
    public static <T extends ChannelInterceptor> List<T> getInterceptors(ChannelPipeline pipeline) {
        List<T> result = new ArrayList<>(10);

        for (Map.Entry<String, ChannelHandler> entry : pipeline) {
            if (ChannelInterceptor.class.isAssignableFrom(entry.getValue().getClass())) {
                result.add((T) entry.getValue());
            }
        }

        return result;
    }

    /**
     * reset all channel interceptors in the pipeline
     *
     * @param channel channel
     */
    public static void freeAll(Channel channel) {
        freeAll(channel.pipeline());
    }

    /**
     * reset all channel interceptors in the pipeline
     *
     * @param ctx ctx
     */
    public static void freeAll(ChannelHandlerContext ctx) {
        freeAll(ctx.pipeline());
    }

    /**
     * reset all channel interceptors in the pipeline
     *
     * @param pipeline pipeline
     */
    public static void freeAll(ChannelPipeline pipeline) {
        getInterceptors(pipeline).stream().filter(ChannelInterceptor::isNotFreed).forEach(ChannelInterceptor::free);
    }

    /**
     * reset all channel interceptors in the pipeline
     *
     * @param channel channel
     */
    public static void resetAll(Channel channel) {
        resetAll(channel.pipeline());
    }

    /**
     * reset all channel interceptors in the pipeline
     *
     * @param ctx ctx
     */
    public static void resetAll(ChannelHandlerContext ctx) {
        resetAll(ctx.pipeline());
    }

    /**
     * reset all channel interceptors in the pipeline
     *
     * @param pipeline pipeline
     */
    public static void resetAll(ChannelPipeline pipeline) {
        getInterceptors(pipeline).stream().filter(ChannelInterceptor::isFreed).forEach(ChannelInterceptor::reset);
    }
}
