package mobi.kairos.android

sealed class KairosNav(val route: String) {
    object Home : KairosNav("HOME")
}