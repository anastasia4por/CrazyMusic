package com.example.crazymusic

import android.content.Intent
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
            val intent = Intent(this, CreateMusicActivity::class.java)
            startActivity(intent)
        }
        findViewById<MaterialButton>(R.id.buttonRepeat).setOnClickListener {
            val intent = Intent(this, RepeatMusicActivity::class.java)
            startActivity(intent)
        }
        findViewById<MaterialButton>(R.id.buttonSongs).setOnClickListener {
            val intent = Intent(this, SaveMusicActivity::class.java)
            startActivity(intent)
        }
        findViewById<MaterialButton>(R.id.buttonSettings).setOnClickListener {
            // ПЕРЕХОД НА ЭКРАН НАСТРОЕК
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}