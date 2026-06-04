package com.dwipayana.literalearn.Screens

import android.util.Log
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dwipayana.literalearn.data.network.SessionManager
import com.dwipayana.literalearn.ui.viewmodel.QuizViewModel
import kotlinx.coroutines.delay

@Composable
fun KuisScreen(
    quizUuid: String,
    onBack: () -> Unit,
    onQuizPassed: (Int) -> Unit = {},
    viewModel: QuizViewModel = viewModel()
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val quizDetail by viewModel.quizDetail.collectAsState()
    val quizResult by viewModel.quizResult.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.errorMessage.collectAsState()
    val feedback by viewModel.answerFeedback.collectAsState()
    val timeLeft by viewModel.timeLeft.collectAsState()
    val isTimeCritical by viewModel.isTimeCritical.collectAsState()

    var currentQuestionIndex by remember { mutableIntStateOf(0) }
    var selectedOptionLabel by remember { mutableStateOf<String?>(null) }
    var isProcessingAnswer by remember { mutableStateOf(false) }

    LaunchedEffect(quizUuid) {
        val userUuid = sessionManager.getUserUuid()
        if (userUuid != null) viewModel.startQuiz(userUuid, quizUuid)
    }

    // Reset processing state if an error occurs
    LaunchedEffect(error) {
        if (error != null) {
            isProcessingAnswer = false
        }
    }

    // ALUR NAVIGASI OTOMATIS (Langkah 6 & 7)
    LaunchedEffect(feedback) {
        if (feedback != null) {
            Log.d("QuizDebug", "Langkah 6: Menampilkan feedback...")
            delay(1500)
            
            val detail = quizDetail
            if (detail != null && currentQuestionIndex < detail.questions.size - 1) {
                Log.d("QuizDebug", "Langkah 7: Pindah ke soal berikutnya")
                currentQuestionIndex++
                selectedOptionLabel = null
                viewModel.resetFeedback() 
                isProcessingAnswer = false // Lepas kunci loading setelah semua ter-reset
            } else if (detail != null) {
                Log.d("QuizDebug", "Langkah 9: Submit kuis")
                viewModel.submitQuiz()
                isProcessingAnswer = false
            }
        }
    }

    // Handle Finish
    LaunchedEffect(quizResult) {
        val result = quizResult
        if (result != null) {
            Log.d("QuizDebug", "Kuis terkirim ke server, memicu update progres UI")
            onQuizPassed(result.score ?: 0)
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
        if (quizResult != null) {
            QuizResultScreen(
                score = quizResult?.score ?: 0,
                isPassed = quizResult?.isPassed == true,
                onBack = {
                    viewModel.resetQuizState()
                    onBack() // Ini akan memicu reset state di MainScreen
                }
            )
        } else if (isLoading && quizDetail == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF006D8E))
            }
        } else if (error != null && quizDetail == null) {
            Column(Modifier.fillMaxSize(), Arrangement.Center, Alignment.CenterHorizontally) {
                Text(error ?: "Error", color = Color.Red, textAlign = TextAlign.Center, modifier = Modifier.padding(16.dp))
                Button(onClick = onBack) { Text("Kembali") }
            }
        } else {
            quizDetail?.let { detail ->
                val questions = detail.questions
                if (currentQuestionIndex < questions.size) {
                    val currentQuestion = questions[currentQuestionIndex]
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Header
                        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = onBack) { Icon(Icons.Default.Close, null) }
                            Text(
                                text = detail.module?.title ?: "Kuis",
                                modifier = Modifier.weight(1f), textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis
                            )
                            TimerDisplay(time = timeLeft, isCritical = isTimeCritical)
                        }

                        // Progress
                        Column(Modifier.padding(horizontal = 24.dp)) {
                            Text("Soal ${currentQuestionIndex + 1} / ${questions.size}", color = Color.Gray, fontSize = 13.sp)
                            Spacer(Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { (currentQuestionIndex + 1).toFloat() / questions.size.coerceAtLeast(1) },
                                modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                                color = Color(0xFF006D8E)
                            )
                        }

                        // Question
                        Column(Modifier.fillMaxWidth().weight(1f).padding(24.dp).verticalScroll(rememberScrollState())) {
                            Text(currentQuestion.text ?: "", fontSize = 20.sp, fontWeight = FontWeight.Bold, lineHeight = 28.sp)
                            Spacer(Modifier.height(30.dp))
                            
                            currentQuestion.options.forEach { option ->
                                val isCorrect = feedback?.correctOptionLabel == option.label
                                val isUser = selectedOptionLabel == option.label
                                val color = when {
                                    feedback != null && isCorrect -> Color(0xFFE8F5E9)
                                    feedback != null && isUser && feedback?.isCorrect == false -> Color(0xFFFDECEA)
                                    isUser -> Color(0xFFE3F2FD)
                                    else -> Color.White
                                }
                                val border = when {
                                    feedback != null && isCorrect -> Color(0xFF4CAF50)
                                    feedback != null && isUser && feedback?.isCorrect == false -> Color(0xFFF44336)
                                    isUser -> Color(0xFF1976D2)
                                    else -> Color(0xFFE2E8F0)
                                }
                                
                                QuizOption(option.label ?: "", option.text ?: "", color, border, feedback == null && !isProcessingAnswer) {
                                    if (feedback == null) selectedOptionLabel = option.label
                                }
                                Spacer(Modifier.height(12.dp))
                            }
                        }

                        // Bottom Action: Langkah 4 & 5
                        Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                            if (isProcessingAnswer) {
                                CircularProgressIndicator(color = Color(0xFF006D8E))
                            } else if (feedback == null) {
                                Button(
                                    onClick = {
                                        val qUuid = currentQuestion.uuid
                                        if (selectedOptionLabel != null && qUuid != null) {
                                            isProcessingAnswer = true
                                            viewModel.submitAnswer(qUuid, selectedOptionLabel!!)
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth().height(56.dp),
                                    enabled = selectedOptionLabel != null,
                                    shape = RoundedCornerShape(28.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF006D8E))
                                ) {
                                    Text(if (currentQuestionIndex == questions.size - 1) "Selesai" else "Simpan Jawaban", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TimerDisplay(time: String, isCritical: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = if (isCritical) 0.3f else 1f,
        animationSpec = infiniteRepeatable(tween(500), RepeatMode.Reverse), label = ""
    )
    Surface(
        modifier = Modifier.clip(RoundedCornerShape(20.dp)).alpha(if (isCritical) alpha else 1f),
        color = if (isCritical) Color(0xFFFDECEA) else Color(0xFFF1F5F9)
    ) {
        Row(Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Timer, null, tint = if (isCritical) Color.Red else Color(0xFF006D8E), modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text(time, color = if (isCritical) Color.Red else Color.Black, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun QuizOption(label: String, text: String, bgColor: Color, borderColor: Color, enabled: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick, enabled = enabled, shape = RoundedCornerShape(16.dp),
        color = bgColor, border = BorderStroke(2.dp, borderColor), modifier = Modifier.fillMaxWidth()
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(32.dp).background(borderColor.copy(alpha = 0.1f), CircleShape).border(1.dp, borderColor, CircleShape), contentAlignment = Alignment.Center) {
                Text(label, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(16.dp))
            Text(text, fontSize = 16.sp)
        }
    }
}

@Composable
fun QuizResultScreen(score: Int, isPassed: Boolean, onBack: () -> Unit) {
    Column(Modifier.fillMaxSize().background(Color.White).padding(32.dp), Arrangement.Center, Alignment.CenterHorizontally) {
        Icon(
            imageVector = if (isPassed) Icons.Default.CheckCircle else Icons.Default.Error,
            contentDescription = null, tint = if (isPassed) Color(0xFF4CAF50) else Color(0xFFF44336), modifier = Modifier.size(100.dp)
        )
        Spacer(Modifier.height(24.dp))
        Text("Kuis Selesai!", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
        Spacer(Modifier.height(8.dp))
        Text(if (isPassed) "Selamat! Kamu Lulus." else "Jangan menyerah, coba lagi ya!", color = Color.Gray, textAlign = TextAlign.Center)
        Spacer(Modifier.height(40.dp))
        Text("Nilai Kamu", fontSize = 14.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
        Text(score.toString(), fontSize = 80.sp, fontWeight = FontWeight.Black, color = if (isPassed) Color(0xFF4CAF50) else Color(0xFFF44336))
        Spacer(Modifier.height(60.dp))
        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF006D8E))
        ) {
            Text("Kembali ke Beranda", fontWeight = FontWeight.Bold)
        }
    }
}
