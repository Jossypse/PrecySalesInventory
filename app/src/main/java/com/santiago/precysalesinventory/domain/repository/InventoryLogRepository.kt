package com.santiago.precysalesinventory.domain.repository

import com.santiago.precysalesinventory.data.local.entity.InventoryLog
import kotlinx.coroutines.flow.Flow

interface InventoryLogRepository {
    fun getAllLogs(): Flow<List<InventoryLog>>
    suspend fun insertLog(log: InventoryLog)
}
