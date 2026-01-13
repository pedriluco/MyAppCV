package com.example.myapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun ScreenScaffold(
    title: String,
    canBack: Boolean,
    onBack: (() -> Unit)? = null,
    actions: @Composable () -> Unit = {},
    scroll: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    Scaffold(
        topBar = { AppTopBar(title = title, canNavigateBack = canBack, onBack = onBack, actions = actions) }
    ) { inner ->
        val base = Modifier
            .padding(inner)
            .padding(Ui.ScreenPad)
            .fillMaxSize()

        val mod = if (scroll) base.verticalScroll(rememberScrollState()) else base

        Column(
            modifier = mod,
            verticalArrangement = Arrangement.spacedBy(Ui.Gap),
            content = content
        )
    }
}

