package com.example.guscard.ui

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.widget.Button
import android.widget.FrameLayout
import android.widget.RelativeLayout
import com.journeyapps.barcodescanner.CaptureActivity
import com.journeyapps.barcodescanner.DecoratedBarcodeView

class CustomScannerActivity : CaptureActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val id = resources.getIdentifier("zxing_barcode_scanner", "id", packageName)
        val barcodeView = findViewById<DecoratedBarcodeView>(id)
        val parent = barcodeView.parent as? FrameLayout
        if (parent != null) {
            // Создаем кнопку "Стрелочка назад"
            val backButton = Button(this).apply {
                text = "←" // Символ стрелочки
                textSize = 28f
                typeface = Typeface.DEFAULT_BOLD
                setBackgroundColor(Color.TRANSPARENT)
                setTextColor(Color.WHITE)
                setPadding(32, 16, 32, 16)
                setOnClickListener {
                    finish() // Закрываем сканер
                }
            }

            // Настраиваем положение (левый верхний угол)
            val params = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(40, 80, 0, 0) // Отступ слева (40) и сверху (80), чтобы не перекрывать статус-бар
                addRule(RelativeLayout.ALIGN_PARENT_TOP)
                addRule(RelativeLayout.ALIGN_PARENT_START)
            }

            // Добавляем кнопку на экран
            parent.addView(backButton, params)
        }
    }
}