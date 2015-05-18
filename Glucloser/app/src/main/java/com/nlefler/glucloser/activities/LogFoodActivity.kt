package com.nlefler.glucloser.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.nlefler.glucloser.R
import com.nlefler.glucloser.models.FoodDetailDelegate
import com.nlefler.glucloser.models.FoodParcelable
import com.nlefler.glucloser.ui.LogFoodFragment

/**
 * Created by Nathan Lefler on 5/17/15.
 */
public class LogFoodActivity: AppCompatActivity(), FoodDetailDelegate {

    override fun onCreate(savedInstanceState: Bundle?) {
        super<AppCompatActivity>.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log_food)
        val fragment = LogFoodFragment()

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().add(R.id.log_food_activity_container, fragment).commit()
        }
    }

    override fun foodDetailUpdated(foodParcelable: FoodParcelable) {
        val data = Intent()
        data.putExtra(AddFoodActivityResultFoodParcelableKey, foodParcelable)
        setResult(Activity.RESULT_OK, data)
        finish()
    }

    companion object {
        public val AddFoodActivityResultFoodParcelableKey: String = "AddFoodActivityResultFoodParcelableKey"
    }
}
