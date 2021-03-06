package fr.parisrespire.fragment


import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import fr.parisrespire.R
import fr.parisrespire.fragment.data.PollutantItem
import kotlinx.android.synthetic.main.item_pollutant_details.view.*
import net.cachapa.expandablelayout.ExpandableLayout


/**
 * [RecyclerView.Adapter] that can display a [PollutantItem]
 */
class PollutionRecyclerViewAdapter(
    private val recyclerView: RecyclerView,
    private val mValues: List<PollutantItem?>,
    private val displayer: MapDisplayer
) : RecyclerView.Adapter<PollutionRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pollutant_details, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mValues[position]
        item?.let {
            Log.d("Adapter", item.toString())
            // set textviews and the slimchart
            holder.name.text = item.name
            holder.wiki.text = item.wiki
            holder.slimchart.text = item.index.toString()
            holder.slimchart.setStartAnimationDuration(1700)
            item.textColor?.let {
                holder.slimchart.textColor = it
            }
            item.index?.let {
                val mIndex = minOf(90F, it * 0.72F)
                holder.slimchart.stats = arrayOf(mIndex, 90F).toFloatArray()
            }
            item.textColor?.let {
                holder.slimchart.colors = item.colors
                holder.slimchart.playStartAnimation()
            }
            // set map button
            holder.mapButton.isEnabled = item.url != null
            holder.mapButton.setOnClickListener {
                displayer.showMap(item.url, item.name)
            }
        }
    }

    override fun getItemCount(): Int = mValues.size

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView),
        ExpandableLayout.OnExpansionUpdateListener, View.OnClickListener {
        val name: TextView = mView.name_tv
        val wiki: TextView = mView.wiki
        val expandableLayout = mView.expandable_layout
        val slimchart = mView.slimchart
        val infoButton = mView.btn_info
        val mapButton = mView.btn_map

        init {
            expandableLayout.setOnExpansionUpdateListener(this)
            infoButton.setOnClickListener(this)
        }

        override fun onExpansionUpdate(expansionFraction: Float, state: Int) {
            if (state == ExpandableLayout.State.EXPANDING) {
                recyclerView.smoothScrollToPosition(adapterPosition);
            }
        }

        override fun onClick(v: View?) {
            expandableLayout.toggle()
        }

        override fun toString(): String {
            return super.toString() + " '" + wiki.text + "'"
        }
    }
}
