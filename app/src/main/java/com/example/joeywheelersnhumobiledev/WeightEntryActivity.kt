package com.example.joeywheelersnhumobiledev

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class WeightEntryActivity : AppCompatActivity() {
    private lateinit var weightDao: WeightDao
    private lateinit var userDao: UserDao
    private var currentUsername: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.weight_entry_screen)
        supportActionBar?.hide()

        val database = AppDatabase.getDatabase(this)
        weightDao = database.weightDao()
        userDao = database.userDao()
        
        currentUsername = intent.getStringExtra("USERNAME") ?: "guest"

        checkSmsPrompt()

        val weightInput = findViewById<EditText>(R.id.weightInput)
        val submitButton = findViewById<Button>(R.id.submitButton)
        val navTarget = findViewById<ImageButton>(R.id.navTarget)
        val navSettings = findViewById<ImageButton>(R.id.navSettings)

        submitButton.setOnClickListener {
            val weightStr = weightInput.text.toString().filter { it.isDigit() || it == '.' }
            val weightVal = weightStr.toDoubleOrNull()

            if (weightVal != null) {
                val currentDate = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()).format(Date())
                
                lifecycleScope.launch {
                    weightDao.insert(Weight(
                        username = currentUsername!!,
                        quantity = weightVal,
                        date = currentDate
                    ))
                    Toast.makeText(this@WeightEntryActivity, "Weight Saved!", Toast.LENGTH_SHORT).show()
                    weightInput.text.clear()
                }
            } else {
                Toast.makeText(this, "Please enter a valid weight", Toast.LENGTH_SHORT).show()
            }
        }

        navTarget.setOnClickListener {
            val intent = Intent(this, WeightDatabaseActivity::class.java)
            intent.putExtra("USERNAME", currentUsername)
            startActivity(intent)
        }

        navSettings.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun checkSmsPrompt() {
        if (currentUsername == "guest") return

        lifecycleScope.launch {
            val user = userDao.findByUsername(currentUsername!!)
            if (user != null && !user.hasSeenSmsPrompt) {
                showSmsPrompt(user)
            }
        }
    }

    private fun showSmsPrompt(user: User) {
        AlertDialog.Builder(this)
            .setTitle("SMS Notifications")
            .setMessage("Would you like to receive SMS notifications for weight tracking reminders?")
            .setPositiveButton("Yes") { _, _ ->
                updateUserSms(user, true)
            }
            .setNegativeButton("No") { _, _ ->
                updateUserSms(user, false)
            }
            .setCancelable(false)
            .show()
    }

    private fun updateUserSms(user: User, enabled: Boolean) {
        lifecycleScope.launch {
            val updatedUser = user.copy(smsEnabled = enabled, hasSeenSmsPrompt = true)
            userDao.update(updatedUser)
            val msg = if (enabled) "SMS notifications enabled!" else "SMS notifications declined."
            Toast.makeText(this@WeightEntryActivity, msg, Toast.LENGTH_SHORT).show()
        }
    }
}