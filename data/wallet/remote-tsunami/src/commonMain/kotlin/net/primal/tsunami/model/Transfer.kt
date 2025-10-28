package net.primal.tsunami.model

data class Transfer(
    val id: String,
    val createdAt: Long,
    val updatedAt: Long,
    val senderId: String,
    val receiverId: String,
    val status: TransferStatus,
    val totalAmountInSats: Long,
    val expiryTime: Long,
    val direction: TransferDirection,
    val lightningSendRequest: LightningSendRequest? = null,
    val lightningReceiveRequest: LightningReceiveRequest? = null,
)
