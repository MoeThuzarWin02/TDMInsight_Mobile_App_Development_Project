package com.example.tdminsight

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Navy = Color(0xFF07558D)
val Teal = Color(0xFF087EBB)
val Aqua = Color(0xFFBDEBFF)
val Pale = Color(0xFFF7FAFC)
val Ink = Color(0xFF17364A)
val Slate = Color(0xFF7B8994)
val Line = Color(0xFFE3EAF0)
val Mint = Color(0xFFEAF7FD)
val Mist = Color(0xFFF0F5F8)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { TdmTheme { AppNavigation() } }
    }
}

@Composable
fun TdmTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Teal,
            onPrimary = Color.White,
            secondary = Navy,
            background = Pale,
            surface = Color.White,
            surfaceVariant = Mist,
            onSurface = Ink,
            onBackground = Ink,
            outline = Line
        ),
        content = content
    )
}
