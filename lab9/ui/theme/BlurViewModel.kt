package com.example.lab9.ui.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.work.WorkInfo
import com.example.lab9.BluromaticApplication
import com.example.lab9.KEY_IMAGE_URI
import com.example.lab9.data.BlurAmountData
import com.example.lab9.data.BluromaticRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import android.net.Uri
import kotlinx.coroutines.flow.MutableStateFlow

class BlurViewModel(
    private val bluromaticRepository: BluromaticRepository
) : ViewModel() {

    internal val blurAmount = BlurAmountData.blurAmount

    val blurUiState: StateFlow<BlurUiState> =
        bluromaticRepository.outputWorkInfo
            .map { workInfo ->
                when {
                    workInfo == null -> BlurUiState.Default
                    workInfo.state == WorkInfo.State.SUCCEEDED -> {
                        val outputUri = workInfo.outputData.getString(KEY_IMAGE_URI) ?: ""
                        BlurUiState.Complete(outputUri)
                    }
                    workInfo.state == WorkInfo.State.RUNNING ||
                            workInfo.state == WorkInfo.State.ENQUEUED ||
                            workInfo.state == WorkInfo.State.BLOCKED -> {
                        BlurUiState.Loading
                    }
                    else -> BlurUiState.Default
                }
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                BlurUiState.Default
            )

    private val _imageUri = MutableStateFlow<Uri?>(null)
    val imageUri: StateFlow<Uri?> = _imageUri

    fun setImageUri(uri: Uri) {
        _imageUri.value = uri
        bluromaticRepository.setImageUri(uri)
    }

    fun applyBlur(blurLevel: Int) {
        bluromaticRepository.applyBlur(blurLevel)
    }

    fun cancelWork() {
        bluromaticRepository.cancelWork()
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val bluromaticRepository =
                    (this[APPLICATION_KEY] as BluromaticApplication).container.bluromaticRepository
                BlurViewModel(bluromaticRepository)
            }
        }
    }
}

sealed interface BlurUiState {
    data object Default : BlurUiState
    data object Loading : BlurUiState
    data class Complete(val outputUri: String) : BlurUiState
}