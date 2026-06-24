package com.stencilla.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.stencilla.app.ui.navigation.StencillaNavGraph
import com.stencilla.app.ui.theme.StencillaTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            StencillaTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    StencillaNavGraph()
                }
            }
        }
    }
}
