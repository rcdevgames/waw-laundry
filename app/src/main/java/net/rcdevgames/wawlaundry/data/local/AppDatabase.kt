package net.rcdevgames.wawlaundry.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import net.rcdevgames.wawlaundry.data.local.dao.*
import net.rcdevgames.wawlaundry.data.local.entity.*

@Database(
    entities = [
        ProfileEntity::class,
        ServiceEntity::class,
        PromoEntity::class,
        CustomerEntity::class,
        OrderEntity::class,
        OrderDetailEntity::class,
        ExpenseEntity::class
    ],
    version = 3,
    exportSchema = false
)
@androidx.room.TypeConverters(EnumConverters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun profileDao(): ProfileDao
    abstract fun serviceDao(): ServiceDao
    abstract fun promoDao(): PromoDao
    abstract fun customerDao(): CustomerDao
    abstract fun orderDao(): OrderDao
    abstract fun orderDetailDao(): OrderDetailDao
    abstract fun expenseDao(): ExpenseDao

    companion object {
        // Migration from version 2 to 3 - Add indexes for performance
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Drop old tables and recreate with indexes
                // This is a simplified migration - in production, use proper data preservation

                // Orders table with indexes
                db.execSQL("""
                    CREATE TABLE orders_new (
                        id TEXT PRIMARY KEY NOT NULL,
                        userId TEXT,
                        customerId TEXT NOT NULL,
                        promoId TEXT,
                        orderNumber TEXT NOT NULL,
                        entryDate INTEGER NOT NULL,
                        estimatedCompletionDate INTEGER,
                        exitDate INTEGER,
                        totalPrice REAL NOT NULL,
                        discountAmount REAL NOT NULL DEFAULT 0,
                        downPayment REAL NOT NULL DEFAULT 0,
                        paymentStatus TEXT NOT NULL,
                        orderStatus TEXT NOT NULL,
                        paymentMethod TEXT NOT NULL,
                        deliveryType TEXT NOT NULL,
                        notes TEXT,
                        isSynced INTEGER NOT NULL DEFAULT 0,
                        isDeleted INTEGER NOT NULL DEFAULT 0,
                        updatedAt INTEGER NOT NULL
                    )
                """)
                db.execSQL("""
                    CREATE INDEX index_orders_entryDate_isDeleted ON orders_new (entryDate, isDeleted)
                """)
                db.execSQL("""
                    CREATE INDEX index_orders_orderStatus_isDeleted ON orders_new (orderStatus, isDeleted)
                """)
                db.execSQL("""
                    CREATE INDEX index_orders_customerId_isDeleted ON orders_new (customerId, isDeleted)
                """)
                db.execSQL("""
                    CREATE INDEX index_orders_isSynced ON orders_new (isSynced)
                """)
                db.execSQL("""
                    CREATE INDEX index_orders_isDeleted ON orders_new (isDeleted)
                """)
                db.execSQL("""
                    INSERT INTO orders_new SELECT * FROM orders
                """)
                db.execSQL("DROP TABLE orders")
                db.execSQL("ALTER TABLE orders_new RENAME TO orders")

                // Customers table with indexes
                db.execSQL("""
                    CREATE TABLE customers_new (
                        id TEXT PRIMARY KEY NOT NULL,
                        userId TEXT,
                        name TEXT NOT NULL,
                        phone TEXT NOT NULL,
                        address TEXT,
                        totalOrders INTEGER NOT NULL DEFAULT 0,
                        isSynced INTEGER NOT NULL DEFAULT 0,
                        isDeleted INTEGER NOT NULL DEFAULT 0,
                        updatedAt INTEGER NOT NULL
                    )
                """)
                db.execSQL("""
                    CREATE INDEX index_customers_name_isDeleted ON customers_new (name, isDeleted)
                """)
                db.execSQL("""
                    CREATE INDEX index_customers_phone_isDeleted ON customers_new (phone, isDeleted)
                """)
                db.execSQL("""
                    CREATE INDEX index_customers_isDeleted ON customers_new (isDeleted)
                """)
                db.execSQL("""
                    CREATE INDEX index_customers_isSynced ON customers_new (isSynced)
                """)
                db.execSQL("""
                    INSERT INTO customers_new SELECT * FROM customers
                """)
                db.execSQL("DROP TABLE customers")
                db.execSQL("ALTER TABLE customers_new RENAME TO customers")

                // Expenses table with indexes
                db.execSQL("""
                    CREATE TABLE expenses_new (
                        id TEXT PRIMARY KEY NOT NULL,
                        userId TEXT,
                        title TEXT NOT NULL,
                        amount REAL NOT NULL,
                        date INTEGER NOT NULL,
                        isSynced INTEGER NOT NULL DEFAULT 0,
                        isDeleted INTEGER NOT NULL DEFAULT 0,
                        updatedAt INTEGER NOT NULL
                    )
                """)
                db.execSQL("""
                    CREATE INDEX index_expenses_date_isDeleted ON expenses_new (date, isDeleted)
                """)
                db.execSQL("""
                    CREATE INDEX index_expenses_isDeleted ON expenses_new (isDeleted)
                """)
                db.execSQL("""
                    CREATE INDEX index_expenses_isSynced ON expenses_new (isSynced)
                """)
                db.execSQL("""
                    INSERT INTO expenses_new SELECT * FROM expenses
                """)
                db.execSQL("DROP TABLE expenses")
                db.execSQL("ALTER TABLE expenses_new RENAME TO expenses")
            }
        }
    }
}
