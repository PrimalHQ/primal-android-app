package net.primal.android.wallet.store.play

open class InAppPurchaseException : RuntimeException()

class ProductNotAvailable : InAppPurchaseException()

class BillingNotAvailable : InAppPurchaseException()
