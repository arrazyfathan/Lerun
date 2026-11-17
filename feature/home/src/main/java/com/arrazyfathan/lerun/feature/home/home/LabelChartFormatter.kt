package com.arrazyfathan.lerun.feature.home.home

import com.github.mikephil.charting.formatter.ValueFormatter

class LabelChartFormatter : ValueFormatter() {
    override fun getFormattedValue(value: Float): String = "$value Km/h"
}
