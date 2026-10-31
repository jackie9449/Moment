package com.jackie.moment.editor

import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import org.junit.Assert.*
import org.junit.Test

class EditorSessionModelTest {
    private val scope = SaverScope { true }

    @Test fun windowRecreationKeepsLatestEditorAndUndoHistory() {
        val model = EditorSessionModel()
        val editor = model.editor()
        editor.startSession("原文")
        editor.update(TextFieldValue("修改一"))
        val snapshot = with(model.saver) { scope.save(editor) }!!
        editor.update(TextFieldValue("修改二"))
        val restored = model.saver.restore(snapshot)!!
        assertSame(editor, restored)
        assertEquals("修改二", restored.value.text)
        restored.undo()
        assertEquals("修改一", restored.value.text)
        restored.redo()
        assertEquals("修改二", restored.value.text)
    }

    @Test fun processRestoreRecoversTextAndSelectionWithoutSerializingHistory() {
        val oldModel = EditorSessionModel()
        val editor = oldModel.editor()
        editor.startSession("原文")
        editor.update(TextFieldValue("  文字🙂\n ", TextRange(2, 4)))
        val snapshot = with(oldModel.saver) { scope.save(editor) }!!
        val newModel = EditorSessionModel()
        val restored = newModel.saver.restore(snapshot)!!
        assertSame(restored, newModel.editor())
        assertEquals(editor.value.text, restored.value.text)
        assertEquals(TextRange(2, 4), restored.value.selection)
        assertFalse(restored.canUndo)
        assertFalse(restored.canRedo)
    }
}
