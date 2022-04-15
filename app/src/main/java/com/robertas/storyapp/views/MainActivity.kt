package com.robertas.storyapp.views

import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.robertas.storyapp.databinding.ActivityMainBinding
import com.robertas.storyapp.models.enums.LanguageMode
import com.robertas.storyapp.repositories.UserAccountRepository
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    @Inject
    lateinit var pref: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)

        setupLanguage()

        setContentView(binding.root)
    }

    private fun setupLanguage() {
        val config = resources.configuration

        val language = when (pref.getString(UserAccountRepository.LANGUAGE_KEY, "")) {
            "id" -> LanguageMode.ID

            "en" -> LanguageMode.EN

            else -> LanguageMode.DEFAULT
        }

        if (language == LanguageMode.DEFAULT) {

            val newLocale = Locale.getDefault()

            Locale.setDefault(newLocale)

            config.setLocale(newLocale)
        } else {

            val newLocale = Locale(language)

            Locale.setDefault(newLocale)

            config.setLocale(newLocale)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            createConfigurationContext(config)

            resources.updateConfiguration(config, resources.displayMetrics)
        }

    }
}