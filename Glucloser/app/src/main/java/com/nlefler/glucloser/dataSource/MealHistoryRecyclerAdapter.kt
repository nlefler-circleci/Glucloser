package com.nlefler.glucloser.dataSource

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import com.nlefler.glucloser.R
import com.nlefler.glucloser.models.Meal

/**
 * Created by Nathan Lefler on 12/25/14.
 */
public class MealHistoryRecyclerAdapter(private var meals: List<Meal>?) : RecyclerView.Adapter<MealHistoryRecyclerAdapter.ViewHolder>() {

    public class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var meal: Meal? = null
        internal var placeName: TextView
        internal var carbsValue: TextView
        internal var insulinValue: TextView
        internal var clickListener: View.OnClickListener

        init{

            this.placeName = itemView.findViewById(R.id.meal_detail_card_place_name) as TextView
            this.carbsValue = itemView.findViewById(R.id.meal_detail_card_carbs_value) as TextView
            this.insulinValue = itemView.findViewById(R.id.meal_detail_card_insulin_value) as TextView
            this.clickListener = object : View.OnClickListener {
                override fun onClick(v: View) {
                }
            }
            itemView.setOnClickListener(this.clickListener)
        }
    }

    public fun setMeals(meals: List<Meal>) {
        this.meals = meals
        notifyDataSetChanged()
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.meal_detail_card, viewGroup, false)

        // Setup view

        return ViewHolder(view)
    }

    // Replaces the contents of a view (invoked by the view holder)
    override fun onBindViewHolder(viewHolder: ViewHolder, i: Int) {
        if (i >= this.meals!!.size()) {
            return
        }

        val meal = this.meals!!.get(i)
        viewHolder.meal = meal
        viewHolder.placeName.setText(meal.place?.name ?: "")
        viewHolder.carbsValue.setText("${meal.getCarbs()}")
        viewHolder.insulinValue.setText("${meal.getInsulin()}")
    }

    override fun getItemCount(): Int {
        return this.meals!!.size()
    }
}
