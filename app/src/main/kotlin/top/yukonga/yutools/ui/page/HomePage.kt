package top.yukonga.yutools.ui.page

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import top.yukonga.yutools.ui.components.LevelMeter

@Composable
fun HomePage(
    padding: PaddingValues
) {
    Column(
        Modifier.fillMaxHeight()
    ) {
        LevelMeter()
        Spacer(modifier = Modifier.height(padding.calculateBottomPadding()))
    }
}