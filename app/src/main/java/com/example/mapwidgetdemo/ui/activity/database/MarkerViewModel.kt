package com.example.mapwidgetdemo.ui.activity.database

import androidx.lifecycle.*
import com.example.mapwidgetdemo.ui.activity.database.model.MarkerModel
import kotlinx.coroutines.launch

class MarkerViewModel(private val repository: MarkerRepository) : ViewModel() {

    val allWords: LiveData<List<MarkerModel>> = repository.allWords.asLiveData()


    fun insert(word: MarkerModel) = viewModelScope.launch {
        repository.insert(word)
    }
}

class WordViewModelFactory(private val repository: MarkerRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MarkerViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MarkerViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}