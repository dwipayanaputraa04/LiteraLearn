package com.dwipayana.literalearn.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dwipayana.literalearn.data.model.Subject
import com.dwipayana.literalearn.data.model.UpdateProgressRequest
import com.dwipayana.literalearn.data.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SubjectViewModel : ViewModel() {

    private val _subjects = MutableStateFlow<List<Subject>>(emptyList())
    val subjects: StateFlow<List<Subject>> = _subjects

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun fetchSubjectsWithProgress(userUuid: String, grade: Int? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                // 1. Ambil semua subject
                val subjectResponse = RetrofitClient.subjectApiService.getAllSubjects(grade)
                
                // 2. Ambil semua progress user
                val progressResponse = RetrofitClient.apiService.getUserProgress(userUuid)

                if (subjectResponse.isSuccessful && progressResponse.isSuccessful) {
                    val allSubjects = subjectResponse.body()?.data ?: emptyList()
                    val allProgress = progressResponse.body()?.data ?: emptyList()

                    // 3. Mapping progress ke subject
                    val mappedSubjects = allSubjects.map { subject ->
                        val progress = allProgress.find { it.subject.uuid == subject.uuid }
                        subject.copy(currentProgress = progress?.progressPercent ?: 0.0)
                    }
                    
                    _subjects.value = mappedSubjects
                } else {
                    _errorMessage.value = "Gagal memuat data belajar"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Terjadi kesalahan: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateSubjectProgress(userUuid: String, subjectUuid: String, percentage: Int) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.updateSubjectProgress(
                    subjectUuid,
                    UpdateProgressRequest(userUuid, percentage)
                )
                if (response.isSuccessful) {
                    android.util.Log.d("SubjectViewModel", "Server Update Success. Refreshing list for $userUuid...")
                    // Paksa ambil data terbaru dari server untuk sinkronisasi UI
                    fetchSubjectsWithProgress(userUuid)
                }
            } catch (e: Exception) {
                android.util.Log.e("SubjectViewModel", "Error updating progress", e)
            }
        }
    }
}
