package org.fz.nettyx.util;

import static cn.hutool.core.collection.CollUtil.newArrayList;
import static cn.hutool.core.text.CharSequenceUtil.containsIgnoreCase;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.system.OsInfo;
import cn.hutool.system.SystemUtil;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;


/**
 * @author fengbinbin
 * @version 1.0
 * @since 3/31/2022 11:06 AM
 */
@Slf4j
@UtilityClass
public class CommPorts {

    static final OsInfo                 OS_INFO = SystemUtil.getOsInfo();
    static final Supplier<List<String>> GET_COM_PORT;

    static {
        if (OS_INFO.isLinux()) { GET_COM_PORT = CommPorts::getComPortsLinux; } else if (OS_INFO.isWindows()) {
            GET_COM_PORT = CommPorts::getComPortsWindows;
        }
        // default is linux
        else { GET_COM_PORT = CommPorts::getComPortsLinux; }
    }

    public static List<String> getLocalComPorts() {
        return getLocalComPorts(false);
    }

    public static List<String> getLocalComPorts(boolean usbOnly) {
        List<String> commPorts = GET_COM_PORT.get();
        // windows
        if (OS_INFO.isWindows()) return commPorts;

        // linux
        if (usbOnly) CollUtil.filter(commPorts, commPort -> containsIgnoreCase(commPort, "ttyUSB"));

        return commPorts;
    }

    @SneakyThrows(IOException.class)
    public static List<String> getComPortsWindows() {
        List<String>   ports   = new ArrayList<>();
        String         command = "reg query HKEY_LOCAL_MACHINE\\HARDWARE\\DEVICEMAP\\SERIALCOMM";
        Process        process = Runtime.getRuntime().exec(command);
        InputStream    in      = process.getInputStream();
        BufferedReader br      = new BufferedReader(new InputStreamReader(in));
        String         line;
        int            index   = 0;
        try {
            while ((line = br.readLine()) != null) {
                if (line.isEmpty()) {
                    continue;
                }
                if (index != 0) {
                    String[] strs    = line.replaceAll(" +", ",").split(",");
                    String   comPort = strs[strs.length - 1];
                    ports.add(comPort);
                }
                index++;
            }
        }
        catch (IOException ioException) {
            throw new UnsupportedOperationException("exception occur while reading windows regedit, command: " + command);
        }

        return ports;
    }

    public static List<String> getComPortsLinux() {
        try {
            String cmd        = "dmesg";
            String execResult = executeLinuxCmd(cmd);

            String[] infos = execResult.split("\\s+");

            Set<String> ttys = new HashSet<>();
            for (String info : infos) {
                if (info.contains("ttyS") || info.contains("ttyU")) {
                    String info2   = info.replace("[", "").replace(":", "").replace("]", "");
                    String ttyPath = "/dev/" + info2;

                    File file = new File(ttyPath);
                    if (file.exists()) {
                        ttys.add(ttyPath);
                    }
                }
            }
            return newArrayList(ttys);
        }
        catch (IOException exception) {
            throw new UnsupportedOperationException("can not find comm-ports please check");
        }
    }

    public static String executeLinuxCmd(String cmd) throws IOException {
        Runtime run = Runtime.getRuntime();
        Process process;
        process = run.exec(cmd);
        InputStream   in  = process.getInputStream();
        StringBuilder out = new StringBuilder();
        byte[]        b   = new byte[8192];
        for (int n; (n = in.read(b)) != -1; ) {
            out.append(new String(b, 0, n));
        }

        in.close();
        process.destroy();
        return out.toString();
    }

}
