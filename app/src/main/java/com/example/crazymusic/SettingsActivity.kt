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

class SettingsActivity : AppCompatActivity() {

    private lateinit var textVolumePercent: TextView
    private lateinit var seekBarVolume: SeekBar
    private lateinit var audioManager: AudioManager
    private lateinit var switchInterfaceSounds: SwitchCompat

    // Слушатель системных изменений громкости
    private val volumeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "android.media.VOLUME_CHANGED_ACTION") {
                updateVolumeFromSystem()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // Инициализация элементов для громкости
        textVolumePercent = findViewById(R.id.textVolumePercent)
        seekBarVolume = findViewById(R.id.seekBarVolume)
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        switchInterfaceSounds = findViewById(R.id.switchInterfaceSounds)

        // Настройка всех компонентов
        setupBackButton()
        setupSwitches()
        setupVolumeControls()
        setupDeleteButton()
    }

    private fun setupBackButton() {
        val backButton = findViewById<ImageButton>(R.id.buttonBack)
        backButton.setOnClickListener {
            finish() // Закрывает экран настроек и возвращает на главный экран
        }
    }

    private fun setupSwitches() {
        // Переключатель звуков интерфейса
        switchInterfaceSounds.setOnCheckedChangeListener { _, isChecked ->
            val message = if (isChecked) "Звуки интерфейса включены" else "Звуки интерфейса выключены"
            showToast(message)
            // Здесь будет логика сохранения настройки звуков интерфейса
        }
    }

    private fun setupVolumeControls() {
        // Получение текущей громкости
        val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)

        // Конвертация в проценты (0-100)
        val volumePercent = (currentVolume.toFloat() / maxVolume.toFloat() * 100).toInt()

        // Установка начальных значений
        textVolumePercent.text = "$volumePercent%"
        seekBarVolume.progress = volumePercent

        // Обработчик изменения SeekBar
        seekBarVolume.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    // Обновление текста
                    textVolumePercent.text = "$progress%"

                    // Установка новой громкости
                    val newVolume = (progress / 100.0 * maxVolume).toInt()
                    audioManager.setStreamVolume(
                        AudioManager.STREAM_MUSIC,
                        newVolume,
                        0
                    )
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // Не нужно
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                showToast("Громкость установлена на ${seekBar?.progress}%")
                // Здесь будет логика сохранения громкости
            }
        })
    }

    private fun updateVolumeFromSystem() {
        val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val volumePercent = (currentVolume.toFloat() / maxVolume.toFloat() * 100).toInt()

        textVolumePercent.text = "$volumePercent%"
        seekBarVolume.progress = volumePercent
        seekBarVolume.invalidate() // Перерисовываем для обновления анимации
    }

    private fun setupDeleteButton() {
        val deleteButton = findViewById<MaterialButton>(R.id.buttonDeleteSounds)
        deleteButton.setOnClickListener {
            showDeleteConfirmationDialog()
        }
    }

    private fun showDeleteConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Подтверждение удаления")
            .setMessage("Вы уверены, что хотите удалить все записанные звуки овощей? Это действие нельзя отменить.")
            .setPositiveButton("Удалить") { dialog, which ->
                // Действие при подтверждении удаления
                deleteAllRecordedSounds()
            }
            .setNegativeButton("Отмена") { dialog, which ->
                // Действие при отмене
                dialog.dismiss()
            }
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show()
    }

    private fun deleteAllRecordedSounds() {
        // Здесь будет реальная логика удаления звуков
        // Пока просто показываем сообщение об успешном удалении
        showToast("Все записанные звуки овощей удалены")

        // TODO: Добавьте здесь реальную логику удаления файлов звуков
        // Например:
        // - Удаление файлов из внутреннего хранилища
        // - Очистка базы данных или SharedPreferences
        // - Обновление списка звуков в приложении
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onResume() {
        super.onResume()
        // Регистрация слушателя изменений громкости
        val filter = IntentFilter("android.media.VOLUME_CHANGED_ACTION")
        registerReceiver(volumeReceiver, filter)
    }

    override fun onPause() {
        super.onPause()
        // Отмена регистрации слушателя
        try {
            unregisterReceiver(volumeReceiver)
        } catch (e: IllegalArgumentException) {
            // Игнорируем если receiver не был зарегистрирован
        }
    }
}