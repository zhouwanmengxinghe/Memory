package com.example.myapplication1.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp
import com.example.myapplication1.ui.theme.RomanticPink
import com.example.myapplication1.ui.theme.SoftPink
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun FloatingHeartsAnimation(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "hearts")
    
    val animationProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "progress"
    )
    
    Canvas(modifier = modifier.fillMaxSize()) {
        drawFloatingHearts(animationProgress)
    }
}

private fun DrawScope.drawFloatingHearts(progress: Float) {
    val heartCount = 8
    val colors = listOf(RomanticPink, SoftPink, Color(0xFFFFB6C1), Color(0xFFFFC0CB))
    
    repeat(heartCount) { index ->
        val random = Random(index)
        val startX = random.nextFloat() * size.width
        val heartSize = random.nextFloat() * 20 + 10
        
        val currentY = size.height - (progress * (size.height + heartSize))
        val currentX = startX + sin(progress * 6.28f + index) * 30
        
        if (currentY > -heartSize) {
            val color = colors[index % colors.size]
            val alpha = (1f - progress) * 0.7f
            
            drawHeart(
                centerX = currentX,
                centerY = currentY,
                size = heartSize,
                color = color.copy(alpha = alpha)
            )
        }
    }
}

private fun DrawScope.drawHeart(
    centerX: Float,
    centerY: Float,
    size: Float,
    color: Color
) {
    val path = androidx.compose.ui.graphics.Path()
    
    // 简化的心形绘制
    val heartWidth = size
    val heartHeight = size * 0.8f
    
    // 左半圆
    path.addOval(
        androidx.compose.ui.geometry.Rect(
            left = centerX - heartWidth * 0.5f,
            top = centerY - heartHeight * 0.3f,
            right = centerX,
            bottom = centerY + heartHeight * 0.2f
        )
    )
    
    // 右半圆
    path.addOval(
        androidx.compose.ui.geometry.Rect(
            left = centerX,
            top = centerY - heartHeight * 0.3f,
            right = centerX + heartWidth * 0.5f,
            bottom = centerY + heartHeight * 0.2f
        )
    )
    
    // 下三角
    path.moveTo(centerX - heartWidth * 0.5f, centerY)
    path.lineTo(centerX, centerY + heartHeight * 0.5f)
    path.lineTo(centerX + heartWidth * 0.5f, centerY)
    path.close()
    
    drawPath(path, color)
}