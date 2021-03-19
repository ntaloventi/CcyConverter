package co.iqbalrizky.ccyconverter.mvp.main.fragments

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import co.iqbalrizky.ccyconverter.R
import co.iqbalrizky.ccyconverter.databinding.FragmentSavedBinding
import co.iqbalrizky.ccyconverter.mvp.main.adapters.SavedAdapter
import co.iqbalrizky.ccyconverter.mvp.main.models.CurrencySaved
import co.iqbalrizky.ccyconverter.mvp.main.models.CurrencySaved_
import co.iqbalrizky.ccyconverter.mvp.main.view.MainActivity
import co.iqbalrizky.ccyconverter.myutils.ObjectBox
import io.objectbox.Box
import io.objectbox.kotlin.boxFor
import io.objectbox.query.QueryBuilder

class Saved : Fragment(), SavedAdapter.SavedListener, TextWatcher {

    private lateinit var mContext: Context
    private lateinit var binding: FragmentSavedBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mContext = activity!!
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentSavedBinding.inflate(inflater, container, false)
        return binding.root
    }

    var adapter: SavedAdapter? = null
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val savedBox: Box<CurrencySaved> = ObjectBox.boxStore.boxFor()
        val data: List<CurrencySaved> = savedBox.query().order(CurrencySaved_.id, QueryBuilder.DESCENDING).build().find()
        binding.rvSaved.layoutManager = GridLayoutManager(mContext, 1)
        adapter = SavedAdapter(mContext, data, this)
        binding.rvSaved.adapter = adapter

        binding.ivSearch.setOnClickListener {
            val text = binding.etSearch.text.toString()
            adapter!!.filter.filter(text)
        }
        binding.etSearch.addTextChangedListener(this)
    }

    override fun onTapped(source: String?, dest: String?, srcAmount: String?) {
        (mContext as MainActivity).attemptRedoConversion(source, dest, srcAmount)
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        adapter?.filter?.filter(s)
    }

    override fun afterTextChanged(s: Editable?) {

    }
}