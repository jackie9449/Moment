package com.jackie.moment.editor

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import java.util.ArrayDeque

@Stable
class EditorState(initialText: String = "", selection: TextRange = TextRange(initialText.length)) {
    var value by mutableStateOf(TextFieldValue(initialText, selection))
        private set
    private val undoHistory = ArrayDeque<TextFieldValue>()
    private val redoHistory = ArrayDeque<TextFieldValue>()
    private var revision by mutableIntStateOf(0)

    val canUndo: Boolean get() { revision; return undoHistory.isNotEmpty() }
    val canRedo: Boolean get() { revision; return redoHistory.isNotEmpty() }

    fun update(next: TextFieldValue) {
        if (next.text != value.text) {
            // Keep an IME composition (for example Chinese pinyin -> 汉字) in one
            // undo step instead of restoring half-composed input.
            if (value.composition == null) {
                undoHistory.addLast(value.copy(composition = null))
                if (undoHistory.size > 100) undoHistory.removeFirst()
            }
            redoHistory.clear()
            revision++
        }
        value = next
    }

    fun undo() {
        if (undoHistory.isEmpty()) return
        redoHistory.addLast(value.copy(composition = null))
        value = undoHistory.removeLast()
        revision++
    }

    fun redo() {
        if (redoHistory.isEmpty()) return
        undoHistory.addLast(value.copy(composition = null))
        value = redoHistory.removeLast()
        revision++
    }

    fun startSession(text: String) {
        value = TextFieldValue(text, TextRange(text.length))
        undoHistory.clear()
        redoHistory.clear()
        revision++
    }

    companion object {
        // Save transient text only for Android's configuration/process restoration.
        // Preferences and files never receive clipboard text.
        val Saver = Saver<EditorState, List<Any>>(
            save = { listOf(it.value.text, it.value.selection.start, it.value.selection.end) },
            restore = { EditorState(it[0] as String, TextRange(it[1] as Int, it[2] as Int)) },
        )
    }
}

data class TextMetrics(val characters: Int, val lines: Int) {
    companion object {
        fun from(text: String) = TextMetrics(
            characters = text.codePointCount(0, text.length),
            lines = if (text.isEmpty()) 0 else text.count { it == '\n' } + 1,
        )
    }
}
