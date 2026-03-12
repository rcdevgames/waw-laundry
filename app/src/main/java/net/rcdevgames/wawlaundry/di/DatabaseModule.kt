package net.rcdevgames.wawlaundry.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.sqlcipher.database.SupportFactory
import net.rcdevgames.wawlaundry.data.local.AppDatabase
import net.rcdevgames.wawlaundry.data.local.SecurityPrefs
import net.rcdevgames.wawlaundry.data.local.dao.*
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context,
        securityPrefs: SecurityPrefs
    ): AppDatabase {
        val masterPassword = securityPrefs.getMasterPassword()
        
        // If master password is not yet set (first install), we initialize with an empty byte array
        // In a real flow, the UI will force them to create it before fetching DAOs
        val passphrase = masterPassword?.toByteArray() ?: ByteArray(0)
        val supportFactory = SupportFactory(passphrase)

        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "waw_laundry_secure.db"
        )
            .openHelperFactory(supportFactory)
            .addMigrations(AppDatabase.MIGRATION_2_3)
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideProfileDao(database: AppDatabase): ProfileDao = database.profileDao()

    @Provides
    fun provideServiceDao(database: AppDatabase): ServiceDao = database.serviceDao()

    @Provides
    fun providePromoDao(database: AppDatabase): PromoDao = database.promoDao()

    @Provides
    fun provideCustomerDao(database: AppDatabase): CustomerDao = database.customerDao()

    @Provides
    fun provideOrderDao(database: AppDatabase): OrderDao = database.orderDao()

    @Provides
    fun provideOrderDetailDao(database: AppDatabase): OrderDetailDao = database.orderDetailDao()

    @Provides
    fun provideExpenseDao(database: AppDatabase): ExpenseDao = database.expenseDao()
}
