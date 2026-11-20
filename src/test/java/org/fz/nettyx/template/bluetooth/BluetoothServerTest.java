package org.fz.nettyx.template.bluetooth;

import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.LocalDevice;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.StreamConnectionNotifier;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Runs a standalone Bluetooth server using the Bluetooth stream APIs.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2024/5/30 0:09
 */
public class BluetoothServerTest extends Thread {
    // Local Bluetooth device
    private LocalDevice              local            = null;
    // Stream connection
    private StreamConnection         streamConnection = null;
    // Incoming byte buffer
    private byte[]                   acceptdByteArray = new byte[1024];
    // Input stream
    private InputStream              inputStream;
    private OutputStream             outputStream;
    // Incoming connection notifier
    private StreamConnectionNotifier notifier;

    private             boolean stopFlag   = false;
    public final static String  serverName = "Bluetooth Test";
    public final static String  serverUUID = "1000110100001000800000805F9B34FB";

    public BluetoothServerTest() {
        try {
            local = LocalDevice.getLocalDevice();
            if (!local.setDiscoverable(DiscoveryAgent.GIAC)) System.out.println("Please enable Bluetooth discoverability");
            String url = "btspp://localhost:" + serverUUID + ";name=" + serverName;
            notifier = (StreamConnectionNotifier) Connector.open(url);

        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void run() {
        try {
            String inStr = null;
            streamConnection = notifier.acceptAndOpen();                // Blocks until a device connects
            inputStream      = streamConnection.openInputStream();
            outputStream     = streamConnection.openOutputStream();

            System.out.printf("Connected");

            // Communication code

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
