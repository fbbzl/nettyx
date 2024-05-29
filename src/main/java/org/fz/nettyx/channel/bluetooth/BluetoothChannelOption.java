package org.fz.nettyx.channel.bluetooth;

import com.intel.bluetooth.BlueCoveConfigProperties;
import io.netty.channel.ChannelOption;

/**
 * bluetooth channel option
 * @author fengbinbin
 * @version 1.0
 * @since 2024/5/15 15:18
 */
public class BluetoothChannelOption extends ChannelOption<String> {

    public static final ChannelOption<String> PROPERTY_DEBUG = option(BlueCoveConfigProperties.PROPERTY_DEBUG);

    /**
     * BlueCove log when enabled is printed to System.out. You can disable this
     * feature. Initialization property.
     */
    public static final ChannelOption<String> PROPERTY_DEBUG_STDOUT = option(BlueCoveConfigProperties.PROPERTY_DEBUG_STDOUT);

    /**
     * BlueCove log is redirected to log4j when log4j classes are available in
     * classpath. You can disable this feature. Initialization property.
     */
    public static final ChannelOption<String> PROPERTY_DEBUG_LOG4J = option(BlueCoveConfigProperties.PROPERTY_DEBUG_LOG4J);

    /**
     * If automatic Bluetooth Stack detection is not enough this can be used to
     * force desired Stack Initialization. Values "widcomm", "bluesoleil" or
     * "winsock". Use "emulator" value to start jsr-82 emulator. By default
     * winsock is selected if available. Initialization property.
     */
    public static final ChannelOption<String> PROPERTY_STACK = option(BlueCoveConfigProperties.PROPERTY_STACK);

    /**
     * Used to optimize stack detection. If -Dbluecove.stack.first=widcomm then
     * widcomm (bluecove.dll) stack is loaded first and if not available then
     * BlueCove will switch to winsock. By default intelbth.dll is loaded first.
     * Initialization property.
     */
    public static final ChannelOption<String> PROPERTY_STACK_FIRST = option(BlueCoveConfigProperties.PROPERTY_STACK_FIRST);

    /**
     * "false" to disable the load of native library from resources.
     * Initialization property.
     */
    public static final ChannelOption<String> PROPERTY_NATIVE_RESOURCE = option(BlueCoveConfigProperties.PROPERTY_NATIVE_RESOURCE);

    /**
     * Load library (.dll) from specified location. Initialization property.
     * <p>
     * Path separated by system-dependent path-separator (: or ;) accepted.
     */
    public static final ChannelOption<String> PROPERTY_NATIVE_PATH = option(BlueCoveConfigProperties.PROPERTY_NATIVE_PATH);

    /**
     * Linux implementation class. Initialization property.
     */
    public static final ChannelOption<String> PROPERTY_BLUEZ_CLASS = option(BlueCoveConfigProperties.PROPERTY_BLUEZ_CLASS);

    /**
     * If Stack support multiple bluetooth adapters select one by its system ID.
     * (Linux BlueZ and Emulator) Initialization property.
     */
    public static final ChannelOption<String> PROPERTY_LOCAL_DEVICE_ID = option(BlueCoveConfigProperties.PROPERTY_LOCAL_DEVICE_ID);

    /**
     * If Stack support multiple bluetooth adapters select one by its bluetooth
     * address. (Linux BlueZ and Emulator) Initialization property.
     */
    public static final ChannelOption<String> PROPERTY_LOCAL_DEVICE_ADDRESS = option(BlueCoveConfigProperties.PROPERTY_LOCAL_DEVICE_ADDRESS);

    /**
     * JSR-82 simulator class. Initialization property.
     */
    public static final ChannelOption<String> PROPERTY_EMULATOR_CLASS = option(BlueCoveConfigProperties.PROPERTY_EMULATOR_CLASS);

    /**
     * JSR-82 air simulator server can be on remote computer, default
     * 'localhost'. Initialization property.
     */
    public static final ChannelOption<String> PROPERTY_EMULATOR_HOST = option(BlueCoveConfigProperties.PROPERTY_EMULATOR_HOST);

    /**
     * JSR-82 air simulator server listen on different port, default 8090.
     * <p>
     * Use 0 on the client to enable in process server, RMI will not be used.
     * Initialization property.
     */
    public static final ChannelOption<String> PROPERTY_EMULATOR_PORT = option(BlueCoveConfigProperties.PROPERTY_EMULATOR_PORT);

    /**
     * JSR-82 air simulator server and RMI registry can be started inside client
     * JVM, default 'false'. Initialization property.
     */
    public static final ChannelOption<String> PROPERTY_EMULATOR_RMI_REGISTRY = option(BlueCoveConfigProperties.PROPERTY_EMULATOR_RMI_REGISTRY);

    /**
     * Some properties can't be changed at runtime once the Stack was
     * initialized.
     */
    public static final ChannelOption<?>[] INITIALIZATION_PROPERTIES = new ChannelOption[]{PROPERTY_STACK, PROPERTY_STACK_FIRST,
                                                                                           PROPERTY_NATIVE_RESOURCE, PROPERTY_BLUEZ_CLASS, PROPERTY_LOCAL_DEVICE_ID, PROPERTY_LOCAL_DEVICE_ADDRESS, PROPERTY_EMULATOR_CLASS, PROPERTY_EMULATOR_HOST, PROPERTY_EMULATOR_PORT};

    /**
     * The amount of time in milliseconds for which the implementation will
     * attempt to establish connection RFCOMM or L2CAP before it throws
     * BluetoothConnectionException. Defaults to 2 minutes. WIDCOMM and OS X
     * only.
     */
    public static final ChannelOption<String> PROPERTY_CONNECT_TIMEOUT = option(BlueCoveConfigProperties.PROPERTY_CONNECT_TIMEOUT);

    /**
     * On MS stack retry connection automatically when received WSAENETUNREACH
     * during connect. Default to 2 retry attempts.
     *
     * @since bluecove 2.1.0
     */
    public static final ChannelOption<String> PROPERTY_CONNECT_UNREACHABLE_RETRY = option(BlueCoveConfigProperties.PROPERTY_CONNECT_UNREACHABLE_RETRY);

    /**
     * Device Inquiry time in seconds defaults to 11 seconds. MS Stack and OS X
     * only.
     */
    public static final ChannelOption<String> PROPERTY_INQUIRY_DURATION = option(BlueCoveConfigProperties.PROPERTY_INQUIRY_DURATION);

    static final int PROPERTY_INQUIRY_DURATION_DEFAULT = 11;

    /**
     * Set true to make Device Inquiry call DiscoveryListener?.deviceDiscovered
     * without waiting for updated service class. WIDCOMM only.
     */
    public static final ChannelOption<String> PROPERTY_INQUIRY_REPORT_ASAP = option(BlueCoveConfigProperties.PROPERTY_INQUIRY_REPORT_ASAP);

    /**
     * You can increase OBEX transfer speed by changing mtu to bigger value.
     * Default is 1024
     */
    public static final ChannelOption<String> PROPERTY_OBEX_MTU = option(BlueCoveConfigProperties.PROPERTY_OBEX_MTU);

    /**
     * The amount of time in milliseconds for which the implementation will
     * attempt to successfully transmit a packet before it throws
     * InterruptedIOException. Defaults to 2 minutes.
     */
    public static final ChannelOption<String> PROPERTY_OBEX_TIMEOUT = option(BlueCoveConfigProperties.PROPERTY_OBEX_TIMEOUT);

    /**
     * Remove JSR-82 1.1 restriction for legal PSM values are in the range
     * (0x1001..0xFFFF).
     * <p>
     * For JSR-82 1.2 Reserved Ranges @see <A HREF="https://opensource.motorola.com/sf/discussion/do/listPosts/projects.jsr82/discussion.jsr_82_1_2_open_discussion.topc1808"
     * >JSR-82 1.2</A>
     */
    public static final ChannelOption<String> PROPERTY_JSR_82_PSM_MINIMUM_OFF = option(BlueCoveConfigProperties.PROPERTY_JSR_82_PSM_MINIMUM_OFF);

    /**
     * In some cases BlueCove has a control how String are encoded in SDP records.
     * This one will force it to be encoded as ASCII (charsetName "US-ASCII").
     * May be useful for connections with some OEM devices.
     * <p>
     * Used on MS stack and BlueZ GPL module for now.
     * <p>
     * Defaults to false.
     */
    public static final ChannelOption<String> PROPERTY_SDP_STRING_ENCODING_ASCII = option(BlueCoveConfigProperties.PROPERTY_SDP_STRING_ENCODING_ASCII);

    private static ChannelOption<String> option(String secondNameComponent) {
        return valueOf(BluetoothChannelOption.class, secondNameComponent);
    }

    @SuppressWarnings("deprecation")
    private BluetoothChannelOption() {
        super("bluetooth-config");
    }
}
