package net.primal.android.networking.primal.upload.domain

import java.util.*
import kotlinx.coroutines.Job

data class UploadJob(
    val job: Job,
    val id: UUID,
)
