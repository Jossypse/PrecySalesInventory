package com.santiago.precysalesinventory.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.santiago.precysalesinventory.data.local.dao.InventoryLogDao
import com.santiago.precysalesinventory.data.local.dao.ProductDao
import com.santiago.precysalesinventory.data.local.dao.SaleDao
import com.santiago.precysalesinventory.data.local.entity.InventoryLog
import com.santiago.precysalesinventory.data.local.entity.Product
import com.santiago.precysalesinventory.data.local.entity.Sale
import com.santiago.precysalesinventory.data.local.entity.SaleItem

@Database(entities = [Product::class, Sale::class, SaleItem::class, InventoryLog::class], version = 4, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun saleDao(): SaleDao
    abstract fun inventoryLogDao(): InventoryLogDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `sales` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                        `totalAmount` REAL NOT NULL, 
                        `paymentAmount` REAL NOT NULL, 
                        `change` REAL NOT NULL, 
                        `timestamp` INTEGER NOT NULL
                    )
                """)
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `sale_items` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                        `saleId` INTEGER NOT NULL, 
                        `productId` INTEGER NOT NULL, 
                        `productName` TEXT NOT NULL, 
                        `priceAtSale` REAL NOT NULL, 
                        `quantity` INTEGER NOT NULL
                    )
                """)
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `inventory_logs` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                        `productName` TEXT NOT NULL, 
                        `capitalPrice` REAL NOT NULL, 
                        `sellingPrice` REAL NOT NULL, 
                        `quantityAdded` INTEGER NOT NULL, 
                        `imageUri` TEXT, 
                        `timestamp` INTEGER NOT NULL
                    )
                """)
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `sale_items` ADD COLUMN `capitalAtSale` REAL NOT NULL DEFAULT 0.0")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "precy_sales_inventory_db"
                )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
