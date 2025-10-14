import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.onlyone.app.composables.CustomColorOverlay
import com.onlyone.app.theme.ThemeTokens

@Composable
fun CustomAlertDialog(
    theme: ThemeTokens,
    shape: Shape = RoundedCornerShape(32.dp),
    borderColor: Color =  Color(0xFF80DFFF),
    onDismiss: () -> Unit,
    content: @Composable () -> Unit
) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        CustomColorOverlay(
            shape = shape,
            overlayColor = Color.Gray,
            onDismiss = onDismiss,
            borderColor = borderColor,
            paddingBox1 = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
            paddingBox2 = PaddingValues(10.dp),
            theme = theme
        ) {
            content()
        }
    }
}

