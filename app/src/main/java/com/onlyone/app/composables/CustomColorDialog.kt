package com.onlyone.app.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.onlyone.app.theme.ThemeTokens

@Composable
fun CustomColorOverlay(
    modifier: Modifier = Modifier,
    theme: ThemeTokens,
    overlayColor: Color = Color.Transparent,
    borderColor: Color =  Color(0xFF80DFFF),
    gradientColor1: Color = Color(0xFF353535).copy(alpha = 0.95f),      //Color(0xFF433A52)
    gradientColor2: Color = Color(0xFF1F1F1F).copy(alpha = 0.95f),      //Color(0xFF5A4A6A)
    borderWidth : Dp = 0.1.dp,
    shape: Shape = RoundedCornerShape(32.dp),
    paddingBox1: PaddingValues = PaddingValues(horizontal = 4.dp), // outer Box padding
    paddingBox2: PaddingValues = PaddingValues(12.dp),             // inner Box padding
    onDismiss: () -> Unit,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(paddingBox1) // ✅ applied here
            .border(
                width = borderWidth,
                color = theme.borderColor.copy(alpha = 0.6f),
                shape = shape
            )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(),
            shape = shape,
            elevation = 8.dp,
            //color = overlayColor
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                theme.gradientColor1,
                                theme.gradientColor2
                            )
                        ),
                        shape = shape
                    )
                    .padding(paddingBox2) // ✅ applied here
            ) {
                content()
            }
        }
    }
}












