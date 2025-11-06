package com.example.crazymusic

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatSeekBar

class VineSeekBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatSeekBar(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val leafPositions = listOf(0.1f, 0.2f, 0.3f, 0.4f, 0.7f, 0.9f)
    private val activeLeafPositions = listOf(0.05f, 0.25f, 0.5f, 0.75f)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 24f
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
    }

    private val vineGradient = LinearGradient(
        0f, 0f, 0f, 0f,
        intArrayOf(
            Color.parseColor("#86EFAC"), // green-300
            Color.parseColor("#4ADE80"), // green-400
            Color.parseColor("#22C55E")  // green-500
        ),
        null,
        Shader.TileMode.CLAMP
    )

    private val activeVineGradient = LinearGradient(
        0f, 0f, 0f, 0f,
        intArrayOf(
            Color.parseColor("#34D399"), // emerald-400
            Color.parseColor("#22C55E"), // green-500
            Color.parseColor("#a3fd1c")  // lime-500
        ),
        null,
        Shader.TileMode.CLAMP
    )

    init {
        // Убираем стандартный thumb
        splitTrack = false
        thumb = null
    }

    override fun onDraw(canvas: Canvas) {
        val width = width.toFloat()
        val height = height.toFloat()
        val trackHeight = height * 0.2f
        val trackTop = (height - trackHeight) / 2
        val progress = progress.toFloat() / max.toFloat()
        val progressWidth = width * progress

        // Рисуем фон лианы
        paint.shader = vineGradient
        canvas.drawRoundRect(0f, trackTop, width, trackTop + trackHeight, trackHeight/2, trackHeight/2, paint)

        // Рисуем активную часть лианы
        if (progress > 0) {
            paint.shader = activeVineGradient
            canvas.drawRoundRect(0f, trackTop, progressWidth, trackTop + trackHeight, trackHeight/2, trackHeight/2, paint)
        }

        // Рисуем цветок-ползунок
        drawFlowerThumb(canvas, progressWidth, height / 2, progress)

    }


    private fun drawFlowerThumb(canvas: Canvas, x: Float, centerY: Float, progress: Float) {
        val flower = when {
            progress < 0.1 -> "🌱"
            progress < 0.5 -> "🌸"
            progress < 0.85 -> "🌺"
            else -> "🌻"
        }

        val scale = if (isPressed) 1.8f else 1.5f
        textPaint.textSize = 28f * scale
        textPaint.alpha = if (isEnabled) 255 else 128

        canvas.drawText(flower, x, centerY, textPaint)
    }

    private fun shouldPulse(position: Float): Boolean {
        return System.currentTimeMillis() % 1000 < 500
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredHeight = (textPaint.textSize * 2).toInt()
        val height = resolveSizeAndState(desiredHeight, heightMeasureSpec, 0)
        setMeasuredDimension(widthMeasureSpec, height)
    }
}