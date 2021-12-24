package org.fz.nettyx.support;

import org.slf4j.Logger;
import org.slf4j.Marker;

/**
 * avoid log performance loss. OFF > FATAL > ERROR > WARN > INFO > DEBUG > TRACE > ALL
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2021/3/2 11:05
 */
public final class Logs {

    private Logs() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static void error(Logger logger, String msg) {
        if (logger.isErrorEnabled()) {
            logger.error(msg);
        }
    }

    public static void error(Logger logger, String replacementMsg, Object object) {
        if (logger.isErrorEnabled()) {
            logger.error(replacementMsg, object);
        }
    }

    public static void error(Logger logger, String replacementMsg, Object... objects) {
        if (logger.isErrorEnabled()) {
            logger.error(replacementMsg, objects);
        }
    }

    public static void error(Logger logger, String replacementMsg, Object object1, Object object2) {
        if (logger.isErrorEnabled()) {
            logger.error(replacementMsg, object1, object2);
        }
    }

    public static void error(Logger logger, String msg, Throwable throwable) {
        if (logger.isErrorEnabled()) {
            logger.error(msg, throwable);
        }
    }

    public static void error(Logger logger, Marker marker, String msg) {
        if (logger.isErrorEnabled()) {
            logger.error(marker, msg);
        }
    }

    public static void error(Logger logger, Marker marker, String msg, Throwable throwable) {
        if (logger.isErrorEnabled()) {
            logger.error(marker, msg, throwable);
        }
    }

    public static void error(Logger logger, Marker marker, String msg, Object object) {
        if (logger.isErrorEnabled()) {
            logger.error(marker, msg, object);
        }
    }

    public static void error(Logger logger, Marker marker, String msg, Object... objects) {
        if (logger.isErrorEnabled()) {
            logger.error(marker, msg, objects);
        }
    }

    public static void error(Logger logger, Marker marker, String msg, Object object1, Object object2) {
        if (logger.isErrorEnabled()) {
            logger.error(marker, msg, object1, object2);
        }
    }

    public static void warn(Logger logger, String msg) {
        if (logger.isWarnEnabled()) {
            logger.warn(msg);
        }
    }

    public static void warn(Logger logger, String replacementMsg, Object object) {
        if (logger.isWarnEnabled()) {
            logger.warn(replacementMsg, object);
        }
    }

    public static void warn(Logger logger, String replacementMsg, Object... objects) {
        if (logger.isWarnEnabled()) {
            logger.warn(replacementMsg, objects);
        }
    }

    public static void warn(Logger logger, String replacementMsg, Object object1, Object object2) {
        if (logger.isWarnEnabled()) {
            logger.warn(replacementMsg, object1, object2);
        }
    }

    public static void warn(Logger logger, String msg, Throwable throwable) {
        if (logger.isWarnEnabled()) {
            logger.warn(msg, throwable);
        }
    }

    public static void warn(Logger logger, Marker marker, String msg) {
        if (logger.isWarnEnabled()) {
            logger.warn(marker, msg);
        }
    }

    public static void warn(Logger logger, Marker marker, String msg, Throwable throwable) {
        if (logger.isWarnEnabled()) {
            logger.warn(marker, msg, throwable);
        }
    }

    public static void warn(Logger logger, Marker marker, String msg, Object object) {
        if (logger.isWarnEnabled()) {
            logger.warn(marker, msg, object);
        }
    }

    public static void warn(Logger logger, Marker marker, String msg, Object... objects) {
        if (logger.isWarnEnabled()) {
            logger.warn(marker, msg, objects);
        }
    }

    public static void warn(Logger logger, Marker marker, String msg, Object object1, Object object2) {
        if (logger.isWarnEnabled()) {
            logger.warn(marker, msg, object1, object2);
        }
    }

    public static void info(Logger logger, String msg) {
        if (logger.isInfoEnabled()) {
            logger.info(msg);
        }
    }

    public static void info(Logger logger, String replacementMsg, Object object) {
        if (logger.isInfoEnabled()) {
            logger.info(replacementMsg, object);
        }
    }

    public static void info(Logger logger, String replacementMsg, Object... objects) {
        if (logger.isInfoEnabled()) {
            logger.info(replacementMsg, objects);
        }
    }

    public static void info(Logger logger, String replacementMsg, Object object1, Object object2) {
        if (logger.isInfoEnabled()) {
            logger.info(replacementMsg, object1, object2);
        }
    }

    public static void info(Logger logger, String msg, Throwable throwable) {
        if (logger.isInfoEnabled()) {
            logger.info(msg, throwable);
        }
    }

    public static void info(Logger logger, Marker marker, String msg) {
        if (logger.isInfoEnabled()) {
            logger.info(marker, msg);
        }
    }

    public static void info(Logger logger, Marker marker, String msg, Throwable throwable) {
        if (logger.isInfoEnabled()) {
            logger.info(marker, msg, throwable);
        }
    }

    public static void info(Logger logger, Marker marker, String msg, Object object) {
        if (logger.isInfoEnabled()) {
            logger.info(marker, msg, object);
        }
    }

    public static void info(Logger logger, Marker marker, String msg, Object... objects) {
        if (logger.isInfoEnabled()) {
            logger.info(marker, msg, objects);
        }
    }

    public static void info(Logger logger, Marker marker, String msg, Object object1, Object object2) {
        if (logger.isInfoEnabled()) {
            logger.info(marker, msg, object1, object2);
        }
    }

    public static void debug(Logger logger, String msg) {
        if (logger.isDebugEnabled()) {
            logger.debug(msg);
        }
    }

    public static void debug(Logger logger, String replacementMsg, Object object) {
        if (logger.isDebugEnabled()) {
            logger.debug(replacementMsg, object);
        }
    }

    public static void debug(Logger logger, String replacementMsg, Object... objects) {
        if (logger.isDebugEnabled()) {
            logger.debug(replacementMsg, objects);
        }
    }

    public static void debug(Logger logger, String replacementMsg, Object object1, Object object2) {
        if (logger.isDebugEnabled()) {
            logger.debug(replacementMsg, object1, object2);
        }
    }

    public static void debug(Logger logger, String msg, Throwable throwable) {
        if (logger.isDebugEnabled()) {
            logger.debug(msg, throwable);
        }
    }

    public static void debug(Logger logger, Marker marker, String msg) {
        if (logger.isDebugEnabled()) {
            logger.debug(marker, msg);
        }
    }

    public static void debug(Logger logger, Marker marker, String msg, Throwable throwable) {
        if (logger.isDebugEnabled()) {
            logger.debug(marker, msg, throwable);
        }
    }

    public static void debug(Logger logger, Marker marker, String msg, Object object) {
        if (logger.isDebugEnabled()) {
            logger.debug(marker, msg, object);
        }
    }

    public static void debug(Logger logger, Marker marker, String msg, Object... objects) {
        if (logger.isDebugEnabled()) {
            logger.debug(marker, msg, objects);
        }
    }

    public static void debug(Logger logger, Marker marker, String msg, Object object1, Object object2) {
        if (logger.isDebugEnabled()) {
            logger.debug(marker, msg, object1, object2);
        }
    }

    public static void trace(Logger logger, String msg) {
        if (logger.isTraceEnabled()) {
            logger.trace(msg);
        }
    }

    public static void trace(Logger logger, String replacementMsg, Object object) {
        if (logger.isTraceEnabled()) {
            logger.trace(replacementMsg, object);
        }
    }

    public static void trace(Logger logger, String replacementMsg, Object... objects) {
        if (logger.isTraceEnabled()) {
            logger.trace(replacementMsg, objects);
        }
    }

    public static void trace(Logger logger, String replacementMsg, Object object1, Object object2) {
        if (logger.isTraceEnabled()) {
            logger.trace(replacementMsg, object1, object2);
        }
    }

    public static void trace(Logger logger, String msg, Throwable throwable) {
        if (logger.isTraceEnabled()) {
            logger.trace(msg, throwable);
        }
    }

    public static void trace(Logger logger, Marker marker, String msg) {
        if (logger.isTraceEnabled()) {
            logger.trace(marker, msg);
        }
    }

    public static void trace(Logger logger, Marker marker, String msg, Throwable throwable) {
        if (logger.isTraceEnabled()) {
            logger.trace(marker, msg, throwable);
        }
    }

    public static void trace(Logger logger, Marker marker, String msg, Object object) {
        if (logger.isTraceEnabled()) {
            logger.trace(marker, msg, object);
        }
    }

    public static void trace(Logger logger, Marker marker, String msg, Object... objects) {
        if (logger.isTraceEnabled()) {
            logger.trace(marker, msg, objects);
        }
    }

    public static void trace(Logger logger, Marker marker, String msg, Object object1, Object object2) {
        if (logger.isTraceEnabled()) {
            logger.trace(marker, msg, object1, object2);
        }
    }

}
