package com.karychel.app.ui.intro

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import kotlin.math.hypot

@Composable
fun IntroAnimation(
    onFinished: () -> Unit
) {
    val progress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 2000)
        )
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF000000)), // Negro OLED Puro
        contentAlignment = Alignment.Center
    ) {
        // Efecto de "tinta líquida" de fondo
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val radius = (progress.value * 1.2f) * hypot(w, h).toFloat() / 2f

            drawCircle(
                color = Color(0x11FFFFFF),
                radius = radius * 0.7f,
                center = Offset(w / 2f, h / 2f)
            )
            drawCircle(
                color = Color(0x22FFFFFF),
                radius = radius,
                center = Offset(w / 2f, h / 2f)
            )
        }

        // Logo KARYCHEL centrado con fade-in
        Text(
            text = "KARYCHEL",
            style = TextStyle(
                color = Color(0xFFFF0000), // Rojo Intenso
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 8.sp,
                fontFamily = FontFamily.Serif
            ),
            modifier = Modifier.alpha(0.4f + 0.6f * progress.value)
        )
    }
}
