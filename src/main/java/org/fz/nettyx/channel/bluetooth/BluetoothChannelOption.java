package org.fz.nettyx.channel.bluetooth;

import io.netty.channel.ChannelOption;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/5/15 15:18
 */
public class BluetoothChannelOption extends ChannelOption<String> {

    public static final String PROPERTY_DEBUG = "bluecove.debug";

    /**
     * BlueCove log when enabled is printed to System.out. You can disable this
     * feature. Initialization property.
     */
    public static final String PROPERTY_DEBUG_STDOUT = "bluecove.debug.stdout";

    /**
     * BlueCove log is redirected to log4j when log4j classes are available in
     * classpath. You can disable this feature. Initialization property.
     */
    public static final String PROPERTY_DEBUG_LOG4J = "bluecove.debug.log4j";

    /**
     * If automatic Bluetooth Stack detection is not enough this can be used to
     * force desired Stack Initialization. Values "widcomm", "bluesoleil" or
     * "winsock". Use "emulator" value to start jsr-82 emulator. By default
     * winsock is selected if available. Initialization property.
     */
    public static final String PROPERTY_STACK = "bluecove.stack";

    /**
     * Used to optimize stack detection. If -Dbluecove.stack.first=widcomm then
     * widcomm (bluecove.dll) stack is loaded first and if not available then
     * BlueCove will switch to winsock. By default intelbth.dll is loaded first.
     * Initialization property.
     */
    public static final String PROPERTY_STACK_FIRST = "bluecove.stack.first";

    /**
     * "false" to disable the load of native library from resources.
     * Initialization property.
     */
    public static final String PROPERTY_NATIVE_RESOURCE = "bluecove.native.resource";

    /**
     * Load library (.dll) from specified location. Initialization property.
     *
     * Path separated by system-dependent path-separator (: or ;) accepted.
     */
    public static final String PROPERTY_NATIVE_PATH = "bluecove.native.path";

    /**
     * Linux implementation class. Initialization property.
     */
    public static final String PROPERTY_BLUEZ_CLASS = "bluecove.bluez.class";

    /**
     * If Stack support multiple bluetooth adapters select one by its system ID.
     * (Linux BlueZ and Emulator) Initialization property.
     */
    public static final String PROPERTY_LOCAL_DEVICE_ID = "bluecove.deviceID";

    /**
     * If Stack support multiple bluetooth adapters select one by its bluetooth
     * address. (Linux BlueZ and Emulator) Initialization property.
     */
    public static final String PROPERTY_LOCAL_DEVICE_ADDRESS = "bluecove.deviceAddress";

    /**
     * JSR-82 simulator class. Initialization property.
     */
    public static final String PROPERTY_EMULATOR_CLASS = "bluecove.emulator.class";

    /**
     * JSR-82 air simulator server can be on remote computer, default
     * 'localhost'. Initialization property.
     */
    public static final String PROPERTY_EMULATOR_HOST = "bluecove.emu.rmiRegistryHost";

    /**
     * JSR-82 air simulator server listen on different port, default 8090.
     * <p>
     * Use 0 on the client to enable in process server, RMI will not be used.
     * Initialization property.
     */
    public static final String PROPERTY_EMULATOR_PORT = "bluecove.emu.rmiRegistryPort";

    /**
     * JSR-82 air simulator server and RMI registry can be started inside client
     * JVM, default 'false'. Initialization property.
     */
    public static final String PROPERTY_EMULATOR_RMI_REGISTRY = "bluecove.emu.rmiRegistry";

    /**
     * Some properties can't be changed at runtime once the Stack was
     * initialized.
     */
    public static final String[] INITIALIZATION_PROPERTIES = new String[] { PROPERTY_STACK, PROPERTY_STACK_FIRST, PROPERTY_NATIVE_RESOURCE,
                                                                            PROPERTY_NATIVE_RESOURCE, PROPERTY_BLUEZ_CLASS, PROPERTY_LOCAL_DEVICE_ID, PROPERTY_LOCAL_DEVICE_ADDRESS, PROPERTY_EMULATOR_CLASS, PROPERTY_EMULATOR_HOST, PROPERTY_EMULATOR_PORT };

    /**
     * The amount of time in milliseconds for which the implementation will
     * attempt to establish connection RFCOMM or L2CAP before it throws
     * BluetoothConnectionException. Defaults to 2 minutes. WIDCOMM and OS X
     * only.
     */
    public static final String PROPERTY_CONNECT_TIMEOUT = "bluecove.connect.timeout";

    /**
     * On MS stack retry connection automatically when received WSAENETUNREACH
     * during connect. Default to 2 retry attempts.
     *
     * @since bluecove 2.1.0
     */
    public static final String PROPERTY_CONNECT_UNREACHABLE_RETRY = "bluecove.connect.unreachable_retry";

    /**
     * Device Inquiry time in seconds defaults to 11 seconds. MS Stack and OS X
     * only.
     */
    public static final String PROPERTY_INQUIRY_DURATION = "bluecove.inquiry.duration";

    static final int PROPERTY_INQUIRY_DURATION_DEFAULT = 11;

    /**
     * Set true to make Device Inquiry call DiscoveryListener?.deviceDiscovered
     * without waiting for updated service class. WIDCOMM only.
     */
    public static final String PROPERTY_INQUIRY_REPORT_ASAP = "bluecove.inquiry.report_asap";

    /**
     * You can increase OBEX transfer speed by changing mtu to bigger value.
     * Default is 1024
     */
    public static final String PROPERTY_OBEX_MTU = "bluecove.obex.mtu";

    /**
     * The amount of time in milliseconds for which the implementation will
     * attempt to successfully transmit a packet before it throws
     * InterruptedIOException. Defaults to 2 minutes.
     */
    public static final String PROPERTY_OBEX_TIMEOUT = "bluecove.obex.timeout";

    /**
     * Remove JSR-82 1.1 restriction for legal PSM values are in the range
     * (0x1001..0xFFFF).
     *
     * For JSR-82 1.2 Reserved Ranges @see <A HREF="https://opensource.motorola.com/sf/discussion/do/listPosts/projects.jsr82/discussion.jsr_82_1_2_open_discussion.topc1808"
     * >JSR-82 1.2</A>
     */
    public static final String PROPERTY_JSR_82_PSM_MINIMUM_OFF = "bluecove.jsr82.psm_minimum_off";

    /**
     * In some cases BlueCove has a control how String are encoded in SDP records.
     * This one will force it to be encoded as ASCII (charsetName "US-ASCII").
     * May be useful for connections with some OEM devices.
     *
     * Used on MS stack and BlueZ GPL module for now.
     *
     * Defaults to false.
     */
    public static final String PROPERTY_SDP_STRING_ENCODING_ASCII = "bluecove.sdp.string_encoding_ascii";


    private static <O> ChannelOption<O> option(String secondNameComponent) {
        return valueOf(BluetoothChannelOption.class, secondNameComponent);
    }

    @SuppressWarnings("deprecation")
    private BluetoothChannelOption() {
        super("bluetooth-config");
    }
}
