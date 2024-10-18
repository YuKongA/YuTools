import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Star
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.basic.HorizontalPager
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.NavigationBar
import top.yukonga.miuix.kmp.basic.NavigationItem
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.ScrollBehavior
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.basic.rememberTopAppBarState
import top.yukonga.yutools.R
import top.yukonga.yutools.ui.page.HomePage
import top.yukonga.yutools.ui.theme.AppTheme

@OptIn(FlowPreview::class)
@Composable
fun App() {
    val pagerState = rememberPagerState(pageCount = { 3 })
    var targetPage by remember { mutableIntStateOf(pagerState.currentPage) }
    val coroutineScope = rememberCoroutineScope()
    val items = listOf(
        NavigationItem("HomePage", Icons.Rounded.Home),
        NavigationItem("SecondPage", Icons.Rounded.Star),
        NavigationItem("ThirdPage", Icons.Rounded.Settings)
    )

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.debounce(150).collectLatest {
            targetPage = pagerState.currentPage
        }
    }
    AppTheme {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                NavigationBar(
                    items = items,
                    selected = targetPage,
                    onClick = { index ->
                        targetPage = index
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    }
                )
            }
        ) { padding ->
            AppHorizontalPager(
                modifier = Modifier,
                pagerState = pagerState,
                padding = padding
            )
        }
    }
}

@Composable
fun AppHorizontalPager(
    modifier: Modifier = Modifier,
    pagerState: PagerState,
    padding: PaddingValues
) {
    HorizontalPager(
        modifier = modifier,
        pagerState = pagerState,
        pageContent = { page ->
            when (page) {
                0 -> HomePage(
                    padding = padding
                )

                1 -> HomePage(
                    padding = padding
                )

                else -> HomePage(
                    padding = padding
                )
            }
        }
    )
}