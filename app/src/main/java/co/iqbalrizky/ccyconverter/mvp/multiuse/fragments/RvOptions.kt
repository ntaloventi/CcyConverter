package co.iqbalrizky.ccyconverter.mvp.multiuse.fragments

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import co.iqbalrizky.ccyconverter.R
import co.iqbalrizky.ccyconverter.databinding.FragmentRvOptionsBinding
import co.iqbalrizky.ccyconverter.mvp.multiuse.adapters.CurrencyAdapter
import co.iqbalrizky.ccyconverter.mvp.multiuse.adapters.NoInetAdapter
import co.iqbalrizky.ccyconverter.myutils.Reused
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class RvOptions(mode: Int, listener: OptionsListener, list: ArrayList<String>) : BottomSheetDialogFragment(),
    NoInetAdapter.OptionListener,
    CurrencyAdapter.OptionListener {

    private lateinit var mContext: Context
    private lateinit var binding: FragmentRvOptionsBinding
    private lateinit var bsDialog: BottomSheetDialog
    private val mMode: Int = mode
    private val mListener: OptionsListener = listener
    private val mListData: ArrayList<String> = list

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mContext = activity!!
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        bsDialog = BottomSheetDialog(mContext, R.style.MyBottSheet)
        return bsDialog
        //return super.onCreateDialog(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentRvOptionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (mMode == 0){
            setupOptionsOfflineExit()
        } else if (mMode == 1){
            setupOptionsCurrency()
        }
    }

    private fun setupOptionsCurrency() {
        binding.tvMessage.text = "Please Select Currency"
        binding.rvOptions.layoutManager = GridLayoutManager(mContext, 1)
        val adapter = CurrencyAdapter(mContext, mListData, this)
        binding.rvOptions.adapter = adapter
    }

    private fun setupOptionsOfflineExit() {
        binding.tvMessage.text = "Internet Connection Not Available \nPlease Select Options"
        binding.rvOptions.layoutManager = GridLayoutManager(mContext, 1)
        val adapter = NoInetAdapter(mContext, this)
        binding.rvOptions.adapter = adapter
    }

    override fun onOptionSelect(pos: Int) {
        if (mMode == 0){
            bsDialog.dismiss()
            if (pos == 0){
                mListener.onOfflineWorks()
            } else {
                mListener.onAttemptExit()
            }
        } else {
            bsDialog.dismiss()
            val selected = mListData[pos]
            mListener.onSelectedTitle(selected)
        }
    }

    interface OptionsListener {
        fun onAttemptExit()
        fun onOfflineWorks()
        fun onSelectedTitle(title: String)
    }
}