package com.dwipayana.literalearn.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.dwipayana.literalearn.data.network.SessionManager
import com.dwipayana.literalearn.ml.VideoRecommendationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class RecommendationViewModel(context: Context) : ViewModel() {

    private val recommendationHelper = VideoRecommendationHelper(context)
    private val sessionManager = SessionManager(context)

    private val _predictedModule = MutableStateFlow<String?>(null)
    val predictedModule: StateFlow<String?> = _predictedModule

    private val _predictedVideoUrl = MutableStateFlow<String>("https://www.youtube.com")
    val predictedVideoUrl: StateFlow<String> = _predictedVideoUrl

    private val _recommendationMessage = MutableStateFlow<String>("Memuat rekomendasi...")
    val recommendationMessage: StateFlow<String> = _recommendationMessage

    // Daftar Mapping Modul ke Link YouTube
    private val videoLinks = mapOf(
        "1. Hitung Dasar dan Bangun Datar" to "https://youtu.be/EB2rOeSnheY?si=yFn1A1_7uui8zJmB",
        "2. Pengukuran Waktu, Panjang, dan Berat" to "https://youtu.be/xKwmhIvMovs?si=OEaqpPzeSqOvXNme",
        "3. Membaca Suku Kata dan Merangkai Kalimat" to "https://youtu.be/67Lds8dGXWw?si=2N9WXXIE23JjUAJf",
        "4. Membaca Cerita dan Menulis Kalimat" to "https://youtu.be/IIZYLaCpm9M?si=f5MbbHUdS78zZ8XT",
        "5. Mengenal Anggota Tubuh dan Lingkungan Sekitar" to "https://youtu.be/TUymwsAu3KI?si=xXbuAzXxCGGk0Jim",
        "6. Merawat Tumbuhan dan Hewan" to "https://youtu.be/EUaU2YPYYIU?si=yWMx2q6Wvgno_QAD",
        "7. Basic Greeting, Color, and Number" to "https://youtu.be/4LLRkQoW5s4?si=RDm2WeDXCkVSxWcg",
        "8. Parts of the Body" to "https://youtu.be/fmD-oemQiVE?si=zQIJQc890LtM52ro"
    )

    init {
        refreshRecommendation()
    }

    fun refreshRecommendation() {
        val history = sessionManager.getQuizHistory()
        val historyArray = history.toFloatArray()
        val lastScore = sessionManager.getLastQuizScore()
        val lastModule = sessionManager.getLastQuizModule()

        val isNewUser = (lastModule == null || lastModule.isEmpty()) && history.all { it == 0f }

        val result = if (isNewUser) {
            val mlRes = recommendationHelper.predictModule(historyArray)
            if (mlRes.contains("Error") || mlRes.contains("initialized") || mlRes.isEmpty()) {
                "1. Hitung Dasar dan Bangun Datar"
            } else mlRes
        } else if (lastScore < 70f && lastModule != null) {
            lastModule
        } else {
            recommendationHelper.predictModule(historyArray)
        }

        _predictedModule.value = result
        _predictedVideoUrl.value = videoLinks[result] ?: "https://www.youtube.com"

        // Update Pesan
        _recommendationMessage.value = when {
            isNewUser -> "Selamat datang di LiteraLearn! Yuk mulai petualangan belajarmu dengan modul rekomendasi berikut:"
            lastScore < 70f -> "Nilai kuismu di modul ini masih di bawah 70. Yuk, tonton kembali videonya agar lebih paham!"
            else -> "Hebat! Kamu sudah menguasai modul sebelumnya. Berdasarkan analisis ML, langkah selanjutnya adalah:"
        }
    }

    override fun onCleared() {
        super.onCleared()
        recommendationHelper.close()
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(RecommendationViewModel::class.java)) {
                return RecommendationViewModel(context) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
