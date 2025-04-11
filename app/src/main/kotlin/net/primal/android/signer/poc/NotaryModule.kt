package net.primal.android.signer.poc

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent

@Module
@InstallIn(ActivityRetainedComponent::class)
abstract class NotaryModule {

    @Binds
    abstract fun bindNotaryImpl(newNotaryHandler: NewNostrNotary): Notary
}
