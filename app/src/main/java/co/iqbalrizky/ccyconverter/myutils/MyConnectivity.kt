package co.iqbalrizky.ccyconverter.myutils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest


class MyConnectivity(ctx: Context) : ConnectivityManager.NetworkCallback() {

    val mContext: Context = ctx
    lateinit var mListener: ConnListener
    lateinit var cm:ConnectivityManager

    fun registerCallback(listener: ConnListener){
        mListener = listener
        cm = mContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (cm.allNetworks.isEmpty()){
            mListener.onDisconnect(null)
        }
        val request = NetworkRequest.Builder().addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET).build()
        cm.registerNetworkCallback(request, this)
    }

    fun removeCallback() {
        cm.unregisterNetworkCallback(this)
    }

    override fun onAvailable(network: Network) {
        super.onAvailable(network)
        mListener.onConnect(network)
    }

    override fun onLost(network: Network) {
        super.onLost(network)
        mListener.onDisconnect(network)
    }

    interface ConnListener {
        fun onConnect(network: Network?)
        fun onDisconnect(network: Network?)
    }
}
