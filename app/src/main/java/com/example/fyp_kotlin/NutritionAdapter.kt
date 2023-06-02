package com.example.fyp_kotlin

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

class NutritionAdapter (context : Context,
                       resource : Int,
                       objects : MutableList<Nutrition>):
    ArrayAdapter<Nutrition>(context, resource, objects) {

    private var resource = resource
    private var nutritions = objects

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var v = convertView
        if (v == null ){
            val layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)
                    as LayoutInflater
            v = layoutInflater.inflate(resource, parent, false)
        }

        var textView = v!!.findViewById<TextView>(android.R.id.text1)
        textView.text = "${nutritions[position].score}"
        var textView2 = v!!.findViewById<TextView>(android.R.id.text2)
        textView2.text = "${nutritions[position].grade}"
        return v!!
    }
}