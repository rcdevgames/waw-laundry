package net.rcdevgames.wawlaundry.ui.owner.expenses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import net.rcdevgames.wawlaundry.data.local.entity.ExpenseEntity
import net.rcdevgames.wawlaundry.domain.repository.ExpenseRepository
import java.util.UUID
import javax.inject.Inject

data class OwnerExpensesState(
    val expenses: List<ExpenseEntity> = emptyList(),
    val totalExpense: Double = 0.0,
    val isLoading: Boolean = true,
    
    // Add Form
    val showDialog: Boolean = false,
    val titleInput: String = "",
    val amountInput: String = "",
    
    val errorMessage: String? = null
)

@HiltViewModel
class OwnerExpensesViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository
) : ViewModel() {

    private val _state = MutableStateFlow(OwnerExpensesState())
    val state: StateFlow<OwnerExpensesState> = _state.asStateFlow()

    init {
        loadExpenses()
    }

    private fun loadExpenses() {
        viewModelScope.launch {
            expenseRepository.getAllExpenses().collect { list ->
                val sorted = list.sortedByDescending { it.date }
                val total = sorted.sumOf { it.amount }
                _state.update { it.copy(expenses = sorted, totalExpense = total, isLoading = false) }
            }
        }
    }

    fun toggleDialog(show: Boolean) {
        _state.update {
            it.copy(
                showDialog = show,
                titleInput = "",
                amountInput = "",
                errorMessage = null
            )
        }
    }

    fun onTitleChange(title: String) = _state.update { it.copy(titleInput = title) }
    fun onAmountChange(amountStr: String) {
        val filtered = amountStr.filter { it.isDigit() }
        _state.update { it.copy(amountInput = filtered) }
    }

    fun saveExpense() {
        val currentState = _state.value
        
        if (currentState.titleInput.isBlank() || currentState.amountInput.isBlank()) {
            _state.update { it.copy(errorMessage = "Judul dan Nominal tidak boleh kosong") }
            return
        }

        val amount = currentState.amountInput.toDoubleOrNull() ?: 0.0
        val now = System.currentTimeMillis()

        val expense = ExpenseEntity(
            id = UUID.randomUUID().toString(),
            userId = null,
            title = currentState.titleInput,
            amount = amount,
            date = now,
            updatedAt = now
        )

        viewModelScope.launch {
            try {
                expenseRepository.saveExpense(expense)
                toggleDialog(false)
            } catch (e: Exception) {
                _state.update { it.copy(errorMessage = "Gagal menyimpan pengeluaran: ${e.message}") }
            }
        }
    }

    fun deleteExpense(expenseId: String) {
        viewModelScope.launch {
            try {
                expenseRepository.softDeleteExpense(expenseId, System.currentTimeMillis())
            } catch (e: Exception) {
                _state.update { it.copy(errorMessage = "Gagal menghapus: ${e.message}") }
            }
        }
    }
}
