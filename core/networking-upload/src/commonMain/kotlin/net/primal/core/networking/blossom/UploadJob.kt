package net.primal.core.networking.blossom

import kotlinx.coroutines.Job

data class UploadJob(
    val job: Job,
    val id: String,
)
