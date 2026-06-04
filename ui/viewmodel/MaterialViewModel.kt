package com.dwipayana.literalearn.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dwipayana.literalearn.data.model.MaterialDetail
import com.dwipayana.literalearn.data.model.MaterialListItem
import com.dwipayana.literalearn.data.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MaterialViewModel : ViewModel() {

    private val _materials = MutableStateFlow<List<MaterialListItem>>(emptyList())
    val materials: StateFlow<List<MaterialListItem>> = _materials

    private val _detail = MutableStateFlow<MaterialDetail?>(null)
    val detail: StateFlow<MaterialDetail?> = _detail

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun fetchMaterials(moduleUuid: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val response = RetrofitClient.materialApiService.getMaterialsByModule(moduleUuid)
                if (response.isSuccessful) {
                    _materials.value = response.body()?.data ?: emptyList()
                } else {
                    _errorMessage.value = "Gagal mengambil daftar materi"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Koneksi bermasalah: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchDetail(materialUuid: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val response = RetrofitClient.materialApiService.getMaterialDetail(materialUuid)
                if (response.isSuccessful) {
                    _detail.value = response.body()?.data
                } else {
                    _errorMessage.value = "Gagal mengambil detail materi"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Koneksi bermasalah"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
