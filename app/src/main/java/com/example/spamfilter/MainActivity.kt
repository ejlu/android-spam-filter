package com.example.spamfilter

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.spamfilter.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var keywordAdapter: KeywordAdapter
    private val keywords = mutableListOf<String>()

    companion object {
        private const val SMS_PERMISSION_REQUEST_CODE = 123
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView() // Move this before loadKeywords()
        loadKeywords()
        checkSmsPermission()
        setupAddButton()
    }

    private fun setupRecyclerView() {
        keywordAdapter = KeywordAdapter(keywords) { position ->
            keywords.removeAt(position)
            keywordAdapter.notifyItemRemoved(position)
            saveKeywords() // Save keywords after removal
        }
        binding.keywordRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = keywordAdapter
        }
    }

    private fun loadKeywords() {
        val sharedPrefs = getSharedPreferences("SpamFilter", Context.MODE_PRIVATE)
        val keywordsSet = sharedPrefs.getStringSet("keywords", emptySet()) ?: emptySet()
        keywords.clear() // Clear existing keywords before adding
        keywords.addAll(keywordsSet)
        keywordAdapter.notifyDataSetChanged()
    }

    private fun checkSmsPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECEIVE_SMS
            ) == PackageManager.PERMISSION_GRANTED -> {
                // Permission is already granted
                Toast.makeText(this, "SMS permission already granted", Toast.LENGTH_SHORT).show()
            }
            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.RECEIVE_SMS
            ) -> {
                // Explain why the app needs this permission
                showPermissionExplanationDialog()
            }
            else -> {
                // Request the permission
                requestSmsPermission()
            }
        }
    }

    private fun showPermissionExplanationDialog() {
        AlertDialog.Builder(this)
            .setTitle("SMS Permission Required")
            .setMessage("This app needs SMS permission to filter spam messages. Without this permission, the app won't be able to function properly.")
            .setPositiveButton("Grant Permission") { _, _ ->
                requestSmsPermission()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
                Toast.makeText(this, "SMS filtering won't work without permission", Toast.LENGTH_LONG).show()
            }
            .create()
            .show()
    }

    private fun requestSmsPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.RECEIVE_SMS),
            SMS_PERMISSION_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            SMS_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted
                    Toast.makeText(this, "SMS permission granted", Toast.LENGTH_SHORT).show()
                } else {
                    // Permission denied
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECEIVE_SMS)) {
                        // Permission permanently denied
                        showPermissionPermanentlyDeniedDialog()
                    } else {
                        Toast.makeText(this, "SMS permission denied. App may not work as expected.", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun showPermissionPermanentlyDeniedDialog() {
        AlertDialog.Builder(this)
            .setTitle("Permission Permanently Denied")
            .setMessage("SMS permission has been permanently denied. Please go to Settings to enable it manually.")
            .setPositiveButton("Go to Settings") { _, _ ->
                openAppSettings()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
                Toast.makeText(this, "SMS filtering won't work without permission", Toast.LENGTH_LONG).show()
            }
            .create()
            .show()
    }

    private fun openAppSettings() {
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
            startActivity(this)
        }
    }

    private fun setupAddButton() {
        binding.addButton.setOnClickListener {
            val keyword = binding.keywordEditText.text.toString().trim()
            if (keyword.isNotEmpty()) {
                keywords.add(keyword)
                keywordAdapter.notifyItemInserted(keywords.size - 1)
                binding.keywordEditText.text?.clear()
                saveKeywords()
            } else {
                Toast.makeText(this, "Please enter a keyword", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveKeywords() {
        val sharedPrefs = getSharedPreferences("SpamFilter", Context.MODE_PRIVATE)
        sharedPrefs.edit().putStringSet("keywords", keywords.toSet()).apply()
    }
}