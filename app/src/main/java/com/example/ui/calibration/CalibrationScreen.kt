package com.example.ui.calibration

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.ar.ArCalibrationView
import com.example.camera.CameraPreview
import com.example.ui.components.GradientBackground

@Composable
fun CalibrationScreen(
    onCalibrationComplete: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Camera Preview Backdrop
        CameraPreview(modifier = Modifier.fillMaxSize())

        // Calibration Controls & Overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
        ) {
            ArCalibrationView(
                onCalibrationComplete = onCalibrationComplete
            )
        }
    }
}
