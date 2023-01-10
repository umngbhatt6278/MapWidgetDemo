package com.example.mapwidgetdemo.ui.activity.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
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

    /*@Query("DELETE FROM markers")
    suspend fun deleteAll()*/
}