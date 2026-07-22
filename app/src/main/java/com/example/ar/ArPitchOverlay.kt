package com.example.ar

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.*
import com.example.ui.theme.DarkGlass
import com.example.ui.theme.NeonGreen
import kotlin.math.abs

@Composable
fun ArPitchOverlay(
    modifier: Modifier = Modifier,
    drsResult: DrsDecisionResult,
    physicsInput: DeliveryPhysicsInput,
    onPitchTap: ((xFrac: Float, yOffsetMeters: Float) -> Unit)? = null
) {
    val infiniteTransition = rememberInfiniteTransition(label = "AR Pulse")
    val pulseAnim by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val trajectoryProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "trajectory"
    )

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .clickable {
                    // Tap on pitch canvas to manually relocate bounce / pitch spot
                    onPitchTap?.invoke(0.65f, 0.0f)
                }
        ) {
            val w = size.width
            val h = size.height

            // 1. Define Perspective Pitch Canvas Geometry
            val topWidth = w * 0.38f
            val bottomWidth = w * 0.88f
            val topY = h * 0.32f
            val bottomY = h * 0.82f

            val topLeft = Offset((w - topWidth) / 2, topY)
            val topRight = Offset((w + topWidth) / 2, topY)
            val bottomLeft = Offset((w - bottomWidth) / 2, bottomY)
            val bottomRight = Offset((w + bottomWidth) / 2, bottomY)

            // Draw Pitch Surface (Perspective Trapezoid)
            val pitchPath = Path().apply {
                moveTo(topLeft.x, topLeft.y)
                lineTo(topRight.x, topRight.y)
                lineTo(bottomRight.x, bottomRight.y)
                lineTo(bottomLeft.x, bottomLeft.y)
                close()
            }

            drawPath(
                path = pitchPath,
                color = Color(0xFF1B5E20).copy(alpha = 0.30f),
                style = androidx.compose.ui.graphics.drawscope.Fill
            )
            drawPath(
                path = pitchPath,
                color = NeonGreen.copy(alpha = 0.8f),
                style = Stroke(width = 3.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 12f)))
            )

            // 2. Crease Lines & Stumps Corridor Grid
            val poppingCreaseY = bottomY - (bottomY - topY) * 0.12f
            val poppingLeftX = bottomLeft.x + (topLeft.x - bottomLeft.x) * 0.12f
            val poppingRightX = bottomRight.x + (topRight.x - bottomRight.x) * 0.12f

            drawLine(
                color = Color.White,
                start = Offset(poppingLeftX, poppingCreaseY),
                end = Offset(poppingRightX, poppingCreaseY),
                strokeWidth = 3.5f.dp.toPx()
            )

            // 3. Mathematical 3D-to-2D Projection Function
            fun project3DTo2D(xMeters: Float, yMeters: Float, zMeters: Float): Offset {
                val u = (xMeters / BallTrackerPhysicsEngine.PITCH_LENGTH_METERS).coerceIn(0f, 1f)
                val curPitchY = topY + u * (bottomY - topY)
                val curWidth = topWidth + u * (bottomWidth - topWidth)

                val screenX = (w / 2f) + (yMeters / 1.5f) * (curWidth / 2f)
                val heightScale = (0.25f + u * 0.75f) * 160.dp.toPx()
                val screenY = curPitchY - zMeters * heightScale

                return Offset(screenX, screenY)
            }

            // 4. Draw Trajectory Segments
            val trajectoryPoints = drsResult.trajectoryPoints
            if (trajectoryPoints.size > 1) {
                val flightPath = Path()
                val bounceToImpactPath = Path()
                val projectedPath = Path()

                var startedFlight = false
                var startedPostBounce = false
                var startedProjected = false

                trajectoryPoints.forEach { pt ->
                    val screenPt = project3DTo2D(pt.xMeters, pt.yMeters, pt.zMeters)
                    when (pt.phase) {
                        TrajectoryPhase.FLIGHT_TO_PITCH -> {
                            if (!startedFlight) {
                                flightPath.moveTo(screenPt.x, screenPt.y)
                                startedFlight = true
                            } else {
                                flightPath.lineTo(screenPt.x, screenPt.y)
                            }
                        }
                        TrajectoryPhase.POST_BOUNCE_TO_IMPACT -> {
                            if (!startedPostBounce) {
                                bounceToImpactPath.moveTo(screenPt.x, screenPt.y)
                                startedPostBounce = true
                            } else {
                                bounceToImpactPath.lineTo(screenPt.x, screenPt.y)
                            }
                        }
                        TrajectoryPhase.PROJECTED_TO_STUMPS -> {
                            if (!startedProjected) {
                                projectedPath.moveTo(screenPt.x, screenPt.y)
                                startedProjected = true
                            } else {
                                projectedPath.lineTo(screenPt.x, screenPt.y)
                            }
                        }
                    }
                }

                // Phase 1: Cyan Flight Arc
                drawPath(
                    path = flightPath,
                    color = Color(0xFF00E5FF),
                    style = Stroke(width = 4.dp.toPx())
                )

                // Phase 2: Yellow Post-Bounce Arc
                drawPath(
                    path = bounceToImpactPath,
                    color = Color.Yellow,
                    style = Stroke(width = 4.dp.toPx())
                )

                // Phase 3: Red Dotted Projected Path to Stumps
                drawPath(
                    path = projectedPath,
                    color = if (drsResult.finalVerdict.isOut) Color(0xFFFF1744) else NeonGreen,
                    style = Stroke(width = 4.5f.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 12f)))
                )
            }

            // 5. Draw Pitch Bounce Spot (Impact Ring)
            val bounceScreen = project3DTo2D(drsResult.bouncePoint.xMeters, drsResult.bouncePoint.yMeters, 0f)
            val pitchColor = when (drsResult.pitchingZone) {
                PitchingZone.IN_LINE -> NeonGreen
                PitchingZone.OUTSIDE_OFF -> Color(0xFF00E5FF)
                PitchingZone.OUTSIDE_LEG -> Color(0xFFFF1744)
            }
            drawCircle(color = pitchColor.copy(alpha = 0.4f), radius = 18.dp.toPx() * pulseAnim, center = bounceScreen)
            drawCircle(color = pitchColor, radius = 8.dp.toPx(), center = bounceScreen)

            // 6. Draw Pad Impact Spot
            val padScreen = project3DTo2D(drsResult.impactPoint.xMeters, drsResult.impactPoint.yMeters, drsResult.impactPoint.zMeters)
            val impactColor = when (drsResult.impactZone) {
                ImpactZone.IN_LINE -> NeonGreen
                else -> Color(0xFFFF9100)
            }
            drawCircle(color = impactColor.copy(alpha = 0.4f), radius = 16.dp.toPx() * pulseAnim, center = padScreen)
            drawCircle(color = impactColor, radius = 7.dp.toPx(), center = padScreen)

            // 7. Draw Stumps & Bails at Striker End
            val stumpsBaseScreen = project3DTo2D(BallTrackerPhysicsEngine.PITCH_LENGTH_METERS, 0f, 0f)
            val stumpTopScreen = project3DTo2D(BallTrackerPhysicsEngine.PITCH_LENGTH_METERS, 0f, BallTrackerPhysicsEngine.BAIL_HEIGHT_METERS)
            val stumpHeightPx = abs(stumpsBaseScreen.y - stumpTopScreen.y)

            val stumpSpacing = 16.dp.toPx()
            for (i in -1..1) {
                val sx = stumpsBaseScreen.x + (i * stumpSpacing)
                drawLine(
                    color = Color.Yellow,
                    start = Offset(sx, stumpsBaseScreen.y),
                    end = Offset(sx, stumpsBaseScreen.y - stumpHeightPx),
                    strokeWidth = 7.dp.toPx()
                )
            }
            // Bail Line
            drawLine(
                color = Color.Yellow,
                start = Offset(stumpsBaseScreen.x - stumpSpacing * 1.2f, stumpsBaseScreen.y - stumpHeightPx),
                end = Offset(stumpsBaseScreen.x + stumpSpacing * 1.2f, stumpsBaseScreen.y - stumpHeightPx),
                strokeWidth = 5.dp.toPx()
            )

            // 8. Projected Ball Arrival Spot at Stumps Plane
            val stumpBallScreen = project3DTo2D(
                BallTrackerPhysicsEngine.PITCH_LENGTH_METERS,
                drsResult.stumpsPoint.yMeters,
                drsResult.stumpsPoint.zMeters
            )
            drawCircle(
                color = if (drsResult.finalVerdict.isOut) Color(0xFFFF1744) else NeonGreen,
                radius = 11.dp.toPx() * pulseAnim,
                center = stumpBallScreen
            )

            // 9. Moving Ball along Trajectory Progress
            val currentPtIndex = (trajectoryProgress * (trajectoryPoints.size - 1)).toInt().coerceIn(0, trajectoryPoints.size - 1)
            val activePt = trajectoryPoints[currentPtIndex]
            val ballScreenPos = project3DTo2D(activePt.xMeters, activePt.yMeters, activePt.zMeters)
            drawCircle(
                color = Color.White,
                radius = 10.dp.toPx(),
                center = ballScreenPos
            )
        }

        // Top DRS Decision Banner Overlay
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 70.dp, start = 16.dp, end = 16.dp)
        ) {
            Surface(
                color = if (drsResult.finalVerdict.isOut) Color(0xFFFF1744).copy(alpha = 0.92f) else Color(0xFF00E676).copy(alpha = 0.92f),
                shape = RoundedCornerShape(16.dp),
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "HAWK-EYE DRS: ${drsResult.finalVerdict.title}",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}
