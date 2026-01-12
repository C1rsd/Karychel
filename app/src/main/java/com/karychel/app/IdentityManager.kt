package com.karychel.app

import android.content.Context
import android.content.SharedPreferences
import java.util.regex.Pattern

/**
 * IdentityManager: Maneja el ID de 8 números (XXXX-XXXX) y el PIN de 4 dígitos.
 * Almacena las credenciales de forma segura usando SharedPreferences.
 */
class IdentityManager(private val context: Context) {

    companion object {
        private const val PREFS_NAME = "karychel_identity"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_PIN = "pin"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        
        // Patrones de validación
        private val ID_PATTERN = Pattern.compile("^\\d{4}-\\d{4}$")
        private val PIN_PATTERN = Pattern.compile("^\\d{4}$")
    }

    private val prefs: SharedPreferences = 
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * Valida el formato del ID (XXXX-XXXX)
     * @param id ID a validar
     * @return true si el formato es válido
     */
    fun isValidIdFormat(id: String): Boolean {
        return ID_PATTERN.matcher(id).matches()
    }

    /**
     * Valida el formato del PIN (4 dígitos)
     * @param pin PIN a validar
     * @return true si el formato es válido
     */
    fun isValidPinFormat(pin: String): Boolean {
        return PIN_PATTERN.matcher(pin).matches()
    }

    /**
     * Formatea un ID de 8 dígitos sin guión a formato XXXX-XXXX
     * @param id ID sin formato
     * @return ID formateado o null si no es válido
     */
    fun formatId(id: String): String? {
        val digitsOnly = id.replace("-", "").replace(" ", "")
        return if (digitsOnly.length == 8 && digitsOnly.all { it.isDigit() }) {
            "${digitsOnly.substring(0, 4)}-${digitsOnly.substring(4, 8)}"
        } else {
            null
        }
    }

    /**
     * Guarda el ID del usuario
     * @param id ID formateado (XXXX-XXXX)
     * @return true si se guardó correctamente
     */
    fun saveUserId(id: String): Boolean {
        return if (isValidIdFormat(id)) {
            prefs.edit().putString(KEY_USER_ID, id).apply()
            true
        } else {
            false
        }
    }

    /**
     * Guarda el PIN del usuario
     * @param pin PIN de 4 dígitos
     * @return true si se guardó correctamente
     */
    fun savePin(pin: String): Boolean {
        return if (isValidPinFormat(pin)) {
            prefs.edit().putString(KEY_PIN, pin).apply()
            true
        } else {
            false
        }
    }

    /**
     * Obtiene el ID del usuario guardado
     * @return ID guardado o null si no existe
     */
    fun getUserId(): String? {
        return prefs.getString(KEY_USER_ID, null)
    }

    /**
     * Obtiene el PIN del usuario guardado
     * @return PIN guardado o null si no existe
     */
    fun getPin(): String? {
        return prefs.getString(KEY_PIN, null)
    }

    /**
     * Verifica las credenciales del usuario
     * @param id ID a verificar
     * @param pin PIN a verificar
     * @return true si las credenciales coinciden
     */
    fun verifyCredentials(id: String, pin: String): Boolean {
        val savedId = getUserId()
        val savedPin = getPin()
        return savedId != null && savedPin != null && 
               savedId == id && savedPin == pin
    }

    /**
     * Establece el estado de sesión
     * @param isLoggedIn true si el usuario está logueado
     */
    fun setLoggedIn(isLoggedIn: Boolean) {
        prefs.edit().putBoolean(KEY_IS_LOGGED_IN, isLoggedIn).apply()
    }

    /**
     * Verifica si el usuario está logueado
     * @return true si está logueado
     */
    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    /**
     * Limpia todas las credenciales y cierra sesión
     */
    fun clearCredentials() {
        prefs.edit()
            .remove(KEY_USER_ID)
            .remove(KEY_PIN)
            .putBoolean(KEY_IS_LOGGED_IN, false)
            .apply()
    }

    /**
     * Verifica si el usuario tiene credenciales guardadas
     * @return true si tiene ID y PIN guardados
     */
    fun hasCredentials(): Boolean {
        return getUserId() != null && getPin() != null
    }
}
