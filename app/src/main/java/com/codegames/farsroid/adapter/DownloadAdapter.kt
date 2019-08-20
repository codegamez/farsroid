package com.codegames.farsroid.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.codegames.farsroid.R
import kotlinx.android.synthetic.main.item_category.view.*

class DownloadAdapter(private val downloadList: Array<Pair<String, String>>) :
    RecyclerView.Adapter<DownloadAdapter.ViewHolder>() {

    var onClick: ((category: Pair<String, String>) -> Unit)? = null
    var onLongClick: ((category: Pair<String, String>) -> Unit)? = null

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val button = itemView.ic_button!!.apply {

            setOnClickListener {
                val category = downloadList[adapterPosition]
                onClick?.invoke(category)
            }

            setOnLongClickListener {
                val category = downloadList[adapterPosition]
                onLongClick?.invoke(category)
                return@setOnLongClickListener onLongClick != null
            }

        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_download, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val category = downloadList[position]
        holder.button.text = category.first
    }

    override fun getItemCount(): Int = downloadList.size

}