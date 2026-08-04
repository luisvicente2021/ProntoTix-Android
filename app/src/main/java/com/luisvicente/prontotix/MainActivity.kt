package com.luisvicente.prontotix

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.luisvicente.prontotix.navigation.AppNavigation
import com.luisvicente.prontotix.ui.theme.ProntoTixTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            ProntoTixTheme {
                AppNavigation()
            }
        }
    }
}