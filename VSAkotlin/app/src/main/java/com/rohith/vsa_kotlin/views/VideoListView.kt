package com.rohith.vsa_kotlin.views

import android.annotation.SuppressLint
import android.content.Context
import android.view.Gravity
import android.widget.GridView
import com.rohith.vsa_kotlin.fragments.ProfilePageFragment


@SuppressLint("ViewConstructor")
class VideoListView(context: Context, mAdapter: ProfilePageFragment.VideoListAdapter) : GridView(context) {

    private val densityFactor : Float = context.resources.displayMetrics.density

    init {
        horizontalSpacing = (10 * densityFactor).toInt()
        verticalSpacing = (10 * densityFactor).toInt()
        columnWidth = (120 * densityFactor).toInt()
        stretchMode = STRETCH_COLUMN_WIDTH
        numColumns = AUTO_FIT
        gravity = Gravity.TOP or Gravity.CENTER_VERTICAL
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)

        adapter = mAdapter
    }



}