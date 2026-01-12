package com.example.myapp

import android.os.Bundle
import com.example.myapp.ui.AppNav
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.myapp.ui.screens.CreateBusinessScreen
import com.example.myapp.ui.theme.MyAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyAppTheme {
                AppNav()
            }
        }
    }
}
