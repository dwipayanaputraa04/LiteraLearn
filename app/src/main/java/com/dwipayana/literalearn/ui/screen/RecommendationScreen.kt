package com.dwipayana.literalearn.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dwipayana.literalearn.viewmodel.RecommendationViewModel

@Composable
fun RecommendationScreen(
    score: Int,
    topic: String,
    wrongAnswers: Int,
    viewModel: RecommendationViewModel,
    onBackToHome: () -> Unit
) {
    // Membaca state prediksi modul dari ViewModel
    val predictedModule by viewModel.predictedModule.collectAsState()

    // Jika skor rendah, jalankan analisis ML
    LaunchedEffect(Unit) {
        if (score < 70) {
            viewModel.refreshRecommendation()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F9FF))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Hasil Belajar Kamu",
            fontSize = 26.sp,
            fontWeight = FontWeight.Black,
            color = Color(0xFF006D8E)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // --- Kartu Skor ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                RecommendationRow("Skor Kuis", "$score%")
                RecommendationRow("Topik Pelajaran", topic)
                RecommendationRow("Jawaban Salah", wrongAnswers.toString())
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // --- Logika Rekomendasi ---
        if (score < 70) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Saran untuk Kamu",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333)
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                if (predictedModule != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBBDEFB))
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                text = "Berdasarkan analisis cerdas, kamu disarankan mempelajari:",
                                fontSize = 13.sp,
                                color = Color.DarkGray
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = predictedModule!!,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF006D8E),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Tonton video pembelajaran materi ini untuk memperkuat pemahamanmu.",
                                fontSize = 12.sp,
                                color = Color.Gray,
                                lineHeight = 18.sp
                            )
                        }
                    }
                } else {
                    Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFF006D8E))
                    }
                }
            }
        } else {
            // Tampilan jika skor bagus
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
            ) {
                Text(
                    text = "Luar biasa! Kamu sudah memahami materi $topic dengan sangat baik. Teruskan belajarmu!",
                    fontSize = 15.sp,
                    color = Color(0xFF2E7D32),
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // --- Tombol Kembali ---
        Button(
            onClick = onBackToHome,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF006D8E))
        ) {
            Text("Kembali ke Beranda", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}

@Composable
fun RecommendationRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = Color.Gray, fontSize = 15.sp)
        Text(text = value, fontWeight = FontWeight.Bold, color = Color(0xFF212121), fontSize = 15.sp)
    }
}
