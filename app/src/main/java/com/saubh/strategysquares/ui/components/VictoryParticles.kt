package com.saubh.strategysquares.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun VictoryParticles(
    isVisible: Boolean,
    particleColor: Color,
    modifier: Modifier = Modifier
) {
    if (!isVisible) return

    val particles = remember {
        List(30) {
            Particle(
                initialX = Random.nextFloat() * 1000,
                initialY = Random.nextFloat() * 2000,
                angle = Random.nextFloat() * 360f,
                speed = Random.nextFloat() * 500f + 200f,
                rotationSpeed = Random.nextFloat() * 360f - 180f,
                size = Random.nextFloat() * 20f + 10f
            )
        }
    }

    val animatedProgress = rememberInfiniteTransition().animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        particles.forEach { particle ->
            drawParticle(
                particle = particle,
                progress = animatedProgress.value,
                color = particleColor
            )
        }
    }
}

private fun DrawScope.drawParticle(
    particle: Particle,
    progress: Float,
    color: Color
) {
    val distance = particle.speed * progress
    val x = particle.initialX + cos(Math.toRadians(particle.angle.toDouble())).toFloat() * distance
    val y = particle.initialY + sin(Math.toRadians(particle.angle.toDouble())).toFloat() * distance
    val rotation = particle.rotationSpeed * progress

    rotate(rotation, Offset(x, y)) {
        drawCircle(
            color = color.copy(alpha = 1f - progress),
            radius = particle.size * (1f - progress * 0.5f),
            center = Offset(x, y)
        )
    }
}

private data class Particle(
    val initialX: Float,
    val initialY: Float,
    val angle: Float,
    val speed: Float,
    val rotationSpeed: Float,
    val size: Float
)
