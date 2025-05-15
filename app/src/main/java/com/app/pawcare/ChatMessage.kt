package com.app.pawcare

data class ChatMessage(
    var messageId: String = "",
    var text: String? = null,
    var imageUrl: String? = null,
    var senderId: String = "",
    var receiverId: String = "",
    var timestamp: Long = 0,
    var senderName: String? = null // Nombre del remitente para mostrar en la UI
) {
    // Constructor vacío requerido por Firebase Realtime Database.
    constructor() : this("", null, null, "", "", 0L, null)
}