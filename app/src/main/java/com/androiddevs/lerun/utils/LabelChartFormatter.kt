package com.androiddevs.lerun.utils


import com.github.mikephil.charting.formatter.ValueFormatter

class LabelChartFormatter : ValueFormatter() {

    override fun getFormattedValue(value: Float): String {
        return "$value Km/h"
    }
}