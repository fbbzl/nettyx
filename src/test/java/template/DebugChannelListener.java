package template;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.lang.Console;
import org.fz.nettyx.listener.ActionChannelFutureListener;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/5/30 20:36
 */

public class DebugChannelListener extends ActionChannelFutureListener {

    public DebugChannelListener() {
        this.whenSuccess((l, cf) -> Console.log(cf));
        this.whenCancel((l, cf) -> Console.log(cf));
        this.whenFailure((l, cf) -> {
            System.err.println(cf.cause());
            ExceptionUtil.getThrowableList(cf.cause()).forEach(System.err::println);
        });

        this.whenDone((l, cf) -> Console.log(cf));
    }
}
