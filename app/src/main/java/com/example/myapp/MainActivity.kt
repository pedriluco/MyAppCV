package com.example.myapp

import android.os.Bundle
import com.example.myapp.ui.AppNav
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.myapp.ui.screens.CreateBusinessScreen
import com.example.myapp.ui.theme.MyAppTheme
import com.example.myapp.data.TokenStore
import com.example.myapp.ui.AppNav
import androidx.compose.runtime.remember

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val tokenStore = remember { TokenStore(applicationContext) }
            AppNav(tokenStore = tokenStore)
        }
    }
}
