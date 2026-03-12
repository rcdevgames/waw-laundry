package net.rcdevgames.wawlaundry.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.first
import net.rcdevgames.wawlaundry.data.local.entity.*
import net.rcdevgames.wawlaundry.domain.repository.*

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val supabaseClient: SupabaseClient,
    private val profileRepository: ProfileRepository,
    private val serviceRepository: ServiceRepository,
    private val customerRepository: CustomerRepository,
    private val orderRepository: OrderRepository,
    private val promoRepository: PromoRepository,
    private val expenseRepository: ExpenseRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            // NOTE: In a production app, we should check if the user is authenticated 
            // inside Supabase before proceeding to sync.
            
            syncProfiles()
            syncServices()
            syncCustomers()
            syncPromos()
            syncExpenses()
            syncOrders()
            // Pull logic would go here as well

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }

    private suspend fun syncProfiles() {
        val unsynced = profileRepository.getProfile().first() 
        // Real implementation should check unsynced flags, but profile is a singleton here.
        if (unsynced != null && !unsynced.isSynced) {
            supabaseClient.postgrest["profiles"].upsert(unsynced)
            profileRepository.saveProfile(unsynced.copy(isSynced = true))
        }
    }

    private suspend fun syncServices() {
        val unsynced = serviceRepository.getUnsyncedServices()
        if (unsynced.isNotEmpty()) {
            supabaseClient.postgrest["services"].upsert(unsynced)
            unsynced.forEach { 
                serviceRepository.saveService(it.copy(isSynced = true)) 
            }
        }
    }

    private suspend fun syncCustomers() {
        val unsynced = customerRepository.getUnsyncedCustomers()
        if (unsynced.isNotEmpty()) {
            supabaseClient.postgrest["customers"].upsert(unsynced)
            unsynced.forEach { 
                customerRepository.saveCustomer(it.copy(isSynced = true)) 
            }
        }
    }

    private suspend fun syncOrders() {
        val unsyncedOrders = orderRepository.getUnsyncedOrders()
        if (unsyncedOrders.isNotEmpty()) {
            supabaseClient.postgrest["orders"].upsert(unsyncedOrders)
            unsyncedOrders.forEach {
                orderRepository.updateOrder(it.copy(isSynced = true))
            }
        }

        val unsyncedDetails = orderRepository.getUnsyncedOrderDetails()
        if (unsyncedDetails.isNotEmpty()) {
            supabaseClient.postgrest["order_details"].upsert(unsyncedDetails)
            unsyncedDetails.forEach {
                orderRepository.updateOrderDetail(it.copy(isSynced = true))
            }
        }
    }
    
    private suspend fun syncPromos() {
       val unsynced = promoRepository.getUnsyncedPromos()
        if (unsynced.isNotEmpty()) {
            supabaseClient.postgrest["promos"].upsert(unsynced)
            unsynced.forEach { 
                promoRepository.savePromo(it.copy(isSynced = true)) 
            }
        }
    }
    
     private suspend fun syncExpenses() {
       val unsynced = expenseRepository.getUnsyncedExpenses()
        if (unsynced.isNotEmpty()) {
            supabaseClient.postgrest["expenses"].upsert(unsynced)
            unsynced.forEach { 
                expenseRepository.saveExpense(it.copy(isSynced = true)) 
            }
        }
    }
}
