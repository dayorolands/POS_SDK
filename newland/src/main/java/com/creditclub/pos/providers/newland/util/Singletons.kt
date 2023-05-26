package com.creditclub.pos.providers.newland.util

import com.newland.nsdk.core.api.internal.NSDKModuleManager
import com.newland.sdk.emvl3.api.internal.EmvL3

class Singletons {

    companion object{
        private var emvL3 : EmvL3? = null
        private var nsdkModuleManager : NSDKModuleManager? = null

        fun setEmvL3(emvL3Value: EmvL3){
            emvL3 = emvL3Value
        }

        fun getEvmL3() : EmvL3? {
            return emvL3
        }

        fun setNsdkModuleManager(moduleManager: NSDKModuleManager){
            nsdkModuleManager = moduleManager
        }

        fun getNsdkModuleManager(): NSDKModuleManager?{
            return nsdkModuleManager
        }

        fun clearEmvL3(){
            emvL3 = null
        }

        fun clearNsdkModuleManager(){
            nsdkModuleManager = null
        }
    }
}