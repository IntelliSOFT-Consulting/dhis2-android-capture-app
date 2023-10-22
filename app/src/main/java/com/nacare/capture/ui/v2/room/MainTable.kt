package com.nacare.capture.ui.v2.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable
import java.util.Date

@Entity(tableName = "organizations")
data class OrganizationData(
    val name: String,
    val code: String,
    @ColumnInfo(name = "children")
    val children: String,
    @ColumnInfo(name = "created_at")
    val createdAt: Date = Date(),
    @ColumnInfo(name = "updated_at")
    val updatedAt: Date = Date(),
) : Serializable {
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null
}

@Entity(tableName = "programDataValues")
data class ProgramDataValues(
    val enrollmentUid: String,
    val programUid: String,
    val attributeUid: String,
    val dataValue: String,
    val completed: Boolean = false,
    val synced: Boolean = false,
    @ColumnInfo(name = "created_at")
    val createdAt: Date = Date(),
    @ColumnInfo(name = "updated_at")
    val updatedAt: Date = Date(),
) : Serializable {
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null
}

@Entity(tableName = "trackedEntityValues")
data class TrackedEntityValues(
    val entityUid: String,
    val attributeUid: String,
    val dataValue: String,
    val completed: Boolean = false,
    val synced: Boolean = false,
    @ColumnInfo(name = "created_at")
    val createdAt: Date = Date(),
    @ColumnInfo(name = "updated_at")
    val updatedAt: Date = Date(),
) : Serializable {
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null
}

