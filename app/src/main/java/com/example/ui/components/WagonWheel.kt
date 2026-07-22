package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkGlass
import com.example.ui.theme.NeonGreen
import kotlin.math.atan2

@Composable
fun WagonWheel(
    modifier: Modifier = Modifier,
    selectedDegree: Float? = null,
    shotDegreesList: List<Float> = emptyList(),
    onDegreeSelected: (Float) -> Unit
) {
    var tappedDegree by remember { mutableStateOf(selectedDegree) }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DarkGlass),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "WAGON WHEEL (TAP FIELD AREA)",
                color = NeonGreen,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .size(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectTapGestures { offset ->
                                val center = Offset(size.width / 2f, size.height / 2f)
                                val dx = offset.x - center.x
                                val dy = offset.y - center.y
                                var angle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
                                if (angle < 0) angle += 360f
                                tappedDegree = angle
                                onDegreeSelected(angle)
                            }
                        }
                ) {
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val radius = size.width / 2f - 10f

                    // Cricket Field Boundary Circle
                    drawCircle(
                        color = Color(0xFF1B4332),
                        radius = radius
                    )
                    drawCircle(
                        color = NeonGreen.copy(alpha = 0.5f),
                        radius = radius,
                        style = Stroke(width = 3.dp.toPx())
                    )

                    // Inner Pitch Rectangle
                    drawRect(
                        color = Color(0xFFD4A373),
                        topLeft = Offset(center.x - 12f, center.y - 30f),
                        size = androidx.compose.ui.geometry.Size(24f, 60f)
                    )

                    // Sector radial lines
                    for (i in 0 until 8) {
                        val rad = Math.toRadians((i * 45).toDouble())
                        val endX = center.x + radius * Math.cos(rad).toFloat()
                        val endY = center.y + radius * Math.sin(rad).toFloat()
                        drawLine(
                            color = Color.White.copy(alpha = 0.2f),
                            start = center,
                            end = Offset(endX, endY),
                            strokeWidth = 1.dp.toPx()
                        )
                    }

                    // Draw historical shots
                    shotDegreesList.forEach { deg ->
                        val rad = Math.toRadians(deg.toDouble())
                        val endX = center.x + radius * Math.cos(rad).toFloat()
                        val endY = center.y + radius * Math.sin(rad).toFloat()
                        drawLine(
                            color = NeonGreen.copy(alpha = 0.6f),
                            start = center,
                            end = Offset(endX, endY),
                            strokeWidth = 2.dp.toPx()
                        )
                    }

                    // Draw currently selected shot
                    tappedDegree?.let { deg ->
                        val rad = Math.toRadians(deg.toDouble())
                        val endX = center.x + radius * Math.cos(rad).toFloat()
                        val endY = center.y + radius * Math.sin(rad).toFloat()
                        drawLine(
                            color = Color(0xFFFF0055),
                            start = center,
                            end = Offset(endX, endY),
                            strokeWidth = 4.dp.toPx()
                        )
                        drawCircle(
                            color = Color(0xFFFF0055),
                            radius = 6.dp.toPx(),
                            center = Offset(endX, endY)
                        )
                    }
                }
            }
        }
    }
}
