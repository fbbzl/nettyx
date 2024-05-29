package template.bluetooth;

import cn.hutool.core.lang.Console;

import javax.bluetooth.*;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import java.io.IOException;

import static javax.bluetooth.ServiceRecord.NOAUTHENTICATE_NOENCRYPT;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/5/15 17:42
 */
public class TestBluetooth {

    private static DiscoveryListener listener = new DiscoveryListener() {
        public void inquiryCompleted(int discType) {
            System.out.println("#" + "搜索完成");

        }

        @Override
        public void deviceDiscovered(RemoteDevice remoteDevice, DeviceClass deviceClass) {
            try {
                System.out.println("#发现设备" + remoteDevice.getFriendlyName(false));
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        @Override
        public void servicesDiscovered(int transID, ServiceRecord[] servRecord) {
            for (int i = 0; i < servRecord.length; i++) {
                String url = servRecord[i].getConnectionURL(ServiceRecord.NOAUTHENTICATE_NOENCRYPT, false);
                if (url == null) {
                    continue;
                }

                DataElement serviceName = servRecord[i].getAttributeValue(0x0100);
                if (serviceName != null) {
                    System.out.println("service " + serviceName.getValue() + " found " + url);
                } else {
                    System.out.println("service found " + url);
                }
            }
            System.out.println("#" + "servicesDiscovered");
        }

        @Override
        public void serviceSearchCompleted(int arg0, int arg1) {
            System.out.println("#" + "serviceSearchCompleted");

        }
    };


    private static void findDevices() throws IOException, InterruptedException {
        LocalDevice localDevice = LocalDevice.getLocalDevice();

        System.out.println("本地蓝牙适配器名称：" + localDevice.getFriendlyName());
        System.out.println("本地蓝牙适配器地址：" + localDevice.getBluetoothAddress());

        boolean started = LocalDevice.getLocalDevice().getDiscoveryAgent().startInquiry(DiscoveryAgent.GIAC, listener);

        if (started) {
            System.out.println("#" + "等待搜索完成...");
            LocalDevice.getLocalDevice().getDiscoveryAgent().cancelInquiry(listener);
        }
    }

    public static void main(String[] args) throws Exception {
        DiscoveryAgent agent = LocalDevice.getLocalDevice().getDiscoveryAgent();
        agent.startInquiry(DiscoveryAgent.GIAC, listener);


        RemoteDevice remoteDevice = agent.retrieveDevices(DiscoveryAgent.PREKNOWN)[0];
        UUID         uuid         = new UUID(NOAUTHENTICATE_NOENCRYPT);

        String url = agent.selectService(uuid, NOAUTHENTICATE_NOENCRYPT, false);
        Console.log(url);
        StreamConnection streamConnection = (StreamConnection) Connector.open(url);


    }
}
