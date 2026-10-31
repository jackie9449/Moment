package com.jackie.moment.editor

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import org.junit.Assert.*
import org.junit.Test

class EditorStateTest {
    @Test fun undoRedoPreservesWhitespaceAndEmoji() {
        val state = EditorState("  第一行\n🙂  ")
        state.update(TextFieldValue("  第一行\n🙂 完成  "))
        state.undo()
        assertEquals("  第一行\n🙂  ", state.value.text)
        state.redo()
        assertEquals("  第一行\n🙂 完成  ", state.value.text)
    }

    @Test fun chineseImeCompositionIsOneUndoStep() {
        val state = EditorState()
        state.update(TextFieldValue("p", TextRange(1), TextRange(0, 1)))
        state.update(TextFieldValue("pian", TextRange(4), TextRange(0, 4)))
        state.update(TextFieldValue("片", TextRange(1)))
        state.undo()
        assertEquals("", state.value.text)
        state.redo()
        assertEquals("片", state.value.text)
    }

    @Test fun movingCursorDoesNotDestroyRedo() {
        val state = EditorState("原文")
        state.update(TextFieldValue("修改后的原文"))
        state.undo()
        state.update(state.value.copy(selection = TextRange(0)))
        assertTrue(state.canRedo)
        state.redo()
        assertEquals("修改后的原文", state.value.text)
    }

    @Test fun newSessionResetsHistoryAndPlacesCursorAtEnd() {
        val state = EditorState("旧文本")
        state.update(TextFieldValue("旧修改"))
        state.startSession("\n新文本🙂\n")
        assertEquals("\n新文本🙂\n", state.value.text)
        assertEquals(state.value.text.length, state.value.selection.start)
        assertFalse(state.canUndo)
        assertFalse(state.canRedo)
    }

    @Test fun emptyTextIsAValidUndoableEdit() {
        val state = EditorState("保留的文字")
        state.update(TextFieldValue(""))
        assertEquals("", state.value.text)
        state.undo()
        assertEquals("保留的文字", state.value.text)
    }

    @Test fun historyIsBoundedWithoutBreakingLatestEdits() {
        val state = EditorState()
        repeat(200) { state.update(TextFieldValue("版本 $it")) }
        var undos = 0
        while (state.canUndo) { state.undo(); undos++ }
        assertEquals(100, undos)
        assertEquals("版本 99", state.value.text)
        repeat(100) { state.redo() }
        assertEquals("版本 199", state.value.text)
    }

    @Test fun metricsCountCodePointsAndPreserveTrailingLines() {
        assertEquals(TextMetrics(0, 0), TextMetrics.from(""))
        assertEquals(TextMetrics(4, 2), TextMetrics.from("字🙂\n "))
        assertEquals(TextMetrics(2, 2), TextMetrics.from("字\n"))
    }
}
