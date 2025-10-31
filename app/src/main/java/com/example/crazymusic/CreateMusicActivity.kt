package com.example.crazymusic

import android.os.Bundle
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import com.google.android.material.button.MaterialButton

class CreateMusicActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_music)

        // Здесь будет логика создания мелодий
        setupCreateMusicUI()
        setupBackButton()
    }

    private fun setupCreateMusicUI() {
        // Инициализация элементов интерфейса для создания музыки
    }

    private fun setupBackButton() {
        val backButton = findViewById<ImageButton>(R.id.buttonBack)
        backButton.setOnClickListener {
            finish() // Закрывает экран настроек и возвращает на главный экран
        }
    }
}