package net.rcdevgames.wawlaundry.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import net.rcdevgames.wawlaundry.data.local.SecurityPrefs
import net.rcdevgames.wawlaundry.data.remote.SupabaseConfig
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideSupabaseClient(): SupabaseClient {
        return SupabaseConfig.client
    }

    @Provides
    @Singleton
    fun provideSecurityPrefs(@ApplicationContext context: Context): SecurityPrefs {
        return SecurityPrefs(context)
    }

    // Database providers will be added here in Phase 2
}
