package net.primal.domain.wallet.exception

sealed class WalletException(
    override val message: String,
    override val cause: Throwable? = null,
) : Exception(message, cause) {

    class WalletNotFound(
        cause: Throwable? = null,
    ) : WalletException(
        message = "Wallet not found.",
        cause = cause,
    )

    class WalletNotInitialized(
        cause: Throwable? = null,
    ) : WalletException(
        message = "Wallet not initialized.",
        cause = cause,
    )

    class WalletNetworkException(
        cause: Throwable? = null,
    ) : WalletException(
        message = "Network error. Please check your connection.",
        cause = cause,
    )
}

sealed class WalletPaymentException(
    override val message: String,
    override val cause: Throwable? = null,
) : WalletException(message, cause) {

    class InsufficientBalance(
        cause: Throwable? = null,
    ) : WalletPaymentException(
        message = "Insufficient balance.",
        cause = cause,
    )

    class PaymentFailed(
        val reason: String,
        cause: Throwable? = null,
    ) : WalletPaymentException(
        message = "Payment failed: $reason",
        cause = cause,
    )

    class PaymentTimeout(
        cause: Throwable? = null,
    ) : WalletPaymentException(
        message = "Payment timed out.",
        cause = cause,
    )

    class InvalidPaymentRequest(
        val reason: String,
        cause: Throwable? = null,
    ) : WalletPaymentException(
        message = "Invalid payment request: $reason",
        cause = cause,
    )

    class OperationNotSupported(
        val operation: String,
        cause: Throwable? = null,
    ) : WalletPaymentException(
        message = "Operation not supported: $operation",
        cause = cause,
    )
}

sealed class WalletInvoiceException(
    override val message: String,
    override val cause: Throwable? = null,
) : WalletException(message, cause) {

    class FailedToCreateInvoice(
        val reason: String,
        cause: Throwable? = null,
    ) : WalletInvoiceException(
        message = "Failed to create invoice: $reason",
        cause = cause,
    )

    class InvalidInvoiceAmount(
        cause: Throwable? = null,
    ) : WalletInvoiceException(
        message = "Invalid invoice amount.",
        cause = cause,
    )
}

sealed class WalletFeesException(
    override val message: String,
    override val cause: Throwable? = null,
) : WalletException(message, cause) {

    class FailedToFetchFees(
        val reason: String,
        cause: Throwable? = null,
    ) : WalletFeesException(
        message = "Failed to fetch fees: $reason",
        cause = cause,
    )

    class FeeQuoteExpired(
        cause: Throwable? = null,
    ) : WalletFeesException(
        message = "Fee quote has expired. Please refresh and try again.",
        cause = cause,
    )
}

sealed class WalletConnectionException(
    override val message: String,
    override val cause: Throwable? = null,
) : WalletException(message, cause) {

    class RateLimited(
        cause: Throwable? = null,
    ) : WalletConnectionException(
        message = "Rate limited. Please try again later.",
        cause = cause,
    )

    class Unauthorized(
        cause: Throwable? = null,
    ) : WalletConnectionException(
        message = "Unauthorized. Please reconnect your wallet.",
        cause = cause,
    )

    class QuotaExceeded(
        cause: Throwable? = null,
    ) : WalletConnectionException(
        message = "Quota exceeded.",
        cause = cause,
    )

    class ConnectionFailed(
        val reason: String,
        cause: Throwable? = null,
    ) : WalletConnectionException(
        message = "Connection failed: $reason",
        cause = cause,
    )
}
