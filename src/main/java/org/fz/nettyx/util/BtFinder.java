package org.fz.nettyx.util;

import cn.hutool.core.text.CharSequenceUtil;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import javax.bluetooth.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/5/30 11:04
 */
public final class BtFinder {

    @NoArgsConstructor
    public static class BtDeviceFinder {

        private final Object             completedTag = new Object();
        private final List<RemoteDevice> devices      = new ArrayList<>(64);
        private       DiscoveryListener  listener     = new DiscoveryListenerAdapter() {
            @Override
            public void deviceDiscovered(RemoteDevice btDevice, DeviceClass cod) {
                devices.add(btDevice);
            }

            @Override
            public void inquiryCompleted(int discType) {
                synchronized (completedTag) {
                    completedTag.notifyAll();
                }
            }
        };

        public BtDeviceFinder(DiscoveryListener listener) {
            this.listener = listener;
        }

        public List<RemoteDevice> getDevice() throws BluetoothStateException {
            return getDevice(device -> true);
        }

        @SneakyThrows({ InterruptedException.class })
        public List<RemoteDevice> getDevice(Predicate<RemoteDevice> condition) throws BluetoothStateException {
            devices.clear();

            synchronized (completedTag) {
                DiscoveryAgent discoveryAgent = LocalDevice.getLocalDevice().getDiscoveryAgent();
                boolean        started        = discoveryAgent.startInquiry(DiscoveryAgent.GIAC, listener);

                if (started) {
                    completedTag.wait();
                    discoveryAgent.cancelInquiry(listener);
                }
            }
            devices.removeIf(condition.negate());
            return devices;
        }
    }

    @RequiredArgsConstructor
    public static class BtServiceFinder {

        private static final int               DEFAULT_ATTR_ID = 0x0100;
        private static final Object            completedTag    = new Object();
        private final        List<String>      services        = new ArrayList<>(32);
        private              DiscoveryListener listener        = new DiscoveryListenerAdapter() {
            @Override
            public void servicesDiscovered(int transID, ServiceRecord[] servRecord) {
                for (ServiceRecord serviceRecord : servRecord) {
                    String url = serviceRecord.getConnectionURL(ServiceRecord.NOAUTHENTICATE_NOENCRYPT, false);
                    if (CharSequenceUtil.isEmpty(url)) {
                        continue;
                    }
                    services.add(url);
                }
            }

            @Override
            public void serviceSearchCompleted(int transID, int respCode) {
                synchronized (completedTag) {
                    completedTag.notifyAll();
                }
            }
        };

        public BtServiceFinder(DiscoveryListener listener) {
            this.listener = listener;
        }

        public List<String> getService(RemoteDevice btDevice, String serviceUUID, Predicate<String> condition) throws IOException, InterruptedException {
            UUID[] searchUuidSet = new UUID[]{ new UUID(serviceUUID, false) };

            synchronized (completedTag) {
                LocalDevice.getLocalDevice().getDiscoveryAgent().searchServices(new int[]{ DEFAULT_ATTR_ID }, searchUuidSet, btDevice, listener);
                completedTag.wait();
            }

            services.removeIf(condition.negate());

            return services;
        }
    }


    private abstract static class DiscoveryListenerAdapter implements DiscoveryListener {
        @Override
        public void deviceDiscovered(RemoteDevice btDevice, DeviceClass cod) {
        }

        @Override
        public void servicesDiscovered(int transID, ServiceRecord[] servRecord) {
        }

        @Override
        public void serviceSearchCompleted(int transID, int respCode) {
        }

        @Override
        public void inquiryCompleted(int discType) {
        }
    }
}
