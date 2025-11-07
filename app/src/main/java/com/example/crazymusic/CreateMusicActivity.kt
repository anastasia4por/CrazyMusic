package com.example.crazymusic

import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.view.DragEvent
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import java.util.*
import android.graphics.Color
import android.text.InputFilter
import android.text.InputType

class CreateMusicActivity : AppCompatActivity() {

    private val potVegetables = mutableMapOf<Int, Vegetable>()
    private val activeSounds = mutableMapOf<Int, MediaPlayer>()
    private val handlers = Array(3) { Handler() }
    private var isRecording = false
    private var recordingStartTime = 0L
    private var currentComposition: MusicComposition? = null
    private lateinit var recordingTimeView: TextView
    private lateinit var modelManager: SimpleModelManager

    // Звуки интерфейса
    private var interfaceSoundsEnabled = true
    private var soundPlace: MediaPlayer? = null
    private var soundRemove: MediaPlayer? = null

    companion object {
        private const val MAX_RECORDING_TIME = 90000L
        private val SOUND_PLACE = R.raw.place_sound
        private val SOUND_REMOVE = R.raw.remove_sound
    }

    private val potIds by lazy {
        listOf(R.id.pot1, R.id.pot2, R.id.pot3, R.id.pot4, R.id.pot5)
    }

    private val vegetableIds by lazy {
        listOf(R.id.vegetableCard1, R.id.vegetableCard2, R.id.vegetableCard3,
            R.id.vegetableCard4, R.id.vegetableCard5, R.id.vegetableCard6)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_music)

        modelManager = SimpleModelManager(this)
        recordingTimeView = findViewById(R.id.recordingTime)

        loadInterfaceSoundsSetting()
        setupBackButton()
        setupRecordButton()
        setupInfoButton()
        setupSettingsButton()
        setupVegetableDrag()
        initializePots()
        preloadInterfaceSounds()
    }

    private fun loadInterfaceSoundsSetting() {
        val prefs = getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        interfaceSoundsEnabled = prefs.getBoolean("interface_sounds", true)
    }

    private fun preloadInterfaceSounds() {
        soundPlace = MediaPlayer.create(this, SOUND_PLACE)
        soundRemove = MediaPlayer.create(this, SOUND_REMOVE)
    }

    private fun playInterfaceSound(soundType: Int) {
        if (!interfaceSoundsEnabled) return

        when (soundType) {
            SOUND_PLACE -> {
                soundPlace?.let {
                    if (it.isPlaying) it.seekTo(0)
                    it.start()
                }
            }
            SOUND_REMOVE -> {
                soundRemove?.let {
                    if (it.isPlaying) it.seekTo(0)
                    it.start()
                }
            }
        }
    }

    private fun setupBackButton() {
        findViewById<ImageButton>(R.id.buttonBack).setOnClickListener {
            finish()
        }
    }

    private fun setupRecordButton() {
        findViewById<ImageButton>(R.id.buttonRecord).setOnClickListener {
            if (!isRecording) startRecording() else stopRecording()
        }
    }

    private fun setupInfoButton() {
        findViewById<ImageButton>(R.id.buttonInfo).setOnClickListener {
            showGameInfoDialog()
        }
    }

    private fun setupSettingsButton() {
        findViewById<ImageButton>(R.id.buttonSettings).setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }
    }

    private fun showGameInfoDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_game_info, null)
        val builder = AlertDialog.Builder(this).setView(dialogView)
        val dialog = builder.create()

        dialogView.findViewById<Button>(R.id.closeButton).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun startRecording() {
        isRecording = true
        recordingStartTime = System.currentTimeMillis()
        currentComposition = MusicComposition("", 0)

        recordingTimeView.visibility = View.VISIBLE
        setVegetablesEnabled(false)
        updateRecordButton(true)
        startRecordingTimer()
    }

    private fun stopRecording() {
        isRecording = false
        handlers[2].removeCallbacksAndMessages(null)
        setVegetablesEnabled(true)
        recordingTimeView.visibility = View.GONE
        updateRecordButton(false)

        currentComposition?.let { composition ->
            composition.duration = System.currentTimeMillis() - recordingStartTime
            showSaveCompositionDialog()
        }
    }

    private fun startRecordingTimer() {
        handlers[2].postDelayed(object : Runnable {
            override fun run() {
                if (isRecording) {
                    val elapsedTime = System.currentTimeMillis() - recordingStartTime
                    updateRecordingTime(elapsedTime)

                    if (elapsedTime >= MAX_RECORDING_TIME) {
                        stopRecording()
                    } else {
                        handlers[2].postDelayed(this, 1000)
                    }
                }
            }
        }, 1000)
    }

    private fun updateRecordingTime(elapsedTime: Long) {
        val seconds = elapsedTime / 1000
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60

        val timeText = String.format("%02d:%02d", minutes, remainingSeconds)
        recordingTimeView.text = timeText
    }

    private fun setVegetablesEnabled(enabled: Boolean) {
        val vegetablesContainer = findViewById<GridLayout>(R.id.vegetablesContainer)
        for (i in 0 until vegetablesContainer.childCount) {
            val vegetableCard = vegetablesContainer.getChildAt(i)
            vegetableCard.isEnabled = enabled
            vegetableCard.alpha = if (enabled) 1.0f else 0.3f
        }
    }

    private fun updateRecordButton(recording: Boolean) {
        val recordButton = findViewById<ImageButton>(R.id.buttonRecord)
        if (recording) {
            recordButton.setImageResource(R.drawable.ic_stop)
            recordButton.setColorFilter(Color.RED)
        } else {
            recordButton.setImageResource(R.drawable.ic_mic)
            recordButton.setColorFilter(Color.WHITE)
        }
    }

    private fun showSaveCompositionDialog() {
        val input = EditText(this).apply {
            inputType = InputType.TYPE_CLASS_TEXT
            hint = "Введите название (макс. 24 символов)"
                    filters = arrayOf(InputFilter.LengthFilter(24))
        }

        AlertDialog.Builder(this)
            .setTitle("Сохранить мелодию")
            .setView(input)
            .setPositiveButton("Сохранить") { dialog, _ ->
                val name = input.text.toString().trim()
                if (name.isNotEmpty()) {
                    currentComposition?.name = name
                    currentComposition?.let { saveComposition(it) }
                    showToast("Мелодия сохранена!")
                }
                dialog.dismiss()
            }
            .setNegativeButton("Отмена") { dialog, _ ->
                currentComposition = null
                dialog.cancel()
            }
            .show()
    }

    private fun saveComposition(composition: MusicComposition) {
        val prefs = getSharedPreferences("music_compositions", MODE_PRIVATE)
        val editor = prefs.edit()

        val gson = Gson()
        val compositionJson = gson.toJson(composition)

        editor.putString(composition.id, compositionJson)
        editor.apply()
    }

    private fun initializePots() {
        potIds.forEach { potId ->
            val pot = findViewById<FrameLayout>(potId)
            pot.tag = "empty"
        }
    }

    private fun setupVegetableDrag() {
        vegetableIds.forEach { vegId ->
            val vegetableCard = findViewById<View>(vegId)
            setupLongPressDrag(vegetableCard)
        }

        potIds.forEach { potId ->
            val pot = findViewById<View>(potId)
            pot.setOnDragListener(PotDragListener())
        }
    }

    private fun setupLongPressDrag(view: View) {
        view.setOnLongClickListener { v ->
            val shadowBuilder = View.DragShadowBuilder(v)
            v.startDragAndDrop(null, shadowBuilder, v, 0)
            true
        }
    }

    private inner class PotDragListener : View.OnDragListener {
        override fun onDrag(pot: View, event: DragEvent): Boolean {
            val draggedView = event.localState as View
            val vegetableTag = draggedView.tag as String

            return when (event.action) {
                DragEvent.ACTION_DRAG_STARTED -> true
                DragEvent.ACTION_DRAG_ENTERED -> true
                DragEvent.ACTION_DRAG_EXITED -> true
                DragEvent.ACTION_DROP -> {
                    placeVegetableInPot(pot, draggedView, vegetableTag)
                    true
                }
                DragEvent.ACTION_DRAG_ENDED -> true
                else -> false
            }
        }
    }

    private fun placeVegetableInPot(pot: View, vegetableCard: View, vegetableType: String) {
        if (pot.tag != "empty") return

        val vegetable = createVegetableByType(vegetableType) ?: return

        pot.tag = vegetableType
        potVegetables[pot.id] = vegetable

        vegetableCard.alpha = 0.3f
        vegetableCard.isEnabled = false

        val modelArea = findModelAreaForPot(pot.id)
        showVegetableModel(modelArea, vegetable)
        playVegetableSound(pot.id, vegetable)

        playInterfaceSound(SOUND_PLACE)

        if (isRecording) {
            val currentTime = System.currentTimeMillis() - recordingStartTime
            val marker = VegetableMarker(vegetableType, pot.id, currentTime, 0)
            currentComposition?.addMarker(marker)
        }
    }

    private fun findModelAreaForPot(potId: Int): FrameLayout {
        return when (potId) {
            R.id.pot1 -> findViewById(R.id.modelArea1)
            R.id.pot2 -> findViewById(R.id.modelArea2)
            R.id.pot3 -> findViewById(R.id.modelArea3)
            R.id.pot4 -> findViewById(R.id.modelArea4)
            R.id.pot5 -> findViewById(R.id.modelArea5)
            else -> findViewById(R.id.modelArea1)
        }
    }

    private fun showVegetableModel(modelArea: FrameLayout, vegetable: Vegetable) {
        modelManager.showModelInPot(modelArea, vegetable)
        setupModelLongPress(modelArea, vegetable)
    }

    private fun setupModelLongPress(modelArea: FrameLayout, vegetable: Vegetable) {
        modelArea.setOnLongClickListener {
            // Находим горшок, соответствующий этой области модели
            val pot = findPotForModelArea(modelArea.id)
            removeVegetableFromPot(pot, vegetable)
            true
        }
    }

    private fun removeVegetableFromPot(pot: View, vegetable: Vegetable) {
        stopVegetableSound(pot.id)

        // Очищаем область модели
        val modelArea = findModelAreaForPot(pot.id)
        modelManager.clearPot(modelArea)

        // Освобождаем горшок
        pot.tag = "empty"
        potVegetables.remove(pot.id)

        // Активируем карточку
        activateVegetableCard(vegetable)
        playInterfaceSound(SOUND_REMOVE)
    }

    private fun findPotForModelArea(modelAreaId: Int): FrameLayout {
        return when (modelAreaId) {
            R.id.modelArea1 -> findViewById(R.id.pot1)
            R.id.modelArea2 -> findViewById(R.id.pot2)
            R.id.modelArea3 -> findViewById(R.id.pot3)
            R.id.modelArea4 -> findViewById(R.id.pot4)
            R.id.modelArea5 -> findViewById(R.id.pot5)
            else -> findViewById(R.id.pot1)
        }
    }

    private fun activateVegetableCard(vegetable: Vegetable) {
        val vegetablesContainer = findViewById<GridLayout>(R.id.vegetablesContainer)
        for (i in 0 until vegetablesContainer.childCount) {
            val child = vegetablesContainer.getChildAt(i)
            val vegetableType = getVegetableTypeFromCardTag(child.tag)
            if (vegetableType == vegetable.name) {
                child.alpha = 1.0f
                child.isEnabled = true
                break
            }
        }
    }

    private fun getVegetableTypeFromCardTag(tag: Any?): String {
        return when (tag) {
            "orange" -> "Апельсин"
            "cherry" -> "Вишня"
            "apple" -> "Яблоко"
            "strawberry" -> "Клубника"
            "pumpkin" -> "Тыква"
            "grape" -> "Виноград"
            else -> ""
        }
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

    private fun playVegetableSound(potId: Int, vegetable: Vegetable) {
        MediaPlayer.create(this, vegetable.soundId)?.apply {
            isLooping = true
            start()
            activeSounds[potId] = this
        }
    }

    private fun stopVegetableSound(potId: Int) {
        activeSounds[potId]?.apply {
            stop()
            release()
        }
        activeSounds.remove(potId)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onResume() {
        super.onResume()
        loadInterfaceSoundsSetting()
    }

    override fun onDestroy() {
        super.onDestroy()
        activeSounds.values.forEach { it.release() }
        activeSounds.clear()
        soundPlace?.release()
        soundRemove?.release()
        handlers.forEach { it.removeCallbacksAndMessages(null) }
        modelManager.cleanup()
    }
}