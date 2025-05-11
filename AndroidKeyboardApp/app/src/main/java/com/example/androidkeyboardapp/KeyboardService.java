package com.example.androidkeyboardapp;

import android.content.SharedPreferences;
import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.preference.PreferenceManager;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputConnection;
import android.widget.FrameLayout;
import android.widget.ImageButton;

public class KeyboardService extends InputMethodService implements KeyboardView.OnKeyboardActionListener {

    private KeyboardView keyboardView;
    private Keyboard qwertyKeyboard;
    private Keyboard numericKeyboard;
    private Keyboard commandsKeyboard;

    private boolean isCaps = false;
    private KeyboardType currentKeyboardType = KeyboardType.QWERTY;

    private SharedPreferences prefs;

    private boolean oneHandedMode = false;
    private KeyboardPosition keyboardPosition = KeyboardPosition.RIGHT;
    private int keyboardWidthPercent = 70;
    private int keyboardHeightDp = 250;

    private FrameLayout container;
    private ImageButton btnToggleOneHanded;
    private ImageButton btnMoveLeft;
    private ImageButton btnMoveRight;
    private ImageButton btnIncreaseSize;
    private ImageButton btnDecreaseSize;

    public enum KeyboardType {
        QWERTY, NUMERIC, COMMANDS
    }

    public enum KeyboardPosition {
        LEFT, RIGHT
    }

    @Override
    public void onCreate() {
        super.onCreate();
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        oneHandedMode = prefs.getBoolean("oneHandedMode", false);
        keyboardPosition = prefs.getString("keyboardPosition", "RIGHT").equals("LEFT") ? 
            KeyboardPosition.LEFT : KeyboardPosition.RIGHT;
        keyboardWidthPercent = prefs.getInt("keyboardWidthPercent", 70);
        keyboardHeightDp = prefs.getInt("keyboardHeightDp", 250);
    }

    @Override
    public View onCreateInputView() {
        container = new FrameLayout(this);

        keyboardView = (KeyboardView) getLayoutInflater().inflate(R.layout.keyboard_view, null);
        qwertyKeyboard = new Keyboard(this, R.xml.qwerty_keyboard);
        numericKeyboard = new Keyboard(this, R.xml.numeric_keyboard);
        commandsKeyboard = new Keyboard(this, R.xml.commands_keyboard);

        setKeyboard(KeyboardType.QWERTY);

        keyboardView.setOnKeyboardActionListener(this);

        container.addView(keyboardView);

        setupOneHandedControls();

        applyOneHandedModeSettings();

        return container;
    }

    private void setupOneHandedControls() {
        btnToggleOneHanded = new ImageButton(this);
        btnToggleOneHanded.setImageResource(android.R.drawable.ic_menu_manage);
        btnToggleOneHanded.setBackgroundColor(0x00000000);
        btnToggleOneHanded.setOnClickListener(v -> {
            oneHandedMode = !oneHandedMode;
            prefs.edit().putBoolean("oneHandedMode", oneHandedMode).apply();
            applyOneHandedModeSettings();
        });

        btnMoveLeft = new ImageButton(this);
        btnMoveLeft.setImageResource(android.R.drawable.ic_media_previous);
        btnMoveLeft.setBackgroundColor(0x00000000);
        btnMoveLeft.setOnClickListener(v -> {
            keyboardPosition = KeyboardPosition.LEFT;
            prefs.edit().putString("keyboardPosition", "LEFT").apply();
            applyOneHandedModeSettings();
        });

        btnMoveRight = new ImageButton(this);
        btnMoveRight.setImageResource(android.R.drawable.ic_media_next);
        btnMoveRight.setBackgroundColor(0x00000000);
        btnMoveRight.setOnClickListener(v -> {
            keyboardPosition = KeyboardPosition.RIGHT;
            prefs.edit().putString("keyboardPosition", "RIGHT").apply();
            applyOneHandedModeSettings();
        });

        btnIncreaseSize = new ImageButton(this);
        btnIncreaseSize.setImageResource(android.R.drawable.ic_input_add);
        btnIncreaseSize.setBackgroundColor(0x00000000);
        btnIncreaseSize.setOnClickListener(v -> {
            if (keyboardWidthPercent < 100) {
                keyboardWidthPercent += 5;
                prefs.edit().putInt("keyboardWidthPercent", keyboardWidthPercent).apply();
                applyOneHandedModeSettings();
            }
        });

        btnDecreaseSize = new ImageButton(this);
        btnDecreaseSize.setImageResource(android.R.drawable.ic_input_delete);
        btnDecreaseSize.setBackgroundColor(0x00000000);
        btnDecreaseSize.setOnClickListener(v -> {
            if (keyboardWidthPercent > 30) {
                keyboardWidthPercent -= 5;
                prefs.edit().putInt("keyboardWidthPercent", keyboardWidthPercent).apply();
                applyOneHandedModeSettings();
            }
        });

        int buttonSize = (int) TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 40f, getResources().getDisplayMetrics());

        FrameLayout.LayoutParams paramsToggle = new FrameLayout.LayoutParams(buttonSize, buttonSize);
        paramsToggle.gravity = Gravity.TOP | Gravity.END;
        paramsToggle.rightMargin = 10;
        paramsToggle.topMargin = 10;

        FrameLayout.LayoutParams paramsMoveLeft = new FrameLayout.LayoutParams(buttonSize, buttonSize);
        paramsMoveLeft.gravity = Gravity.TOP | Gravity.START;
        paramsMoveLeft.leftMargin = 10;
        paramsMoveLeft.topMargin = 10;

        FrameLayout.LayoutParams paramsMoveRight = new FrameLayout.LayoutParams(buttonSize, buttonSize);
        paramsMoveRight.gravity = Gravity.TOP | Gravity.START;
        paramsMoveRight.leftMargin = 60;
        paramsMoveRight.topMargin = 10;

        FrameLayout.LayoutParams paramsIncrease = new FrameLayout.LayoutParams(buttonSize, buttonSize);
        paramsIncrease.gravity = Gravity.TOP | Gravity.START;
        paramsIncrease.leftMargin = 110;
        paramsIncrease.topMargin = 10;

        FrameLayout.LayoutParams paramsDecrease = new FrameLayout.LayoutParams(buttonSize, buttonSize);
        paramsDecrease.gravity = Gravity.TOP | Gravity.START;
        paramsDecrease.leftMargin = 160;
        paramsDecrease.topMargin = 10;

        container.addView(btnToggleOneHanded, paramsToggle);
        container.addView(btnMoveLeft, paramsMoveLeft);
        container.addView(btnMoveRight, paramsMoveRight);
        container.addView(btnIncreaseSize, paramsIncrease);
        container.addView(btnDecreaseSize, paramsDecrease);
    }

    private void applyOneHandedModeSettings() {
        if (oneHandedMode) {
            int widthPixels = (int) (getResources().getDisplayMetrics().widthPixels * 
                (keyboardWidthPercent / 100f));
            int heightPixels = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 
                keyboardHeightDp, 
                getResources().getDisplayMetrics()
            );

            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(widthPixels, heightPixels);
            params.gravity = (keyboardPosition == KeyboardPosition.LEFT ? 
                Gravity.START : Gravity.END) | Gravity.BOTTOM;
            keyboardView.setLayoutParams(params);
        } else {
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            );
            params.gravity = Gravity.BOTTOM;
            keyboardView.setLayoutParams(params);
        }
        keyboardView.invalidate();
    }

    private void setKeyboard(KeyboardType type) {
        currentKeyboardType = type;
        switch (type) {
            case QWERTY:
                keyboardView.setKeyboard(qwertyKeyboard);
                break;
            case NUMERIC:
                keyboardView.setKeyboard(numericKeyboard);
                break;
            case COMMANDS:
                keyboardView.setKeyboard(commandsKeyboard);
                break;
        }
        keyboardView.invalidateAllKeys();
    }

    @Override
    public void onKey(int primaryCode, int[] keyCodes) {
        InputConnection inputConnection = getCurrentInputConnection();
        if (inputConnection == null) return;

        switch (primaryCode) {
            case Keyboard.KEYCODE_DELETE:
                inputConnection.deleteSurroundingText(1, 0);
                break;
            case Keyboard.KEYCODE_SHIFT:
                isCaps = !isCaps;
                qwertyKeyboard.setShifted(isCaps);
                keyboardView.invalidateAllKeys();
                break;
            case Keyboard.KEYCODE_DONE:
                inputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
                inputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER));
                break;
            case -2: // Cambiar key to switch keyboards
                switch (currentKeyboardType) {
                    case QWERTY:
                        setKeyboard(KeyboardType.NUMERIC);
                        break;
                    case NUMERIC:
                        setKeyboard(KeyboardType.COMMANDS);
                        break;
                    case COMMANDS:
                        setKeyboard(KeyboardType.QWERTY);
                        break;
                }
                break;
            default:
                int code = primaryCode;
                if (isCaps && currentKeyboardType == KeyboardType.QWERTY) {
                    code = Character.toUpperCase(code);
                }
                if (currentKeyboardType == KeyboardType.COMMANDS) {
                    int ctrlNumber = 0;
                    switch (primaryCode) {
                        case 101: ctrlNumber = 1; break;
                        case 102: ctrlNumber = 2; break;
                        case 103: ctrlNumber = 3; break;
                        case 104: ctrlNumber = 4; break;
                        case 105: ctrlNumber = 5; break;
                        case 106: ctrlNumber = 6; break;
                        case 107: ctrlNumber = 7; break;
                        case 108: ctrlNumber = 8; break;
                        case 109: ctrlNumber = 9; break;
                    }
                    if (ctrlNumber != 0) {
                        sendCtrlNumberEnter(inputConnection, ctrlNumber);
                        return;
                    }
                }
                inputConnection.commitText(String.valueOf((char) code), 1);
        }
    }

    private void sendCtrlNumberEnter(InputConnection inputConnection, int number) {
        // Send CTRL key down
        inputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_CTRL_LEFT));
        // Send number key down and up
        int numberKeyCode = KeyEvent.keyCodeFromString("KEYCODE_" + number);
        inputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, numberKeyCode));
        inputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, numberKeyCode));
        // Send CTRL key up
        inputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_CTRL_LEFT));
        // Send ENTER key
        inputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
        inputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER));
    }

    @Override
    public void onPress(int primaryCode) {}

    @Override
    public void onRelease(int primaryCode) {}

    @Override
    public void onText(CharSequence text) {}

    @Override
    public void swipeLeft() {}

    @Override
    public void swipeRight() {}

    @Override
    public void swipeDown() {}

    @Override
    public void swipeUp() {}
}
