package net.primal.core.utils.coroutines

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual object DispatcherProviderFactory {
    actual fun create(): DispatcherProvider = AndroidDispatcherProvider()
}
