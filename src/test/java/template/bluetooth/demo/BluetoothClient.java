package template.bluetooth.demo;

import org.fz.nettyx.util.BtFinder;

import javax.bluetooth.BluetoothConnectionException;
import javax.bluetooth.RemoteDevice;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;


/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/5/30 11:07
 */
public class BluetoothClient {
    private StreamConnection   streamConnection;//流连接
    private OnDiscoverListener onDiscoverListener = null;//发现监听
    private OnClientListener   onClientListener   = null;//客户端监听


    public static void main(String[] args) throws Exception {
        List<RemoteDevice> device = new BtFinder.DeviceFinder().getDevices();
    }
    /**
     * 客户端监听
     */
    public interface OnClientListener {
        void onConnected(DataInputStream inputStream, OutputStream outputStream);

        void onConnectionFailed();

        void onDisconnected();

        void onClose();
    }

    /**
     * 发现监听
     */
    public interface OnDiscoverListener {
        void onDiscover(RemoteDevice remoteDevice);
    }


    /**
     * 无参构造函数
     */
    public BluetoothClient() {
    }

    /**
     * 查找所有
     *
     * @throws IOException
     * @throws InterruptedException
     */
    public void find() throws IOException, InterruptedException {
        //附近所有的蓝牙设备，必须先执行 runDiscovery
        List<RemoteDevice>     devicesDiscovered = new BtFinder.DeviceFinder().getDevices();
        Iterator<RemoteDevice> itr               = devicesDiscovered.iterator();
        //连接
        while (itr.hasNext()) {
            RemoteDevice remoteDevice = itr.next();

            onDiscoverListener.onDiscover(remoteDevice);
        }
    }

    /**
     * 启动连接
     *
     * @param remoteDevice
     * @throws IOException
     * @throws InterruptedException
     */
    public void startClient(RemoteDevice remoteDevice) throws IOException, InterruptedException {
//        String url = RemoteDeviceDiscovery.searchService(remoteDevice, serviceUUID);
//        System.out.println("url=="+url);
//        1 为通道;authenticate=true;encrypt=true表示需要验证pin码
//        btspp://<蓝牙设备地址>:<通道号>
        String url = "btspp://" + remoteDevice.getBluetoothAddress() + ":1;authenticate=true;encrypt=true";
        try {
            streamConnection = (StreamConnection) Connector.open(url);
            if (this.onClientListener != null) {
                this.onClientListener.onConnected(streamConnection.openDataInputStream(), streamConnection.openOutputStream());
            } else {
                System.out.println("请打开蓝牙");
            }
        } catch (BluetoothConnectionException e) {
            e.printStackTrace();
            System.out.println("蓝牙连接错误，请查看蓝牙是否打开。");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public OnDiscoverListener getOnDiscoverListener() {
        return onDiscoverListener;
    }


    public void setOnDiscoverListener(OnDiscoverListener onDiscoverListener) {
        this.onDiscoverListener = onDiscoverListener;
    }


    public OnClientListener getClientListener() {
        return onClientListener;
    }


    public void setClientListener(OnClientListener onClientListener) {
        this.onClientListener = onClientListener;
    }

}