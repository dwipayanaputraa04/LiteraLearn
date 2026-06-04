package com.dwipayana.literalearn.Screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dwipayana.literalearn.R 
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onNavigateToNext: () -> Unit) {
    // 1. Animasi Muncul Pertama Kali
    val scale = remember { Animatable(0f) }
    val alpha = remember { Animatable(0f) }
    
    // 2. Animasi Mengambang (Floating)
    val infiniteTransition = rememberInfiniteTransition(label = "floating")
    val offsetY by infiniteTransition.animateFloat(
        initialValue = -15f,
        targetValue = 15f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offsetY"
    )

    LaunchedEffect(key1 = true) {
        // Animasi Logo membesar dengan efek membal
        scale.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
        )
        // Animasi Alpha (muncul perlahan)
        alpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800)
        )
        
        delay(2500L)
        onNavigateToNext()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF001F3F), // Biru Navy Gelap
                        Color(0xFF003366), // Biru Safir
                        Color(0xFF00509D)  // Biru Cerah
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo dengan efek mengambang (Floating)
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Logo LiteraLearn",
                modifier = Modifier
                    .size(170.dp)
                    .offset(y = offsetY.dp)
                    .scale(scale.value) 
                    .alpha(alpha.value) 
            )

            Spacer(modifier = Modifier.height(30.dp))

            // Teks Nama Aplikasi dengan efek Slide Up
            Text(
                text = "LiteraLearn",
                color = Color.White,
                fontSize = 38.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier
                    .alpha(alpha.value)
                    .offset(y = (30 * (1 - alpha.value)).dp)
            )
            
            Text(
                text = "Cerdas Bersama Sahabat",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .alpha(alpha.value)
                    .padding(top = 4.dp)
            )
        }
    }
}
