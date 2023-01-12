package com.example.mapwidgetdemo.ui.activity.database.dao

import androidx.room.*
import com.example.mapwidgetdemo.ui.activity.database.model.MarkerModel
import kotlinx.coroutines.flow.Flow

@Dao
interface MarkerDao {

    @Query("SELECT * FROM markers ORDER BY id ASC")
    fun getallMarkers(): Flow<List<MarkerModel>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(word: MarkerModel)

    @Update
    fun updateNote(note: MarkerModel)

    @Delete
    fun delete(model: MarkerModel)

    /*@Query("DELETE FROM markers")
    suspend fun deleteAll()*/
}