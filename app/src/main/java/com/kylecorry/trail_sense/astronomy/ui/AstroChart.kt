package com.kylecorry.trail_sense.astronomy.ui

import android.graphics.Color
import androidx.annotation.DrawableRes
import com.kylecorry.andromeda.core.system.Resources
import com.kylecorry.andromeda.core.ui.Colors.withAlpha
import com.kylecorry.ceres.chart.Chart
import com.kylecorry.ceres.chart.data.AreaChartLayer
import com.kylecorry.ceres.chart.data.BitmapChartLayer
import com.kylecorry.ceres.chart.data.FullAreaChartLayer
import com.kylecorry.ceres.chart.data.LineChartLayer
import com.kylecorry.sol.math.Vector2
import com.kylecorry.sol.units.Reading
import com.kylecorry.trail_sense.R
import com.kylecorry.trail_sense.navigation.ui.BitmapLoader
import com.kylecorry.trail_sense.shared.colors.ColorUtils
import com.kylecorry.trail_sense.shared.views.chart.label.HourChartLabelFormatter
import java.time.Instant


class AstroChart(chart: Chart, private val onImageClick: () -> Unit) {

    // TODO: Experiment with this, if it isn't needed, remove it
    private val fillArea = true

    private var startTime = Instant.now()

    private val bitmapLoader = BitmapLoader(chart.context)

    private var previousMoonImage = R.drawable.ic_moon

    private val sunLine = LineChartLayer(
        emptyList(),
        Resources.color(chart.context, R.color.sun),
        2.5f
    )

    private val sunArea = AreaChartLayer(
        emptyList(),
        Color.TRANSPARENT,
        Resources.color(chart.context, R.color.sun).withAlpha(50),
        0f
    )

    private val moonLine = LineChartLayer(
        emptyList(),
        Resources.androidTextColorSecondary(chart.context).withAlpha(100),
        1f
    )

    private val horizon = HorizontalLineChartLayer(
        0f,
        Resources.color(chart.context, R.color.colorSecondary).withAlpha(100),
        2f
    )

    private val horizonLabel = TextChartLayer(
        chart.context.getString(R.string.horizon),
        Vector2(0f, 5f),
        Resources.androidTextColorSecondary(chart.context).withAlpha(100),
        10f,
        TextChartLayer.TextVerticalPosition.Bottom,
        TextChartLayer.TextHorizontalPosition.Right
    )

    private val night = FullAreaChartLayer(
        0f,
        -100f,
        ColorUtils.backgroundColor(chart.context).withAlpha(180)
    )

    private val imageSize = Resources.dp(chart.context, 24f)

    private val sunImage = BitmapChartLayer(
        emptyList(),
        bitmapLoader.load(R.drawable.ic_sun, imageSize.toInt()),
        16f,
    ) {
        onImageClick()
        true
    }

    private val moonImage = BitmapChartLayer(
        emptyList(),
        bitmapLoader.load(R.drawable.ic_moon, imageSize.toInt()),
        16f
    ) {
        onImageClick()
        true
    }

    init {
        chart.configureYAxis(
            labelCount = 0,
            drawGridLines = false,
            minimum = -100f,
            maximum = 100f
        )

        chart.configureXAxis(
            labelCount = 7,
            drawGridLines = false,
            labelFormatter = HourChartLabelFormatter(chart.context) { startTime }
        )

        chart.emptyText = chart.context.getString(R.string.no_data)

        chart.plot(
            listOfNotNull(
                horizon,
                horizonLabel,
                if (fillArea) sunArea else null,
                moonLine,
                sunLine,
                moonImage,
                sunImage,
                night
            )
        )
    }

    fun plot(sun: List<Reading<Float>>, moon: List<Reading<Float>>) {
        startTime = sun.firstOrNull()?.time ?: Instant.now()
        sunLine.data = Chart.getDataFromReadings(sun, startTime) { it }
        moonLine.data = Chart.getDataFromReadings(moon, startTime) { it }
        val endX = sunLine.data.lastOrNull()?.x ?: 0f
        horizonLabel.position = horizonLabel.position.copy(x = endX - 0.1f)
        updateSunArea()
    }

    fun moveSun(position: Reading<Float>?) {
        sunImage.data = if (position == null) {
            emptyList()
        } else {
            Chart.getDataFromReadings(listOf(position), startTime) { it }
        }
        updateSunArea()
    }

    fun setMoonImage(@DrawableRes icon: Int) {
        moonImage.bitmap = bitmapLoader.load(icon, imageSize.toInt())
        if (icon != previousMoonImage) {
            bitmapLoader.unload(previousMoonImage)
        }
        previousMoonImage = icon
    }

    fun moveMoon(position: Reading<Float>?) {
        moonImage.data = if (position == null) {
            emptyList()
        } else {
            Chart.getDataFromReadings(listOf(position), startTime) { it }
        }
    }

    private fun updateSunArea(){
        if (!fillArea){
            return
        }
        val position = sunImage.data.firstOrNull()
        if (position == null){
            sunArea.data = emptyList()
        } else {
            sunArea.data = sunLine.data.filter { it.x <= position.x } + position
        }
    }

}