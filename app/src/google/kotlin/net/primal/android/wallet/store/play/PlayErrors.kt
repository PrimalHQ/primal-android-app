package net.primal.android.wallet.store.play

import net.primal.android.wallet.store.domain.InAppPurchaseException

class ProductNotAvailable : InAppPurchaseException()

class BillingNotAvailable : InAppPurchaseException()
