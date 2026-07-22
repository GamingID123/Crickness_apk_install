package com.example.engine

import kotlin.math.*

enum class PitchingZone { IN_LINE, OUTSIDE_OFF, OUTSIDE_LEG }
enum class ImpactZone { IN_LINE, OUTSIDE_OFF, OUTSIDE_LEG }
enum class StumpHitStatus { HITTING, MISSING_HIGH, MISSING_OFF, MISSING_LEG, UMPIRES_CALL }
enum class DrsVerdict(val title: String, val isOut: Boolean) {
    OUT_LBW("OUT (LBW)", true),
    NOT_OUT_PITCHING_OUTSIDE_LEG("NOT OUT (Pitching Outside Leg)", false),
    NOT_OUT_IMPACT_OUTSIDE_OFF("NOT OUT (Impact Outside Off)", false),
    NOT_OUT_MISSING_STUMPS("NOT OUT (Wickets Missing)", false),
    UMPIRES_CALL("UMPIRE'S CALL", false)
}

enum class TrajectoryPhase { FLIGHT_TO_PITCH, POST_BOUNCE_TO_IMPACT, PROJECTED_TO_STUMPS }

data class TrajectoryPoint(
    val xMeters: Float, // Distance along pitch: 0m (release) to 20.12m (stumps)
    val yMeters: Float, // Lateral offset from center line (-1.5m to +1.5m)
    val zMeters: Float, // Height above ground in meters (0m to 2.5m)
    val timeSeconds: Float,
    val phase: TrajectoryPhase
)

data class DeliveryPhysicsInput(
    val releaseSpeedKph: Float = 132.0f,     // Speed in km/h (e.g., 100 to 150)
    val pitchFraction: Float = 0.65f,         // 0.2 = Full, 0.65 = Good length, 0.85 = Short
    val swingDegrees: Float = 1.2f,           // Positive = In-swing, Negative = Out-swing
    val spinDegrees: Float = 0.0f,            // Off-break / Leg-break turn angle post bounce
    val impactDistanceMeters: Float = 1.8f,  // Distance of pad impact in front of stumps (e.g. 1.2m to 2.5m)
    val padLateralOffsetMeters: Float = 0.02f,// Lateral offset of pad from center stump
    val releaseHeightMeters: Float = 1.95f,   // Bowler height release point
    val isRightHandedBatsman: Boolean = true,
    val shotOffered: Boolean = true,
    val ballTypeRestitution: Float = 0.62f    // Bounce coefficient (Tennis: 0.72, Leather: 0.58)
)

data class DrsDecisionResult(
    val pitchingZone: PitchingZone,
    val impactZone: ImpactZone,
    val stumpHitStatus: StumpHitStatus,
    val finalVerdict: DrsVerdict,
    val heightAtStumpsCm: Float,
    val lateralOffsetAtStumpsCm: Float,
    val speedAtPitchKph: Float,
    val bouncePoint: TrajectoryPoint,
    val impactPoint: TrajectoryPoint,
    val stumpsPoint: TrajectoryPoint,
    val trajectoryPoints: List<TrajectoryPoint>,
    val scientificDetails: String
)

object BallTrackerPhysicsEngine {
    // Standard ICC / Hawk-Eye Metric Dimensions
    const val PITCH_LENGTH_METERS = 20.12f       // 22 Yards
    const val STUMP_WIDTH_METERS = 0.2286f       // 9 Inches total stump set
    const val HALF_STUMP_WIDTH = STUMP_WIDTH_METERS / 2f
    const val BAIL_HEIGHT_METERS = 0.737f        // 29 Inches to top of bails
    const val GRAVITY = 9.81f                    // m/s^2
    const val BALL_RADIUS_METERS = 0.036f        // Standard cricket ball radius

    fun calculateDeliveryTrajectory(input: DeliveryPhysicsInput): DrsDecisionResult {
        val speedMs = (input.releaseSpeedKph / 3.6f).coerceIn(15f, 45f) // m/s
        val pitchX = (PITCH_LENGTH_METERS * input.pitchFraction.coerceIn(0.2f, 0.88f))

        // Phase 1: Flight to pitch (0 to pitchX)
        val timeToPitch = pitchX / speedMs
        val releaseY = -0.15f // Slightly wide of bowler stumps release
        val swingRad = Math.toRadians(input.swingDegrees.toDouble()).toFloat()

        val points = mutableListOf<TrajectoryPoint>()
        val dt = 0.015f // 15ms step resolution
        var t = 0f

        var currentX = 0f
        var currentY = releaseY
        var currentZ = input.releaseHeightMeters

        // Initial vertical velocity calculated to bounce exactly at pitchX
        val vzInitial = (0f - input.releaseHeightMeters + 0.5f * GRAVITY * timeToPitch * timeToPitch) / timeToPitch
        var vz = vzInitial
        val vyInitial = speedMs * tan(swingRad.toDouble()).toFloat()
        var vy = vyInitial

        // Simulate Phase 1: Release to Bounce
        while (currentX < pitchX && currentZ > 0f) {
            points.add(
                TrajectoryPoint(
                    xMeters = currentX,
                    yMeters = currentY,
                    zMeters = currentZ.coerceAtLeast(0f),
                    timeSeconds = t,
                    phase = TrajectoryPhase.FLIGHT_TO_PITCH
                )
            )
            t += dt
            currentX += speedMs * dt
            currentY += vy * dt
            vz -= GRAVITY * dt
            currentZ += vz * dt
        }

        val bouncePoint = TrajectoryPoint(
            xMeters = pitchX,
            yMeters = currentY,
            zMeters = 0f,
            timeSeconds = t,
            phase = TrajectoryPhase.FLIGHT_TO_PITCH
        )
        points.add(bouncePoint)

        // Bounce Dynamics (Energy Loss & Spin Coefficient)
        var vzPost = -vz * input.ballTypeRestitution.coerceIn(0.4f, 0.8f)
        val spinRad = Math.toRadians(input.spinDegrees.toDouble()).toFloat()
        var vyPost = vy + speedMs * tan(spinRad.toDouble()).toFloat()
        val speedPostPitchKph = (speedMs * 0.85f) * 3.6f

        // Phase 2: Bounce to Impact Point (pitchX to impactX)
        val impactX = (PITCH_LENGTH_METERS - input.impactDistanceMeters.coerceIn(0.5f, 3.5f)).coerceAtLeast(pitchX + 0.5f)

        while (currentX < impactX) {
            points.add(
                TrajectoryPoint(
                    xMeters = currentX,
                    yMeters = currentY,
                    zMeters = currentZ.coerceAtLeast(0f),
                    timeSeconds = t,
                    phase = TrajectoryPhase.POST_BOUNCE_TO_IMPACT
                )
            )
            t += dt
            currentX += (speedMs * 0.85f) * dt
            currentY += vyPost * dt
            vzPost -= GRAVITY * dt
            currentZ += vzPost * dt
        }

        val impactPoint = TrajectoryPoint(
            xMeters = impactX,
            yMeters = input.padLateralOffsetMeters,
            zMeters = currentZ.coerceAtLeast(0.1f),
            timeSeconds = t,
            phase = TrajectoryPhase.POST_BOUNCE_TO_IMPACT
        )
        points.add(impactPoint)

        // Phase 3: Projected Trajectory to Stumps (impactX to PITCH_LENGTH_METERS)
        while (currentX <= PITCH_LENGTH_METERS) {
            points.add(
                TrajectoryPoint(
                    xMeters = currentX,
                    yMeters = currentY,
                    zMeters = currentZ.coerceAtLeast(0f),
                    timeSeconds = t,
                    phase = TrajectoryPhase.PROJECTED_TO_STUMPS
                )
            )
            t += dt
            currentX += (speedMs * 0.8f) * dt
            currentY += vyPost * dt
            vzPost -= GRAVITY * dt
            currentZ += vzPost * dt
        }

        val stumpsArrivalPoint = TrajectoryPoint(
            xMeters = PITCH_LENGTH_METERS,
            yMeters = currentY,
            zMeters = currentZ,
            timeSeconds = t,
            phase = TrajectoryPhase.PROJECTED_TO_STUMPS
        )
        points.add(stumpsArrivalPoint)

        // Evaluate Hawk-Eye Hawk Eye Scientific LBW Rules
        val offSideRight = input.isRightHandedBatsman

        // 1. Pitching Zone
        val pitchingZone = when {
            bouncePoint.yMeters > HALF_STUMP_WIDTH -> if (offSideRight) PitchingZone.OUTSIDE_OFF else PitchingZone.OUTSIDE_LEG
            bouncePoint.yMeters < -HALF_STUMP_WIDTH -> if (offSideRight) PitchingZone.OUTSIDE_LEG else PitchingZone.OUTSIDE_OFF
            else -> PitchingZone.IN_LINE
        }

        // 2. Impact Zone
        val impactZone = when {
            impactPoint.yMeters > HALF_STUMP_WIDTH -> if (offSideRight) ImpactZone.OUTSIDE_OFF else ImpactZone.OUTSIDE_LEG
            impactPoint.yMeters < -HALF_STUMP_WIDTH -> if (offSideRight) ImpactZone.OUTSIDE_LEG else ImpactZone.OUTSIDE_OFF
            else -> ImpactZone.IN_LINE
        }

        // 3. Stump Hit Status
        val stumpZCm = stumpsArrivalPoint.zMeters * 100f
        val stumpYCm = stumpsArrivalPoint.yMeters * 100f

        val stumpHitStatus = when {
            stumpsArrivalPoint.zMeters > BAIL_HEIGHT_METERS + 0.08f -> StumpHitStatus.MISSING_HIGH
            stumpsArrivalPoint.zMeters > BAIL_HEIGHT_METERS -> StumpHitStatus.UMPIRES_CALL
            stumpsArrivalPoint.zMeters < 0.02f -> StumpHitStatus.MISSING_HIGH // Ground hit
            abs(stumpsArrivalPoint.yMeters) > HALF_STUMP_WIDTH + 0.08f -> {
                if (stumpsArrivalPoint.yMeters > 0) StumpHitStatus.MISSING_OFF else StumpHitStatus.MISSING_LEG
            }
            abs(stumpsArrivalPoint.yMeters) > HALF_STUMP_WIDTH -> StumpHitStatus.UMPIRES_CALL
            else -> StumpHitStatus.HITTING
        }

        // Final Hawk-Eye DRS Verdict Calculation
        val finalVerdict = when {
            pitchingZone == PitchingZone.OUTSIDE_LEG -> DrsVerdict.NOT_OUT_PITCHING_OUTSIDE_LEG
            impactZone == ImpactZone.OUTSIDE_OFF && input.shotOffered -> DrsVerdict.NOT_OUT_IMPACT_OUTSIDE_OFF
            stumpHitStatus == StumpHitStatus.MISSING_HIGH || stumpHitStatus == StumpHitStatus.MISSING_OFF || stumpHitStatus == StumpHitStatus.MISSING_LEG -> DrsVerdict.NOT_OUT_MISSING_STUMPS
            stumpHitStatus == StumpHitStatus.UMPIRES_CALL -> DrsVerdict.UMPIRES_CALL
            else -> DrsVerdict.OUT_LBW
        }

        val details = buildString {
            append("• Release Speed: %.1f km/h\n".format(input.releaseSpeedKph))
            append("• Speed Post-Pitch: %.1f km/h\n".format(speedPostPitchKph))
            append("• Pitching: ${pitchingZone.name} (%.2f m)\n".format(bouncePoint.yMeters))
            append("• Pad Impact: ${impactZone.name} (%.2f m in front of stumps)\n".format(input.impactDistanceMeters))
            append("• Projected Stumps Height: %.1f cm (Bails = 73.7 cm)\n".format(stumpZCm))
            append("• Projected Stumps Line: %.1f cm from center\n".format(stumpYCm))
            append("• Physics Status: ${stumpHitStatus.name}")
        }

        return DrsDecisionResult(
            pitchingZone = pitchingZone,
            impactZone = impactZone,
            stumpHitStatus = stumpHitStatus,
            finalVerdict = finalVerdict,
            heightAtStumpsCm = stumpZCm,
            lateralOffsetAtStumpsCm = stumpYCm,
            speedAtPitchKph = speedPostPitchKph,
            bouncePoint = bouncePoint,
            impactPoint = impactPoint,
            stumpsPoint = stumpsArrivalPoint,
            trajectoryPoints = points,
            scientificDetails = details
        )
    }
}
