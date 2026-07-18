package com.plataformas.horoscoapp.app

enum class AppTab(
    val route: String,
    val label: String,
    val icon: String,
    val debugOnly: Boolean = false,
) {
    Horoscope("horoscope", "Inicio", "♈"),
    History("history", "Historial", "☾"),
    Settings("settings", "Ajustes", "⚙"),
    Tests("tests", "Pruebas", "✉", debugOnly = true),
}

enum class TestNotificationType {
    Data,
    Notification,
}
