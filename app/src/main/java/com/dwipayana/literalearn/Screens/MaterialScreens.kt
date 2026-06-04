package com.dwipayana.literalearn.Screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dwipayana.literalearn.data.network.SessionManager
import com.dwipayana.literalearn.ui.viewmodel.MaterialViewModel
import com.dwipayana.literalearn.ui.viewmodel.SavedMaterialViewModel
import kotlinx.coroutines.delay

// --- LAYAR DAFTAR MATERI ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaterialListScreen(
    moduleUuid: String,
    moduleTitle: String,
    onBack: () -> Unit,
    onMaterialClick: (String) -> Unit,
    viewModel: MaterialViewModel = viewModel()
) {
    val materials by viewModel.materials.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.errorMessage.collectAsState()

    LaunchedEffect(moduleUuid) {
        viewModel.fetchMaterials(moduleUuid)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(moduleTitle, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF006D8E), 
                    titleContentColor = Color.White, 
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Color(0xFF006D8E)) }
        } else if (error != null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(text = "Gagal memuat: $error", color = Color.Red, textAlign = TextAlign.Center)
            }
        } else if (materials.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(text = "Belum ada materi di bab ini", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .background(Color(0xFFF8FAFC)), 
                contentPadding = PaddingValues(16.dp), 
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(materials) { item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onMaterialClick(item.uuid) },
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            Modifier
                                .padding(16.dp)
                                .fillMaxWidth(), 
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                Modifier.size(40.dp), 
                                shape = RoundedCornerShape(12.dp), 
                                color = Color(0xFFE0F2F1)
                            ) {
                                Box(contentAlignment = Alignment.Center) { 
                                    Text(
                                        item.order.toString(), 
                                        fontWeight = FontWeight.ExtraBold, 
                                        color = Color(0xFF00796B),
                                        fontSize = 18.sp
                                    ) 
                                }
                            }
                            Spacer(Modifier.width(16.dp))
                            Text(
                                item.title, 
                                fontSize = 16.sp, 
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B),
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                Icons.Default.Book, 
                                contentDescription = null, 
                                tint = Color(0xFFCBD5E1),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// --- LAYAR DETAIL MATERI (KONTEN) ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaterialDetailScreen(
    materialUuid: String,
    onBack: () -> Unit,
    viewModel: MaterialViewModel = viewModel(),
    bookmarkViewModel: SavedMaterialViewModel = viewModel()
) {
    val detail by viewModel.detail.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isBookmarked by bookmarkViewModel.isBookmarked.collectAsState()
    val isActionLoading by bookmarkViewModel.isLoading.collectAsState()

    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val scrollState = rememberScrollState()

    LaunchedEffect(materialUuid) {
        viewModel.fetchDetail(materialUuid)
        val userUuid = sessionManager.getUserUuid()
        if (userUuid != null) {
            bookmarkViewModel.checkBookmarkStatus(userUuid, materialUuid)
        }
    }

    // Hitung Progress Membaca
    val readingProgress = if (scrollState.maxValue > 0) {
        scrollState.value.toFloat() / scrollState.maxValue.toFloat()
    } else 0f

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("Materi Belajar", fontWeight = FontWeight.ExtraBold) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.White,
                        titleContentColor = Color(0xFF0F172A),
                        navigationIconContentColor = Color(0xFF0F172A)
                    )
                )
                // Linear Progress Membaca
                LinearProgressIndicator(
                    progress = { readingProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp),
                    color = Color(0xFF006D8E),
                    trackColor = Color(0xFFE2E8F0)
                )
            }
        }
    ) { padding ->
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { 
                CircularProgressIndicator(color = Color(0xFF006D8E)) 
            }
        } else if (detail != null) {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .background(Color.White)
                    .verticalScroll(scrollState)
            ) {
                // Header Image Placeholder / Icon
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color(0xFF006D8E), Color(0xFF00ACC1))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.MenuBook, 
                        contentDescription = null, 
                        tint = Color.White.copy(alpha = 0.2f),
                        modifier = Modifier.size(120.dp)
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(
                            color = Color.White.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = detail?.module?.title ?: "Materi",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Column(modifier = Modifier.padding(24.dp)) {
                    // Title
                    Text(
                        text = detail?.title ?: "", 
                        fontSize = 28.sp, 
                        fontWeight = FontWeight.Black, 
                        color = Color(0xFF0F172A),
                        lineHeight = 36.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // PROMINENT SAVE BUTTON
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable(enabled = !isActionLoading) {
                                val userUuid = sessionManager.getUserUuid()
                                if (userUuid != null) {
                                    bookmarkViewModel.toggleBookmark(userUuid, materialUuid)
                                }
                            },
                        color = if (isBookmarked) Color(0xFFFFFDE7) else Color(0xFFF8FAFC),
                        border = BorderStroke(
                            1.dp, 
                            if (isBookmarked) Color(0xFFFBC02D) else Color(0xFFE2E8F0)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            if (isActionLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(
                                    imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                    contentDescription = null,
                                    tint = if (isBookmarked) Color(0xFFFBC02D) else Color(0xFF64748B),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = if (isBookmarked) "Tersimpan di Favorit" else "Simpan Materi",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = if (isBookmarked) Color(0xFF827717) else Color(0xFF1E293B)
                                    )
                                    Text(
                                        text = "Baca kapan saja tanpa mencarinya lagi",
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(30.dp))
                    
                    // Content Body
                    Text(
                        text = detail?.content ?: "",
                        fontSize = 17.sp,
                        lineHeight = 30.sp, // Jarak antar baris yang nyaman
                        color = Color(0xFF334155),
                        textAlign = TextAlign.Justify
                    )
                    
                    Spacer(Modifier.height(80.dp))
                }
            }
        }
    }
}
