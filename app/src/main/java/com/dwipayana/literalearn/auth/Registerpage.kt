package com.dwipayana.literalearn.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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
import com.dwipayana.literalearn.data.model.RegisterRequest
import com.dwipayana.literalearn.data.network.RetrofitClient
import kotlinx.coroutines.launch

@Composable
fun RegisterPage(
    onRegisterSuccess: () -> Unit,
    onLoginClick: () -> Unit,
) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var grade by remember { mutableStateOf("") }
    var kataSandi by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(value = false) }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
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
            .verticalScroll(scrollState)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Area Avatar/Mascot
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "Logo LiteraLearn",
            modifier = Modifier.size(100.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Daftar Akun Baru",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF006080)
        )
        Text(
            text = "Ayo bergabung dan mulai belajar!",
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Form Input
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
            // Input Nama
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("User Name", fontSize = 14.sp) },
                placeholder = { Text("Tulis nama lengkapmu...", fontSize = 14.sp) },
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

            // Input Email
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email", fontSize = 14.sp) },
                placeholder = { Text("nama@email.com", fontSize = 14.sp) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = Color(0xFF006D8E)
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Input Grade
            OutlinedTextField(
                value = grade,
                onValueChange = { if (it.all { char -> char.isDigit() }) grade = it },
                label = { Text("Kelas (Angka)", fontSize = 14.sp) },
                placeholder = { Text("Contoh: 4", fontSize = 14.sp) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = Color(0xFF006D8E)
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Input Password
            OutlinedTextField(
                value = kataSandi,
                onValueChange = { kataSandi = it },
                label = { Text("Kata Sandi", fontSize = 14.sp) },
                placeholder = { Text("Buat kata sandi...", fontSize = 14.sp) },
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
                    if (username.isNotEmpty() && email.isNotEmpty() && kataSandi.isNotEmpty() && grade.isNotEmpty()) {
                        isLoading = true
                        errorMessage = null
                        scope.launch {
                            try {
                                val request = RegisterRequest(
                                    username = username,
                                    email = email,
                                    password = kataSandi,
                                    kelas = grade.toIntOrNull() ?: 0
                                )
                                val result = RetrofitClient.apiService.register(request)
                                if (result.isSuccessful) {
                                    val responseBody = result.body()
                                    // Pendaftaran Berhasil jika ada token
                                    if (responseBody?.accessToken?.token != null) {
                                        onRegisterSuccess()
                                    } else {
                                        errorMessage = "Gagal: Respon server tidak lengkap"
                                    }
                                } else {
                                    val errorJson = result.errorBody()?.string()
                                    errorMessage = if (errorJson != null && errorJson.contains("message")) {
                                        errorJson.substringAfter("\"message\":\"").substringBefore("\"")
                                    } else {
                                        "Error ${result.code()}: Pendaftaran Gagal"
                                    }
                                }
                            } catch (e: Exception) {
                                errorMessage = "Kesalahan: ${e.localizedMessage ?: "Gagal terhubung ke server"}"
                                e.printStackTrace()
                            } finally {
                                isLoading = false
                            }
                        }
                    } else {
                        errorMessage = "Semua data harus diisi"
                    }
                },
                modifier = Modifier.fillMaxWidth().height(54.dp),
                enabled = !isLoading,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC2185B))
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Daftar Sekarang ➔", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Sudah punya akun? ", fontSize = 13.sp)
            TextButton(onClick = { onLoginClick() }, contentPadding = PaddingValues(0.dp)) {
                Text(
                    "Masuk di sini",
                    fontSize = 13.sp,
                    color = Color(0xFF006D8E),
                    fontWeight = FontWeight.Bold,
                    textDecoration = TextDecoration.Underline
                )
            }
        }
    }
}
