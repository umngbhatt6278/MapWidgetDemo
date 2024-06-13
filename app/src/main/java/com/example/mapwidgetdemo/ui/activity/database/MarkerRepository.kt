package com.example.mapwidgetdemo.ui.activity.database

import com.example.mapwidgetdemo.ui.activity.database.dao.MarkerDao
import com.example.mapwidgetdemo.ui.activity.database.model.MarkerModel
import kotlinx.coroutines.flow.Flow


class MarkerRepository(private val wordDao: MarkerDao) {

    val allWords: Flow<List<MarkerModel>> = wordDao.getallMarkers()

    suspend fun insert(word: MarkerModel) {
        wordDao.insert(word)
    }

    suspend fun update(word: MarkerModel) {
        wordDao.updateNote(word)
    }

    suspend fun delete(word: MarkerModel) {
        wordDao.delete(word)
    }
}