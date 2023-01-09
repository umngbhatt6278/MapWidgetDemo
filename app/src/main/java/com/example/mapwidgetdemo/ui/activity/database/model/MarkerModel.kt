package com.example.mapwidgetdemo.ui.activity.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.android.gms.maps.model.LatLng
import java.util.Random

@Entity(tableName = "markers")
class MarkerModel(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "latitude") val latitude: Double,
    @ColumnInfo(name = "longitude") val longitude: Double,
    @ColumnInfo(name = "videopath") val videopath: String,
    @ColumnInfo(name = "videoname") val videoname: String,
    @ColumnInfo(name = "isserver") val isserver: Boolean = false,
)

