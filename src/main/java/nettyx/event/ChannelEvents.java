package nettyx.event;

import static io.netty.handler.timeout.IdleState.ALL_IDLE;
import static io.netty.handler.timeout.IdleState.READER_IDLE;
import static io.netty.handler.timeout.IdleState.WRITER_IDLE;

import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Channel Event tool
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2021/5/13 9:10
 */

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ChannelEvents {

    public static boolean isReadIdle(Object evt) {
        return isAssignedState(evt, READER_IDLE);
    }

    public static boolean isWriteIdle(Object evt) {
        return isAssignedState(evt, WRITER_IDLE);
    }

    public static boolean isAllIdle(Object evt) {
        return isAssignedState(evt, ALL_IDLE);
    }

    public static boolean isAssignedState(Object evt, IdleState state) {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            return event.state() == state;
        }
        return false;
    }

}
