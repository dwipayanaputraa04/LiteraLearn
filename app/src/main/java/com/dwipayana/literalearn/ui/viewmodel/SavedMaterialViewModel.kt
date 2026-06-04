package com.dwipayana.literalearn.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dwipayana.literalearn.data.model.SaveMaterialRequest
import com.dwipayana.literalearn.data.model.SavedMaterialItem
import com.dwipayana.literalearn.data.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SavedMaterialViewModel : ViewModel() {

    private val _savedMaterials = MutableStateFlow<List<SavedMaterialItem>>(emptyList())
    val savedMaterials: StateFlow<List<SavedMaterialItem>> = _savedMaterials

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isBookmarked = MutableStateFlow(false)
    val isBookmarked: StateFlow<Boolean> = _isBookmarked

    fun fetchSavedMaterials(userUuid: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = RetrofitClient.apiService.getSavedMaterials(userUuid)
                if (response.isSuccessful) {
                    _savedMaterials.value = response.body()?.data ?: emptyList()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun checkBookmarkStatus(userUuid: String, materialUuid: String) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.getSavedMaterials(userUuid)
                if (response.isSuccessful) {
                    val list = response.body()?.data ?: emptyList()
                    _isBookmarked.value = list.any { it.material?.uuid == materialUuid }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun toggleBookmark(userUuid: String, materialUuid: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                if (_isBookmarked.value) {
                    // Unsave
                    val response = RetrofitClient.apiService.unsaveMaterial(materialUuid, userUuid)
                    if (response.isSuccessful) {
                        _isBookmarked.value = false
                    }
                } else {
                    // Save
                    val response = RetrofitClient.apiService.saveMaterial(SaveMaterialRequest(userUuid, materialUuid))
                    if (response.isSuccessful) {
                        _isBookmarked.value = true
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
}
