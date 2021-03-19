package co.iqbalrizky.ccyconverter.mvp.main.view

import android.content.Intent
import android.content.SharedPreferences
import android.net.Network
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import co.iqbalrizky.ccyconverter.R
import co.iqbalrizky.ccyconverter.application.App
import co.iqbalrizky.ccyconverter.base.BaseActivity
import co.iqbalrizky.ccyconverter.components.ActivityComponent
import co.iqbalrizky.ccyconverter.databinding.ActivityMainBinding
import co.iqbalrizky.ccyconverter.mvp.main.fragments.Chart
import co.iqbalrizky.ccyconverter.mvp.main.fragments.Convert
import co.iqbalrizky.ccyconverter.mvp.main.fragments.Saved
import co.iqbalrizky.ccyconverter.mvp.main.models.CcyPair
import co.iqbalrizky.ccyconverter.mvp.main.models.CcyPair_
import co.iqbalrizky.ccyconverter.mvp.main.models.HistoryPair
import co.iqbalrizky.ccyconverter.mvp.main.models.HistoryPair_
import co.iqbalrizky.ccyconverter.mvp.main.presenter.MainPresenter
import co.iqbalrizky.ccyconverter.myutils.MyConnectivity
import co.iqbalrizky.ccyconverter.myutils.ObjectBox
import io.objectbox.Box
import io.objectbox.kotlin.boxFor
import io.objectbox.query.QueryBuilder
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

private const val TAG = "MainActivity"

class MainActivity : BaseActivity(), MainView, MyConnectivity.ConnListener {

    private lateinit var binding: ActivityMainBinding
    @Inject lateinit var msp: SharedPreferences
    @Inject lateinit var mPresenter: MainPresenter

    override fun onBindingView() {
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view: View = binding.root
        setContentView(view)
    }

    override fun onInjectDagger() {
        val activityComponent: ActivityComponent = (application as App).getMyAppComponent().getActivityComponent()
        activityComponent.injectMain(this)
        mPresenter.setView(this)
    }

    private lateinit var fm:FragmentManager
    private val fragConvert = 1
    private val fragSaved = 2
    private val fragChart = 3
    override fun onApplyFragment(fragId: Int, stack: Boolean) {
        fm = supportFragmentManager
        val fragment: Fragment = when(fragId){
            fragConvert -> Convert()
            fragSaved -> Saved()
            fragChart -> Chart()
            else -> {
                Convert()
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

    }

    override fun onShowLog(msg: String) {
        showLog(TAG, msg)
    }

    override fun onShowSnackBar(msg: String, type: Int) {
        showSnackBar(binding.root, msg, type)
    }

    private var online: Boolean = false
    override fun onViewReady(savedInstanceState: Bundle?, intent: Intent?) {
        super.onViewReady(savedInstanceState, intent)

        if (intent != null){
            if (intent.hasExtra("online")){
                online = intent.getBooleanExtra("online", false)
            }
        }

        onApplyFragment(fragConvert, false)
        toggleOnlineStatus();

        registerConnectivity()
        binding.navigation.setOnNavigationItemSelectedListener {
            val ret: Boolean = when (it.itemId) {
                R.id.nav1 -> {
                    onApplyFragment(fragConvert, false)
                    true
                }
                R.id.nav2 -> {
                    onApplyFragment(fragSaved, false)
                    true
                }
                else -> {
                    onApplyFragment(fragChart, false)
                    true
                }
            }
            ret
        }
    }

    private fun registerConnectivity() {
        val connect = MyConnectivity(this)
        connect.registerCallback(this)
    }

    override fun onConnect(network: Network?) {
        online = true
        toggleOnlineStatus()
        attemptStartOnlineProcess()
    }

    private fun attemptStartOnlineProcess() {
        val fragment: Fragment? = fm.findFragmentById(R.id.container)
        if (fragment != null){
            if (fragment is Convert){
                fragment.goesOnline()
            } else if (fragment is Chart){
                fragment.goesOnline()
            }
        }
    }

    override fun onDisconnect(network: Network?) {
        online = false
        toggleOnlineStatus()
    }

    private fun toggleOnlineStatus(){
        val state: Int = binding.motionOnline.currentState
        if (online){
            if (state == R.id.begin){
                binding.motionOnline.transitionToState(R.id.end)
            }
        } else {
            if (state == R.id.end){
                binding.motionOnline.transitionToState(R.id.begin)
            }
        }
    }

    override fun updatePairData(mapData: Map<String, Any>) {
        val cPair = manageCurrencyData(mapData)
        val fragment: Fragment? = fm.findFragmentById(R.id.container)
        if (fragment != null){
            if (fragment is Convert){
                fragment.updatePairDataUi(cPair)
            } else if (fragment is Chart){
                fragment.updatePairDataUi(cPair)
            }
        }
    }

    private val sdfToday = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
    private fun manageCurrencyData(mapData: Map<String, Any>): CcyPair {
        val timeLastUpdateUnix: Int = mapData["time_last_update_unix"] as Int
        val timeNextUpdateUnix: Int = mapData["time_next_update_unix"] as Int
        val conversionRate: Double = mapData["conversion_rate"] as Double
        val baseCode: String = mapData["base_code"] as String
        val targetCode: String = mapData["target_code"] as String
        val timeLastUpdateUtc: String = mapData["time_last_update_utc"] as String
        val timeNextUpdateUtc: String = mapData["time_next_update_utc"] as String

        // remove existing last data
        val ccyBox: Box<CcyPair> = ObjectBox.boxStore.boxFor()
        val listData: List<CcyPair> = ccyBox.query().equal(CcyPair_.baseCode, baseCode).equal(CcyPair_.targetCode, targetCode).order(CcyPair_.id, QueryBuilder.DESCENDING).build().find()
        ccyBox.remove(listData)

        // store new data
        val cPair = CcyPair()
        cPair.timeLastUpdateUnix = timeLastUpdateUnix
        cPair.timeNextUpdateUnix = timeNextUpdateUnix
        cPair.conversionRate = conversionRate
        cPair.baseCode = baseCode
        cPair.targetCode = targetCode
        cPair.timeLastUpdateUtc = timeLastUpdateUtc
        cPair.timeNextUpdateUtc = timeNextUpdateUtc
        ccyBox.put(cPair)

        //store history if today not exist
        val now: Long = System.currentTimeMillis()
        val second: Int = (now / 1000).toInt()
        val nowDate: Date = Date(now)
        val today: String = sdfToday.format(nowDate)
        val histBox: Box<HistoryPair> = ObjectBox.boxStore.boxFor()
        val todayRecord: HistoryPair? = histBox.query().equal(HistoryPair_.baseCode, baseCode).equal(HistoryPair_.targetCode, targetCode).equal(HistoryPair_.dateStrSaved, today).build().findFirst()
        if (todayRecord == null){
            //insert
            val histNew = HistoryPair()
            histNew.timeLastUpdateUnix = timeLastUpdateUnix
            histNew.timeNextUpdateUnix = timeNextUpdateUnix
            histNew.conversionRate = conversionRate
            histNew.baseCode = baseCode
            histNew.targetCode = targetCode
            histNew.timeLastUpdateUtc = timeLastUpdateUtc
            histNew.timeNextUpdateUtc = timeNextUpdateUtc
            histNew.timeSaved = second
            histNew.dateStrSaved = today

            histBox.put(histNew)
        } else {
            //update
            todayRecord.timeLastUpdateUnix = timeLastUpdateUnix
            todayRecord.timeNextUpdateUnix = timeNextUpdateUnix
            todayRecord.conversionRate = conversionRate
            todayRecord.baseCode = baseCode
            todayRecord.targetCode = targetCode
            todayRecord.timeLastUpdateUtc = timeLastUpdateUtc
            todayRecord.timeNextUpdateUtc = timeNextUpdateUtc
            todayRecord.timeSaved = second
            todayRecord.dateStrSaved = today

            histBox.put(todayRecord)
        }

        return cPair
    }

    override fun unSupportedConversion(error: String) {
        onShowSnackBar(error, -1)

    }

    fun attemptConversion(srcCcy: String, dstCcy: String) {
        val ccyBox: Box<CcyPair> = ObjectBox.boxStore.boxFor()
        val result = ccyBox.query()
            .equal(CcyPair_.baseCode, srcCcy)
            .equal(CcyPair_.targetCode, dstCcy)
            .order(CcyPair_.id, QueryBuilder.DESCENDING)
            .build()
            .findFirst()

        var needApiCall = false
        if (result != null){
            val timeNextUpdateUnix: Int? = result.timeNextUpdateUnix
            val nowUnix: Int = (System.currentTimeMillis() / 1000).toInt()
            if (timeNextUpdateUnix != null){
                if (nowUnix > timeNextUpdateUnix){
                    needApiCall = true
                }
            }
        }
        if (needApiCall){
            if (online){
                val pairUrl =  "/pair/".plus(srcCcy).plus("/").plus(dstCcy)
                mPresenter.grabApiData(pairUrl)
            } else {
                val fragment: Fragment? = fm.findFragmentById(R.id.container)
                if (fragment is Chart){
                    fragment.releaseLock()
                }
            }
        } else {
            if (result != null){
                val fragment: Fragment? = fm.findFragmentById(R.id.container)
                if (fragment != null){
                    if (fragment is Convert){
                        fragment.updatePairDataUi(result)
                    } else if (fragment is Chart){
                        fragment.updatePairDataUi(result)
                    }
                }
            }
        }
    }

    var tempSource: String = "USD"
    var tempDest: String = "IDR"
    var tempSrcAmount: String = "1"
    fun attemptRedoConversion(source: String?, dest: String?, srcAmount: String?) {
        if (source != null && dest != null && srcAmount != null){
            tempSource = source
            tempDest = dest
            tempSrcAmount = srcAmount
            binding.navigation.selectedItemId = R.id.nav1
        }
    }

    fun isOnline(): Boolean {
        return online
    }

}