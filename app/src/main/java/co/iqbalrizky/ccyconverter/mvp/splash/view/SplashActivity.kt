package co.iqbalrizky.ccyconverter.mvp.splash.view

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import co.iqbalrizky.ccyconverter.R
import co.iqbalrizky.ccyconverter.application.App
import co.iqbalrizky.ccyconverter.base.BaseActivity
import co.iqbalrizky.ccyconverter.components.ActivityComponent
import co.iqbalrizky.ccyconverter.databinding.ActivitySplashBinding
import co.iqbalrizky.ccyconverter.mvp.main.view.MainActivity
import co.iqbalrizky.ccyconverter.mvp.multiuse.fragments.RvOptions
import co.iqbalrizky.ccyconverter.mvp.splash.fragments.Splash
import co.iqbalrizky.ccyconverter.mvp.splash.presenter.SplashPresenter
import javax.inject.Inject


private const val TAG = "SplashActivity"

class SplashActivity : BaseActivity(), SplashView, RvOptions.OptionsListener {

    private lateinit var binding: ActivitySplashBinding
    @Inject lateinit var msp: SharedPreferences
    @Inject lateinit var mPresenter: SplashPresenter

    override fun onBindingView() {
        binding = ActivitySplashBinding.inflate(layoutInflater)
        val view: View = binding.root
        setContentView(view)
    }

    override fun onInjectDagger() {
        val activityComponent:ActivityComponent = (application as App).getMyAppComponent().getActivityComponent()
        activityComponent.injectSplash(this)
        mPresenter.setView(this)
    }

    private lateinit var fm: FragmentManager
    private val fragSplash = 1
    override fun onApplyFragment(fragId: Int, stack: Boolean) {
        fm = supportFragmentManager
        val fragment: Fragment = when(fragId){
            fragSplash -> Splash()
            else -> {
                Splash()
            }
        }

        val transaction: FragmentTransaction = fm.beginTransaction()
        if (stack){
            transaction.replace(R.id.container, fragment)
                .addToBackStack(TAG + fragId)
                .commitAllowingStateLoss()
        } else {
            for (i in 0 until fm.backStackEntryCount) {
                fm.popBackStack()
            }
            transaction.replace(R.id.container, fragment).commitAllowingStateLoss()
        }
    }

    override fun onShowKeyboard(view: View) {
        showKeyboard(view)
    }

    override fun onHideKeyboard(view: View) {
        hideKeyboard(view)
    }

    override fun onFailInternet() {
        val list: ArrayList<String> = ArrayList()
        val fragment = RvOptions(0, this, list)
        fragment.isCancelable = false
        fragment.show(fm, fragment.javaClass.simpleName)
    }

    override fun onShowLog(msg: String) {
        showLog(TAG, msg)
    }

    override fun onShowSnackBar(msg: String, type: Int) {
        showSnackBar(binding.root, msg, type)
    }

    override fun onViewReady(savedInstanceState: Bundle?, intent: Intent?) {
        super.onViewReady(savedInstanceState, intent)

        onApplyFragment(fragSplash, false)
    }

    override fun onDestroy() {
        super.onDestroy()

        val fragment: Fragment? = fm.findFragmentById(R.id.container)
        if (fragment != null){
            if (fragment is Splash){
                fragment.attemptRemoveCallback()
            }
        }
    }

    override fun onAttemptExit() {
        finish()
    }

    override fun onOfflineWorks() {
        goToMainActivity(false)
    }

    override fun onSelectedTitle(title: String) {

    }

    fun goToMainActivity(online: Boolean){
        val fragment: Fragment? = fm.findFragmentById(R.id.container)
        if (fragment != null){
            if (fragment is Splash){
                fragment.attemptRemoveCallback()
            }
        }

        val main = Intent(this, MainActivity::class.java)
        main.putExtra("online", online)
        startActivity(main)
        finish()
    }

}