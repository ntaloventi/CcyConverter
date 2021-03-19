package co.iqbalrizky.ccyconverter.mvp.main.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import co.iqbalrizky.ccyconverter.R
import co.iqbalrizky.ccyconverter.mvp.main.models.CurrencySaved
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class SavedAdapter(context: Context, list: List<CurrencySaved>, listener: SavedListener) : RecyclerView.Adapter<SavedAdapter.VH>(), Filterable {

    private val mContext: Context = context
    private val mData: List<CurrencySaved> = list
    private var mDataFiltered: List<CurrencySaved> = list
    private val mInflater: LayoutInflater = LayoutInflater.from(context)
    private val mListener: SavedListener = listener

    class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        val tvCcySrc: TextView = itemView.findViewById(R.id.tvCcySrc)
        val tvCcyDst: TextView = itemView.findViewById(R.id.tvCcyDst)
        val tvRate: TextView = itemView.findViewById(R.id.tvRate)
        val tvTime: TextView = itemView.findViewById(R.id.tvTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view: View = mInflater.inflate(R.layout.item_saved, parent, false)
        return VH(view)
    }

    private val sdf = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault())
    private val dec = DecimalFormat("###,###,###,###.##########")
    override fun onBindViewHolder(holder: VH, position: Int) {
        //val saved: CurrencySaved = mData[position]
        val saved: CurrencySaved = mDataFiltered[position]

        holder.tvTitle.text = saved.baseCode.plus(" to ").plus(saved.targetCode)
        holder.tvCcySrc.text = saved.userSrcAmount.plus(" ").plus(saved.baseCode)
        holder.tvCcyDst.text =saved.userDestAmount.plus(" ").plus(saved.targetCode)
        holder.tvRate.text = "1 ".plus(saved.baseCode).plus(" = ").plus(dec.format(saved.conversionRate)).plus(
            " "
        ).plus(saved.targetCode)

        val date = Date(saved.userTimeSaved?.times(1000L)!!)
        holder.tvTime.text = sdf.format(date)

        val source: String? = saved.baseCode
        val dest: String? = saved.targetCode
        val sourceAmount: String? = saved.userSrcAmount
        holder.itemView.setOnClickListener {
            mListener.onTapped(source, dest, sourceAmount)
        }
    }

    override fun getItemCount(): Int {
        //return mData.size
        return mDataFiltered.size
    }

    interface SavedListener {
        fun onTapped(source: String?, dest: String?, srcAmount: String?)
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filterString = constraint.toString().toLowerCase(Locale.getDefault())
                if (filterString.isEmpty()){
                    mDataFiltered = mData
                } else {
                    val filteredList : ArrayList<CurrencySaved> = ArrayList()
                    for (item in mData){
                        val containSrcCcy = item.baseCode?.toLowerCase(Locale.getDefault())!!.contains(
                            filterString
                        )
                        val containDstCcy = item.targetCode?.toLowerCase(Locale.getDefault())!!.contains(
                            filterString
                        )
                        val date = Date(item.userTimeSaved?.times(1000L)!!)
                        val strDate = sdf.format(date)
                        val containDate = strDate.toLowerCase(Locale.getDefault()).contains(
                            filterString
                        )
                        if (containSrcCcy || containDstCcy || containDate){
                            filteredList.add(item)
                        }
                    }
                    mDataFiltered = filteredList
                }
                val results = FilterResults()
                results.values = mDataFiltered

                return results
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                mDataFiltered = results?.values as List<CurrencySaved>
                notifyDataSetChanged()
            }

        }
    }
}