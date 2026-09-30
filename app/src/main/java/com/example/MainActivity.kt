package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.ui.MainAppNavigation
import com.example.ui.theme.FocusFitTheme
import com.example.ui.viewmodel.FocusFitViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: FocusFitViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FocusFitTheme {
                MainAppNavigation(viewModel = viewModel)
            }
        }
    }
}
