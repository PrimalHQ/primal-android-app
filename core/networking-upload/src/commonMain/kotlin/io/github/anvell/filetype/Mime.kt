@file:Suppress("unused")

package io.github.anvell.filetype

/**
 * A media type (also known as a Multipurpose Internet Mail Extensions or MIME type)
 * indicates the nature and format of a document, file, or assortment of bytes.
 * MIME types are defined and standardized in IETF's RFC 6838.
 */
data class Mime(
    val type: Type,
    val subtype: Subtype,
) {
    /**
     * The [Type] represents the general category into which
     * the data type falls, such as video or text.
     *
     * Note that *example* type is excluded here.
     */
    enum class Type(private val value: String) {
        Application("application"),
        Audio("audio"),
        Font("font"),
        Image("image"),
        Model("model"),
        Text("text"),
        Video("video"),
        ;

        override fun toString(): String = value
    }

    /**
     * The [Subtype] identifies the exact kind of data
     * of the specified type the MIME type represents.
     */
    data class Subtype(private val value: String) {

        override fun toString(): String = value
    }

    /**
     * Formats [Type]-[Subtype] pair as RFC 6838 string.
     */
    override fun toString(): String = "$type/$subtype"

    /**
     * Formats [Type]-[Subtype] pair as RFC 6838 string.
     * Optional parameter may be added to provide
     * additional details.
     */
    fun toString(parameter: Pair<String, String>): String {
        val (k, v) = parameter

        return "${toString()};$k=$v"
    }
}
