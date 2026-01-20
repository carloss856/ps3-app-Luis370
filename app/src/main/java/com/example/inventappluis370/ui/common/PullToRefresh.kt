package com.example.inventappluis370.ui.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState

@Composable
fun PullToRefreshContainer(
    refreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val state = rememberSwipeRefreshState(isRefreshing = refreshing)
    SwipeRefresh(
        state = state,
        onRefresh = onRefresh,
        modifier = modifier
    ) {
        Box(modifier = Modifier.fillMaxSize(), content = content)
    }
}
