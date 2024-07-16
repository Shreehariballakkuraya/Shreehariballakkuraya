import android.content.Context
import android.content.SharedPreferences

object SavedPreference {
    private const val PREFS_NAME = "user_prefs"
    private const val KEY_EMAIL = "email"
    private const val KEY_USERNAME = "username"

    private fun getSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun setEmail(context: Context, email: String) {
        val prefs = getSharedPreferences(context)
        prefs.edit().putString(KEY_EMAIL, email).apply()
    }

    fun setUsername(context: Context, username: String) {
        val prefs = getSharedPreferences(context)
        prefs.edit().putString(KEY_USERNAME, username).apply()
    }

    fun getEmail(context: Context): String? {
        return getSharedPreferences(context).getString(KEY_EMAIL, null)
    }

    fun getUsername(context: Context): String? {
        return getSharedPreferences(context).getString(KEY_USERNAME, null)
    }
}
