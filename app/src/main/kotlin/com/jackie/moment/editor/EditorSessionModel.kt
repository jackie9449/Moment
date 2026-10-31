package com.jackie.moment.editor

import androidx.compose.runtime.saveable.Saver
import androidx.lifecycle.ViewModel

/** Keep undo in memory across a window-mode recreation; process restoration
 * still stores only the current text and selection through EditorState.Saver. */
class EditorSessionModel : ViewModel() {
    private var retained: EditorState? = null
    val saver = Saver<EditorState, List<Any>>(
        save = { state -> with(EditorState.Saver) { save(state) } },
        restore = { snapshot -> retained ?: EditorState.Saver.restore(snapshot)?.also { retained = it } },
    )
    fun editor(): EditorState = retained ?: EditorState().also { retained = it }
}
