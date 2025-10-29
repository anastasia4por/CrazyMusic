package com.example.crazymusic

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupButtons()
    }

    private fun setupButtons() {
        findViewById<MaterialButton>(R.id.buttonCreate).setOnClickListener {
            showToast("Создание мелодии")
        }
        findViewById<MaterialButton>(R.id.buttonRepeat).setOnClickListener {
            showToast("Повтор звука")
        }
        findViewById<MaterialButton>(R.id.buttonSongs).setOnClickListener {
            showToast("Мои песни")
        }
        findViewById<MaterialButton>(R.id.buttonSettings).setOnClickListener {
            showToast("Настройки")
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}