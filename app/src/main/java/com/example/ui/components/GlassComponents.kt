package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.DarkCanvas
import com.example.ui.theme.DarkGlass
import com.example.ui.theme.NeonGreen

@Composable
fun GradientBackground(
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkCanvas)
    ) {
        // Stadium Art image background
        Image(
            painter = painterResource(id = R.drawable.img_stadium_bg),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
            alpha = 0.35f
        )

        // Gradient overlay for readability
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xCC0D1117),
                            Color(0xEE161B22),
                            Color(0xFF08120B)
                        )
                    )
                )
        )

        content()
    }
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    borderColor: Color = NeonGreen.copy(alpha = 0.3f),
    cornerRadius: Dp = 16.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        shape = RoundedCornerShape(cornerRadius),
        colors = CardDefaults.cardColors(containerColor = DarkGlass),
        modifier = modifier
            .border(1.dp, borderColor, RoundedCornerShape(cornerRadius))
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content = content
        )
    }
}

@Composable
fun GlassButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = NeonGreen,
    contentColor: Color = Color.Black,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = containerColor.copy(alpha = 0.3f),
            disabledContentColor = contentColor.copy(alpha = 0.5f)
        ),
        modifier = modifier.height(52.dp)
    ) {
        Text(
            text = text,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
    }
}

@Composable
fun ScoreChip(
    text: String,
    isWicket: Boolean = false,
    isBoundary: Boolean = false,
    isSix: Boolean = false,
    isExtra: Boolean = false
) {
    val bgColor = when {
        isWicket -> Color(0xFFFF1744)
        isSix -> Color(0xFFD500F9)
        isBoundary -> Color(0xFF00E676)
        isExtra -> Color(0xFFFF9100)
        else -> Color.White.copy(alpha = 0.15f)
    }

    val textColor = if (isWicket || isSix || isBoundary || isExtra) Color.White else NeonGreen

    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(bgColor)
            .border(1.dp, textColor.copy(alpha = 0.5f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun AnimatedScore(
    score: Int,
    wickets: Int,
    modifier: Modifier = Modifier
) {
    AnimatedContent(
        targetState = Pair(score, wickets),
        transitionSpec = {
            scaleIn(animationSpec = tween(300)) + fadeIn() togetherWith scaleOut(animationSpec = tween(300)) + fadeOut()
        },
        label = "ScoreAnim"
    ) { (s, w) ->
        Text(
            text = "$s / $w",
            color = NeonGreen,
            fontSize = 42.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.sp,
            modifier = modifier
        )
    }
}
