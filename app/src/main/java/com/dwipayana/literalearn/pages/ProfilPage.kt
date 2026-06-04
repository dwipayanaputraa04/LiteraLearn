package com.dwipayana.literalearn.pages

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dwipayana.literalearn.R
import androidx.compose.ui.platform.LocalContext
import com.dwipayana.literalearn.data.model.User
import com.dwipayana.literalearn.data.network.RetrofitClient
import com.dwipayana.literalearn.data.network.SessionManager
import androidx.compose.ui.graphics.asImageBitmap
import com.dwipayana.literalearn.utils.ImageUtils

@Composable
fun ProfilPage(
    modifier: Modifier = Modifier,
    onLogoutClick: () -> Unit = {},
    onEditProfileClick: () -> Unit = {}
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    
    var userData by remember { mutableStateOf<User?>(null) }
    var totalPoints by remember { mutableIntStateOf(0) }
    var totalProgress by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        val token = sessionManager.getToken()
        if (token != null) {
            try {
                val response = RetrofitClient.apiService.getProfile("Bearer $token")
                if (response.isSuccessful) {
                    userData = response.body()?.data
                }

                // Ambil Total Poin Realtime dari SessionManager
                totalPoints = sessionManager.getTotalPoints()

                // Hitung Total Belajar (Total progres semua mata pelajaran)
                val userUuid = sessionManager.getUserUuid()
                if (userUuid != null) {
                    val progressRes = RetrofitClient.apiService.getUserProgress(userUuid)
                    if (progressRes.isSuccessful) {
                        val allProgress = progressRes.body()?.data ?: emptyList()
                        totalProgress = allProgress.sumOf { it.progressPercent.toInt() }
                    }
                }
            } catch (e: Exception) {
                // Error handling
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF5F9FF))
            .verticalScroll(scrollState)
            .padding(bottom = 100.dp)
    ) {
        // --- 1. HEADER ---
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(painterResource(R.drawable.logo1), null, modifier = Modifier.size(32.dp))
                Spacer(Modifier.width(8.dp))
                Text("LiteraLearn", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF006080))
            }
        }

        // --- 2. AVATAR & USERNAME ---
        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(contentAlignment = Alignment.BottomEnd) {
                Surface(
                    modifier = Modifier.size(130.dp).padding(4.dp),
                    shape = CircleShape,
                    border = BorderStroke(4.dp, Color(0xFFFFD54F)),
                    color = Color.White
                ) {
                    val bitmap = ImageUtils.base64ToBitmap(userData?.profile)
                    if (bitmap != null) {
                        Image(bitmap.asImageBitmap(), null, modifier = Modifier.fillMaxSize().clip(CircleShape), contentScale = ContentScale.Crop)
                    } else {
                        Icon(Icons.Default.Person, null, modifier = Modifier.padding(20.dp).fillMaxSize(), tint = Color(0xFF006080))
                    }
                }
                Surface(
                    modifier = Modifier.size(40.dp).offset(x = 5.dp, y = 5.dp),
                    shape = CircleShape,
                    color = Color(0xFF006D8E),
                    border = BorderStroke(2.dp, Color.White),
                    onClick = onEditProfileClick
                ) {
                    Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.CameraAlt, null, tint = Color.White, modifier = Modifier.size(20.dp)) }
                }
            }

            Spacer(Modifier.height(28.dp))

            Text(text = userData?.username ?: "Memuat...", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A237E))
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { onEditProfileClick() }) {
                Text(text = if (userData != null) "Siswa • Kelas ${userData?.grade ?: "-"} SD" else "Memuat...", color = Color.Gray, fontSize = 14.sp)
                Spacer(Modifier.width(8.dp))
                Icon(Icons.Default.Edit, null, tint = Color(0xFF006D8E), modifier = Modifier.size(14.dp))
            }
        }

        Spacer(Modifier.height(24.dp))

        // --- 3. STATISTIK ---
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            StatCard(totalPoints.toString(), "TOTAL POIN", Icons.Default.Star, Color(0xFFFFD600), Color(0xFF00838F), Modifier.weight(1f))
            StatCard("$totalProgress%", "TOTAL BELAJAR", Icons.Default.Insights, Color(0xFF4CAF50), Color(0xFF2E7D32), Modifier.weight(1f))
        }

        // --- 4. MENU ---
        SectionHeader("Pengaturan Profil", "")
        Surface(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), shape = RoundedCornerShape(20.dp), color = Color.White, border = BorderStroke(1.dp, Color(0xFFECEFF1))) {
            Column {
                MenuItem("Ubah Nama & Profil", Icons.Default.Badge, Color(0xFF3F51B5), onEditProfileClick)
                HorizontalDivider(Modifier.padding(horizontal = 16.dp), 0.5.dp, Color(0xFFEEEEEE))
                MenuItem("Ubah Kelas Belajar", Icons.Default.School, Color(0xFF4CAF50), onEditProfileClick)
                HorizontalDivider(Modifier.padding(horizontal = 16.dp), 0.5.dp, Color(0xFFEEEEEE))
                MenuItem("Pengaturan Notifikasi", Icons.Default.Notifications, Color(0xFFFF9800)) {}
            }
        }

        // --- 6. LOGOUT ---
        Spacer(Modifier.height(32.dp))
        Button(onClick = onLogoutClick, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(56.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFF1F1)), border = BorderStroke(1.5.dp, Color(0xFFFFCDD2))) {
            Icon(Icons.AutoMirrored.Filled.ExitToApp, null, tint = Color(0xFFD32F2F))
            Spacer(Modifier.width(12.dp))
            Text("Keluar dari Akun", color = Color(0xFFD32F2F), fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun MenuItem(title: String, icon: ImageVector, iconColor: Color, onClick: () -> Unit) {
    Surface(onClick = onClick, modifier = Modifier.fillMaxWidth(), color = Color.Transparent) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(modifier = Modifier.size(40.dp), shape = RoundedCornerShape(10.dp), color = iconColor.copy(alpha = 0.1f)) {
                Box(contentAlignment = Alignment.Center) { Icon(icon, null, tint = iconColor, modifier = Modifier.size(20.dp)) }
            }
            Spacer(Modifier.width(16.dp))
            Text(title, Modifier.weight(1f), fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF37474F))
            Icon(Icons.Default.ChevronRight, null, tint = Color.LightGray)
        }
    }
}

@Composable
fun StatCard(value: String, label: String, icon: ImageVector, iconColor: Color, textColor: Color, modifier: Modifier = Modifier) {
    Card(modifier = modifier.height(110.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(2.dp)) {
        Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Icon(icon, null, tint = iconColor, modifier = Modifier.size(24.dp))
            Spacer(Modifier.height(4.dp))
            Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = textColor)
            Text(label, fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun SectionHeader(title: String, actionText: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 28.dp, bottom = 12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF263238))
        if (actionText.isNotEmpty()) Text(actionText, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0288D1))
    }
}

@Composable
fun BadgeCard(name: String, icon: ImageVector, themeColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(90.dp)) {
        Card(modifier = Modifier.size(80.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = themeColor.copy(alpha = 0.15f)), border = BorderStroke(2.dp, themeColor)) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Icon(icon, null, tint = themeColor, modifier = Modifier.size(36.dp)) }
        }
        Spacer(Modifier.height(6.dp))
        Text(name, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
    }
}
