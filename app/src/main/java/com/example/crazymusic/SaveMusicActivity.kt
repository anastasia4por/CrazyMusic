package com.example.crazymusic

import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
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
        private val onCompositionClick: (Int) -> Unit
    ) : RecyclerView.Adapter<MusicCompositionAdapter.ViewHolder>() {

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val name: TextView = itemView.findViewById(R.id.textCompositionName)
            val duration: TextView = itemView.findViewById(R.id.textCompositionDuration)
            val date: TextView = itemView.findViewById(R.id.textCompositionDate)
            val playButton: ImageButton = itemView.findViewById(R.id.buttonPlay)
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