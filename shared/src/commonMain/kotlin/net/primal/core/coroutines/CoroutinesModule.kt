package net.primal.core.coroutines

import org.koin.dsl.module

internal val coroutinesModule = module {
    single {
        DispatcherProvider()
    }
}
