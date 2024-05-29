package template.bluetooth;

import cn.hutool.core.lang.Console;

import javax.bluetooth.*;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;

import static javax.bluetooth.ServiceRecord.NOAUTHENTICATE_NOENCRYPT;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/5/15 17:42
 */
public class TestBluetooth {

    public static void main(String[] args) throws Exception {
        LocalDevice localDevice = LocalDevice.getLocalDevice();
        System.out.println("本地蓝牙适配器名称：" + localDevice.getFriendlyName());
        System.out.println("本地蓝牙适配器地址：" + localDevice.getBluetoothAddress());

        DiscoveryAgent agent = localDevice.getDiscoveryAgent();
        agent.startInquiry(DiscoveryAgent.GIAC, new DiscoveryListener() {
            @Override
            public void deviceDiscovered(RemoteDevice remoteDevice, DeviceClass deviceClass) {
                System.err.println(remoteDevice);
                System.err.println(deviceClass);
            }

            @Override
            public void servicesDiscovered(int i, ServiceRecord[] serviceRecords) {
                System.err.println(serviceRecords.length);
            }

            @Override
            public void serviceSearchCompleted(int i, int i1) {
                System.err.println(i);
                System.err.println(i1);
            }

            @Override
            public void inquiryCompleted(int i) {
                System.err.println(i);
            }
        });


        RemoteDevice remoteDevice = agent.retrieveDevices(DiscoveryAgent.PREKNOWN)[0];
        UUID         uuid         = new UUID(NOAUTHENTICATE_NOENCRYPT);

        String           url              = agent.selectService(uuid, NOAUTHENTICATE_NOENCRYPT, false);
        Console.log(url);
        StreamConnection streamConnection = (StreamConnection) Connector.open(url);




    }
}
