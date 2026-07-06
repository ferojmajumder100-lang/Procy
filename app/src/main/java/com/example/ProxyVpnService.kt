package com.example

import android.content.Context
import android.content.Intent
import android.net.VpnService
import android.os.ParcelFileDescriptor

class ProxyVpnService : VpnService() {
    private var vpnInterface: ParcelFileDescriptor? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val prefs = getSharedPreferences("proxy_prefs", Context.MODE_PRIVATE)
        val host = prefs.getString("host", "")
        val port = prefs.getString("port", "")

        if (!host.isNullOrEmpty() && !port.isNullOrEmpty()) {
            // Establish the VPN interface
            val builder = Builder()
            builder.setSession("ProxyConnection")
            builder.addAddress("10.0.0.2", 24)
            builder.addRoute("0.0.0.0", 0)
            vpnInterface = builder.establish()
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        vpnInterface?.close()
        vpnInterface = null
    }
}
