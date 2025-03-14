package net.primal.domain.upload

import kotlinx.coroutines.Job

data class UploadJob(
    val job: Job,
    val id: String,
)
