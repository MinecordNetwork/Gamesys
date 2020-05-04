package net.minecord.gamesys.utils

fun Int.toMinutesString(): String {
    val minutes = this / 60
    val seconds = this - minutes * 60
    return String.format("%02d:%02d", minutes, seconds)
}
