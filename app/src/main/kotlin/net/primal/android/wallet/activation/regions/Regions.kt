package net.primal.android.wallet.activation.regions

import kotlinx.serialization.Serializable

@Serializable
data class Regions(
    val countries: List<List<String>>,
    val states: List<List<String>>,
)

sealed class Region(
    open val name: String,
    open val code: String,
)

data class Country(
    override val name: String,
    override val code: String,
    val states: List<State>,
) : Region(name, code)

data class State(
    override val name: String,
    override val code: String,
) : Region(name, code)
