package co.iqbalrizky.ccyconverter.components

import co.iqbalrizky.ccyconverter.mvp.main.view.MainActivity
import co.iqbalrizky.ccyconverter.mvp.splash.view.SplashActivity
import dagger.Subcomponent

@Subcomponent
interface ActivityComponent {
    fun injectSplash(splashActivity: SplashActivity)
    fun injectMain(mainActivity: MainActivity)
}