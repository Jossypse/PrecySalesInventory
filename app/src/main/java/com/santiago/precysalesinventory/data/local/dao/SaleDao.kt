package com.santiago.precysalesinventory.data.local.dao

import androidx.room.*
import com.santiago.precysalesinventory.data.local.entity.Sale
import com.santiago.precysalesinventory.data.local.entity.SaleItem
import kotlinx.coroutines.flow.Flow

@Dao
interface SaleDao {
    @Insert
    suspend fun insertSale(sale: Sale): Long

    @Insert
    suspend fun insertSaleItems(items: List<SaleItem>)

    @Query("SELECT * FROM sales ORDER BY timestamp DESC")
    fun getAllSales(): Flow<List<Sale>>

    @Query("SELECT * FROM sale_items WHERE saleId = :saleId")
    suspend fun getItemsForSale(saleId: Int): List<SaleItem>

    @Query("SELECT * FROM sale_items")
    fun getAllSaleItems(): Flow<List<SaleItem>>

    @Transaction
    suspend fun completeTransaction(sale: Sale, items: List<SaleItem>) {
        val id = insertSale(sale)
        val itemsWithId = items.map { it.copy(saleId = id.toInt()) }
        insertSaleItems(itemsWithId)
    }
}
