package net.primal.data.account.local.dao

enum class RequestState {
    PendingUserAction,
    PendingResponse,
    Approved,
    Rejected,
}
