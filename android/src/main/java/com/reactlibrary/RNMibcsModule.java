
package com.reactlibrary;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import java.util.Hashtable;

public class RNMibcsModule extends ReactContextBaseJavaModule implements IReceiveBLEData{


  static BluetoothGatt GATT;
  private BluetoothAdapter mBTAdapter;
  private BluetoothManager mBTManager;
  private BLEQueue mBLEQueue;
  private DeviceEventManagerModule.RCTDeviceEventEmitter mDeviceEventEmitter;
  private ReactApplicationContext mContext;
  public boolean isGetBlooth=false;
  public RNMibcsModule(ReactApplicationContext reactContext) {
    super(reactContext);
    mContext = reactContext;
  }

  @Override
  public String getName() {
    return "RNMibcs";
  }
  @Override
  public void initialize() {
    minit();

  }
  public void minit(){
    mBTAdapter = BluetoothAdapter.getDefaultAdapter();
    try {
      if (mBTAdapter != null) {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences("REACT-NATIVE", Context.MODE_PRIVATE);
        String deviceMac = sharedPreferences.getString("DEVICE-MAC", "");

        mBLEQueue = new BLEQueue(this, mContext);
        ReactContext reactContext = getReactApplicationContext();
        mDeviceEventEmitter = reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class);
        mBTManager = (BluetoothManager) reactContext.getSystemService(Context.BLUETOOTH_SERVICE);
        mBTAdapter = mBTManager.getAdapter();
        if (!mBTAdapter.isEnabled()) {
          mBTAdapter.enable();
        }
        if (!TextUtils.isEmpty(deviceMac)) {
          mBLEQueue.autoConnect(mBTAdapter, deviceMac);
        }
      }
      isGetBlooth = true;
    }catch (Exception e){
      isGetBlooth = false;
    }
  }
  @ReactMethod
  public void initAgin(){
    minit();
  }
  @ReactMethod
  public void getBloothStatus(Promise promise){
    promise.resolve(isGetBlooth);
  }
  @Override
  public void onCatalystInstanceDestroy() {
    if (mBTAdapter != null) {
      if (GATT != null) {
        closeBLEConnection();
      }
      mBLEQueue.stopScan(mBTAdapter);
      mBTAdapter = null;
      mBTManager = null;
      mDeviceEventEmitter = null;
      mBLEQueue = null;
    }

  }

  @ReactMethod
  public void calculateBodyfat(ReadableMap obj, Promise promise) {
    double weight = obj.getDouble("weight");
    double height = obj.getDouble("height");
    int age = obj.getInt("age");
    int imp = obj.getInt("imp");
    int gender = obj.getInt("gender");
    com.holtek.libHTBodyfat.HTPeopleGeneral bodyfat = new com.holtek.libHTBodyfat.HTPeopleGeneral(weight, height, gender, age, imp);
    bodyfat.calculate();
    WritableMap result = Arguments.createMap();
    result.putDouble("baseCost", bodyfat.baseCost);
    result.putDouble("bodyFat", bodyfat.bodyFat);
    result.putDouble("bone", bodyfat.bone);
    result.putDouble("muscle", bodyfat.muscle);
    result.putDouble("water", bodyfat.water);
    result.putInt("organFat", bodyfat.internalOrganFat);
    result.putArray("baseCostValues", arrayToWritableArray(bodyfat.standardCostValues));
    result.putArray("bodyFatValues", arrayToWritableArray(bodyfat.bodyFatValues));
    result.putArray("boneValues", arrayToWritableArray(bodyfat.boneValues));
    result.putArray("muscleValues", arrayToWritableArray(bodyfat.muscleValues));
    result.putArray("waterValues", arrayToWritableArray(bodyfat.waterValues));
    result.putArray("organFatValues", arrayToWritableArray(bodyfat.interalOrganFatValues));
    promise.resolve(result);
  }

  private WritableArray arrayToWritableArray(double[] array) {
    WritableArray result = Arguments.createArray();
    for (int i = 0; i < array.length; i++) {
      result.pushDouble(array[i]);
    }
    return result;
  }

  private WritableMap mapToWritableMap(Hashtable table) {

    WritableMap result = Arguments.createMap();

    return result;
  }


  @Override
  public int receiveData(byte[] data) {
    //result 用来返回得到的体重值，因为多个设备对应多个体重秤的情况下，容易出现多台设备绑定同一个体重秤的问题
    //所以用result来返回体重值给蓝牙扫描模块，这样可以过滤掉休眠中但是仍然广播的体重秤
    int result = -1;
    if (data.length >= 0xd) {
      int unit = data[0] & 0xff;
//            mWeightUnit.setText(String.format("单位:  %s", unit != 1 ? "KG" : "斤"));
      int weightH = data[0xc] & 0xff;
      int weightL = data[0xb] & 0xff;
      int weightI = weightH << 8 | weightL;
      float weightValue = (float) weightI;
      float weightUnit = unit != 1 ? 200f : 100f;
      float weight = weightValue / weightUnit;
      weightI = (int) (weight * 100);
      weight = weightI / 100.0f;
//            mWeight.setText(String.format("体重:  %.2f", weight));
      int status = data[1] & 0xff;
      boolean hasStableWeight = (status & 0x20) != 0;
      boolean isWeightRemove = (status & 0x80) != 0;
      boolean isImpStable = (status & 0x02) != 0;
//            mWeightStable.setText("是否稳定:  " + (hasStableWeight ? "是" : "否"));
//            mWeightRemove.setText("是否移除:  " + (isWeightRemove ? "是" : "否"));
//            mImpStable.setText("是否稳定:  " + (isImpStable ? "是" : "否"));
//            System.out.println(String.format("IMP_STABLE_RAW_VALUE: %02x", value[1] & 0xff));
      int impH = data[0xa] & 0xff;
      int impL = data[0x9] & 0xff;
      int imp = impH << 8 | impL;
//            mImp.setText("阻抗:  " + imp);
//            int yearH = value[3] & 0xff;
//            int yearL = value[2] & 0xff;
//            int year = yearH << 8 | yearL;
//            int month = value[4] & 0xff;
//            int day = value[5] & 0xff;
//            int hour = value[6] & 0xff;
//            int minute = value[7] & 0xff;
//            int second = value[8] & 0xff;
//            mDeviceTime.setText(String.format("%04d-%02d-%02d %02d:%02d:%02d", year, month, day, hour, minute, second));
//            if (isImpStable) {
//                HTPeopleGeneral people = new HTPeopleGeneral(weight, 170, 1, 24, imp);
//                people.calculate();
//                mBMI.setText(String.format("BMI: %.2f  %s", people.bmi, people.bmiRange.toString()));
//                mBaseCost.setText(String.format("基础代谢: %d  %s", people.baseCost, people.standardCost.toString()));
//                mInternalOrganFat.setText(String.format("内脏脂肪: %d  %s", people.internalOrganFat, people.internalOrganFatRange.toString()));
//                mBone.setText(String.format("骨量: %.2f斤  %s", people.bone * 2, people.boneRange.toString()));
//                mBodyFat.setText(String.format("体脂: %.2f  %s", people.bodyFat, people.bodyFatRange.toString()));
//                mWater.setText(String.format("水分: %.2f  %s", people.water, people.waterRange.toString()));
//                mMusle.setText(String.format("肌肉: %.2f斤  %s", people.muscle * 2, people.muscleRange.toString()));
//            }
      WritableMap event = Arguments.createMap();
      event.putBoolean("isWeightStable", hasStableWeight);
      event.putDouble("weight", weight);
      event.putBoolean("isWeightRemove", isWeightRemove);
      event.putInt("imp", imp);
      event.putBoolean("isImpStable", isImpStable);
      if (mDeviceEventEmitter == null) {
        mDeviceEventEmitter = mContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class);
      }
      mDeviceEventEmitter.emit("BLEUpdate", event);
      result= (int) weight;
    }
    return result;
  }

  public void closeBLEConnection() {
    GATT.disconnect();
    GATT.close();
    GATT = null;
  }

  @ReactMethod
  public void manualSearch() {
    mBLEQueue.search(mBTAdapter);
  }

  @ReactMethod
  public void bindMAC() {
    mBLEQueue.bindMAC();
  }
}