package net.rcdevgames.wawlaundry.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.rcdevgames.wawlaundry.data.local.SecurityPrefs
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // NOTE: Supabase removed - using local backup only
    // Cloud sync client provider removed from here

    @Provides
    @Singleton
    fun provideSecurityPrefs(@ApplicationContext context: Context): SecurityPrefs {
        return SecurityPrefs(context)
    }

    // Database providers will be added here in Phase 2
}
