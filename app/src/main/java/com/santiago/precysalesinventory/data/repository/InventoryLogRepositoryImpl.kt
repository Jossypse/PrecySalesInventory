package com.santiago.precysalesinventory.data.repository

import com.santiago.precysalesinventory.data.local.dao.InventoryLogDao
import com.santiago.precysalesinventory.data.local.entity.InventoryLog
import com.santiago.precysalesinventory.domain.repository.InventoryLogRepository
import kotlinx.coroutines.flow.Flow

class InventoryLogRepositoryImpl(
    private val inventoryLogDao: InventoryLogDao
) : InventoryLogRepository {
    override fun getAllLogs(): Flow<List<InventoryLog>> = inventoryLogDao.getAllLogs()

    override suspend fun insertLog(log: InventoryLog) = inventoryLogDao.insertLog(log)
}
