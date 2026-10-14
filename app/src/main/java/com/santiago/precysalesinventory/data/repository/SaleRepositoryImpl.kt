package com.santiago.precysalesinventory.data.repository

import com.santiago.precysalesinventory.data.local.dao.SaleDao
import com.santiago.precysalesinventory.data.local.entity.Sale
import com.santiago.precysalesinventory.data.local.entity.SaleItem
import com.santiago.precysalesinventory.domain.repository.SaleRepository
import kotlinx.coroutines.flow.Flow

class SaleRepositoryImpl(
    private val saleDao: SaleDao
) : SaleRepository {
    override fun getAllSales(): Flow<List<Sale>> = saleDao.getAllSales()

    override fun getAllSaleItems(): Flow<List<SaleItem>> = saleDao.getAllSaleItems()

    override suspend fun getItemsForSale(saleId: Int): List<SaleItem> = saleDao.getItemsForSale(saleId)

    override suspend fun completeTransaction(sale: Sale, items: List<SaleItem>) =
        saleDao.completeTransaction(sale, items)
}
