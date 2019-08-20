package com.codegames.farsroid.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.codegames.farsroid.R
import com.codegames.farsroid.model.App
import com.codegames.farsroid.util.toGoodDate
import com.codegames.farsroid.util.toPrettyNumber
import com.squareup.picasso.Picasso
import kotlinx.coroutines.*
import lib.codegames.extension.fanum

class AppAdapter(val appList: MutableList<App>, private val layout: Int, val coroutineScope: CoroutineScope?) :
    RecyclerView.Adapter<AppAdapter.ViewHolder>() {

    var fetchData: ((page: Int) -> Array<App>?)? = null
    var onClick: ((app: App) -> Unit)? = null

    var isPageable = false

    private var page = 0
    private var ended = false

    private var loading = false

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivIcon: ImageView? = itemView.findViewById(R.id.ia_icon)
        val tvName: TextView? = itemView.findViewById(R.id.ia_name)
        val tvSize: TextView? = itemView.findViewById(R.id.ia_size)
        val tvStaff: TextView? = itemView.findViewById(R.id.ia_staff)
        val rateBar: RatingBar? = itemView.findViewById<RatingBar>(R.id.ia_rate)?.apply {
            setIsIndicator(true)
        }

        init {

            itemView.setOnClickListener {
                onClick?.invoke(appList[adapterPosition])
            }

        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: AppAdapter.ViewHolder, position: Int) {

        val app = appList[position]

        Picasso.get()
            .load(app.imageUrl)
            .placeholder(R.drawable.placeholder)
            .into(holder.ivIcon)

        when (layout) {
            R.layout.item_app -> {
                holder.tvName?.text = app.name
                holder.tvSize?.text = app.visitCount.toPrettyNumber().fanum()
            }
            R.layout.item_app_4, R.layout.item_app_5 -> {
                holder.tvName?.text = app.name
                holder.tvSize?.text = app.likeCount.toPrettyNumber().fanum()
            }
            R.layout.item_app_horizontal -> {
                holder.tvName?.text = app.title
                holder.tvSize?.text = app.likeCount.toPrettyNumber().fanum()
                holder.tvStaff?.text = app.lastUpdate.toGoodDate().fanum()
            }
            R.layout.item_app_2, R.layout.item_app_3 -> {
                holder.tvName?.text = app.name
                holder.rateBar?.rating = (app.rate.toIntOrNull() ?: 0).toFloat()
                if (app.downloadCount.isNotBlank())
                    holder.tvSize?.text = app.downloadCount.toPrettyNumber().fanum()
                else
                    holder.tvSize?.visibility = View.GONE
            }
        }

        if (checkFetchData(position)) fetchData()

    }

    private fun checkFetchData(position: Int) = synchronized(this) {
        val condition = isPageable && !ended && !loading && position + 2 >= itemCount
        if (condition) {
            loading = true
        }
        return@synchronized loading
    }

    fun fetchData() {
        loading = true

        coroutineScope?.launch(Dispatchers.IO) {

            val newApps = fetchData?.invoke(++page)

            if (newApps == null) {
                page--
                return@launch
            } else if (newApps.isEmpty()) {
                ended = true
                synchronized(this) {
                    loading = false
                }
                return@launch
            }

            appList.addAll(newApps)

            withContext(Dispatchers.Main) {

                notifyItemRangeInserted(appList.size - newApps.size, newApps.size)

                synchronized(this) {
                    loading = false
                }

            }

        }

    }

    override fun getItemCount(): Int = appList.size

}