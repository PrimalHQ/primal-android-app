package net.primal.networking.primal.upload.domain

import kotlinx.coroutines.Job

data class UploadJob(
    val job: Job,
    val id: String,
)
