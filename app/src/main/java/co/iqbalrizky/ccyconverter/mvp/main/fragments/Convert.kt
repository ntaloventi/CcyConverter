package co.iqbalrizky.ccyconverter.mvp.main.fragments

import android.content.Context
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import co.iqbalrizky.ccyconverter.R
import co.iqbalrizky.ccyconverter.databinding.FragmentConvertBinding
import co.iqbalrizky.ccyconverter.mvp.main.models.CcyPair
import co.iqbalrizky.ccyconverter.mvp.main.models.CurrencySaved
import co.iqbalrizky.ccyconverter.mvp.main.view.MainActivity
import co.iqbalrizky.ccyconverter.mvp.multiuse.fragments.RvOptions
import co.iqbalrizky.ccyconverter.myutils.ObjectBox
import co.iqbalrizky.ccyconverter.myutils.Reused
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import io.objectbox.Box
import io.objectbox.kotlin.boxFor
import java.text.DecimalFormat
import java.util.*
import kotlin.collections.ArrayList


class Convert : Fragment(), RvOptions.OptionsListener, TextWatcher, CompoundButton.OnCheckedChangeListener {

    private lateinit var mContext:Context
    private lateinit var binding: FragmentConvertBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mContext = activity!!
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentConvertBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Glide.with(mContext)
            .load(R.drawable.logo_ccy)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .transition(withCrossFade())
            .into(binding.ivIcon)

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
        binding.tvTitle.text = appName

        val caption = "Choose the currency and the amounts to get the exchange rate"
        binding.tvCaption.text = caption

        initializeStartCcy()
        binding.etCcySource.addTextChangedListener(this)

        binding.tvCcy1.setOnClickListener {
            val ccy = binding.tvCcy1.text.toString()
            showCurrencyOptions(ccy, 1)
        }
        binding.tvCcy2.setOnClickListener {
            val ccy = binding.tvCcy2.text.toString()
            showCurrencyOptions(ccy, 2)
        }

        binding.btnSwap.setOnClickListener {
            switchingColumns()
        }

        binding.swLimit.setOnCheckedChangeListener(this)

        binding.btnSave.setOnClickListener {
            attemptSaveData()
        }
    }

    private var savedBox: Box<CurrencySaved>? = null
    private fun attemptSaveData() {
        if (savedBox == null){
            savedBox = ObjectBox.boxStore.boxFor()
        }
        if (inUsePair != null){
            (mContext as MainActivity).onShowSnackBar("Berhasil Menyimpan Conversion!", 0)
            val record = CurrencySaved()
            record.timeLastUpdateUnix = inUsePair?.timeLastUpdateUnix
            record.timeNextUpdateUnix = inUsePair?.timeNextUpdateUnix
            record.conversionRate = inUsePair?.conversionRate
            record.baseCode = inUsePair?.baseCode
            record.targetCode = inUsePair?.targetCode
            record.timeLastUpdateUtc = inUsePair?.timeLastUpdateUtc
            record.timeNextUpdateUtc = inUsePair?.timeNextUpdateUtc
            record.userSrcAmount = binding.etCcySource.text.toString()
            record.userDestAmount =  binding.tvCcyDest.text.toString()
            record.userTimeSaved = (System.currentTimeMillis() / 1000).toInt()
            savedBox?.put(record)
        }
    }

    private fun switchingColumns(){
        // switching need to resest to 1
        val srcCcy = binding.tvCcy1.text.toString()
        val dstCcy = binding.tvCcy2.text.toString()
        binding.tvCcy1.text = dstCcy
        binding.tvCcy2.text = srcCcy
        binding.etCcySource.setText("1")
        attemptConvert()
    }

    var tempMode: Int = 0
    private fun showCurrencyOptions(ccy: String, mode: Int) {
        if (binding.etCcySource.isEnabled){
            tempMode = mode
            var otherCurr: String = ""
            if (mode == 1){
                otherCurr = binding.tvCcy2.text.toString()
            } else if (mode == 2){
                otherCurr = binding.tvCcy1.text.toString()
            }

            val currencies = Reused.getCurrencies()
            val filteredCcy: ArrayList<String> = ArrayList()
            for (curr in currencies){
                if (!curr.equals(ccy, true) && !curr.equals(otherCurr, true)){
                    filteredCcy.add(curr)
                }
            }

            val fm: FragmentManager = (mContext as MainActivity).supportFragmentManager
            val rvOptions = RvOptions(1, this, filteredCcy)
            rvOptions.show(fm, rvOptions.javaClass.simpleName)
        }
    }

    private fun initializeStartCcy() {
        val mSource: String = (mContext as MainActivity).tempSource
        val mDest: String = (mContext as MainActivity).tempDest
        val mAmount: String = (mContext as MainActivity).tempSrcAmount
        binding.tvCcy1.text = mSource
        binding.tvCcy2.text = mDest
        binding.etCcySource.setText(mAmount)

        attemptConvert()
    }

    override fun onSelectedTitle(title: String) {
        if (tempMode == 1){
            binding.tvCcy1.text = title
        } else if (tempMode == 2){
            binding.tvCcy2.text = title
        }
        attemptConvert()
    }

    override fun onAttemptExit() {

    }

    override fun onOfflineWorks() {

    }

    private val dec = DecimalFormat("###,###,###,###.##########")
    private var inUsePair: CcyPair? = null
    fun updatePairDataUi(cPair: CcyPair) {
        inUsePair = cPair
        val baseCode: String? = cPair.baseCode
        val targetCode: String? = cPair.targetCode
        val conversionRate: Double? = cPair.conversionRate

        Handler(Looper.getMainLooper()).post {
            val isLimit: Boolean = binding.swLimit.isChecked
            if (!isLimit){
                enableInteraction(true)
            }

            binding.tvCcy1.text = baseCode
            binding.tvCcy2.text = targetCode
            val strAmount: String = binding.etCcySource.text.toString()
            val amount = Integer.parseInt(strAmount)
            binding.tvCcyDest.text = dec.format(conversionRate?.times(amount))
            val conversion = dec.format(conversionRate)
            binding.tvRate.text = "1 ".plus(baseCode).plus(" = ").plus(conversion).plus(" ").plus(
                targetCode
            )
        }

    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

    }

    override fun afterTextChanged(s: Editable?) {
        if (s != null){
            if (s.isNotEmpty() && s.first().equals('0', true)){
                s.replace(0, 1, "")
            }

            if (s.isNotEmpty()){
                attemptCalculateDivide()
            }
        }
    }

    private fun attemptCalculateDivide(){
        val source: String = binding.etCcySource.text.toString()
        if (source.isNotEmpty()){
            val numSource: Int = Integer.parseInt(source)
            if (numSource > 0){
                if (inUsePair != null){
                    val conversion =  dec.format(numSource * inUsePair!!.conversionRate!!)
                    binding.tvCcyDest.text = conversion
                }
            }
        }
    }

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {

    }

    private var timer: CountDownTimer? = null
    private fun startCountDown(){
        enableInteraction(false)

        if (timer != null) {
            timer!!.cancel()
        }
        val timers = 5000.toLong()
        timer = object : CountDownTimer(timers, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val seconds = millisUntilFinished / 1000
                val remainTime =
                    String.format(Locale.getDefault(), "%02d", seconds / 60) + ":" + String.format(
                        Locale.getDefault(), "%02d", seconds % 60
                    )
                Handler(Looper.getMainLooper()).post {
                    binding.tvCountDown.text = remainTime
                }
            }

            override fun onFinish() {
                if (isAdded) {
                    Handler(Looper.getMainLooper()).post {
                        binding.tvCountDown.text = "00:00"
                        enableInteraction(true)
                    }
                    timer!!.cancel()
                }
            }
        }.start()
    }

    private fun enableInteraction(enable: Boolean){
        if (binding.etCcySource.hasFocus()){
            binding.etCcySource.clearFocus()
        }

        if (enable){
            binding.etCcySource.isEnabled = true
            binding.btnSwap.isEnabled = true
        } else {
            binding.etCcySource.isEnabled = false
            binding.btnSwap.isEnabled = false
        }

    }

    private fun attemptConvert() {
        val isOnline = (mContext as MainActivity).isOnline()
        if (isOnline){
            //(mContext as MainActivity).onShowSnackBar("Proccessing Conversion!", 0)
            val isLimit: Boolean = binding.swLimit.isChecked
            if (isLimit){
                startCountDown()
            } else {
                enableInteraction(false)
            }

            val srcCcy: String = binding.tvCcy1.text.toString()
            val dstCcy: String = binding.tvCcy2.text.toString()
            (mContext as MainActivity).attemptConversion(srcCcy, dstCcy)
        }
    }

    fun goesOnline() {
        attemptConvert()
    }
}