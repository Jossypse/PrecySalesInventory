package com.santiago.precysalesinventory.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sales")
data class Sale(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val totalAmount: Double,
    val paymentAmount: Double,
    val change: Double,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "sale_items")
data class SaleItem(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val saleId: Int,
    val productId: Int,
    val productName: String,
    val priceAtSale: Double,
    val capitalAtSale: Double,
    val quantity: Int
)
