package com.dwipayana.literalearn.ui.viewmodel

import android.os.CountDownTimer
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dwipayana.literalearn.data.model.*
import com.dwipayana.literalearn.data.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.TimeUnit

class QuizViewModel : ViewModel() {

    private val _quizDetail = MutableStateFlow<QuizDetailResponse?>(null)
    val quizDetail: StateFlow<QuizDetailResponse?> = _quizDetail

    private val _attemptUuid = MutableStateFlow<String?>(null)
    
    private val _quizResult = MutableStateFlow<QuizResultResponse?>(null)
    val quizResult: StateFlow<QuizResultResponse?> = _quizResult

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _answerFeedback = MutableStateFlow<SubmitAnswerResponse?>(null)
    val answerFeedback: StateFlow<SubmitAnswerResponse?> = _answerFeedback

    private val _timeLeft = MutableStateFlow("10:00")
    val timeLeft: StateFlow<String> = _timeLeft

    private val _isTimeCritical = MutableStateFlow(false)
    val isTimeCritical: StateFlow<Boolean> = _isTimeCritical

    private var countDownTimer: CountDownTimer? = null

    fun startQuiz(userUuid: String, quizUuid: String) {
        resetQuizState()
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                Log.d("QuizDebug", "Langkah 1: POST /quiz-attempts (User: $userUuid, Quiz: $quizUuid)")
                val attemptResult = RetrofitClient.quizApiService.createAttempt(
                    CreateAttemptRequest(userUuid = userUuid, quizUuid = quizUuid)
                )
                
                if (attemptResult.isSuccessful && attemptResult.body()?.success == true) {
                    val data = attemptResult.body()?.data
                    val attemptUuid = data?.attemptUuid ?: data?.uuid
                    
                    if (attemptUuid != null) {
                        _attemptUuid.value = attemptUuid
                        Log.d("QuizDebug", "Langkah 1 Sukses: attemptUuid=$attemptUuid")

                        Log.d("QuizDebug", "Langkah 2: GET /quizzes/$quizUuid")
                        val detailRes = RetrofitClient.quizApiService.getQuizDetail(quizUuid)
                        if (detailRes.isSuccessful && detailRes.body()?.success == true) {
                            _quizDetail.value = detailRes.body()?.data
                            Log.d("QuizDebug", "Langkah 2 Sukses: ${detailRes.body()?.data?.questions?.size} soal dimuat")
                            startTimer()
                        } else {
                            _errorMessage.value = "Gagal memuat detail kuis"
                        }
                    } else {
                        _errorMessage.value = "ID Sesi tidak valid"
                    }
                } else {
                    _errorMessage.value = attemptResult.body()?.message ?: "Gagal memulai kuis"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Kesalahan Sistem: ${e.localizedMessage}"
                Log.e("QuizDebug", "startQuiz Error", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun startTimer() {
        countDownTimer?.cancel()
        countDownTimer = object : CountDownTimer(600000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val minutes = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)
                val seconds = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % 60
                _timeLeft.value = String.format(Locale.US, "%02d:%02d", minutes, seconds)
                _isTimeCritical.value = millisUntilFinished <= 60000
            }
            override fun onFinish() {
                _timeLeft.value = "00:00"
                submitQuiz()
            }
        }.start()
    }

    fun submitAnswer(questionUuid: String, chosenLabel: String) {
        val attemptUuid = _attemptUuid.value
        if (attemptUuid == null) {
            _errorMessage.value = "Sesi kuis tidak ditemukan"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                Log.d("QuizDebug", "Langkah 5: POST /answers Q=$questionUuid, Label=$chosenLabel")
                val result = RetrofitClient.quizApiService.submitAnswer(
                    attemptUuid, 
                    SubmitAnswerRequest(questionUuid, chosenLabel)
                )
                if (result.isSuccessful && result.body()?.success == true) {
                    val feedbackData = result.body()?.data
                    Log.d("QuizDebug", "Langkah 5 Sukses: isCorrect=${feedbackData?.isCorrect}")
                    _answerFeedback.value = feedbackData
                } else {
                    _errorMessage.value = result.body()?.message ?: "Gagal menyimpan jawaban"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Koneksi terputus saat menyimpan jawaban"
                Log.e("QuizDebug", "submitAnswer Error", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun resetFeedback() { _answerFeedback.value = null }

    fun submitQuiz() {
        val attemptUuid = _attemptUuid.value ?: return
        countDownTimer?.cancel()
        viewModelScope.launch {
            _isLoading.value = true
            try {
                Log.d("QuizDebug", "Langkah 9: POST /submit untuk $attemptUuid")
                val result = RetrofitClient.quizApiService.submitQuiz(attemptUuid)
                if (result.isSuccessful && result.body()?.success == true) {
                    _quizResult.value = result.body()?.data
                    Log.d("QuizDebug", "Kuis Selesai! Skor: ${result.body()?.data?.score}")
                }
            } catch (e: Exception) {
                _errorMessage.value = "Gagal menyelesaikan kuis"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun resetQuizState() {
        _quizDetail.value = null
        _quizResult.value = null
        _attemptUuid.value = null
        _answerFeedback.value = null
        _timeLeft.value = "10:00"
        _isTimeCritical.value = false
        countDownTimer?.cancel()
    }

    override fun onCleared() {
        super.onCleared()
        countDownTimer?.cancel()
    }
}
