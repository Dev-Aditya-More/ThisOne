
// ─────────────────────────────────────────────
// MainActivity.kt  (updated)
// ─────────────────────────────────────────────
package com.aditya1875.thisone

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.aditya1875.thisone.ui.navigation.ThisOneNavGraph
import com.aditya1875.thisone.ui.theme.ThisOneTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ThisOneTheme {
                ThisOneNavGraph()
            }
        }
    }
}
