// IKeyLoaderService.aidl
package com.wizarpos.security.injectkey.aidl;

// Declare any non-default types here with import statements

interface IKeyLoaderService {
    int importKeyInfo(in byte[] keyInfo);
    byte[] getAuthInfo();
    /**
    * reset Master Session key
    * set master key:
    * 0x38 0x38 0x38 0x38 0x38 0x38 0x38 0x38 0x38 0x38 0x38 0x38 0x38 0x38 0x38 0x38
    * 0x38 0x38 0x38 0x38 0x38 0x38 0x38 0x38 0x38 0x38 0x38 0x38 0x38 0x38 0x38 0x38
    * @param slot: Master Session key's index.
    * */
    boolean resetMasterKey(int slot);
}
