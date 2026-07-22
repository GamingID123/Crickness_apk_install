package com.example.ar

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.NeonGreen
import kotlin.math.sin

@Composable
fun ArPitchOverlay(
    modifier: Modifier = Modifier,
    decisionType: String? = null, // "OUT", "NOT OUT", "LBW HITTING", "WIDE", "NO BALL"
    showTrajectory: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "AR Pulse")
    val pulseAnim by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val trajectoryProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "trajectory"
    )

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Perspective Pitch Trapezoid Points
            val topWidth = w * 0.35f
            val bottomWidth = w * 0.85f
            val topY = h * 0.35f
            val bottomY = h * 0.85f

            val topLeft = Offset((w - topWidth) / 2, topY)
            val topRight = Offset((w + topWidth) / 2, topY)
            val bottomLeft = Offset((w - bottomWidth) / 2, bottomY)
            val bottomRight = Offset((w + bottomWidth) / 2, bottomY)

            // Draw Pitch Boundary (Neon Box)
            val pitchPath = Path().apply {
                moveTo(topLeft.x, topLeft.y)
                lineTo(topRight.x, topRight.y)
                lineTo(bottomRight.x, bottomRight.y)
                lineTo(bottomLeft.x, bottomLeft.y)
                close()
            }

            drawPath(
                path = pitchPath,
                color = NeonGreen.copy(alpha = 0.25f),
                style = androidx.compose.ui.graphics.drawscope.Fill
            )
            drawPath(
                path = pitchPath,
                color = NeonGreen,
                style = Stroke(width = 3.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f)))
            )

            // Draw Crease Lines
            val poppingCreaseY = bottomY - (bottomY - topY) * 0.18f
            val poppingLeftX = bottomLeft.x + (topLeft.x - bottomLeft.x) * 0.18f
            val poppingRightX = bottomRight.x + (topRight.x - bottomRight.x) * 0.18f

            drawLine(
                color = Color.White,
                start = Offset(poppingLeftX, poppingCreaseY),
                end = Offset(poppingRightX, poppingCreaseY),
                strokeWidth = 4.dp.toPx()
            )

            // Bowling Crease Line (Top)
            val bowlingCreaseY = topY + (bottomY - topY) * 0.15f
            val bowlingLeftX = topLeft.x + (bottomLeft.x - topLeft.x) * 0.15f
            val bowlingRightX = topRight.x + (bottomRight.x - topRight.x) * 0.15f

            drawLine(
                color = Color.White.copy(alpha = 0.7f),
                start = Offset(bowlingLeftX, bowlingCreaseY),
                end = Offset(bowlingRightX, bowlingCreaseY),
                strokeWidth = 3.dp.toPx()
            )

            // Draw Stumps at Striker End
            val stumpCenterX = w / 2f
            val stumpBaseY = poppingCreaseY - 10f
            val stumpWidth = 8.dp.toPx()
            val stumpHeight = 45.dp.toPx()

            for (i in -1..1) {
                val sx = stumpCenterX + (i * 18.dp.toPx())
                drawLine(
                    color = Color.Yellow,
                    start = Offset(sx, stumpBaseY),
                    end = Offset(sx, stumpBaseY - stumpHeight),
                    strokeWidth = stumpWidth
                )
            }
            // Bails
            drawLine(
                color = Color.Yellow,
                start = Offset(stumpCenterX - 22.dp.toPx(), stumpBaseY - stumpHeight),
                end = Offset(stumpCenterX + 22.dp.toPx(), stumpBaseY - stumpHeight),
                strokeWidth = 5.dp.toPx()
            )

            // Draw Ball Trajectory Arc
            if (showTrajectory) {
                val startPoint = Offset(w / 2f, topY)
                val impactPoint = Offset(w / 2f + 10f, topY + (bottomY - topY) * 0.65f)
                val endPoint = Offset(stumpCenterX, stumpBaseY - stumpHeight * 0.5f)

                val trajPath = Path().apply {
                    moveTo(startPoint.x, startPoint.y)
                    quadraticTo(
                        (startPoint.x + impactPoint.x) / 2,
                        startPoint.y - 40f,
                        impactPoint.x,
                        impactPoint.y
                    )
                    quadraticTo(
                        (impactPoint.x + endPoint.x) / 2,
                        impactPoint.y - 30f,
                        endPoint.x,
                        endPoint.y
                    )
                }

                drawPath(
                    path = trajPath,
                    color = Color(0xFF00E5FF),
                    style = Stroke(width = 4.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f)))
                )

                // Draw Impact Spot
                drawCircle(
                    color = Color.Red,
                    radius = 12.dp.toPx() * pulseAnim,
                    center = impactPoint
                )
                drawCircle(
                    color = Color.Yellow,
                    radius = 6.dp.toPx(),
                    center = impactPoint
                )

                // Moving ball along trajectory
                val currentX = startPoint.x + (endPoint.x - startPoint.x) * trajectoryProgress
                val currentY = startPoint.y + (endPoint.y - startPoint.y) * trajectoryProgress - sin(trajectoryProgress * Math.PI).toFloat() * 60f
                drawCircle(
                    color = NeonGreen,
                    radius = 10.dp.toPx(),
                    center = Offset(currentX, currentY)
                )
            }
        }

        // Decision Overlay Badge
        if (decisionType != null) {
            val isOut = decisionType.contains("OUT")
            val badgeBg = if (isOut) Color(0xFFFF1744) else Color(0xFF00E676)
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(16.dp)
            ) {
                androidx.compose.material3.Surface(
                    color = badgeBg.copy(alpha = 0.9f),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                    shadowElevation = 8.dp
                ) {
                    Text(
                        text = "AR UMPIRE DECISION: $decisionType",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 14.dp)
                    )
                }
            }
        }
    }
}
