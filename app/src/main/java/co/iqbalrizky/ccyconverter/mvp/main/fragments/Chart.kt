package co.iqbalrizky.ccyconverter.mvp.main.fragments

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import co.iqbalrizky.ccyconverter.R
import co.iqbalrizky.ccyconverter.databinding.FragmentChartBinding
import co.iqbalrizky.ccyconverter.mvp.main.models.CcyPair
import co.iqbalrizky.ccyconverter.mvp.main.view.MainActivity
import co.iqbalrizky.ccyconverter.mvp.multiuse.fragments.RvOptions
import co.iqbalrizky.ccyconverter.myutils.Reused
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import java.text.DecimalFormat
import java.util.concurrent.TimeUnit

class Chart : Fragment(), RvOptions.OptionsListener {

    private lateinit var mContext: Context
    private lateinit var binding: FragmentChartBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mContext = activity!!
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentChartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvTitle.text = "Conversion Rate Chart"
        initializeStartCcy()
        testDrawLineChart()

        binding.tvCcy1.setOnClickListener {
            val ccy = binding.tvCcy1.text.toString()
            showCurrencyOptions(ccy, 1)
        }
        binding.tvCcy2.setOnClickListener {
            val ccy = binding.tvCcy2.text.toString()
            showCurrencyOptions(ccy, 2)
        }
    }

    var tempMode: Int = 0
    private fun showCurrencyOptions(ccy: String, mode: Int) {
        if (allowChange){
            tempMode = mode
            var otherCurr = ""
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
        binding.tvCcy1.text = mSource
        binding.tvCcy2.text = mDest

        attemptConvert()
    }

    private var allowChange: Boolean = true
    private fun attemptConvert() {
        allowChange = false

        val srcCcy: String = binding.tvCcy1.text.toString()
        val dstCcy: String = binding.tvCcy2.text.toString()
        (mContext as MainActivity).attemptConversion(srcCcy, dstCcy)
    }

    override fun onAttemptExit() {

    }

    override fun onOfflineWorks() {

    }

    override fun onSelectedTitle(title: String) {
        if (tempMode == 1){
            binding.tvCcy1.text = title
        } else if (tempMode == 2){
            binding.tvCcy2.text = title
        }
        attemptConvert()
    }

    private fun testDrawLineChart() {
        val entries: MutableList<Entry> = ArrayList()
        for (i in 0..9){
            val pos: Float = i * 1f
            val amount: Float = (i * 2f)
            entries.add(Entry(pos, amount))
        }

        val dataSet = LineDataSet(entries, "Customized values")
        dataSet.color = ContextCompat.getColor(context!!, R.color.main_dark)
        dataSet.valueTextColor = ContextCompat.getColor(context!!, R.color.main_dark)

        val xAxis = binding.lineChartView.xAxis
        xAxis.textColor = ContextCompat.getColor(context!!, R.color.main_dark)
        xAxis.position = XAxis.XAxisPosition.BOTTOM

        val yAxisRight = binding.lineChartView.axisRight
        yAxisRight.isEnabled = false

        val yAxisLeft = binding.lineChartView.axisLeft
        yAxisLeft.granularity = 1f
        yAxisLeft.textColor = ContextCompat.getColor(context!!, R.color.main_dark)

        val data = LineData(dataSet)
        binding.lineChartView.data = data
        binding.lineChartView.invalidate()

    }

    private val dec = DecimalFormat("###,###,###,###.##########")
    fun updatePairDataUi(cPair: CcyPair) {
        allowChange = true
        val baseCode: String? = cPair.baseCode
        val targetCode: String? = cPair.targetCode
        val conversionRate: Double? = cPair.conversionRate
        val timeLastUpdateUnix: Int? = cPair.timeLastUpdateUnix
        val now = System.currentTimeMillis()
        val rest = now - timeLastUpdateUnix?.times(1000L)!!

        val days = TimeUnit.MILLISECONDS.toDays(rest)
        val hours = TimeUnit.MILLISECONDS.toHours(rest)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(rest)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(rest)
        val conversion =  dec.format(conversionRate)

        Handler(Looper.getMainLooper()).post {
            binding.tvCcy1.text = baseCode
            binding.tvCcy2.text = targetCode
            binding.tvRate.text = "1 ".plus(baseCode).plus(" = ").plus(conversion).plus(" ").plus(
                targetCode
            )
            when {
                seconds < 60 -> {
                    binding.tvMsgTime.text = "Last Updated ".plus(seconds).plus(" seconds ago")
                }
                minutes < 60 -> {
                    binding.tvMsgTime.text = "Last Updated ".plus(minutes).plus(" minutes ago")
                }
                hours < 60 -> {
                    binding.tvMsgTime.text = "Last Updated ".plus(hours).plus(" minutes ago")
                }
                else -> {
                    binding.tvMsgTime.text = "Last Updated ".plus(days).plus(" days ago")
                }
            }
        }
    }

    fun goesOnline() {
        attemptConvert()
    }

    fun releaseLock() {
        allowChange = true
    }
}