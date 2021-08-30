package com.kylecorry.trail_sense.tools.ruler.ui

import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.kylecorry.andromeda.core.units.DistanceUnits
import com.kylecorry.trail_sense.R
import com.kylecorry.trail_sense.shared.CustomUiUtils
import com.kylecorry.trail_sense.shared.QuickActionButton
import com.kylecorry.trail_sense.shared.UserPreferences

class QuickActionRuler(btn: FloatingActionButton, fragment: Fragment, private val rulerView: ConstraintLayout): QuickActionButton(btn, fragment) {
    private lateinit var ruler: Ruler
    private val prefs by lazy { UserPreferences(context) }

    override fun onCreate() {
        button.setImageResource(R.drawable.ruler)
        ruler = Ruler(rulerView, if (prefs.distanceUnits == UserPreferences.DistanceUnits.Meters) DistanceUnits.Centimeters else DistanceUnits.Inches)
        button.setOnClickListener {
            if (ruler.visible) {
                CustomUiUtils.setButtonState(button, false)
                ruler.hide()
            } else {
                CustomUiUtils.setButtonState(button, true)
                ruler.show()
            }
        }


    }

    override fun onResume() {
        // Nothing needed here
    }

    override fun onPause() {
        // Nothing needed here
    }

    override fun onDestroy() {
        // Nothing needed here
    }
}