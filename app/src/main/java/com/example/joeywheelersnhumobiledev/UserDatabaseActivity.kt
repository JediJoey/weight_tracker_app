package com.example.joeywheelersnhumobiledev

import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class UserDatabaseActivity : AppCompatActivity() {
    private lateinit var userDao: UserDao
    private lateinit var dataGrid: TableLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.db_screen_users)
        supportActionBar?.hide()

        val database = AppDatabase.getDatabase(this)
        userDao = database.userDao()
        dataGrid = findViewById(R.id.dataGrid)

        val usernameInput = findViewById<EditText>(R.id.inputUsername)
        val passwordInput = findViewById<EditText>(R.id.inputPassword)
        val addDataButton = findViewById<Button>(R.id.buttonAddData)

        addDataButton.setOnClickListener {
            val username = usernameInput.text.toString()
            val password = passwordInput.text.toString()

            if (username.isNotEmpty() && password.isNotEmpty()) {
                lifecycleScope.launch {
                    val existingUser = userDao.findByUsername(username)
                    if (existingUser == null) {
                        userDao.insert(User(username = username, password = password))
                        usernameInput.text.clear()
                        passwordInput.text.clear()
                        Toast.makeText(this@UserDatabaseActivity, "User added successfully", Toast.LENGTH_SHORT).show()
                        loadUserData()
                    } else {
                        Toast.makeText(this@UserDatabaseActivity, "Username already exists", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "Please fill in both fields", Toast.LENGTH_SHORT).show()
            }
        }

        loadUserData()
    }

    private fun loadUserData() {
        lifecycleScope.launch {
            val childCount = dataGrid.childCount
            if (childCount > 1) {
                dataGrid.removeViews(1, childCount - 1)
            }

            val users = userDao.getAll()
            for (user in users) {
                val row = TableRow(this@UserDatabaseActivity)
                row.setPadding(0, 4, 0, 4)

                val tvId = TextView(this@UserDatabaseActivity).apply {
                    text = user.id.toString()
                    layoutParams = TableRow.LayoutParams(dpToPx(120), TableRow.LayoutParams.WRAP_CONTENT)
                    setPadding(dpToPx(10), dpToPx(10), dpToPx(10), dpToPx(10))
                    setTextColor(Color.parseColor("#2E2E38"))
                }

                val tvUser = TextView(this@UserDatabaseActivity).apply {
                    text = user.username
                    layoutParams = TableRow.LayoutParams(dpToPx(105), TableRow.LayoutParams.WRAP_CONTENT)
                    setPadding(dpToPx(10), dpToPx(10), dpToPx(10), dpToPx(10))
                    setTextColor(Color.parseColor("#2E2E38"))
                    setOnClickListener { showEditDialog(user, true) }
                }

                val tvPass = TextView(this@UserDatabaseActivity).apply {
                    text = user.password
                    layoutParams = TableRow.LayoutParams(dpToPx(160), TableRow.LayoutParams.WRAP_CONTENT)
                    setPadding(dpToPx(10), dpToPx(10), dpToPx(10), dpToPx(10))
                    setTextColor(Color.parseColor("#2E2E38"))
                    setOnClickListener { showEditDialog(user, false) }
                }

                val btnDelete = Button(this@UserDatabaseActivity).apply {
                    text = "Delete"
                    textSize = 8f
                    setTextColor(Color.WHITE)
                    setBackgroundColor(Color.parseColor("#D9534F"))
                    val params = TableRow.LayoutParams(dpToPx(100), dpToPx(40))
                    params.setMargins(dpToPx(6), dpToPx(6), dpToPx(6), dpToPx(6))
                    layoutParams = params
                    setOnClickListener {
                        lifecycleScope.launch {
                            userDao.deleteById(user.id)
                            loadUserData()
                        }
                    }
                }

                row.addView(tvId)
                row.addView(tvUser)
                row.addView(tvPass)
                row.addView(btnDelete)
                dataGrid.addView(row)
            }
        }
    }

    private fun showEditDialog(user: User, isUsername: Boolean) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(if (isUsername) "Edit Username" else "Edit Password")
        val input = EditText(this)
        input.setText(if (isUsername) user.username else user.password)
        builder.setView(input)

        builder.setPositiveButton("Update") { _, _ ->
            val newValue = input.text.toString()
            if (newValue.isNotEmpty()) {
                lifecycleScope.launch {
                    val updatedUser = if (isUsername) user.copy(username = newValue) else user.copy(password = newValue)
                    userDao.update(updatedUser)
                    loadUserData()
                }
            }
        }
        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
        builder.show()
    }

    private fun dpToPx(dp: Int): Int {
        val density = resources.displayMetrics.density
        return (dp * density).toInt()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}