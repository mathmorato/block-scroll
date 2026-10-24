package com.scrollguard.app.service.overlay

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.os.CountDownTimer
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.scrollguard.app.R
import com.scrollguard.app.core.util.PermissionHelper
import java.util.concurrent.atomic.AtomicBoolean

class OverlayWindowManager(private val context: Context) {

    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private var currentView: View? = null
    private var countDownTimer: CountDownTimer? = null
    private val isShowing = AtomicBoolean(false)

    @SuppressLint("InflateParams")
    fun showIntervention(
        data: OverlayData,
        onHomeClicked: () -> Unit,
        onDismissAllowed: () -> Unit
    ) {
        if (!PermissionHelper.canDrawOverlays(context)) {
            // If overlay permission is not granted, directly execute fallback
            onHomeClicked()
            return
        }

        if (isShowing.get()) {
            hideOverlay()
        }

        val layoutParams = WindowManager.LayoutParams().apply {
            type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            format = PixelFormat.TRANSLUCENT
            flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
            width = WindowManager.LayoutParams.MATCH_PARENT
            height = WindowManager.LayoutParams.MATCH_PARENT
            gravity = Gravity.CENTER
        }

        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.layout_overlay_intervention, null)
        currentView = view

        val tvTitle = view.findViewById<TextView>(R.id.overlay_title)
        val tvMessage = view.findViewById<TextView>(R.id.overlay_message)
        val tvDetails = view.findViewById<TextView>(R.id.overlay_details)
        val tvTimer = view.findViewById<TextView>(R.id.overlay_timer)
        val btnExit = view.findViewById<Button>(R.id.btn_exit_home)
        val btnDismiss = view.findViewById<Button>(R.id.btn_dismiss)
        val iconView = view.findViewById<ImageView>(R.id.overlay_icon)

        tvTitle.text = data.title
        tvMessage.text = data.message
        tvDetails.text = data.details

        btnDismiss.isEnabled = false
        btnDismiss.alpha = 0.5f

        btnExit.setOnClickListener {
            hideOverlay()
            onHomeClicked()
        }

        btnDismiss.setOnClickListener {
            hideOverlay()
            onDismissAllowed()
        }

        // 5-second conscious delay timer
        val totalMs = data.countdownSeconds * 1000L
        countDownTimer?.cancel()
        countDownTimer = object : CountDownTimer(totalMs, 1000L) {
            override fun onTick(millisUntilFinished: Long) {
                val sec = (millisUntilFinished / 1000).toInt() + 1
                tvTimer.text = "Respire fundo... (${sec}s)"
            }

            override fun onFinish() {
                tvTimer.text = "Pausa consciente concluída."
                btnDismiss.isEnabled = true
                btnDismiss.alpha = 1.0f
                btnDismiss.text = "Continuar Consciente"
            }
        }.start()

        try {
            windowManager.addView(view, layoutParams)
            isShowing.set(true)
        } catch (e: Exception) {
            e.printStackTrace()
            isShowing.set(false)
        }
    }

    fun hideOverlay() {
        countDownTimer?.cancel()
        countDownTimer = null
        currentView?.let {
            try {
                windowManager.removeView(it)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        currentView = null
        isShowing.set(false)
    }

    fun isOverlayVisible(): Boolean = isShowing.get()
}
