package com.example.chatgptclient.ui.settings

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.*
import com.example.chatgptclient.ChatGPTClientApplication
import com.example.chatgptclient.R
import com.example.chatgptclient.logic.Repository
import com.google.android.material.appbar.MaterialToolbar
import es.dmoral.toasty.Toasty

class SettingsActivity : AppCompatActivity() {

    private lateinit var settingsTopAppBar: MaterialToolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)

        settingsTopAppBar = findViewById(R.id.settingsTopAppBar)
        settingsTopAppBar.setNavigationOnClickListener {
            finish()
        }

        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, SettingsFragment())
                .commit()
        }


    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)

            val apiKeyEditTextPreference = findPreference<Preference>("api_key")
            val enableSwitchPreferenceCompat = findPreference<SwitchPreferenceCompat>("enable_continuous_conversation")
            val fontSizeListPreference = findPreference<ListPreference>("font_size")
            val temSeekBarPreference = findPreference<SeekBarPreference>("temperature")

            apiKeyEditTextPreference?.setOnPreferenceChangeListener { preference, newValue ->
                Repository.resetOpenAI(newValue.toString())
                true
            }
            enableSwitchPreferenceCompat?.setOnPreferenceChangeListener { preference, newValue ->
                Repository.resetIsMultiTurnCon(newValue as Boolean)
                true
            }
            fontSizeListPreference?.setOnPreferenceChangeListener { preference, newValue ->
                true
            }
            temSeekBarPreference?.setOnPreferenceChangeListener { preference, newValue ->
                Repository.resetTem(newValue as Int)
                true
            }

            fontSizeListPreference?.setOnPreferenceClickListener {
                Toasty.info(ChatGPTClientApplication.context,"新的字体大小需要重启应用才能生效", Toast.LENGTH_LONG,true).show()
                true
            }
        }


    }
}