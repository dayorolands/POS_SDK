package com.appzonegroup.creditclub.pos.provider.qpos;

import java.util.Map;

public interface BlueStateListenerCallback {
    void onDeviceInfo(Map<String, String> var1);

    void onTimeout();

    void onError(int var1, String var2);

    void onReadCardData(Map var1);

    void onDeviceFound();

    void onLoadMasterKeySuccess(Boolean var1);

    void onLoadWorkKeySuccess(Boolean var1);

    void onGetMacSuccess(String var1);

    void onGetBatterySuccess(Boolean var1, String var2);

    void onSwipeCardSuccess(String var1);

    void onBluetoothIng();

    void onBluetoothConnected();

    void onBluetoothConnectedFail();

    void onBluetoothDisconnected();

    void onBluetoothPowerOff();

    void onBluetoothPowerOn();

    void onWaitingForCardSwipe();

    void onDetectIC();

    void onGoOnlineProcess(Boolean var1, int var2, String var3);

    void onUpdateRSAState(Boolean var1, int var2);

    void onAddCAPublicKeySuccess();

    void onDeleteCAPublicKeySuccess();

    void onModifyCAPublicKeySuccess();

    void onGetCAPublicKeyListSuccess(String[] var1);

    void onGetCAPublicKeyListFailure(int var1, String var2);

    void onGetCAPublicKeyParamsFailure(int var1, String var2);

    void onModifyCAPublicKeyFailure(int var1, String var2);

    void onAddCAPublicKeyFailure(int var1, String var2);

    void onDeleteCAPublicKeyFailure(int var1, String var2);

    void onGetCAPublicKeyParamsSuccess(String var1);
}
