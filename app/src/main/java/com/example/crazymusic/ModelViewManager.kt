package com.example.crazymusic

import android.content.Context
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView

class SimpleModelManager(private val context: Context) {

    fun showModelInPot(modelArea: FrameLayout, vegetable: Vegetable): ImageView {
        // Очищаем область
        modelArea.removeAllViews()

        val imageView = ImageView(context).apply {
            setImageResource(vegetable.drawableId)
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            scaleType = ImageView.ScaleType.FIT_CENTER
        }

        // Добавляем анимацию
        startFloatAnimation(imageView)

        modelArea.addView(imageView)
        return imageView
    }

    private fun startFloatAnimation(view: View) {
        view.animate()
            .translationY(-10f)
            .setDuration(800)
            .withEndAction {
                view.animate()
                    .translationY(0f)
                    .setDuration(800)
                    .withEndAction {
                        startFloatAnimation(view)
                    }
                    .start()
            }
            .start()
    }

    fun clearPot(modelArea: FrameLayout) {
        modelArea.removeAllViews()
    }

    fun cleanup() {
        // Очистка не требуется
    }
}