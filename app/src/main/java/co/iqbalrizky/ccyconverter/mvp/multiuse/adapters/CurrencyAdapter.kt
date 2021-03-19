package co.iqbalrizky.ccyconverter.mvp.multiuse.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import co.iqbalrizky.ccyconverter.R
import co.iqbalrizky.ccyconverter.myutils.Reused

class CurrencyAdapter(context: Context, list: ArrayList<String>, listener: OptionListener) : RecyclerView.Adapter<CurrencyAdapter.VH>() {

    private val option: ArrayList<String> = list
    private val mInflater: LayoutInflater = LayoutInflater.from(context)
    private val mListener: OptionListener = listener

    class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view: View = mInflater.inflate(R.layout.item_noinet, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val title:String = option[position]
        holder.tvTitle.text = title

        holder.itemView.setOnClickListener {
            mListener.onOptionSelect(position)
        }
    }

    override fun getItemCount(): Int {
        return option.size
    }

    interface OptionListener {
        fun onOptionSelect(pos: Int)
    }
}