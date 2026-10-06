package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sketch_projects")
data class SketchProjectEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val imageUri: String? = null,
    val imagePath: String? = null,
    val stepsJson: String,
    val strokesJson: String,
    val currentStepIndex: Int = 0,
    val totalSteps: Int = 0,
    val completedPercent: Int = 0,
    val detailLevel: String = "NORMAL",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
