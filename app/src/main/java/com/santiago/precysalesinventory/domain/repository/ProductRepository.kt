package com.santiago.precysalesinventory.domain.repository

import com.santiago.precysalesinventory.data.local.entity.Product
import kotlinx.coroutines.flow.Flow

interface ProductRepository {
    fun getAllProducts(): Flow<List<Product>>
    suspend fun getProductById(id: Int): Product?
    suspend fun insertProduct(product: Product)
    suspend fun updateProduct(product: Product)
    suspend fun deleteProduct(product: Product)
    suspend fun deductStock(id: Int, amount: Int)
    suspend fun addStock(id: Int, amount: Int)
}
