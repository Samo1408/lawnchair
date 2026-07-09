package com.superlauncher.util

import android.content.Context
import java.net.Inet4Address
import java.net.NetworkInterface

object IpUtil {
    fun getDeviceIp(): String {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val ni = interfaces.nextElement()
                if (ni.isLoopback || !ni.isUp) continue
                val addrs = ni.inetAddresses
                while (addrs.hasMoreElements()) {
                    val addr = addrs.nextElement()
                    if (addr is Inet4Address && !addr.isLoopbackAddress) {
                        return "IP: ${addr.hostAddress ?: "??"}"
                    }
                }
            }
        } catch (_: Throwable) {}
        return "IP: حيز محيش"
    }
}