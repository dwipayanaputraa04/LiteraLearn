package com.dwipayana.literalearn

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import com.dwipayana.literalearn.Screens.MainScreen
import com.dwipayana.literalearn.Screens.SplashScreen
import com.dwipayana.literalearn.auth.ForgotPasswordPage
import com.dwipayana.literalearn.auth.LoginPage
import com.dwipayana.literalearn.auth.RegisterPage
import com.dwipayana.literalearn.ui.theme.LiteraLearnTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LiteraLearnTheme {
                var currentScreen by remember { mutableStateOf("splash") }

                when (currentScreen) {
                    "splash" -> {
                        SplashScreen(onNavigateToNext = { currentScreen = "login" })
                    }
                    "login" -> {
                        LoginPage(
                            onLoginSuccess = { currentScreen = "main" },
                            onRegisterClick = { currentScreen = "register" },
                            onForgotPasswordClick = { currentScreen = "forgot_password" }
                        )
                    }
                    "register" -> {
                        RegisterPage(
                            onRegisterSuccess = { currentScreen = "login" },
                            onLoginClick = { currentScreen = "login" }
                        )
                    }
                    "forgot_password" -> {
                        ForgotPasswordPage(
                            onBackToLogin = { currentScreen = "login" }
                        )
                    }
                    "main" -> {
                        val sessionManager = remember { com.dwipayana.literalearn.data.network.SessionManager(this@MainActivity) }
                        MainScreen(onLogout = { 
                            sessionManager.clearSession()
                            currentScreen = "login" 
                        })
                    }
                }
            }
        }
    }
}
