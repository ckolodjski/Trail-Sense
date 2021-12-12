package com.kylecorry.trail_sense.tools.clinometer.infrastructure

import android.content.Context
import com.kylecorry.sol.math.SolMath.wrap
import com.kylecorry.sol.math.Vector3
import kotlin.math.atan2

class SideClinometer(context: Context) : Clinometer(context) {
    override fun calculateUnitAngle(gravity: Vector3): Float {
        return wrap(
            Math.toDegrees(atan2(gravity.x.toDouble(), gravity.y.toDouble())).toFloat() + 90f,
            0f,
            360f
        )
    }
}