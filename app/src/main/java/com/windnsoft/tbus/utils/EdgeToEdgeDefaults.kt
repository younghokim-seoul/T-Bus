package com.windnsoft.tbus.utils

import android.graphics.Color
import androidx.activity.SystemBarStyle

object EdgeToEdgeDefaults {
    fun lightTransparent(): EdgeToEdgeConfig {
        return EdgeToEdgeConfig(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
        )
    }

    fun darkTransparent(): EdgeToEdgeConfig {
        return EdgeToEdgeConfig(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        )
    }
}
