package com.santiago.precysalesinventory.data.repository

import com.santiago.precysalesinventory.data.local.dao.ProductDao
import com.santiago.precysalesinventory.data.local.entity.Product
import com.santiago.precysalesinventory.domain.repository.ProductRepository
import kotlinx.coroutines.flow.Flow

class ProductRepositoryImpl(
    private val productDao: ProductDao
) : ProductRepository {
    override fun getAllProducts(): Flow<List<Product>> = productDao.getAllProducts()

    override suspend fun getProductById(id: Int): Product? = productDao.getProductById(id)

    override suspend fun insertProduct(product: Product) = productDao.insertProduct(product)

    override suspend fun updateProduct(product: Product) = productDao.updateProduct(product)

    override suspend fun deleteProduct(product: Product) = productDao.deleteProduct(product)

    override suspend fun deductStock(id: Int, amount: Int) = productDao.deductStock(id, amount)

    override suspend fun addStock(id: Int, amount: Int) = productDao.addStock(id, amount)
}
