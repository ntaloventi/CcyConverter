package co.iqbalrizky.ccyconverter.mvp.splash.fragments

import android.content.Context
import android.net.Network
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import co.iqbalrizky.ccyconverter.BuildConfig
import co.iqbalrizky.ccyconverter.R
import co.iqbalrizky.ccyconverter.databinding.FragmentSplashBinding
import co.iqbalrizky.ccyconverter.mvp.splash.view.SplashActivity
import co.iqbalrizky.ccyconverter.myutils.MyConnectivity

class Splash : Fragment(), MyConnectivity.ConnListener {

    private lateinit var mContext: Context;
    private lateinit var binding: FragmentSplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mContext = activity!!
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentSplashBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var appName: String = resources.getText(R.string.app_name).toString()
        val names: Array<String> = appName.split(" ").toTypedArray()
        if (names.size > 2){
            val first:String = names[0].plus(" ").plus(names[1])
            var last = "\n"
            for (i in 2 until names.size){
                last += names[i]
            }
            appName = first.plus(last)
        }
        appName += " v".plus(BuildConfig.VERSION_NAME)

        binding.tvAppName.text = appName
        binding.tvLoadMsg.text = "Loading..."

        attemptDelay()
    }

    private var connect:MyConnectivity? = null
    private fun attemptDelay() {
        Handler(Looper.getMainLooper()).postDelayed({
            connect = MyConnectivity(mContext)
            connect?.registerCallback(this)
        }, 2500)
    }

    override fun onConnect(network: Network?) {
        attemptRemoveCallback()
        (mContext as SplashActivity).goToMainActivity(true)
    }

    override fun onDisconnect(network: Network?) {
        (mContext as SplashActivity).onFailInternet()
    }

    fun attemptRemoveCallback(){
        connect?.removeCallback()
    }

}