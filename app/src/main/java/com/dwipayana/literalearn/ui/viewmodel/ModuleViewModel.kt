package com.dwipayana.literalearn.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dwipayana.literalearn.data.model.Module
import com.dwipayana.literalearn.data.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ModuleViewModel : ViewModel() {

    private val _modules = MutableStateFlow<List<Module>>(emptyList())
    val modules: StateFlow<List<Module>> = _modules

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun fetchModules(subjectUuid: String? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                Log.d("ModuleViewModel", "Fetching modules for subjectUuid: $subjectUuid")
                val response = RetrofitClient.moduleApiService.getAllModules(subjectUuid)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body.success) {
                        val data = body.data ?: emptyList()
                        _modules.value = data
                        Log.d("ModuleViewModel", "Received ${data.size} modules")
                        data.forEach { module ->
                            Log.d("ModuleViewModel", "Module: ${module.title}, Quizzes: ${module.quizzes?.size ?: 0}")
                        }
                    } else {
                        _errorMessage.value = body?.message ?: "Gagal mengambil data modul"
                        Log.e("ModuleViewModel", "Error from API: ${body?.message}")
                    }
                } else {
                    _errorMessage.value = "Error ${response.code()}: ${response.message()}"
                    Log.e("ModuleViewModel", "HTTP Error: ${response.code()}")
                }
            } catch (e: Exception) {
                _errorMessage.value = "Terjadi kesalahan: ${e.localizedMessage}"
                Log.e("ModuleViewModel", "Exception during fetch: ${e.message}", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
}
