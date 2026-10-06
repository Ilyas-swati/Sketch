package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SketchDao {
    @Query("SELECT * FROM sketch_projects ORDER BY updatedAt DESC")
    fun getAllProjects(): Flow<List<SketchProjectEntity>>

    @Query("SELECT * FROM sketch_projects WHERE id = :id LIMIT 1")
    suspend fun getProjectById(id: String): SketchProjectEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(project: SketchProjectEntity)

    @Delete
    suspend fun deleteProject(project: SketchProjectEntity)

    @Query("DELETE FROM sketch_projects WHERE id = :id")
    suspend fun deleteProjectById(id: String)
}
