package com.creditclub.pos

interface RemoteConnectionInfo {
    val id: String
    val label: String
    val key1: String
    val key2: String
    val ip: String
    val port: Int
}