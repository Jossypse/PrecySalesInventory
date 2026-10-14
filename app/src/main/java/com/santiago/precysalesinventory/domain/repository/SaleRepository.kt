package com.santiago.precysalesinventory.domain.repository

import com.santiago.precysalesinventory.data.local.entity.Sale
import com.santiago.precysalesinventory.data.local.entity.SaleItem
import kotlinx.coroutines.flow.Flow

interface SaleRepository {
    fun getAllSales(): Flow<List<Sale>>
    fun getAllSaleItems(): Flow<List<SaleItem>>
    suspend fun getItemsForSale(saleId: Int): List<SaleItem>
    suspend fun completeTransaction(sale: Sale, items: List<SaleItem>)
}
