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

class WeightDatabaseActivity : AppCompatActivity() {
    private lateinit var weightDao: WeightDao
    private lateinit var dataGrid: TableLayout
    private var currentUsername: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.db_screen_weight)
        supportActionBar?.hide()

        val database = AppDatabase.getDatabase(this)
        weightDao = database.weightDao()
        dataGrid = findViewById(R.id.dataGrid)
        
        currentUsername = intent.getStringExtra("USERNAME") ?: "admin"

        val weightInput = findViewById<EditText>(R.id.inputWeight)
        val dateInput = findViewById<EditText>(R.id.inputDate)
        val addDataButton = findViewById<Button>(R.id.buttonAddData)

        addDataButton.setOnClickListener {
            val weightStr = weightInput.text.toString()
            val date = dateInput.text.toString()

            if (weightStr.isNotEmpty() && date.isNotEmpty()) {
                val weightVal = weightStr.toDoubleOrNull()
                if (weightVal != null) {
                    lifecycleScope.launch {
                        weightDao.insert(Weight(username = currentUsername!!, quantity = weightVal, date = date))
                        weightInput.text.clear()
                        dateInput.text.clear()
                        Toast.makeText(this@WeightDatabaseActivity, "Weight record added", Toast.LENGTH_SHORT).show()
                        loadWeightData()
                    }
                } else {
                    Toast.makeText(this, "Invalid weight format", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }

        loadWeightData()
    }

    private fun loadWeightData() {
        lifecycleScope.launch {
            val childCount = dataGrid.childCount
            if (childCount > 1) {
                dataGrid.removeViews(1, childCount - 1)
            }

            val weights = weightDao.getWeightsForUser(currentUsername!!)
            for (w in weights) {
                val row = TableRow(this@WeightDatabaseActivity)
                row.setPadding(0, 4, 0, 4)

                val tvId = TextView(this@WeightDatabaseActivity).apply {
                    text = w.id.toString()
                    layoutParams = TableRow.LayoutParams(dpToPx(120), TableRow.LayoutParams.WRAP_CONTENT)
                    setPadding(dpToPx(10), dpToPx(10), dpToPx(10), dpToPx(10))
                    setTextColor(Color.parseColor("#2E2E38"))
                }

                val tvWeight = TextView(this@WeightDatabaseActivity).apply {
                    text = w.quantity.toString()
                    layoutParams = TableRow.LayoutParams(dpToPx(120), TableRow.LayoutParams.WRAP_CONTENT)
                    setPadding(dpToPx(10), dpToPx(10), dpToPx(10), dpToPx(10))
                    setTextColor(Color.parseColor("#2E2E38"))
                    setOnClickListener { showEditDialog(w, true) }
                }

                val tvDate = TextView(this@WeightDatabaseActivity).apply {
                    text = w.date
                    layoutParams = TableRow.LayoutParams(dpToPx(160), TableRow.LayoutParams.WRAP_CONTENT)
                    setPadding(dpToPx(10), dpToPx(10), dpToPx(10), dpToPx(10))
                    setTextColor(Color.parseColor("#2E2E38"))
                    setOnClickListener { showEditDialog(w, false) }
                }

                val btnDelete = Button(this@WeightDatabaseActivity).apply {
                    text = "Delete"
                    textSize = 8f
                    setTextColor(Color.WHITE)
                    setBackgroundColor(Color.parseColor("#D9534F"))
                    val params = TableRow.LayoutParams(dpToPx(100), dpToPx(40))
                    params.setMargins(dpToPx(6), dpToPx(6), dpToPx(6), dpToPx(6))
                    layoutParams = params
                    setOnClickListener {
                        lifecycleScope.launch {
                            weightDao.deleteById(w.id)
                            loadWeightData()
                        }
                    }
                }

                row.addView(tvId)
                row.addView(tvWeight)
                row.addView(tvDate)
                row.addView(btnDelete)
                dataGrid.addView(row)
            }
        }
    }

    private fun showEditDialog(weight: Weight, isQuantity: Boolean) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(if (isQuantity) "Edit Weight" else "Edit Date")
        val input = EditText(this)
        input.setText(if (isQuantity) weight.quantity.toString() else weight.date)
        builder.setView(input)

        builder.setPositiveButton("Update") { _, _ ->
            val newValue = input.text.toString()
            if (newValue.isNotEmpty()) {
                lifecycleScope.launch {
                    val updatedWeight = if (isQuantity) {
                        val newQty = newValue.toDoubleOrNull()
                        if (newQty != null) weight.copy(quantity = newQty) else weight
                    } else {
                        weight.copy(date = newValue)
                    }
                    weightDao.update(updatedWeight)
                    loadWeightData()
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