package com.codegames.farsroid.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.codegames.farsroid.R
import kotlinx.android.synthetic.main.item_category.view.*

class CategoryAdapter(private val categoryList: Array<Pair<String, String>>, type: String): RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {

    val palette = getPalette(type)

    var onClick: ((category: Pair<String, String>) -> Unit)? = null

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

        val button = itemView.ic_button!!.apply {

            setOnClickListener {
                val category = categoryList[adapterPosition]
                onClick?.invoke(category)
            }

        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_category, parent, false)
        return ViewHolder(view)
    }

    @SuppressLint("RestrictedApi")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val category = categoryList[position]

        holder.button.text = category.first

        val colorRes = palette[position % palette.size]
        holder.button.supportBackgroundTintList =
            ContextCompat.getColorStateList(holder.itemView.context, colorRes)

    }

    override fun getItemCount(): Int = categoryList.size

    private fun getPalette(type: String): Array<Int> {

        if(type == "game") {

            return arrayOf(
                R.color.color_material_purple_500,
                R.color.color_material_purple_600,
                R.color.color_material_purple_700,
                R.color.color_material_purple_800,
                R.color.color_material_purple_900,
                R.color.color_material_deep_purple_900,
                R.color.color_material_deep_purple_800,
                R.color.color_material_deep_purple_700,
                R.color.color_material_deep_purple_600,
                R.color.color_material_deep_purple_500,
                R.color.color_material_indigo_500,
                R.color.color_material_indigo_600,
                R.color.color_material_indigo_700,
                R.color.color_material_indigo_800,
                R.color.color_material_indigo_900,
                R.color.color_material_blue_900,
                R.color.color_material_blue_800,
                R.color.color_material_blue_700,
                R.color.color_material_blue_600,
                R.color.color_material_blue_500
            )

        }

        if(type == "program") {

            return arrayOf(
                R.color.color_material_teal_500,
                R.color.color_material_teal_600,
                R.color.color_material_teal_700,
                R.color.color_material_teal_800,
                R.color.color_material_teal_900,
                R.color.color_material_green_900,
                R.color.color_material_green_800,
                R.color.color_material_green_700,
                R.color.color_material_green_600,
                R.color.color_material_green_500,
                R.color.color_material_light_green_500,
                R.color.color_material_light_green_600,
                R.color.color_material_light_green_700,
                R.color.color_material_light_green_800,
                R.color.color_material_light_green_900,
                R.color.color_material_lime_900,
                R.color.color_material_lime_800,
                R.color.color_material_lime_700,
                R.color.color_material_lime_600,
                R.color.color_material_lime_500
            )

        }

        throw Throwable("Wrong Type")
    }

}