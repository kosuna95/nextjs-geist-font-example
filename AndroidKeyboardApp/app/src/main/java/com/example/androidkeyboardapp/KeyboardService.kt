package com.example.androidkeyboardapp

import android.content.SharedPreferences
import android.inputmethodservice.InputMethodService
import android.inputmethodservice.Keyboard
import android.inputmethodservice.KeyboardView
import android.os.Handler
import android.os.Looper
import android.preference.PreferenceManager
import android.util.TypedValue
import android.view.Gravity
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputConnection
import android.widget.FrameLayout
import android.widget.ImageButton
import androidx.core.view.updateLayoutParams

class KeyboardService : InputMethodService(), KeyboardView.OnKeyboardActionListener {

    private lateinit var keyboardView: KeyboardView
    private lateinit var qwertyKeyboard: Keyboard
    private lateinit var numericKeyboard: Keyboard
    private lateinit var commandsKeyboard: Keyboard

    private var isCaps = false
    private var currentKeyboardType = KeyboardType.QWERTY

    private lateinit var prefs: SharedPreferences

    private var oneHandedMode = false
    private var keyboardPosition = KeyboardPosition.RIGHT
    private var keyboardWidthPercent = 70
    private var keyboardHeightDp = 250

    private lateinit var container: FrameLayout
    private lateinit var btnToggleOneHanded: ImageButton
    private lateinit var btnMoveLeft: ImageButton
    private lateinit var btnMoveRight: ImageButton
    private lateinit var btnIncreaseSize: ImageButton
    private lateinit var btnDecreaseSize: ImageButton

    enum class KeyboardType {
        QWERTY, NUMERIC, COMMANDS
    }

    enum class KeyboardPosition {
        LEFT, RIGHT
    }

    override fun onCreate() {
        super.onCreate()
        prefs = PreferenceManager.getDefaultSharedPreferences(this)
        oneHandedMode = prefs.getBoolean("oneHandedMode", false)
        keyboardPosition = if (prefs.getString("keyboardPosition", "RIGHT") == "LEFT") KeyboardPosition.LEFT else KeyboardPosition.RIGHT
        keyboardWidthPercent = prefs.getInt("keyboardWidthPercent", 70)
        keyboardHeightDp = prefs.getInt("keyboardHeightDp", 250)
    }

    override fun onCreateInputView(): View {
        container = FrameLayout(this)

        keyboardView = layoutInflater.inflate(R.layout.keyboard_view, null) as KeyboardView
        qwertyKeyboard = Keyboard(this, R.xml.qwerty_keyboard)
        numericKeyboard = Keyboard(this, R.xml.numeric_keyboard)
        commandsKeyboard = Keyboard(this, R.xml.commands_keyboard)

        setKeyboard(KeyboardType.QWERTY)

        keyboardView.setOnKeyboardActionListener(this)

        container.addView(keyboardView)

        setupOneHandedControls()

        applyOneHandedModeSettings()

        return container
    }

    private fun setupOneHandedControls() {
        btnToggleOneHanded = ImageButton(this).apply {
            setImageResource(android.R.drawable.ic_menu_manage)
            setBackgroundColor(0x00000000)
            setOnClickListener {
                oneHandedMode = !oneHandedMode
                prefs.edit().putBoolean("oneHandedMode", oneHandedMode).apply()
                applyOneHandedModeSettings()
            }
        }
        btnMoveLeft = ImageButton(this).apply {
            setImageResource(android.R.drawable.ic_media_previous)
            setBackgroundColor(0x00000000)
            setOnClickListener {
                keyboardPosition = KeyboardPosition.LEFT
                prefs.edit().putString("keyboardPosition", "LEFT").apply()
                applyOneHandedModeSettings()
            }
        }
        btnMoveRight = ImageButton(this).apply {
            setImageResource(android.R.drawable.ic_media_next)
            setBackgroundColor(0x00000000)
            setOnClickListener {
                keyboardPosition = KeyboardPosition.RIGHT
                prefs.edit().putString("keyboardPosition", "RIGHT").apply()
                applyOneHandedModeSettings()
            }
        }
        btnIncreaseSize = ImageButton(this).apply {
            setImageResource(android.R.drawable.ic_input_add)
            setBackgroundColor(0x00000000)
            setOnClickListener {
                if (keyboardWidthPercent < 100) {
                    keyboardWidthPercent += 5
                    prefs.edit().putInt("keyboardWidthPercent", keyboardWidthPercent).apply()
                    applyOneHandedModeSettings()
                }
            }
        }
        btnDecreaseSize = ImageButton(this).apply {
            setImageResource(android.R.drawable.ic_input_delete)
            setBackgroundColor(0x00000000)
            setOnClickListener {
                if (keyboardWidthPercent > 30) {
                    keyboardWidthPercent -= 5
                    prefs.edit().putInt("keyboardWidthPercent", keyboardWidthPercent).apply()
                    applyOneHandedModeSettings()
                }
            }
        }

        val buttonSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40f, resources.displayMetrics).toInt()

        val paramsToggle = FrameLayout.LayoutParams(buttonSize, buttonSize).apply {
            gravity = Gravity.TOP or Gravity.END
            marginEnd = 10
            topMargin = 10
        }
        val paramsMoveLeft = FrameLayout.LayoutParams(buttonSize, buttonSize).apply {
            gravity = Gravity.TOP or Gravity.START
            marginStart = 10
            topMargin = 10
        }
        val paramsMoveRight = FrameLayout.LayoutParams(buttonSize, buttonSize).apply {
            gravity = Gravity.TOP or Gravity.START
            marginStart = 60
            topMargin = 10
        }
        val paramsIncrease = FrameLayout.LayoutParams(buttonSize, buttonSize).apply {
            gravity = Gravity.TOP or Gravity.START
            marginStart = 110
            topMargin = 10
        }
        val paramsDecrease = FrameLayout.LayoutParams(buttonSize, buttonSize).apply {
            gravity = Gravity.TOP or Gravity.START
            marginStart = 160
            topMargin = 10
        }

        container.addView(btnToggleOneHanded, paramsToggle)
        container.addView(btnMoveLeft, paramsMoveLeft)
        container.addView(btnMoveRight, paramsMoveRight)
        container.addView(btnIncreaseSize, paramsIncrease)
        container.addView(btnDecreaseSize, paramsDecrease)
    }

    private fun applyOneHandedModeSettings() {
        if (oneHandedMode) {
            val widthPixels = (resources.displayMetrics.widthPixels * (keyboardWidthPercent / 100f)).toInt()
            val heightPixels = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, keyboardHeightDp.toFloat(), resources.displayMetrics).toInt()

            keyboardView.updateLayoutParams<FrameLayout.LayoutParams> {
                width = widthPixels
                height = heightPixels
                gravity = if (keyboardPosition == KeyboardPosition.LEFT) Gravity.START or Gravity.BOTTOM else Gravity.END or Gravity.BOTTOM
            }
        } else {
            keyboardView.updateLayoutParams<FrameLayout.LayoutParams> {
                width = ViewGroup.LayoutParams.MATCH_PARENT
                height = ViewGroup.LayoutParams.WRAP_CONTENT
                gravity = Gravity.BOTTOM
            }
        }
        keyboardView.invalidate()
    }

    private fun setKeyboard(type: KeyboardType) {
        currentKeyboardType = type
        when (type) {
            KeyboardType.QWERTY -> keyboardView.keyboard = qwertyKeyboard
            KeyboardType.NUMERIC -> keyboardView.keyboard = numericKeyboard
            KeyboardType.COMMANDS -> keyboardView.keyboard = commandsKeyboard
        }
        keyboardView.invalidateAllKeys()
    }

    override fun onKey(primaryCode: Int, keyCodes: IntArray?) {
        val inputConnection: InputConnection? = currentInputConnection
        if (inputConnection == null) return

        when (primaryCode) {
            Keyboard.KEYCODE_DELETE -> {
                inputConnection.deleteSurroundingText(1, 0)
            }
            Keyboard.KEYCODE_SHIFT -> {
                isCaps = !isCaps
                qwertyKeyboard.isShifted = isCaps
                keyboardView.invalidateAllKeys()
            }
            Keyboard.KEYCODE_DONE -> {
                inputConnection.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
                inputConnection.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER))
            }
            -2 -> { // Cambiar key to switch keyboards
                when (currentKeyboardType) {
                    KeyboardType.QWERTY -> setKeyboard(KeyboardType.NUMERIC)
                    KeyboardType.NUMERIC -> setKeyboard(KeyboardType.COMMANDS)
                    KeyboardType.COMMANDS -> setKeyboard(KeyboardType.QWERTY)
                }
            }
            else -> {
                var code = primaryCode
                if (isCaps && currentKeyboardType == KeyboardType.QWERTY) {
                    code = Character.toUpperCase(code)
                }
                if (currentKeyboardType == KeyboardType.COMMANDS) {
                    // Send CTRL + number + ENTER for C1 to C9 keys
                    val ctrlNumber = when (primaryCode) {
                        101 -> 1
                        102 -> 2
                        103 -> 3
                        104 -> 4
                        105 -> 5
                        106 -> 6
                        107 -> 7
                        108 -> 8
                        109 -> 9
                        else -> 0
                    }
                    if (ctrlNumber != 0) {
                        sendCtrlNumberEnter(inputConnection, ctrlNumber)
                        return
                    }
                }
                inputConnection.commitText(code.toChar().toString(), 1)
            }
        }
    }

    private fun sendCtrlNumberEnter(inputConnection: InputConnection, number: Int) {
        // Sending CTRL + number + ENTER is not straightforward in Android IME.
        // We simulate by sending control character + number + enter key events.
        // This may require app support to interpret.

        // Send CTRL key down
        inputConnection.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_CTRL_LEFT))
        // Send number key down and up
        val numberKeyCode = KeyEvent.keyCodeFromString("KEYCODE_${number}")
        inputConnection.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, numberKeyCode))
        inputConnection.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, numberKeyCode))
        // Send CTRL key up
        inputConnection.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_CTRL_LEFT))
        // Send ENTER key
        inputConnection.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
        inputConnection.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER))
    }

    override fun onPress(primaryCode: Int) {}
    override fun onRelease(primaryCode: Int) {}
    override fun onText(text: CharSequence?) {}
    override fun swipeLeft() {}
    override fun swipeRight() {}
    override fun swipeDown() {}
    override fun swipeUp() {}

}
