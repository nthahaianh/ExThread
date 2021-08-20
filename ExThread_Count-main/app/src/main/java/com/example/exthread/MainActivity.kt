package com.example.exthread

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.Thread.currentThread

class MainActivity : AppCompatActivity() {
    var mValue = 0
    var X = 0f
    var Y = 0f
    var repeatHandler = Handler()
    var autoIncrement = false
    var autoDecrement = false

    var handler: Handler? = @SuppressLint("HandlerLeak")
    object : Handler() {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            tvNumber.text = msg.arg1.toString()
        }
    }

    var focus0Thread = Thread()
    var focus0Runnable = Runnable {
        var count = tvNumber.text.toString().toInt();
        while (!autoDecrement && !autoIncrement && count != 0) {
            try {
                Thread.sleep(75)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
            if (count < 0)
                count++
            else count--
            val message = Message()
            message.arg1 = count
            handler?.sendMessage(message)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mValue = tvNumber.text.toString().toInt()

        btnAdd.setOnClickListener { increase() }
        btnAdd.setOnTouchListener(View.OnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    stopThread()
                    autoDecrement = false
                    autoIncrement = true
                    repeatHandler.postDelayed(RepeatChangeThread(), 500)
                }
                MotionEvent.ACTION_UP -> {
                    autoIncrement = false
                    repeatHandler.removeCallbacks(RepeatChangeThread())
                    startThread()
                }
            }
            false
        })

        btnMinus.setOnClickListener { decrease() }
        btnMinus.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    stopThread()
                    autoIncrement = false
                    autoDecrement = true
                    repeatHandler.postDelayed(RepeatChangeThread(), 500)
                }
                MotionEvent.ACTION_UP -> {
                    autoDecrement = false
                    repeatHandler.removeCallbacks(RepeatChangeThread())
                    startThread()
                }
            }
            false
        }

        layout.setOnTouchListener(View.OnTouchListener { v, event ->
            val x = event.x.toInt()
            val y = event.y.toInt()
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    autoIncrement = true
                    autoDecrement = true
                    stopThread()
                    X = x.toFloat()
                    Y = y.toFloat()
                }
                MotionEvent.ACTION_MOVE -> {
                    if (x - X > 0) {
                        increase()
                    }
                    if (x - X < 0) {
                        decrease()
                    }
                    if (y - Y > 0) {
                        decrease()
                    }
                    if (y - Y < 0) {
                        increase()
                    }
                    X = x.toFloat()
                    Y = y.toFloat()
                }
                MotionEvent.ACTION_UP -> {
                    autoIncrement = false
                    autoDecrement = false
                    startThread()
                }
            }
            true
        })
    }

    inner class RepeatChangeThread : Runnable {
        override fun run() {
            if (autoIncrement) {
                increase()
                repeatHandler.postDelayed(RepeatChangeThread(), 75)
            } else if (autoDecrement) {
                decrease()
                repeatHandler.postDelayed(RepeatChangeThread(), 75)
            }
        }
    }

    fun startThread() {
        synchronized(this) { !autoIncrement && !autoDecrement }
        focus0Thread = Thread(focus0Runnable)
        handler?.postDelayed({ focus0Thread.start() }, 1500)
    }

    fun stopThread() {
        synchronized(this) { autoIncrement || autoDecrement }
        handler?.removeCallbacksAndMessages(null)
        currentThread().interrupt()
        if (!focus0Thread.isInterrupted)
            focus0Thread?.interrupt()
    }

    private fun increase() {
        val a = Integer.valueOf(tvNumber!!.text.toString()) + 1
        tvNumber!!.text = a.toString() + ""
        mValue = a
        if (mValue == 100 || mValue == -100) randomColor()
    }

    fun decrease() {
        val a = Integer.valueOf(tvNumber!!.text.toString()) - 1
        tvNumber!!.text = a.toString() + ""
        mValue = a
        if (mValue % 100 == 0) randomColor()
    }

    private fun randomColor() {
        tvNumber!!.setTextColor(
            Color.rgb(
                java.util.Random().nextInt(255),
                java.util.Random().nextInt(255),
                java.util.Random().nextInt(255)
            )
        )
    }
}