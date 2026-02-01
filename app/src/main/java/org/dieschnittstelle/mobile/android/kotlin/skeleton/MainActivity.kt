package org.dieschnittstelle.mobile.android.kotlin.skeleton

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import org.dieschnittstelle.mobile.android.kotlin.skeleton.ui.theme.MADDemoTheme
import org.maplibre.android.MapLibre
import org.maplibre.android.WellKnownTileServer

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MADDemoTheme {
                ProjektApp()
            }
        }
        MapLibre.getInstance(
            this,
            null,
            WellKnownTileServer.MapLibre
        )
    }
}
