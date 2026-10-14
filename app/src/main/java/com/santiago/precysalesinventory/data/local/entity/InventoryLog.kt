package com.santiago.precysalesinventory.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "inventory_logs")
data class InventoryLog(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val productName: String,
    val capitalPrice: Double,
    val sellingPrice: Double,
    val quantityAdded: Int,
    val imageUri: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
