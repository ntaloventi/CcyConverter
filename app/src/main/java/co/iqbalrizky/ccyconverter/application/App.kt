package co.iqbalrizky.ccyconverter.application

import android.app.Application
import android.content.Context
import androidx.multidex.MultiDex
import co.iqbalrizky.ccyconverter.components.AppComponent
import co.iqbalrizky.ccyconverter.components.AppModule
import co.iqbalrizky.ccyconverter.components.DaggerAppComponent
import co.iqbalrizky.ccyconverter.myutils.ObjectBox

class App : Application() {

    var mLog:Boolean = true
    var apiUrl:String = "https://v6.exchangerate-api.com/v6/"
    lateinit var appComponent:AppComponent

    override fun onCreate() {
        super.onCreate()
        ObjectBox.init(this)
        appComponent = DaggerAppComponent.builder().appModule(AppModule(baseContext, apiUrl, mLog, "UserData")).build()
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    fun getMyAppComponent(): AppComponent {
        return appComponent
    }

}