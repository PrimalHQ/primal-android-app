package net.primal.core.networking.blossom

open class BlossomException(message: String? = null, cause: Throwable? = null) : RuntimeException(message, cause)

class BlossomUploadException(message: String? = null, cause: Throwable? = null) : BlossomException(message, cause)

class UploadRequirementException(message: String? = null, cause: Throwable? = null) : BlossomException(message, cause)

class BlossomMirrorException(message: String? = null, cause: Throwable? = null) : BlossomException(message, cause)
