package com.dwipayana.literalearn.pages

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dwipayana.literalearn.ml.VideoRecommendationHelper
import com.dwipayana.literalearn.viewmodel.RecommendationViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dwipayana.literalearn.R
import com.dwipayana.literalearn.data.model.Subject
import com.dwipayana.literalearn.data.model.User
import com.dwipayana.literalearn.data.network.RetrofitClient
import com.dwipayana.literalearn.data.network.SessionManager
import com.dwipayana.literalearn.utils.ImageUtils

@Composable
fun HomePage(
    modifier: Modifier = Modifier,
    onSubjectClick: (String) -> Unit = {},
    subjects: List<Subject> = emptyList()
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val sessionManager = remember { SessionManager(context) }
    var userData by remember { mutableStateOf<User?>(null) }

    // Initialize TFLite Recommendation ViewModel
    val recommendationViewModel: RecommendationViewModel = viewModel(
        factory = RecommendationViewModel.Factory(context)
    )
    val predictedModule by recommendationViewModel.predictedModule.collectAsState()
    val predictedVideoUrl by recommendationViewModel.predictedVideoUrl.collectAsState()
    val recommendationMessage by recommendationViewModel.recommendationMessage.collectAsState()

    LaunchedEffect(Unit) {
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

        // REFRESH REKOMENDASI ML SECARA REAL-TIME
        recommendationViewModel.refreshRecommendation()
    }
    
    // Hitung rata-rata progres agar Progress Bar di halaman depan mengikuti database
    val averageProgress = if (subjects.isNotEmpty()) {
        subjects.map { it.currentProgress }.average() / 100
    } else 0.0

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF5F9FF)) // Background biru muda asli
            .verticalScroll(scrollState)
            .padding(bottom = 24.dp)
    ) {
        // --- Bagian Atas: Header Profil & Poin ---
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
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "LiteraLearn",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF006D8E)
                )
            }

            Surface(
                modifier = Modifier.size(40.dp),
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
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }

        // --- Teks Sapaan ---
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            Text(
                text = "Halo, siap belajar\nhari ini?",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 32.sp,
                color = Color(0xFF1A1C1E)
            )
            Text(
                text = "Semangat belajar bersama Sahabat Litera!",
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        // --- Card Level: Petualang Cilik ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF006D8E))
        ) {
            Row(
                modifier = Modifier.padding(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Surface(
                        color = Color.White.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            "Level: Pemula",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            fontSize = 11.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Text(
                        "Petualang Cilik",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    Text(
                        "✪ Lanjut ke Level 2",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 13.sp
                    )
                }
                Icon(
                    imageVector = Icons.Default.School,
                    contentDescription = null,
                    modifier = Modifier.size(56.dp),
                    tint = Color.White
                )
            }
        }

        // --- Card Tantangan Harian & Progress ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Tantangan\nHarian",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        lineHeight = 22.sp
                    )
                }

                val totalTarget = 4
                val completed = (averageProgress * totalTarget).toInt()
                val remaining = (totalTarget - completed).coerceAtLeast(0)

                val challengeText = if (remaining > 0) {
                    "Selesaikan $remaining latihan lagi untuk lanjut ke level 2."
                } else {
                    "Selamat! Kamu telah menyelesaikan semua tantangan hari ini."
                }

                Text(
                    text = challengeText,
                    fontSize = 13.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(vertical = 12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Progres Belajar", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF006D8E))
                    Text("${(averageProgress * 100).toInt()}%", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(8.dp))

                LinearProgressIndicator(
                    progress = { averageProgress.toFloat() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .clip(CircleShape),
                    color = Color(0xFF4FC3F7),
                    trackColor = Color(0xFFE1F5FE)
                )
            }
        }

        // --- Bagian Pilih Mata Pelajaran ---
        Text(
            "Pilih Mata Pelajaran",
            modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 16.dp),
            fontWeight = FontWeight.ExtraBold,
            fontSize = 20.sp
        )

        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
            SubjectBox(
                name = "Matematika",
                icon = Icons.Default.Calculate,
                bgColor = Color(0xFFE0F2F1),
                iconColor = Color(0xFF00796B),
                modifier = Modifier.weight(1f),
                onClick = { onSubjectClick("Matematika") }
            )
            Spacer(modifier = Modifier.width(16.dp))
            SubjectBox(
                name = "Bahasa Indonesia",
                icon = Icons.AutoMirrored.Filled.MenuBook,
                bgColor = Color(0xFFE8F5E9),
                iconColor = Color(0xFF2E7D32),
                modifier = Modifier.weight(1f),
                onClick = { onSubjectClick("Bahasa Indonesia") }
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
            SubjectBox(
                name = "IPAS",
                icon = Icons.Default.Science,
                bgColor = Color(0xFFFCE4EC),
                iconColor = Color(0xFF880E4F),
                modifier = Modifier.weight(1f),
                onClick = { onSubjectClick("IPAS") }
            )
            Spacer(modifier = Modifier.width(16.dp))
            SubjectBox(
                name = "Bahasa Inggris",
                icon = Icons.Default.Translate,
                bgColor = Color(0xFFEFEBE9),
                iconColor = Color(0xFF6D4C41),
                modifier = Modifier.weight(1f),
                onClick = { onSubjectClick("Bahasa Inggris") }
            )
        }

        // --- TFLite ML Recommendation Section ---
        predictedModule?.let { moduleName ->
            TFLiteRecommendationSection(moduleName, recommendationMessage, predictedVideoUrl)
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun TFLiteRecommendationSection(moduleName: String, message: String, videoUrl: String) {
    val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current

    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "Rekomendasi Pintar ✨",
            fontWeight = FontWeight.ExtraBold,
            fontSize = 20.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E5F5)), // Ungu sangat lembut (Lavender)
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            border = BorderStroke(1.dp, Color(0xFFD1C4E9)) // Border ungu muda
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = Color(0xFF7E57C2), // Ungu Medium untuk background ikon
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.AutoGraph,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Analisis Kemampuan",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color(0xFF512DA8) // Ungu Tua untuk teks judul
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = message,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    color = Color(0xFF424242) // Abu-abu gelap agar kontras di atas ungu lembut
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Surface(
                    onClick = { uriHandler.openUri(videoUrl) },
                    color = Color.White,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color(0xFFE1BEE7))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier.size(40.dp),
                            shape = CircleShape,
                            color = Color(0xFFF3E5F5)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.PlayCircleFilled,
                                    contentDescription = null,
                                    tint = Color(0xFF7E57C2),
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = moduleName,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp,
                                color = Color(0xFF311B92) // Ungu pekat
                            )
                            Text(
                                text = "Tonton video rekomendasi!",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SubjectBox(
    name: String,
    icon: ImageVector,
    bgColor: Color,
    iconColor: Color,
    modifier: Modifier,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .height(130.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                modifier = Modifier.size(56.dp),
                shape = RoundedCornerShape(16.dp),
                color = iconColor
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(imageVector = icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                }
            }
            Text(
                text = name,
                modifier = Modifier.padding(top = 10.dp),
                fontWeight = FontWeight.Bold,
                color = iconColor,
                fontSize = 15.sp
            )
        }
    }
}

@Composable
fun RecommendationItem(title: String, desc: String, icon: ImageVector) {
    Card(
        modifier = Modifier.width(240.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFF0F0F0))
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(
                modifier = Modifier.size(50.dp),
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFE8EAF6)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(imageVector = icon, contentDescription = null, tint = Color(0xFF3F51B5), modifier = Modifier.size(26.dp))
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text(desc, fontSize = 12.sp, color = Color.Gray)
            }
        }
    }
}
