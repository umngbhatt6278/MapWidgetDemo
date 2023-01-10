package com.example.mapwidgetdemo.ui.activity.database

import com.example.mapwidgetdemo.ui.activity.database.dao.MarkerDao
import com.example.mapwidgetdemo.ui.activity.database.model.MarkerModel
import kotlinx.coroutines.flow.Flow


class MarkerRepository(private val wordDao: MarkerDao) {

    // Room executes all queries on a separate thread.
    // Observed Flow will notify the observer when the data has changed.


    val allWords: Flow<List<MarkerModel>> = wordDao.getallMarkers()


    // By default Room runs suspend queries off the main thread, therefore, we don't need to
    // implement anything else to ensure we're not doing long running database work
    // off the main thread.


    suspend fun insert(word: MarkerModel) {
        wordDao.insert(word)
    }

    suspend fun update(word: MarkerModel) {
        wordDao.updateNote(word)
    }
}