package net.primal.wallet.domain

enum class TxType(val id: Int) {
    DEPOSIT(id = 1),
    WITHDRAW(id = 2),
}
