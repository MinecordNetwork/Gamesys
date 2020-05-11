package net.minecord.gamesys.utils.chat

fun String.getCenteredChat(): String {
    return this.getCenteredMessage(154)
}

fun String.getCenteredMotd(): String {
    return this.getCenteredMessage(128)
}

private fun String.getCenteredMessage(center_pixel: Int): String {
    var messagePxSize = 0
    var previousCode = false
    var isBold = false

    for (c in this.toCharArray()) {
        when {
            c == '§' -> {
                previousCode = true
            }
            previousCode -> {
                previousCode = false
                isBold = c == 'l' || c == 'L'
            }
            else -> {
                val dFI: DefaultFontInfo = DefaultFontInfo.getDefaultFontInfo(c)
                messagePxSize += if (isBold) dFI.boldLength else dFI.length
                messagePxSize++
            }
        }
    }

    val halvedMessageSize = messagePxSize / 2
    val toCompensate = center_pixel - halvedMessageSize
    val spaceLength: Int = DefaultFontInfo.SPACE.length + 1
    var compensated = 0
    val sb = StringBuilder()
    while (compensated < toCompensate) {
        sb.append(" ")
        compensated += spaceLength
    }

    return sb.toString() + this
}

