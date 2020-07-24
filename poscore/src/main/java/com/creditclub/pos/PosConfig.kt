package com.creditclub.pos

interface PosConfig {
     var apn: String
     var host: String
     var ip: String
     var port: Int
     var callHome: String
     var terminalId: String
     var supervisorPin: String
     var adminPin: String
     var remoteConnectionInfo: RemoteConnectionInfo
     var sslEnabled: Boolean
}