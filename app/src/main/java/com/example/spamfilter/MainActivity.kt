package com.example.spamfilter

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.spamfilter.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val keywords = KeywordStore.load(this).toMutableList()
        val adapter = KeywordAdapter(keywords) { position ->
            keywords.removeAt(position)
            binding.keywordRecyclerView.adapter?.notifyItemRemoved(position)
            KeywordStore.save(this, keywords)
        }
        binding.keywordRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.keywordRecyclerView.adapter = adapter
        binding.addButton.setOnClickListener {
            val keyword = binding.keywordEditText.text.toString().trim()
            if (keyword.isEmpty()) {
                binding.keywordEditText.error = getString(R.string.enter_keyword)
            } else if (keyword !in keywords) {
                keywords.add(keyword)
                adapter.notifyItemInserted(keywords.lastIndex)
                binding.keywordEditText.text?.clear()
                KeywordStore.save(this, keywords)
            } else {
                binding.keywordEditText.error = getString(R.string.keyword_exists)
            }
        }
        binding.notificationAccessButton.setOnClickListener {
            try {
                startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
            } catch (_: ActivityNotFoundException) {
                Toast.makeText(this, R.string.settings_unavailable, Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val enabled = packageName in NotificationManagerCompat.getEnabledListenerPackages(this)
        binding.notificationAccessStatus.setText(
            if (enabled) R.string.notification_access_on else R.string.notification_access_off
        )
    }
}
