package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.Brand400
import com.example.ui.theme.Brand700
import com.example.ui.theme.Ink950
import com.example.ui.theme.PoppinsFontFamily

/**
 * Authentic KungaCV Logo recreated directly from `frontend/kungacv_client/components/Logo.jsx`
 * in `vend-pixel/kungacv`:
 * - Blue gradient "K" (`#38A0FF` -> `#0040D0`) on the left
 * - Folded document sheet with avatar circle + 3 text rule bars on the right
 * - Wordmark `Kunga` + `CV` (`#4DA3FF` on dark / `#0050E0` on light)
 */
@Composable
fun KungaCvLogo(
    modifier: Modifier = Modifier,
    onDark: Boolean = true,
    markSize: Dp = 32.dp,
    fontSize: TextUnit = 20.sp,
    markOnly: Boolean = false
) {
    val docFill = if (onDark) Color.White else Ink950
    val glyphFill = if (onDark) Ink950 else Color.White
    val wordmarkMain = if (onDark) Color.White else Ink950
    val wordmarkAccent = if (onDark) Brand400 else Brand700

    Row(
        modifier = modifier.semantics { contentDescription = "KungaCV Logo" },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Canvas(modifier = Modifier.size(markSize)) {
            val scaleX = size.width / 64f
            val scaleY = size.height / 64f

            val kBrush = Brush.linearGradient(
                colors = listOf(Color(0xFF38A0FF), Color(0xFF0040D0)),
                start = Offset(8f * scaleX, 8f * scaleY),
                end = Offset(52f * scaleX, 56f * scaleY)
            )

            // 1. Vertical stem of 'K': <rect x="5" y="8" width="12" height="48" rx="2" />
            drawRoundRect(
                brush = kBrush,
                topLeft = Offset(5f * scaleX, 8f * scaleY),
                size = Size(12f * scaleX, 48f * scaleY),
                cornerRadius = CornerRadius(2f * scaleX, 2f * scaleY)
            )

            // 2. Upper diagonal arm of 'K': <path d="M17 32L34 8H46L29 32H17Z" />
            val upperArm = Path().apply {
                moveTo(17f * scaleX, 32f * scaleY)
                lineTo(34f * scaleX, 8f * scaleY)
                lineTo(46f * scaleX, 8f * scaleY)
                lineTo(29f * scaleX, 32f * scaleY)
                close()
            }
            drawPath(path = upperArm, brush = kBrush)

            // 3. Lower diagonal arm of 'K': <path d="M17 32H29L46 56H34L17 32Z" />
            val lowerArm = Path().apply {
                moveTo(17f * scaleX, 32f * scaleY)
                lineTo(29f * scaleX, 32f * scaleY)
                lineTo(46f * scaleX, 56f * scaleY)
                lineTo(34f * scaleX, 56f * scaleY)
                close()
            }
            drawPath(path = lowerArm, brush = kBrush)

            // 4. Folded document sheet: <path d="M48 9H59a3 3 0 0 1 3 3v44a3 3 0 0 1-3 3H40a3 3 0 0 1-3-3V23L48 9Z" />
            val docPath = Path().apply {
                moveTo(48f * scaleX, 9f * scaleY)
                lineTo(60f * scaleX, 9f * scaleY)
                lineTo(62f * scaleX, 11f * scaleY)
                lineTo(62f * scaleX, 57f * scaleY)
                lineTo(60f * scaleX, 59f * scaleY)
                lineTo(39f * scaleX, 59f * scaleY)
                lineTo(37f * scaleX, 57f * scaleY)
                lineTo(37f * scaleX, 23f * scaleY)
                close()
            }
            drawPath(path = docPath, color = docFill)

            // 5. Person avatar circle: <circle cx="49.5" cy="31" r="5.5" />
            drawCircle(
                color = glyphFill,
                radius = 5.5f * scaleX,
                center = Offset(49.5f * scaleX, 31f * scaleY)
            )

            // 6. Three text rule bars inside document
            drawRoundRect(
                color = glyphFill,
                topLeft = Offset(43f * scaleX, 40f * scaleY),
                size = Size(14f * scaleX, 3f * scaleY),
                cornerRadius = CornerRadius(1.5f * scaleX, 1.5f * scaleY)
            )
            drawRoundRect(
                color = glyphFill,
                topLeft = Offset(43f * scaleX, 45.5f * scaleY),
                size = Size(14f * scaleX, 3f * scaleY),
                cornerRadius = CornerRadius(1.5f * scaleX, 1.5f * scaleY)
            )
            drawRoundRect(
                color = glyphFill,
                topLeft = Offset(43f * scaleX, 51f * scaleY),
                size = Size(9f * scaleX, 3f * scaleY),
                cornerRadius = CornerRadius(1.5f * scaleX, 1.5f * scaleY)
            )
        }

        if (!markOnly) {
            Text(
                text = buildAnnotatedString {
                    withStyle(
                        SpanStyle(
                            color = wordmarkMain,
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.ExtraBold
                        )
                    ) {
                        append("Kunga")
                    }
                    withStyle(
                        SpanStyle(
                            color = wordmarkAccent,
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.ExtraBold
                        )
                    ) {
                        append("CV")
                    }
                },
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = fontSize,
                    fontWeight = FontWeight.ExtraBold
                )
            )
        }
    }
}
