package org.fz.nettyx.event;

import static io.netty.handler.timeout.IdleState.ALL_IDLE;
import static io.netty.handler.timeout.IdleState.READER_IDLE;
import static io.netty.handler.timeout.IdleState.WRITER_IDLE;

import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.experimental.UtilityClass;

/**
 * Channel Event tool
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2021 /5/13 9:10
 */
@UtilityClass
public class ChannelEvents {

    /**
     * Is read idle boolean.
     *
     * @param evt the evt
     * @return the boolean
     */
    public static boolean isReadIdle(Object evt) {
        return isAssignedState(evt, READER_IDLE);
    }

    /**
     * Is write idle boolean.
     *
     * @param evt the evt
     * @return the boolean
     */
    public static boolean isWriteIdle(Object evt) {
        return isAssignedState(evt, WRITER_IDLE);
    }

    /**
     * Is all idle boolean.
     *
     * @param evt the evt
     * @return the boolean
     */
    public static boolean isAllIdle(Object evt) {
        return isAssignedState(evt, ALL_IDLE);
    }

    /**
     * Is read idle boolean.
     *
     * @param evt the evt
     * @return the boolean
     */
    public static boolean isReadIdle(IdleStateEvent evt) {
        return isAssignedState(evt, READER_IDLE);
    }

    /**
     * Is write idle boolean.
     *
     * @param evt the evt
     * @return the boolean
     */
    public static boolean isWriteIdle(IdleStateEvent evt) {
        return isAssignedState(evt, WRITER_IDLE);
    }

    /**
     * Is all idle boolean.
     *
     * @param evt the evt
     * @return the boolean
     */
    public static boolean isAllIdle(IdleStateEvent evt) {
        return isAssignedState(evt, ALL_IDLE);
    }

    /**
     * Is read idle boolean.
     *
     * @param state the state
     * @return the boolean
     */
    public static boolean isReadIdle(IdleState state) {
        return state == READER_IDLE;
    }

    /**
     * Is write idle boolean.
     *
     * @param state the state
     * @return the boolean
     */
    public static boolean isWriteIdle(IdleState state) {
        return state == WRITER_IDLE;
    }

    /**
     * Is all idle boolean.
     *
     * @param state the state
     * @return the boolean
     */
    public static boolean isAllIdle(IdleState state) {
        return state == ALL_IDLE;
    }

    /**
     * Is assigned state boolean.
     *
     * @param obj   the obj
     * @param state the state
     * @return the boolean
     */
    public static boolean isAssignedState(Object obj, IdleState state) {
        if (obj instanceof IdleStateEvent) return ((IdleStateEvent) obj).state() == state;
        else
        if (obj instanceof IdleState)      return obj == state;
        else                               return false;
    }

}
