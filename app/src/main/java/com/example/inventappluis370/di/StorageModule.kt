package com.example.inventappluis370.di

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyPermanentlyInvalidatedException
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.io.File
import javax.crypto.AEADBadTagException
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object StorageModule {

    private const val PREFS_FILENAME = "invent_app_prefs"

    @Provides
    @Singleton
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        // En algunos dispositivos (especialmente MIUI) / ciertos estados del keystore, puede quedar un
        // keyset corrupto y EncryptedSharedPreferences lanza AEADBadTagException (o una excepción de keystore).
        // En ese caso borramos el archivo de prefs y reintentamos para recuperarnos sin crashear.

        fun createEncrypted(): SharedPreferences {
            val masterKey = MasterKey.Builder(context, MasterKey.DEFAULT_MASTER_KEY_ALIAS)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            return EncryptedSharedPreferences.create(
                context,
                PREFS_FILENAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        }

        fun deletePrefsFile() {
            try {
                // EncryptedSharedPreferences usa SharedPreferences por debajo -> /shared_prefs/<name>.xml
                val appInfo = context.applicationInfo
                val prefsDir = File(appInfo.dataDir, "shared_prefs")
                val prefsFile = File(prefsDir, "$PREFS_FILENAME.xml")
                val deleted = prefsFile.delete()
                Log.w("StorageModule", "Borrando prefs corruptas: ${prefsFile.absolutePath} deleted=$deleted")
            } catch (t: Throwable) {
                Log.w("StorageModule", "No se pudo borrar el archivo de prefs corruptas", t)
            }
        }

        fun looksLikeKeystoreProblem(t: Throwable): Boolean {
            // Evitamos depender de clases nuevas. Detectamos por tipo cuando existe y por nombre para el resto.
            if (t is AEADBadTagException) return true
            if (t is KeyPermanentlyInvalidatedException) return true

            val name = t::class.java.name
            if (name.contains("KeyStore", ignoreCase = true)) return true
            if (name.contains("Keystore", ignoreCase = true)) return true
            if (name.contains("ProviderException", ignoreCase = true)) return true

            val cause = t.cause
            return cause != null && looksLikeKeystoreProblem(cause)
        }

        return try {
            createEncrypted()
        } catch (t: Throwable) {
            if (looksLikeKeystoreProblem(t)) {
                Log.w(
                    "StorageModule",
                    "EncryptedSharedPreferences falló por keyset/keystore inválido. Reintentando tras borrar prefs...",
                    t
                )
                deletePrefsFile()

                // Reintento una vez. Si vuelve a fallar, ya caemos al almacenamiento estándar.
                try {
                    return createEncrypted()
                } catch (t2: Throwable) {
                    Log.w(
                        "StorageModule",
                        "Segundo intento de EncryptedSharedPreferences falló. Usando SharedPreferences estándar.",
                        t2
                    )
                }
            } else {
                Log.w(
                    "StorageModule",
                    "El dispositivo no soporta EncryptedSharedPreferences. Usando SharedPreferences estándar.",
                    t
                )
            }

            context.getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE)
        }
    }
}
