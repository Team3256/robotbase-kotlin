package org.team9432.lib.field

@Suppress("MemberVisibilityCanBePrivate")
object EvergreenField {
    /** Y */
    const val FIELD_HEIGHT = 8.0137

    /** X */
    const val FIELD_WIDTH = 16.54175
    const val FIELD_MIDLINE = FIELD_WIDTH / 2

    fun Point.flip() = Point(FIELD_MIDLINE + (FIELD_MIDLINE - x), y)
    fun Rectangle.flip() = Rectangle(FIELD_MIDLINE + (FIELD_MIDLINE - width - x), y, width, height)
}