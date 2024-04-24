package net.primal.android.note.api

import net.primal.android.note.api.model.NoteActionsRequestBody
import net.primal.android.note.api.model.NoteActionsResponse
import net.primal.android.note.api.model.NoteZapsRequestBody
import net.primal.android.note.api.model.NoteZapsResponse

interface NoteApi {

    suspend fun getNoteZaps(body: NoteZapsRequestBody): NoteZapsResponse

    suspend fun getNoteActions(body: NoteActionsRequestBody): NoteActionsResponse
}
