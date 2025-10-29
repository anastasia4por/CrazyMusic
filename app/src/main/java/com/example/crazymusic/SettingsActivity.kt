package com.example.crazymusic

import android.os.Bundle
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import com.google.android.material.button.MaterialButton

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        setupBackButton()
        setupSwitches()
        setupSeekBar()
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
        val switchInterfaceSounds = findViewById<SwitchCompat>(R.id.switchInterfaceSounds)
        switchInterfaceSounds.setOnCheckedChangeListener { _, isChecked ->
            val message = if (isChecked) "Звуки интерфейса включены" else "Звуки интерфейса выключены"
            showToast(message)
            // Здесь будет логика сохранения настройки звуков интерфейса
        }
    }

    private fun setupSeekBar() {
        val seekBarVolume = findViewById<SeekBar>(R.id.seekBarVolume)
        val textVolumePercent = findViewById<TextView>(R.id.textVolumePercent)

        // Устанавливаем начальное значение
        textVolumePercent.text = "70%"

        seekBarVolume.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                // Обновляем текст процентов при движении ползунка
                textVolumePercent.text = "$progress%"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // Не требуется
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                showToast("Громкость установлена на ${seekBar?.progress}%")
                // Здесь будет логика сохранения громкости
            }
        })
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
}