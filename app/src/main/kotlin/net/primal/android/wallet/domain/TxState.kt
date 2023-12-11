package net.primal.android.wallet.domain

enum class TxState(val id: Int) {
    CREATED(id = 10),
    PROCESSING(id = 20),
    SUCCEEDED(id = 101),
    FAILED(id = 102),
    CANCELED(id = 103),
}
