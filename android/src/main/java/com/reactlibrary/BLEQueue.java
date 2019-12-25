package com.reactlibrary;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import com.facebook.react.bridge.ReactContext;
import com.facebook.react.modules.core.DeviceEventManagerModule.RCTDeviceEventEmitter;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.UUID;

public class BLEQueue {

    private IReceiveBLEData mDataHandler;
    private HandlerThread mHandlerThread;
    private Handler mHandler;
    private BluetoothDevice mDevice;
    private RCTDeviceEventEmitter mEventEmitter;
    private Context mContext;
    public static final String TAG="BLEQueue";

    public BLEQueue(IReceiveBLEData handler, ReactContext context) {
        super();
        mDataHandler = handler;
        mContext = context;
        mHandlerThread = new HandlerThread("BLE_CALLBACK");
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper());
        mEventEmitter = context.getJSModule(RCTDeviceEventEmitter.class);
    }

    void autoConnect(BluetoothAdapter adapter, String mDeviceMac) {
        ScanFilter filter = new ScanFilter.Builder().setDeviceAddress(mDeviceMac).build();
        ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .setReportDelay(0)
                .build();
        adapter.getBluetoothLeScanner().startScan(Arrays.asList(filter), settings, autoScanCallback);
    }

    protected void search(BluetoothAdapter adapter){
        adapter.getBluetoothLeScanner().startScan(new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                super.onScanResult(callbackType, result);
                BluetoothDevice device = result.getDevice();

                Log.d(TAG, "device acquired, name:"+device.getName()+", MAC:"+device.getAddress());
                if ("MIBCS".equals(device.getName()) || "MIBFS".equals(device.getName())){
                    if(mDevice==null)mDevice = device;
                }else{
                    return;
                }
                if(device.getAddress().equals(mDevice.getAddress())){
                    ScanRecord record = result.getScanRecord();
                    byte[] data = record.getBytes();
                    byte[] weight = new byte[0xd];
                    System.arraycopy(data, 11, weight, 0, 0xd);
                    int flag = mDataHandler.receiveData(weight);
                    if(flag<15) {
                        mDevice = null;
                    }else{
                        emmitMsg("DeviceAcquired", device.getAddress());
                    }
                }
            }
        });
    }

    void stopScan(BluetoothAdapter adapter) {
        adapter.getBluetoothLeScanner().stopScan(autoScanCallback);
    }

    private ScanCallback autoScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            ScanRecord record = result.getScanRecord();
            byte[] data = record.getBytes();
            byte[] weight = new byte[0xd];
            System.arraycopy(data, 11, weight, 0, 0xd);
            mDataHandler.receiveData(weight);
        }
    };

    private void emmitMsg(String eventName, Object msg) {
        mEventEmitter.emit(eventName, msg);
    }

    private class BLEConfigOp implements Runnable {

        @Override
        public void run() {
            RNMibcsModule.GATT.discoverServices();
            waitLock();
            // 推测是电阻部分
            {
                UUID serviceUUID1 = calculateUUID(0x1530L);
                BluetoothGattService service1 = RNMibcsModule.GATT.getService(serviceUUID1);
                UUID charUUID1 = calculateUUID(0x1543); // com/xiaomi/hm/health/bt/f/bmi/b;->b_
                BluetoothGattCharacteristic char1 = service1.getCharacteristic(charUUID1);
                UUID charUUID2 = calculateUUID(0x1542); // com/xiaomi/hm/health/bt/f/bmi/b;->muscle
                BluetoothGattCharacteristic char2 = service1.getCharacteristic(charUUID2);
                RNMibcsModule.GATT.setCharacteristicNotification(char2, true);
                UUID descUUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
                BluetoothGattDescriptor desc = char2.getDescriptor(descUUID);
//                    int properties = char2.getProperties();
////                    if ((properties & 0x10) <= 0) {
////
////                    }
                desc.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                RNMibcsModule.GATT.writeDescriptor(desc);
                waitLock();
            }
            {
                UUID serviceUUID2 = UUID.fromString("0000181b-0000-1000-8000-00805f9b34fb");
                BluetoothGattService service2 = RNMibcsModule.GATT.getService(serviceUUID2);
                UUID charUUID1 = UUID.fromString("00002a2b-0000-1000-8000-00805f9b34fb");
                BluetoothGattCharacteristic char1 = service2.getCharacteristic(charUUID1); //com/xiaomi/hm/health/bt/f/bmi/b;->bodyFat
                UUID charUUID2 = UUID.fromString("00002a9c-0000-1000-8000-00805f9b34fb"); //com/xiaomi/hm/health/bt/f/bmi/b;->water
                BluetoothGattCharacteristic char2 = service2.getCharacteristic(charUUID2);
                RNMibcsModule.GATT.setCharacteristicNotification(char2, true);
                UUID descUUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
                BluetoothGattDescriptor desc = char2.getDescriptor(descUUID);
//                    int properties = char2.getProperties();
//                    if ((properties & 0x10) <= 0) {
//
//                    }
                desc.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                RNMibcsModule.GATT.writeDescriptor(desc);
                waitLock();
            }
            emmitMsg("ScaleStatusChanged", "测量中");
        }
    }

    private Object lock = new Object();
    private BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            switch (status) {
                case BluetoothGatt.GATT_SUCCESS: {
                    if (newState == BluetoothProfile.STATE_CONNECTED) {
                        RNMibcsModule.GATT = gatt;
//                        mHandler.post(new BLEConfigOp());
                    } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                        emmitMsg("ScaleStatusChanged", "连接中");
                    }
                }
                break;
                case 19: // disconnected by device
                case 133: // device not found
                {
                    emmitMsg("ScaleStatusChanged", "连接中");
//                    gatt.connect();
                }
                break;
                default: {
                    Log.e("BLE", "Operation has failed! status: " + status + ", new state: " + newState);
                }
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            if (gatt == RNMibcsModule.GATT) {
                synchronized (lock) {
                    lock.notify();
                }
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            switch (characteristic.getUuid().toString()) {
                case "00002a9c-0000-1000-8000-00805f9b34fb":
                {
                    // weight changed
                    byte[] data = characteristic.getValue();
                    mDataHandler.receiveData(data);
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            updateWeightUI();
//                        }
//                    });
                }
                default: {
                    System.out.println("receive char update notify.   "  + characteristic.getUuid().toString());
                    System.out.println(byteArrayToString(characteristic.getValue()));
                }
            }

//            Log.d("Char Change", byteArrayToString(value));
//            System.out.println("");
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorRead(gatt, descriptor, status);
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
        }
    };

    private String byteArrayToString(byte[] array) {
        StringBuilder sb = new StringBuilder();
        for(byte b : array) {
            sb.append(String.format("%02x ", b));
        }
        return sb.toString();
    }

    private UUID calculateUUID(long payload) {
//        long most = 0x3512L;
//        long least = 0x21180009af100700L;
        UUID uuid = new UUID(0x3512L, 0x21180009af100700L);
        long v0 = uuid.getMostSignificantBits();
        long v4 = -0xffff00000001L;
        v0 = v0 & v4;
        v4 = payload & 0xffffL;
        v4 = v4 << 0x20;
        v0 = v0 | v4;
        v4 = uuid.getLeastSignificantBits();
        UUID actualID = new UUID(v0, v4);
        return actualID;
    }

    private void waitLock() {
        synchronized (lock) {
            try {
                lock.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void releaseLock() {
        synchronized (lock) {
            lock.notify();
        }
    }

    protected void clear(){
        mDevice = null;
    }

    protected void bindMAC(){
        if(mDevice==null)return;
        SharedPreferences sp = mContext.getSharedPreferences("REACT-NATIVE", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("DEVICE-MAC", mDevice.getAddress());
        editor.apply();
    }
}
