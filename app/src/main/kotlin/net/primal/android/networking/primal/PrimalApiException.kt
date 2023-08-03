package net.primal.android.networking.primal

class PrimalApiException private constructor(message: String) : Exception() {
    companion object {
        val ContactListNotFound: PrimalApiException = PrimalApiException("Contact list not found")
        val ContactListTagsNotFound: PrimalApiException = PrimalApiException("Contact list tags not found")
        val ContactListCreatedAtNotFound: PrimalApiException = PrimalApiException("Contact list created at not found")
    }
}