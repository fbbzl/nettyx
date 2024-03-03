package org.fz.nettyx.event;

import cn.hutool.core.date.StopWatch;
import java.text.NumberFormat;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StopWatchExpand {

    /**
     * StopWatch实例
     */
    private static StopWatch STOP_WATCH;

    /**
     * StopWatch实例初始化
     */
    public static void init() {
        STOP_WATCH = new StopWatch("运行时间");
    }

    /**
     * 开启计时
     *
     * @param taskName 任务名称
     *
     * @return 提示字符串
     */
    public static String start(String taskName) {
        STOP_WATCH.start(taskName);
        return "[ 任务: " + taskName + " ]" + " 监测运行时间开始......";
    }

    /**
     * 结束计时
     */
    public static void stop() {
        STOP_WATCH.stop();
    }

    /**
     * 格式化的统计输出
     *
     * @return 统计输出
     */
    public static String prettyPrint() {
        // 获取运行的毫秒数与秒数
        long   totalTimeMillis  = STOP_WATCH.getTotalTimeMillis();
        double totalTimeSeconds = STOP_WATCH.getTotalTimeSeconds();
        // 编写总结
        String        shortSummary =
            "StopWatch '" + STOP_WATCH.getId() + "': running time [ " + String.format("%9s", totalTimeMillis) + "ms / "
            + String.format("%9.3f", totalTimeSeconds) + "s ]";
        StringBuilder sb           = new StringBuilder();
        sb.append("---------------------------------------------------------------\n");
        sb.append(shortSummary);
        sb.append('\n');
        sb.append("---------------------------------------------------------------\n");
        sb.append("       ms           s      %      Task name\n");
        sb.append("---------------------------------------------------------------\n");
        NumberFormat pf = NumberFormat.getPercentInstance();
        pf.setMinimumIntegerDigits(3);
        pf.setGroupingUsed(false);
        for (StopWatch.TaskInfo task : STOP_WATCH.getTaskInfo()) {
            sb.append(String.format("%9s", task.getTimeMillis())).append("   ");
            sb.append(String.format("%9.3f", task.getTimeSeconds())).append("   ");
            sb.append(pf.format((double) task.getTimeMillis() / totalTimeMillis)).append("      ");
            sb.append(task.getTaskName()).append("\n");
        }
        return sb.toString();
    }
}
