package net.rcdevgames.wawlaundry.data.local

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SecurityPrefs @Inject constructor(
    @ApplicationContext context: Context
) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences = EncryptedSharedPreferences.create(
        context,
        "secure_laundry_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveMasterPassword(password: String) {
        sharedPreferences.edit().putString(KEY_MASTER_PASSWORD, password).apply()
    }

    fun getMasterPassword(): String? {
        return sharedPreferences.getString(KEY_MASTER_PASSWORD, null)
    }

    fun saveOwnerPin(pin: String) {
        sharedPreferences.edit().putString(KEY_OWNER_PIN, pin).apply()
    }

    fun getOwnerPin(): String? {
        return sharedPreferences.getString(KEY_OWNER_PIN, null)
    }

    fun hasSetupSecurity(): Boolean {
        return getMasterPassword() != null && getOwnerPin() != null
    }

    fun clearAll() {
        sharedPreferences.edit().clear().apply()
    }

    companion object {
        private const val KEY_MASTER_PASSWORD = "master_password"
        private const val KEY_OWNER_PIN = "owner_pin"
    }
}
