package ru.vahta.calculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import ru.vahta.calculator.ui.VahtaApp
import ru.vahta.calculator.ui.theme.VahtaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            VahtaTheme {
                VahtaApp()
            }
        }
    }
}
