package net.primal.domain.wallet

enum class TxType(val id: Int) {
    DEPOSIT(id = 1),
    WITHDRAW(id = 2),
}
