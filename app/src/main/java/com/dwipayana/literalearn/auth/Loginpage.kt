package com.dwipayana.literalearn.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import com.dwipayana.literalearn.R
import com.dwipayana.literalearn.data.model.LoginRequest
import com.dwipayana.literalearn.data.network.RetrofitClient
import com.dwipayana.literalearn.data.network.SessionManager
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch

@Composable
fun LoginPage(
    onLoginSuccess: () -> Unit,
    onRegisterClick: () -> Unit,
    onForgotPasswordClick: () -> Unit,
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }

    var email by remember { mutableStateOf("") }
    var kataSandi by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(value = false) }
    
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    focusManager.clearFocus()
                })
            }
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Area Logo/Maskot
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "Logo LiteraLearn",
            modifier = Modifier.size(120.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Halo, Sahabat!",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF006080)
        )
        Text(
            text = "Siap untuk petualangan belajar hari ini?",
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Form Input
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email", fontSize = 14.sp) },
                placeholder = { Text("Tulis email anda", fontSize = 14.sp) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = Color(0xFF006D8E)
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = kataSandi,
                onValueChange = { kataSandi = it },
                label = { Text("Kata Sandi", fontSize = 14.sp) },
                placeholder = { Text("Masukkan kata sandi", fontSize = 14.sp) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = Color(0xFF006D8E)
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                        Icon(
                            imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = null,
                            tint = Color.Gray
                        )
                    }
                }
            )

            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = Color.Red,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (email.isNotEmpty() && kataSandi.isNotEmpty()) {
                        isLoading = true
                        errorMessage = null
                        scope.launch {
                            try {
                                val result = RetrofitClient.apiService.login(LoginRequest(email, kataSandi))
                                if (result.isSuccessful) {
                                    val responseBody = result.body()
                                    val token = responseBody?.accessToken?.token
                                    if (token != null) {
                                        // Bersihkan data user sebelumnya sebelum menyimpan data user baru
                                        sessionManager.clearSession()

                                        sessionManager.saveToken(token)
                                        
                                        // AMBIL DAN SIMPAN UUID USER
                                        try {
                                            val profileResult = RetrofitClient.apiService.getProfile("Bearer $token")
                                            if (profileResult.isSuccessful) {
                                                profileResult.body()?.data?.uuid?.let { uuid ->
                                                    sessionManager.saveUserUuid(uuid)
                                                }
                                            }
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }

                                        onLoginSuccess()
                                    } else {
                                        errorMessage = "Login Gagal: Token tidak ditemukan"
                                    }
                                } else {
                                    errorMessage = "Gagal Masuk: Akun tidak ditemukan atau sandi salah"
                                }
                            } catch (e: Exception) {
                                errorMessage = "Kesalahan: ${e.localizedMessage ?: "Gagal terhubung ke server"}"
                                e.printStackTrace()
                            } finally {
                                isLoading = false
                            }
                        }
                    } else {
                        errorMessage = "Email dan kata sandi harus diisi"
                    }
                },
                modifier = Modifier.fillMaxWidth().height(54.dp),
                enabled = !isLoading,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF006D8E))
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Ayo Masuk! ➔", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Divider "Atau masuk dengan"
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f), thickness = 1.dp, color = Color(0xFFEEEEEE))
                Text(
                    text = " Atau ",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                HorizontalDivider(modifier = Modifier.weight(1f), thickness = 1.dp, color = Color(0xFFEEEEEE))
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Tombol Google Login
            OutlinedButton(
                onClick = { /* TODO: Implement Google Sign In */ },
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_google),
                        contentDescription = "Logo Google",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Masuk dengan Google",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF5F6368)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            "Lupa kata sandi? Klik di sini",
            fontSize = 12.sp,
            color = Color(0xFF006D8E),
            textAlign = TextAlign.Center,
            modifier = Modifier.clickable { onForgotPasswordClick() }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Belum punya akun? ", fontSize = 13.sp)
            TextButton(onClick = { onRegisterClick() }, contentPadding = PaddingValues(0.dp)) {
                Text(
                    "Daftar di sini",
                    fontSize = 13.sp,
                    color = Color(0xFFC2185B),
                    fontWeight = FontWeight.Bold,
                    textDecoration = TextDecoration.Underline
                )
            }
        }
    }
}
