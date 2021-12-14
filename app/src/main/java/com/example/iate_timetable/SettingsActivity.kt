package com.example.iate_timetable

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceFragmentCompat
import java.util.*

class SettingsActivity : AppCompatActivity() {

    lateinit var locale: Locale

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.title_activity_settings)

        fun setLocale(localeName: String) {
            var currentLang: String? = null
            var currentLanguage = intent.getStringExtra(currentLang).toString()
            if (localeName != currentLanguage) {
                locale = Locale(localeName)
                val res = resources
                val dm = res.displayMetrics
                val conf = res.configuration
                conf.locale = locale
                res.updateConfiguration(conf, dm)
                val refresh = Intent(
                    this,
                    MainActivity::class.java
                )
                refresh.putExtra(currentLang, localeName)
                startActivity(refresh)
            }
        }

        val languageGroup: RadioGroup = findViewById(R.id.language_radiogroup)
        val rus: RadioButton = findViewById(R.id.radio_russia)
        val eng: RadioButton = findViewById(R.id.radio_english)
        val sharedPrefLang = getSharedPreferences("language", 0)

        rus.isChecked = sharedPrefLang.getString("lang", "0") == "ru"

        eng.isChecked = sharedPrefLang.getString("lang", "0") == "en"

        languageGroup.setOnCheckedChangeListener (
            RadioGroup.OnCheckedChangeListener { group, checkedId ->
                val editor = sharedPrefLang.edit()
                if (rus.isChecked) {
                    editor.putString("lang", "ru")
                    editor.apply()
                    setLocale("ru")
                }
                if (eng.isChecked) {
                    editor.putString("lang", "en")
                    editor.apply()
                    setLocale("en")
                }
            }
        )
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                actionBar?.setDisplayHomeAsUpEnabled(false)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        finish()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        actionBar?.setDisplayHomeAsUpEnabled(false)
    }
}