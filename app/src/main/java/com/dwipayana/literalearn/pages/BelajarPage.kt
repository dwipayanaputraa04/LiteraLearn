package com.dwipayana.literalearn.pages

import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.dwipayana.literalearn.R
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dwipayana.literalearn.data.model.User
import com.dwipayana.literalearn.data.network.RetrofitClient
import com.dwipayana.literalearn.data.network.SessionManager
import com.dwipayana.literalearn.ui.viewmodel.SubjectViewModel
import com.dwipayana.literalearn.utils.ImageUtils

@Composable
fun BelajarPage(
    modifier: Modifier = Modifier,
    onSubjectClick: (String) -> Unit = {},
    viewModel: SubjectViewModel = viewModel()
) {
    val subjects by viewModel.subjects.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    var userData by remember { mutableStateOf<User?>(null) }

    LaunchedEffect(Unit) {
        val userUuid = sessionManager.getUserUuid()
        if (userUuid != null) {
            viewModel.fetchSubjectsWithProgress(userUuid)
        }
        
        val token = sessionManager.getToken()
        if (token != null) {
            try {
                val response = RetrofitClient.apiService.getProfile("Bearer $token")
                if (response.isSuccessful) {
                    userData = response.body()?.data
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(scrollState)
            .padding(bottom = 24.dp)
    ) {
        // --- Header (Profil & Koin) ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = R.drawable.logo1),
                    contentDescription = null,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "LiteraLearn",
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF006D8E),
                    fontSize = 18.sp
                )
            }
            Surface(
                modifier = Modifier.size(36.dp),
                shape = CircleShape,
                color = Color(0xFFE3F2FD),
                border = BorderStroke(2.dp, Color(0xFF006D8E))
            ) {
                val bitmap = ImageUtils.base64ToBitmap(userData?.profile)
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = Color(0xFF006D8E),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // --- Judul Halaman ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Ayo Pilih\nPelajaranmu!",
                fontSize = 26.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
                lineHeight = 32.sp,
                color = Color(0xFF212121)
            )
            Text(
                text = "Pilih tantangan seru hari ini dan\nkumpulkan poinnya!",
                fontSize = 13.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        if (isLoading) {
            Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF006D8E))
            }
        }

        // --- Daftar Mata Pelajaran dari Database ---
        subjects.forEach { subject ->
            val icon = when (subject.title) {
                "Matematika" -> Icons.Default.Calculate
                "IPAS" -> Icons.Default.Science
                "Bahasa Inggris" -> Icons.Default.Translate
                else -> Icons.AutoMirrored.Filled.MenuBook
            }
            
            val (containerColor, accentColor) = when (subject.title) {
                "Matematika" -> Pair(Color(0xFFE0F2F1), Color(0xFF00796B))
                "Bahasa Indonesia" -> Pair(Color(0xFFE8F5E9), Color(0xFF2E7D32))
                "IPAS" -> Pair(Color(0xFFFCE4EC), Color(0xFF880E4F))
                "Bahasa Inggris" -> Pair(Color(0xFFEFEBE9), Color(0xFF6D4C41))
                else -> Pair(
                    Color(android.graphics.Color.parseColor(subject.containerColor ?: "#E3F2FD")),
                    Color(android.graphics.Color.parseColor(subject.accentColor ?: "#006D8E"))
                )
            }

            SubjectLongItem(
                title = subject.title,
                unit = "Tingkat ${subject.grade}",
                progress = (subject.currentProgress / 100).toFloat(),
                icon = icon,
                accentColor = accentColor,
                containerColor = containerColor,
                onClick = { onSubjectClick(subject.title) }
            )
        }

        // --- Misi Harian ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Surface(
                    color = Color(0xFF03A9F4),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        "Misi Harian",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        fontSize = 10.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    "Tantangan Membaca 15 Menit",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(top = 12.dp)
                )
                Text(
                    "Selesaikan misi ini untuk mendapatkan lencana spesial!",
                    fontSize = 12.sp,
                    color = Color.DarkGray,
                    modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
                )
                Button(
                    onClick = { },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF006064))
                ) {
                    Text("Mulai Sekarang", fontWeight = FontWeight.Bold)
                    Icon(Icons.AutoMirrored.Filled.NavigateNext, contentDescription = null)
                }
            }
        }
    }
}

@Composable
fun SubjectLongItem(
    title: String,
    unit: String,
    progress: Float,
    icon: ImageVector,
    accentColor: Color,
    containerColor: Color,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier.size(60.dp),
                shape = RoundedCornerShape(16.dp),
                color = accentColor
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(30.dp))
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(title, fontWeight = FontWeight.Black, fontSize = 18.sp)
            Text(unit, fontSize = 12.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.weight(1f).height(10.dp).clip(CircleShape),
                    color = accentColor,
                    trackColor = Color.White.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "${(progress * 100).toInt()}%",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = accentColor
                )
            }
        }
    }
}
