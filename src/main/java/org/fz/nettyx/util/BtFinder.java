package org.fz.nettyx.util;

import cn.hutool.core.text.CharSequenceUtil;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

import javax.bluetooth.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/5/30 11:04
 */

@UtilityClass
public final class BtFinder {

    private static final InternalLogger log = InternalLoggerFactory.getInstance(BtFinder.class);

    @NoArgsConstructor
    public static class DeviceFinder {

        private final Object             completedTag = new Object();
        private final List<RemoteDevice> devices      = new ArrayList<>(64);
        private volatile boolean         completed    = false;
        private final DiscoveryListener  listener     = new DiscoveryListenerAdapter() {
            @Override
            public void deviceDiscovered(RemoteDevice btDevice, DeviceClass cod) {
                synchronized (completedTag) { devices.add(btDevice); }
                if (delegate != null) {
                    delegate.deviceDiscovered(btDevice, cod);
                }
            }

            @Override
            public void inquiryCompleted(int discType) {
                synchronized (completedTag) {
                    completed = true;
                    completedTag.notifyAll();
                }
                if (delegate != null) {
                    delegate.inquiryCompleted(discType);
                }
            }
        };
        private DiscoveryListener delegate;

        public DeviceFinder(DiscoveryListener listener)
        {
            this.delegate = listener;
        }

        public List<RemoteDevice> getDevices()
        {
            return getDevices(device -> true);
        }

        @SneakyThrows({ InterruptedException.class })
        public List<RemoteDevice> getDevices(Predicate<RemoteDevice> condition)
        {
            try {
                synchronized (completedTag) {
                    devices.clear();
                    completed = false;
                    boolean started = startInquiry(listener);

                    if (started) {
                        try {
                            long remainingNanos = timeoutNanos();
                            long deadline = System.nanoTime() + remainingNanos;
                            while (!completed && remainingNanos > 0) {
                                TimeUnit.NANOSECONDS.timedWait(completedTag, remainingNanos);
                                remainingNanos = deadline - System.nanoTime();
                            }
                        } finally {
                            cancelInquiry(listener);
                        }
                    }
                    devices.removeIf(condition.negate());
                }
            } catch (BluetoothStateException stateException) {
                log.error("bluetooth state is illegal, please check");
                return Collections.emptyList();
            }

            return devices;
        }

        boolean startInquiry(DiscoveryListener listener) throws BluetoothStateException
        {
            return LocalDevice.getLocalDevice().getDiscoveryAgent().startInquiry(DiscoveryAgent.GIAC, listener);
        }

        boolean cancelInquiry(DiscoveryListener listener) throws BluetoothStateException
        {
            return LocalDevice.getLocalDevice().getDiscoveryAgent().cancelInquiry(listener);
        }

        long timeoutNanos()
        {
            return TimeUnit.SECONDS.toNanos(30);
        }
    }

    @RequiredArgsConstructor
    public static class ServiceFinder {

        private static final int               DEFAULT_ATTR_ID       = 0x0100;
        private static final int               NO_ACTIVE_TRANSACTION = -1;
        private final        Object            completedTag    = new Object();
        private final List<String>             services        = new ArrayList<>(32);
        private volatile boolean                completed      = false;
        private int                             activeTransactionId = NO_ACTIVE_TRANSACTION;
        private final        DiscoveryListener  listener       = new DiscoveryListenerAdapter() {
            @Override
            public void servicesDiscovered(int transID, ServiceRecord[] servRecord)
            {
                synchronized (completedTag) {
                    if (transID != activeTransactionId) {
                        return;
                    }
                    for (ServiceRecord serviceRecord : servRecord) {
                        String url = serviceRecord.getConnectionURL(ServiceRecord.NOAUTHENTICATE_NOENCRYPT, false);
                        if (CharSequenceUtil.isEmpty(url)) {
                            continue;
                        }
                        services.add(url);
                    }
                }
                if (delegate != null) {
                    delegate.servicesDiscovered(transID, servRecord);
                }
            }

            @Override
            public void serviceSearchCompleted(int transID, int respCode)
            {
                synchronized (completedTag) {
                    if (transID != activeTransactionId) {
                        return;
                    }
                    completed = true;
                    completedTag.notifyAll();
                }
                if (delegate != null) {
                    delegate.serviceSearchCompleted(transID, respCode);
                }
            }
        };
        private DiscoveryListener delegate;

        public ServiceFinder(DiscoveryListener listener)
        {
            this.delegate = listener;
        }

        public List<String> getServices(RemoteDevice btDevice, String serviceUUID) throws IOException, InterruptedException
        {
            return getServices(btDevice, serviceUUID, s -> true);
        }

        public List<String> getServices(RemoteDevice btDevice, String serviceUUID, Predicate<String> condition) throws IOException, InterruptedException
        {
            UUID[] searchUuidSet = new UUID[]{ new UUID(serviceUUID, false) };

            synchronized (completedTag) {
                services.clear();
                completed = false;
                int transactionId = searchServices(searchUuidSet, btDevice, listener);
                activeTransactionId = transactionId;
                try {
                    long remainingNanos = timeoutNanos();
                    long deadline = System.nanoTime() + remainingNanos;
                    while (!completed && remainingNanos > 0) {
                        TimeUnit.NANOSECONDS.timedWait(completedTag, remainingNanos);
                        remainingNanos = deadline - System.nanoTime();
                    }
                    services.removeIf(condition.negate());
                } finally {
                    activeTransactionId = NO_ACTIVE_TRANSACTION;
                    cancelServiceSearch(transactionId);
                }
            }

            return services;
        }

        int searchServices(UUID[] searchUuidSet, RemoteDevice btDevice, DiscoveryListener listener) throws BluetoothStateException
        {
            return LocalDevice.getLocalDevice().getDiscoveryAgent()
                              .searchServices(new int[]{ DEFAULT_ATTR_ID }, searchUuidSet, btDevice, listener);
        }

        boolean cancelServiceSearch(int transId) throws BluetoothStateException
        {
            return LocalDevice.getLocalDevice().getDiscoveryAgent().cancelServiceSearch(transId);
        }

        long timeoutNanos()
        {
            return TimeUnit.SECONDS.toNanos(30);
        }
    }

    abstract static class DiscoveryListenerAdapter implements DiscoveryListener {
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
