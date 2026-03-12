package net.rcdevgames.wawlaundry.domain.repository

import kotlinx.coroutines.flow.Flow
import net.rcdevgames.wawlaundry.data.local.dao.ExpenseDao
import net.rcdevgames.wawlaundry.data.local.entity.ExpenseEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExpenseRepository @Inject constructor(
    private val expenseDao: ExpenseDao
) {

    fun getAllExpenses(): Flow<List<ExpenseEntity>> = expenseDao.getAllExpenses()

    fun getExpensesByDateRange(startDate: Long, endDate: Long): Flow<List<ExpenseEntity>> =
        expenseDao.getExpensesByDateRange(startDate, endDate)

    suspend fun saveExpense(expense: ExpenseEntity) {
        expenseDao.insertOrUpdateExpense(expense)
    }

    suspend fun softDeleteExpense(id: String, timestamp: Long = System.currentTimeMillis()) {
        expenseDao.softDeleteExpense(id, timestamp)
    }

    suspend fun getUnsyncedExpenses(): List<ExpenseEntity> = expenseDao.getUnsyncedExpenses()

    suspend fun markAsSynced(id: String) {
        expenseDao.markAsSynced(id)
    }
}
