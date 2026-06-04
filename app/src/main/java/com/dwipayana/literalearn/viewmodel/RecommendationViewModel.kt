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

    private val _recommendationMessage = MutableStateFlow<String>("Memuat rekomendasi...")
    val recommendationMessage: StateFlow<String> = _recommendationMessage

    init {
        refreshRecommendation()
    }

    /**
     * Mengambil data terbaru dari SessionManager dan memperbarui rekomendasi secara Real-time.
     */
    fun refreshRecommendation() {
        val history = sessionManager.getQuizHistory()
        val historyArray = history.toFloatArray()
        val lastScore = sessionManager.getLastQuizScore()
        val lastModule = sessionManager.getLastQuizModule()

        // Cek apakah user benar-benar baru (tidak ada riwayat kuis dan poin masih 0)
        val isNewUser = (lastModule == null || lastModule.isEmpty()) && history.all { it == 0f }

        if (isNewUser) {
            // SKENARIO 1: USER BARU (Belum pernah kuis)
            val result = recommendationHelper.predictModule(historyArray)
            
            // Berikan fallback ke Bab 1 jika ML belum memberikan hasil yang jelas atau error
            _predictedModule.value = if (result.contains("Error", ignoreCase = true) || 
                result.contains("initialized", ignoreCase = true) || 
                result.isEmpty()) {
                "Bab 1: Hitung Dasar dan Bangun Datar"
            } else {
                result
            }
            
            _recommendationMessage.value = "Selamat datang di LiteraLearn! Yuk mulai petualangan belajarmu dengan modul rekomendasi berikut:"
        } else if (lastScore < 70f && lastModule != null) {
            // SKENARIO 2: REMEDIAL (Nilai kuis terakhir < 70)
            _predictedModule.value = lastModule
            _recommendationMessage.value = "Nilai kuismu di modul \"$lastModule\" masih di bawah 70. Yuk, tonton kembali videonya agar lebih paham!"
        } else {
            // SKENARIO 3: PROGRES (Berhasil kuis atau kuis ulang sukses)
            val result = recommendationHelper.predictModule(historyArray)
            _predictedModule.value = result
            _recommendationMessage.value = "Hebat! Kamu sudah menguasai modul sebelumnya dengan baik. Berdasarkan analisis ML, langkah selanjutnya adalah:"
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
