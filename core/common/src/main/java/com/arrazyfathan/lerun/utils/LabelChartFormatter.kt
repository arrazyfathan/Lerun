package com.arrazyfathan.lerun.utils

import com.github.mikephil.charting.formatter.ValueFormatter

class LabelChartFormatter : ValueFormatter() {

    override fun getFormattedValue(value: Float): String = "$value Km/h"
}
