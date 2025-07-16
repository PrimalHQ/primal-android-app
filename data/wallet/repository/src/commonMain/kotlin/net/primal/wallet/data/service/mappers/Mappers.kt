package net.primal.wallet.data.service.mappers

import net.primal.core.networking.nwc.model.NostrWalletKeypair as NostrWalletKeypairNO
import net.primal.domain.wallet.NostrWalletKeypair as NostrWalletKeypairDO


fun NostrWalletKeypairDO.asNO() =
    NostrWalletKeypairNO(
        privateKey = privateKey,
        pubkey = pubKey,
    )
