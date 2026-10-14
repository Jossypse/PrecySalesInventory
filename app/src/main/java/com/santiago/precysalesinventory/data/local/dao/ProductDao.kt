package com.santiago.precysalesinventory.data.local.dao

import androidx.room.*
import com.santiago.precysalesinventory.data.local.entity.Product
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: Product)

    @Update
    suspend fun updateProduct(product: Product)

    @Delete
    suspend fun deleteProduct(product: Product)

    @Query("SELECT * FROM products ORDER BY dateAdded DESC")
    fun getAllProducts(): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getProductById(id: Int): Product?

    @Query("UPDATE products SET quantity = quantity - :amount WHERE id = :id")
    suspend fun deductStock(id: Int, amount: Int)

    @Query("UPDATE products SET quantity = quantity + :amount WHERE id = :id")
    suspend fun addStock(id: Int, amount: Int)
}
