package org.fz.nettyx.util;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.junit.Test;

import javax.bluetooth.DataElement;
import javax.bluetooth.DeviceClass;
import javax.bluetooth.DiscoveryListener;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.ServiceRecord;
import javax.bluetooth.UUID;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Checks Bluetooth discovery result isolation, callbacks, cancellation, and timeouts.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2026-08-02
 */
public class BtFinderTest {

    @Test(timeout = 1_000)
    public void deviceSearchResultsStayIndependentAcrossSearches()
    {
        RemoteDevice device = new TestRemoteDevice("001122334455");
        StubDeviceFinder finder = new StubDeviceFinder(null, device);
        List<RemoteDevice> first = finder.getDevices();
        List<RemoteDevice> second = finder.getDevices();
        second.clear();

        assertEquals(List.of(device), first);
        assertTrue(finder.getDevices(found -> false).isEmpty());
        assertEquals(List.of(device), first);
    }

    @Test(timeout = 2_000)
    public void serviceSearchResultsStayIndependentAcrossSearches() throws Exception
    {
        RemoteDevice device = new TestRemoteDevice("001122334455");
        AsyncServiceFinder finder = new AsyncServiceFinder(null, 17, new TestServiceRecord("btspp://current:1"));
        List<String> first = finder.getServices(device, "1101");
        finder.callbackThread.join(500);
        List<String> second = finder.getServices(device, "1101");
        finder.callbackThread.join(500);
        second.clear();

        assertEquals(List.of("btspp://current:1"), first);
        assertTrue(finder.getServices(device, "1101", found -> false).isEmpty());
        finder.callbackThread.join(500);
        assertEquals(List.of("btspp://current:1"), first);
    }

    @Test(timeout = 1_000)
    public void deviceFinderKeepsInternalStateWhenUserListenerIsPresent()
    {
        AtomicInteger discoveredCount = new AtomicInteger();
        AtomicInteger completedCount = new AtomicInteger();
        DiscoveryListener userListener = new BtFinder.DiscoveryListenerAdapter() {
            @Override
            public void deviceDiscovered(RemoteDevice btDevice, DeviceClass cod) {
                discoveredCount.incrementAndGet();
            }

            @Override
            public void inquiryCompleted(int discType) {
                completedCount.incrementAndGet();
            }
        };
        RemoteDevice device = new TestRemoteDevice("001122334455");
        StubDeviceFinder finder = new StubDeviceFinder(userListener, device);

        List<RemoteDevice> devices = finder.getDevices();

        assertEquals(List.of(device), devices);
        assertEquals(1, discoveredCount.get());
        assertEquals(1, completedCount.get());
        assertTrue(finder.cancelled);
    }

    @Test(timeout = 2_000)
    public void deviceFinderCancelsInquiryWhenInterrupted() throws Exception
    {
        BlockingDeviceFinder finder = new BlockingDeviceFinder();
        AtomicReference<Throwable> thrown = new AtomicReference<>();
        Thread worker = new Thread(() -> {
            try {
                finder.getDevices();
            } catch (Throwable error) {
                thrown.set(error);
            }
        });

        worker.start();
        assertTrue(finder.started.await(1, TimeUnit.SECONDS));
        worker.interrupt();
        worker.join(1_000);

        assertFalse(worker.isAlive());
        assertTrue(thrown.get() instanceof InterruptedException);
        assertTrue(finder.cancelled);
    }

    @Test(timeout = 1_000)
    public void deviceFinderUsesConfiguredMonotonicTimeout()
    {
        TimedOutDeviceFinder finder = new TimedOutDeviceFinder();

        finder.getDevices();

        assertTrue(finder.cancelled);
    }

    @Test(timeout = 1_000)
    public void serviceFinderKeepsInternalStateWhenUserListenerIsPresent() throws Exception
    {
        AtomicInteger discoveredCount = new AtomicInteger();
        AtomicInteger completedCount = new AtomicInteger();
        DiscoveryListener userListener = new BtFinder.DiscoveryListenerAdapter() {
            @Override
            public void servicesDiscovered(int transID, ServiceRecord[] servRecord) {
                discoveredCount.addAndGet(servRecord.length);
            }

            @Override
            public void serviceSearchCompleted(int transID, int respCode) {
                completedCount.incrementAndGet();
            }
        };
        ServiceRecord record = new TestServiceRecord("btspp://001122334455:1");
        AsyncServiceFinder finder = new AsyncServiceFinder(userListener, 17, record);

        List<String> services = finder.getServices(new TestRemoteDevice("001122334455"), "1101");
        finder.callbackThread.join(500);

        assertEquals(List.of("btspp://001122334455:1"), services);
        assertEquals(1, discoveredCount.get());
        assertEquals(1, completedCount.get());
    }

    @Test(timeout = 1_000)
    public void serviceFinderIgnoresCallbacksOutsideActiveTransaction() throws Exception
    {
        TransactionServiceFinder finder = new TransactionServiceFinder(
                17,
                new TestServiceRecord("btspp://stale:1"),
                new TestServiceRecord("btspp://current:1"));

        List<String> services = finder.getServices(new TestRemoteDevice("001122334455"), "1101");
        finder.callbackThread.join(500);
        finder.emitLate(new TestServiceRecord("btspp://late:1"));

        assertEquals(List.of("btspp://current:1"), services);
    }

    @Test(timeout = 1_000)
    public void serviceFinderUsesConfiguredMonotonicTimeout() throws Exception
    {
        TimedOutServiceFinder finder = new TimedOutServiceFinder();

        assertTrue(finder.getServices(new TestRemoteDevice("001122334455"), "1101").isEmpty());
    }

    @Test(timeout = 2_000)
    public void serviceFinderCancelsSearchWhenInterrupted() throws Exception
    {
        BlockingServiceFinder finder = new BlockingServiceFinder();
        AtomicReference<Throwable> thrown = new AtomicReference<>();
        Thread worker = new Thread(() -> {
            try {
                finder.getServices(new TestRemoteDevice("001122334455"), "1101");
            } catch (Throwable error) {
                thrown.set(error);
            }
        });

        worker.start();
        assertTrue(finder.started.await(1, TimeUnit.SECONDS));
        worker.interrupt();
        worker.join(1_000);

        assertFalse(worker.isAlive());
        assertTrue(thrown.get() instanceof InterruptedException);
        assertEquals(17, finder.cancelledTransactionId);
    }

    /**
     * Reports fixture devices through the Bluetooth inquiry callbacks.
     *
     * @author fengbinbin
     * @version 1.0
     * @since 2026-08-02
     */
    private static final class StubDeviceFinder extends BtFinder.DeviceFinder {

        private final RemoteDevice device;
        private boolean cancelled;

        private StubDeviceFinder(DiscoveryListener listener, RemoteDevice device)
        {
            super(listener);
            this.device = device;
        }

        @Override
        boolean startInquiry(DiscoveryListener listener)
        {
            listener.deviceDiscovered(device, null);
            listener.inquiryCompleted(DiscoveryListener.INQUIRY_COMPLETED);
            return true;
        }

        @Override
        boolean cancelInquiry(DiscoveryListener listener)
        {
            cancelled = true;
            return true;
        }
    }

    /**
     * Creates a remote device fixture from a supplied Bluetooth address.
     *
     * @author fengbinbin
     * @version 1.0
     * @since 2026-08-02
     */
    private static final class TestRemoteDevice extends RemoteDevice {

        private TestRemoteDevice(String address)
        {
            super(address);
        }
    }

    /**
     * Leaves a Bluetooth inquiry pending so interruption can trigger cancellation.
     *
     * @author fengbinbin
     * @version 1.0
     * @since 2026-08-02
     */
    private static final class BlockingDeviceFinder extends BtFinder.DeviceFinder {

        private final CountDownLatch started = new CountDownLatch(1);
        private volatile boolean cancelled;

        @Override
        boolean startInquiry(DiscoveryListener listener)
        {
            started.countDown();
            return true;
        }

        @Override
        boolean cancelInquiry(DiscoveryListener listener)
        {
            cancelled = true;
            return true;
        }
    }

    /**
     * Leaves Bluetooth inquiry pending and sets a five-millisecond deadline to exercise timeout handling.
     *
     * @author fengbinbin
     * @version 1.0
     * @since 2026-08-02
     */
    private static final class TimedOutDeviceFinder extends BtFinder.DeviceFinder {

        private boolean cancelled;

        @Override
        boolean startInquiry(DiscoveryListener listener)
        {
            return true;
        }

        @Override
        boolean cancelInquiry(DiscoveryListener listener)
        {
            cancelled = true;
            return true;
        }

        @Override
        long timeoutNanos()
        {
            return TimeUnit.MILLISECONDS.toNanos(5);
        }
    }

    /**
     * Reports Bluetooth service records from a worker thread.
     *
     * @author fengbinbin
     * @version 1.0
     * @since 2026-08-02
     */
    private static final class AsyncServiceFinder extends BtFinder.ServiceFinder {

        private final int transactionId;
        private final ServiceRecord record;
        private Thread callbackThread;

        private AsyncServiceFinder(DiscoveryListener listener, int transactionId, ServiceRecord record)
        {
            super(listener);
            this.transactionId = transactionId;
            this.record = record;
        }

        @Override
        int searchServices(UUID[] searchUuidSet, RemoteDevice btDevice, DiscoveryListener listener)
        {
            callbackThread = new Thread(() -> {
                listener.servicesDiscovered(transactionId, new ServiceRecord[]{ record });
                listener.serviceSearchCompleted(transactionId, DiscoveryListener.SERVICE_SEARCH_COMPLETED);
            });
            callbackThread.start();
            return transactionId;
        }

        @Override
        boolean cancelServiceSearch(int transId)
        {
            return true;
        }
    }

    /**
     * Emits service discovery callbacks with controlled transaction identifiers.
     *
     * @author fengbinbin
     * @version 1.0
     * @since 2026-08-02
     */
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class TransactionServiceFinder extends BtFinder.ServiceFinder {

        private final int transactionId;
        private final ServiceRecord staleRecord;
        private final ServiceRecord currentRecord;
        private DiscoveryListener listener;
        private Thread callbackThread;

        @Override
        int searchServices(UUID[] searchUuidSet, RemoteDevice btDevice, DiscoveryListener listener)
        {
            this.listener = listener;
            callbackThread = new Thread(() -> {
                listener.servicesDiscovered(transactionId - 1, new ServiceRecord[]{ staleRecord });
                listener.servicesDiscovered(transactionId, new ServiceRecord[]{ currentRecord });
                listener.serviceSearchCompleted(transactionId, DiscoveryListener.SERVICE_SEARCH_COMPLETED);
            });
            callbackThread.start();
            return transactionId;
        }

        @Override
        boolean cancelServiceSearch(int transId)
        {
            return true;
        }

        private void emitLate(ServiceRecord record)
        {
            listener.servicesDiscovered(transactionId, new ServiceRecord[]{ record });
        }
    }

    /**
     * Leaves a Bluetooth service search pending and sets a five-millisecond deadline to exercise timeout handling.
     *
     * @author fengbinbin
     * @version 1.0
     * @since 2026-08-02
     */
    private static final class TimedOutServiceFinder extends BtFinder.ServiceFinder {

        @Override
        int searchServices(UUID[] searchUuidSet, RemoteDevice btDevice, DiscoveryListener listener)
        {
            return 17;
        }

        @Override
        boolean cancelServiceSearch(int transId)
        {
            return true;
        }

        @Override
        long timeoutNanos()
        {
            return TimeUnit.MILLISECONDS.toNanos(5);
        }
    }

    /**
     * Leaves a Bluetooth service search pending so interruption can trigger cancellation.
     *
     * @author fengbinbin
     * @version 1.0
     * @since 2026-08-02
     */
    private static final class BlockingServiceFinder extends BtFinder.ServiceFinder {

        private final CountDownLatch started = new CountDownLatch(1);
        private volatile int cancelledTransactionId = -1;

        @Override
        int searchServices(UUID[] searchUuidSet, RemoteDevice btDevice, DiscoveryListener listener)
        {
            started.countDown();
            return 17;
        }

        @Override
        boolean cancelServiceSearch(int transId)
        {
            cancelledTransactionId = transId;
            return true;
        }
    }

    /**
     * Provides a Bluetooth service record with a fixed connection URL.
     *
     * @author fengbinbin
     * @version 1.0
     * @since 2026-08-02
     */
    private record TestServiceRecord(String url) implements ServiceRecord {

        @Override
        public DataElement getAttributeValue(int attrID) {
            return null;
        }

        @Override
        public RemoteDevice getHostDevice() {
            return null;
        }

        @Override
        public int[] getAttributeIDs() {
            return new int[0];
        }

        @Override
        public boolean populateRecord(int[] attrIDs) {
            return false;
        }

        @Override
        public String getConnectionURL(int requiredSecurity, boolean mustBeMaster) {
            return url;
        }

        @Override
        public void setDeviceServiceClasses(int classes) {
        }

        @Override
        public boolean setAttributeValue(int attrID, DataElement attrValue) {
            return false;
        }
    }
}
