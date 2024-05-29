package template.bluetooth;

import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.LocalDevice;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.StreamConnectionNotifier;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/5/30 0:09
 */
public class BluetoothServerTest extends Thread {
    //本机蓝牙设备
    private LocalDevice              local            = null;
    // 流连接
    private StreamConnection         streamConnection = null;
    // 接受数据的字节流
    private byte[]           acceptdByteArray = new byte[1024];
    // 输入流
    private InputStream              inputStream;
    private OutputStream             outputStream;
    //接入通知
    private StreamConnectionNotifier notifier;

    private boolean stopFlag = false;
    public final static String serverName = "Bluetooth Test";
    public final static String serverUUID = "1000110100001000800000805F9B34FB";

    public BluetoothServerTest() {
        try {
            local = LocalDevice.getLocalDevice();
            if (!local.setDiscoverable(DiscoveryAgent.GIAC))
                System.out.println("请将蓝牙设置为可被发现");
            /**
             * 作为服务端，被请求
             */
            String url = "btspp://localhost:" +  serverUUID + ";name="+serverName;
            notifier = (StreamConnectionNotifier) Connector.open(url);

        } catch (IOException e) {
            System.out.println(e.getMessage());;
        }
    }

    @Override
    public void run() {
        try {
            String inStr = null;
            streamConnection = notifier.acceptAndOpen();                //阻塞的，等待设备连接
            inputStream = streamConnection.openInputStream();
            outputStream = streamConnection.openOutputStream();

            System.out.printf("Connected");

            /**
             * 通信代码
             */

            inputStream.close();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }
    }

    public static void main(String[] argv) {
        new BluetoothServerTest().start();
    }
}