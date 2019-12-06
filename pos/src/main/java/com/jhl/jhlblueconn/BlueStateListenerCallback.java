package com.jhl.jhlblueconn;

import com.jhl.bluetooth.ibridge.BluetoothIBridgeDevice;

import java.util.ArrayList;
import java.util.Map;

public interface BlueStateListenerCallback {
    void onAddCAPublicKeyFailure(int i, String str);

    void onAddCAPublicKeySuccess();

    void onBluetoothConnected();

    void onBluetoothConnectedFail();

    void onBluetoothDisconnected();

    void onBluetoothIng();

    void onBluetoothPowerOff();

    void onBluetoothPowerOn();

    void onDeleteCAPublicKeyFailure(int i, String str);

    void onDeleteCAPublicKeySuccess();

    void onDetectIC();

    void onDeviceFound(ArrayList<BluetoothIBridgeDevice> arrayList);

    void onDeviceInfo(Map<String, String> map);

    void onError(int i, String str);

    void onGetBatterySuccess(Boolean bool, String str);

    void onGetCAPublicKeyListFailure(int i, String str);

    void onGetCAPublicKeyListSuccess(String[] strArr);

    void onGetCAPublicKeyParamsFailure(int i, String str);

    void onGetCAPublicKeyParamsSuccess(String str);

    void onGetMacSuccess(String str);

    void onGoOnlineProcess(Boolean bool, int i, String str);

    void onLoadMasterKeySuccess(Boolean bool);

    void onLoadWorkKeySuccess(Boolean bool);

    void onModifyCAPublicKeyFailure(int i, String str);

    void onModifyCAPublicKeySuccess();

    void onReadCardData(Map map);

    void onSwipeCardSuccess(String str);

    void onTimeout();

    void onUpdateRSAState(Boolean bool, int i);

    void onWaitingForCardSwipe();
}
