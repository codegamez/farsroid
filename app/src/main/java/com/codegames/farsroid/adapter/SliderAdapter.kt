package com.codegames.farsroid.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.codegames.farsroid.R
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.item_slider.view.*

class SliderAdapter(val images: Array<String>): RecyclerView.Adapter<SliderAdapter.ViewHolder>() {

    var onClick: ((url: String, position: Int) -> Unit)? = null

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val ivImage = itemView.is_image!!

        init {

            ivImage.setOnClickListener {
                val url = images[adapterPosition]
                onClick?.invoke(url, adapterPosition)
            }

        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_slider, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val url = images[position]

        Picasso.get().load(url).placeholder(R.drawable.slider_placeholder).into(holder.ivImage)

    }

    override fun getItemCount(): Int = images.size

}