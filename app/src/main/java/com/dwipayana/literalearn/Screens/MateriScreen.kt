package com.dwipayana.literalearn.Screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dwipayana.literalearn.ui.viewmodel.ModuleViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MateriScreen(
    mapel: String,
    subjectUuid: String?,
    currentProgress: Double = 0.0,
    onBack: () -> Unit,
    onStartQuiz: (String, String, Int) -> Unit, // uuid kuis, title, order bab
    onModuleClick: (String, String, Int) -> Unit, // uuid, title, order bab
    viewModel: ModuleViewModel = viewModel()
) {
    val modules by viewModel.modules.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(subjectUuid) {
        if (subjectUuid != null) viewModel.fetchModules(subjectUuid)
    }

    val subjectColors = when (mapel) {
        "Matematika" -> Pair(Color(0xFFE0F2F1), Color(0xFF00796B))
        "Bahasa Indonesia" -> Pair(Color(0xFFE8F5E9), Color(0xFF2E7D32))
        "IPAS" -> Pair(Color(0xFFFCE4EC), Color(0xFF880E4F))
        "Bahasa Inggris" -> Pair(Color(0xFFEFEBE9), Color(0xFF6D4C41))
        else -> Pair(Color.White, Color.Gray)
    }
    val iconColor = subjectColors.second

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = mapel, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF006D8E), titleContentColor = Color.White),
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF006D8E))
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().background(Color(0xFFF5F9FF)).padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        "Daftar Modul Belajar", 
                        fontSize = 22.sp, 
                        fontWeight = FontWeight.ExtraBold, 
                        color = Color(0xFF333333),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                items(modules) { module ->
                    val displayTitle = when {
                        module.title.contains("Bab", ignoreCase = true) || 
                        module.title.contains("Chapter", ignoreCase = true) -> module.title
                        mapel == "Bahasa Inggris" -> "Chapter ${module.order}: ${module.title}"
                        else -> "Bab ${module.order}: ${module.title}"
                    }
                    
                    // Logika Kunci Kuis
                    val minProgressToUnlockQuiz = (module.order * 50.0) - 25.0
                    
                    // Ambil UUID kuis dengan fallback
                    val foundQuizUuid = module.quizzes.firstOrNull()?.uuid 
                                        ?: module.quiz?.uuid 
                                        ?: module.quizUuid

                    ModuleLongItem(
                        title = displayTitle,
                        description = module.description ?: "Klik untuk mempelajari materi ini",
                        bgColor = Color.White,
                        accentColor = iconColor,
                        onReadClick = { onModuleClick(module.uuid, module.title, module.order) },
                        onQuizClick = {
                            Log.d("MateriScreen", "Klik Kuis Bab ${module.order}. UUID kuis: $foundQuizUuid, Jumlah kuis di list: ${module.quizzes.size}")
                            
                            if (foundQuizUuid != null) {
                                if (currentProgress < minProgressToUnlockQuiz) {
                                    Toast.makeText(context, "baca materi terlebih dahulu", Toast.LENGTH_SHORT).show()
                                } else {
                                    onStartQuiz(foundQuizUuid, module.title, module.order)
                                }
                            } else {
                                // Menampilkan pesan debug jika kuis tetap null
                                Toast.makeText(context, "Kuis tidak ditemukan (Data backend: ${module.quizzes.size})", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ModuleLongItem(
    title: String,
    description: String,
    bgColor: Color,
    accentColor: Color,
    onReadClick: () -> Unit,
    onQuizClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    color = accentColor.copy(alpha = 0.1f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null, tint = accentColor, modifier = Modifier.size(24.dp))
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(text = title, fontWeight = FontWeight.Black, fontSize = 18.sp, color = Color(0xFF212121))
                    Text(text = description, fontSize = 12.sp, color = Color.Gray, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = onReadClick,
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Baca Materi", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
                
                Button(
                    onClick = onQuizClick,
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF6C00))
                ) {
                    Icon(Icons.Default.RocketLaunch, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Mulai Kuis", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}
