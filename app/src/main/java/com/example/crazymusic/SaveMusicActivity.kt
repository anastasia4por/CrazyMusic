package com.example.crazymusic

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class SaveMusicActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyStateText: TextView
    private lateinit var adapter: MusicCompositionAdapter
    private var compositions = mutableListOf<MusicComposition>()
    private var currentlyPlayingPosition = -1
    private val handler = Handler(Looper.getMainLooper())
    private val activeSounds = mutableMapOf<Int, MediaPlayer>()

    companion object {
        private const val TAG = "SaveMusicActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_save_music)

        setupUI()
        loadCompositions()
        setupBackButton()
    }

    private fun setupUI() {
        recyclerView = findViewById(R.id.recyclerViewCompositions)
        emptyStateText = findViewById(R.id.emptyStateText)

        adapter = MusicCompositionAdapter(
            compositions = compositions,
            onPlayPauseClick = { position -> togglePlayPause(position) },
            onDeleteClick = { position -> showDeleteDialog(position) },
            onShareClick = { position -> shareComposition(position) },
            onCompositionClick = { position -> showCompositionDetails(position) }
        )

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun loadCompositions() {
        compositions.clear()
        compositions.addAll(MusicCompositionLoader.loadCompositions(this))
        updateEmptyState()
        adapter.notifyDataSetChanged()
    }

    private fun updateEmptyState() {
        if (compositions.isEmpty()) {
            emptyStateText.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            emptyStateText.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
    }

    private fun togglePlayPause(position: Int) {
        if (currentlyPlayingPosition == position) {
            // Останавливаем текущее воспроизведение
            stopPlayback()
            currentlyPlayingPosition = -1
        } else {
            // Останавливаем предыдущее воспроизведение
            stopPlayback()

            // Начинаем новое воспроизведение
            currentlyPlayingPosition = position
            val composition = compositions[position]
            playComposition(composition)
        }
        adapter.notifyDataSetChanged()
    }

    private fun playComposition(composition: MusicComposition) {
        // Очищаем предыдущие звуки
        stopAllSounds()

        // Воспроизводим маркеры по времени
        composition.markers.forEach { marker ->
            handler.postDelayed({
                val vegetable = createVegetableByType(marker.vegetableType)
                vegetable?.let {
                    try {
                        val mediaPlayer = MediaPlayer.create(this, it.soundId)
                        mediaPlayer?.apply {
                            isLooping = true
                            start()
                            activeSounds[marker.potId] = this
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }, marker.startTime)
        }

        // Останавливаем все звуки через duration композиции
        handler.postDelayed({
            stopPlayback()
        }, composition.duration)
    }

    private fun stopAllSounds() {
        activeSounds.values.forEach { player ->
            try {
                player.stop()
                player.release()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        activeSounds.clear()
    }

    private fun stopPlayback() {
        handler.removeCallbacksAndMessages(null)
        stopAllSounds()
        currentlyPlayingPosition = -1
        adapter.notifyDataSetChanged()
    }

    private fun showDeleteDialog(position: Int) {
        val composition = compositions[position]

        AlertDialog.Builder(this)
            .setTitle("Удаление мелодии")
            .setMessage("Вы уверены, что хотите удалить \"${composition.name}\"?")
            .setPositiveButton("Удалить") { dialog, _ ->
                // Останавливаем воспроизведение если удаляем текущую мелодию
                if (currentlyPlayingPosition == position) {
                    stopPlayback()
                }

                MusicCompositionLoader.deleteComposition(this, composition.id)
                compositions.removeAt(position)
                adapter.notifyItemRemoved(position)
                updateEmptyState()
                showToast("Мелодия удалена")
                dialog.dismiss()
            }
            .setNegativeButton("Отмена") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun shareComposition(position: Int) {
        val composition = compositions[position]

        try {
            showToast("Создание аудиофайла...")

            // Создаем временный файл во внешней директории
            val storageDir = getExternalFilesDir(null)
            val audioFile = File(storageDir, "${composition.name}_${System.currentTimeMillis()}.mp3")

            // Создаем простой аудиофайл с основным звуком
            createSimpleAudioFile(composition, audioFile)

            if (audioFile.exists() && audioFile.length() > 0) {
                // Создаем URI для файла используя FileProvider
                val audioUri = FileProvider.getUriForFile(
                    this,
                    "${packageName}.provider",
                    audioFile
                )

                // Создаем Intent для отправки аудио
                val shareIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    type = "audio/*"
                    putExtra(Intent.EXTRA_STREAM, audioUri)
                    putExtra(Intent.EXTRA_SUBJECT, composition.name)
                    putExtra(Intent.EXTRA_TEXT,
                        "Моя мелодия: ${composition.name}\n" +
                                "Длительность: ${formatDuration(composition.duration)}\n" +
                                "Овощей: ${composition.markers.size}\n" +
                                "Создано в приложении Crazy Music!")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                // Запускаем меню выбора приложения для отправки
                startActivity(Intent.createChooser(shareIntent, "Поделиться мелодией"))

            } else {
                showToast("Не удалось создать аудиофайл")
            }

        } catch (e: Exception) {
            e.printStackTrace()
            showToast("Ошибка при создании аудио")
            Log.e(TAG, "Error sharing composition", e)

            // Если не получилось с аудио, делимся текстом
            shareAsText(composition)
        }
    }

    private fun createSimpleAudioFile(composition: MusicComposition, outputFile: File) {
        try {
            // Используем первый овощ как основной звук
            val mainVegetable = createVegetableByType(
                composition.markers.firstOrNull()?.vegetableType ?: "orange"
            )

            if (mainVegetable != null) {
                // Копируем raw ресурс во временный файл
                resources.openRawResource(mainVegetable.soundId).use { input ->
                    FileOutputStream(outputFile).use { output ->
                        input.copyTo(output)
                    }
                }
                Log.d(TAG, "Аудиофайл создан: ${outputFile.absolutePath}, размер: ${outputFile.length()} байт")
            } else {
                // Если овощей нет, создаем простой текстовый файл с описанием
                createDescriptionFile(composition, outputFile)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error creating simple audio file", e)
            // Если не получилось создать аудио, создаем файл с описанием
            createDescriptionFile(composition, outputFile)
        }
    }

    private fun createDescriptionFile(composition: MusicComposition, outputFile: File) {
        try {
            val description = """
                Мелодия: ${composition.name}
                Длительность: ${formatDuration(composition.duration)}
                Дата создания: ${formatDate(composition.creationDate)}
                Количество овощей: ${composition.markers.size}
                Овощи: ${composition.markers.joinToString { getVegetableDisplayName(it.vegetableType) }}
                
                Создано в приложении Crazy Music!
            """.trimIndent()

            FileOutputStream(outputFile).use { output ->
                output.write(description.toByteArray())
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error creating description file", e)
        }
    }

    private fun shareAsText(composition: MusicComposition) {
        try {
            val shareText = """
                🎵 ${composition.name}
                
                Длительность: ${formatDuration(composition.duration)}
                Количество овощей: ${composition.markers.size}
                Дата создания: ${formatDate(composition.creationDate)}
                
                Создано в приложении Crazy Music!
                Скачайте приложение чтобы послушать эту мелодию!
            """.trimIndent()

            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, shareText)
                putExtra(Intent.EXTRA_SUBJECT, "Моя мелодия: ${composition.name}")
            }

            startActivity(Intent.createChooser(shareIntent, "Поделиться мелодией"))

        } catch (e: Exception) {
            e.printStackTrace()
            showToast("Ошибка при отправке")
        }
    }

    private fun showCompositionDetails(position: Int) {
        val composition = compositions[position]

        val details = """
            Название: ${composition.name}
            Длительность: ${formatDuration(composition.duration)}
            Дата создания: ${formatDate(composition.creationDate)}
            Количество овощей: ${composition.markers.size}
        """.trimIndent()

        AlertDialog.Builder(this)
            .setTitle("Информация о мелодии")
            .setMessage(details)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun getVegetableDisplayName(type: String): String {
        return when (type) {
            "orange" -> "🍊 Апельсин"
            "cherry" -> "🍒 Вишня"
            "apple" -> "🍎 Яблоко"
            "strawberry" -> "🍓 Клубника"
            "pumpkin" -> "🎃 Тыква"
            "grape" -> "🍇 Виноград"
            else -> type
        }
    }

    private fun formatDuration(duration: Long): String {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(duration)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(duration) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    private fun formatDate(date: Date): String {
        val formatter = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
        return formatter.format(date)
    }

    private fun setupBackButton() {
        findViewById<ImageButton>(R.id.buttonBack).setOnClickListener {
            stopPlayback()
            finish()
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun createVegetableByType(type: String): Vegetable? {
        return when (type) {
            "orange" -> Vegetable("Апельсин", R.drawable.ic_orange, R.raw.orange_sound, "orange_model")
            "cherry" -> Vegetable("Вишня", R.drawable.ic_cherry, R.raw.cherry_sound, "cherry_model")
            "apple" -> Vegetable("Яблоко", R.drawable.ic_apple, R.raw.apple_sound, "apple_model")
            "strawberry" -> Vegetable("Клубника", R.drawable.ic_strawberry, R.raw.strawberry_sound, "strawberry_model")
            "pumpkin" -> Vegetable("Тыква", R.drawable.ic_pumpkin, R.raw.pumpkin_sound, "pumpkin_model")
            "grape" -> Vegetable("Виноград", R.drawable.ic_grape, R.raw.grape_sound, "grape_model")
            else -> null
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopPlayback()
        handler.removeCallbacksAndMessages(null)
    }

    // Adapter class
    private inner class MusicCompositionAdapter(
        private var compositions: List<MusicComposition>,
        private val onPlayPauseClick: (Int) -> Unit,
        private val onDeleteClick: (Int) -> Unit,
        private val onShareClick: (Int) -> Unit,
        private val onCompositionClick: (Int) -> Unit
    ) : RecyclerView.Adapter<MusicCompositionAdapter.ViewHolder>() {

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val name: TextView = itemView.findViewById(R.id.textCompositionName)
            val duration: TextView = itemView.findViewById(R.id.textCompositionDuration)
            val date: TextView = itemView.findViewById(R.id.textCompositionDate)
            val playButton: ImageButton = itemView.findViewById(R.id.buttonPlay)
            val shareButton: ImageButton = itemView.findViewById(R.id.shareButton)
            val deleteButton: ImageButton = itemView.findViewById(R.id.buttonDelete)
            val vegetableCount: TextView = itemView.findViewById(R.id.textVegetableCount)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_composition, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val composition = compositions[position]

            holder.name.text = composition.name
            holder.duration.text = formatDuration(composition.duration)
            holder.date.text = formatDate(composition.creationDate)
            holder.vegetableCount.text = "Овощей: ${composition.markers.size}"

            // Устанавливаем иконку воспроизведения
            val playIcon = if (position == currentlyPlayingPosition) {
                R.drawable.ic_pause
            } else {
                R.drawable.ic_play
            }
            holder.playButton.setImageResource(playIcon)

            // Обработчики кликов
            holder.playButton.setOnClickListener {
                onPlayPauseClick(position)
            }

            holder.shareButton.setOnClickListener {
                onShareClick(position)
            }

            holder.deleteButton.setOnClickListener {
                onDeleteClick(position)
            }

            holder.itemView.setOnClickListener {
                onCompositionClick(position)
            }
        }

        override fun getItemCount(): Int = compositions.size
    }
}