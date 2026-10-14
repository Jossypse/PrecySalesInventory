package com.santiago.precysalesinventory.data.local.dao

import androidx.room.*
import com.santiago.precysalesinventory.data.local.entity.InventoryLog
import kotlinx.coroutines.flow.Flow

@Dao
interface InventoryLogDao {
    @Insert
    suspend fun insertLog(log: InventoryLog)

    @Query("SELECT * FROM inventory_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<InventoryLog>>
}
