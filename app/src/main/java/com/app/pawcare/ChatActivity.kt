package com.app.pawcare // OJO: Verifica que este sea el paquete donde creaste ChatActivity.kt

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
// OJO: Ajusta la siguiente línea si tu ChatAdapter está en un paquete diferente (ej. com.app.pawcare.adapters)
// OJO: Ajusta la siguiente línea si tu ChatMessage está en un paquete diferente (ej. com.app.pawcare.data.model)
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.*

class ChatActivity : AppCompatActivity() {

    private lateinit var rvChatMessages: RecyclerView
    private lateinit var etMessageInput: EditText
    private lateinit var btnSendMessage: ImageButton
    private lateinit var btnAttachImage: ImageButton
    private lateinit var toolbarChat: Toolbar

    private lateinit var chatAdapter: ChatAdapter
    private val messagesList = mutableListOf<ChatMessage>()

    private lateinit var auth: FirebaseAuth
    private lateinit var dbRootRef: DatabaseReference
    private lateinit var dbMessagesRef: DatabaseReference
    private lateinit var firestoreDb: FirebaseFirestore
    private lateinit var storage: FirebaseStorage

    private var currentUserId: String? = null
    private var currentUserName: String? = null
    private var receiverId: String? = null
    private var receiverName: String? = null
    private var chatRoomId: String? = null

    private var messagesListener: ChildEventListener? = null
    private var progressDialog: AlertDialog? = null

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                uploadImageAndSendMessage(uri)
            }
        }
    }

    companion object {
        const val EXTRA_RECEIVER_ID = "receiver_id"
        const val EXTRA_RECEIVER_NAME = "receiver_name"
        const val EXTRA_CHAT_ROOM_ID = "chat_room_id"
        private const val TAG = "ChatActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        auth = FirebaseAuth.getInstance()
        // --- ¡¡¡ATENCIÓN!!! REEMPLAZA LA URL DE EJEMPLO CON LA URL DE TU REALTIME DATABASE ---
        // Ve a tu consola de Firebase -> Build -> Realtime Database. Si no tienes una, créala.
        // La URL se verá algo así: https://tu-proyecto-id-default-rtdb.firebaseio.com/
        // O si está en otra región: https://tu-proyecto-id-default-rtdb.tu-region.firebasedatabase.app/
        // Si usas la instancia "(default)" y no tienes múltiples bases de datos, puedes intentar solo:
        // dbRootRef = FirebaseDatabase.getInstance().reference
        // PERO ES MÁS SEGURO Y RECOMENDADO ESPECIFICAR LA URL COMPLETA:
        dbRootRef = FirebaseDatabase.getInstance("https://pawcare-c9bdc-default-rtdb.firebaseio.com/").reference // ¡¡REEMPLAZA ESTA URL!!
        firestoreDb = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        currentUserId = auth.currentUser?.uid
        if (currentUserId == null) {
            Toast.makeText(this, getString(R.string.chat_error_auth), Toast.LENGTH_LONG).show()
            finish()
            return
        }
        fetchCurrentUserName()

        receiverId = intent.getStringExtra(EXTRA_RECEIVER_ID)
        receiverName = intent.getStringExtra(EXTRA_RECEIVER_NAME)
        val passedChatRoomId = intent.getStringExtra(EXTRA_CHAT_ROOM_ID)

        if (receiverId == null && passedChatRoomId == null) {
            Toast.makeText(this, getString(R.string.chat_error_no_receiver_info), Toast.LENGTH_LONG).show()
            Log.e(TAG, "receiverId y passedChatRoomId son nulos. No se puede iniciar el chat.")
            finish()
            return
        }

        chatRoomId = passedChatRoomId ?: getChatRoomId(currentUserId!!, receiverId!!)

        if (receiverId == null && chatRoomId != null) {
            receiverId = deriveReceiverIdFromChatRoom(chatRoomId!!, currentUserId!!)
            if (receiverId == null) {
                Toast.makeText(this, getString(R.string.chat_error_no_receiver_info), Toast.LENGTH_LONG).show()
                Log.e(TAG, "No se pudo derivar receiverId desde chatRoomId: $chatRoomId con currentUser: $currentUserId")
                finish()
                return
            }
            // Si no se pasó receiverName, y se derivó receiverId, se usará el título por defecto.
            // Aquí podrías hacer una llamada a Firestore para obtener el nombre del receiverId derivado.
        }


        dbMessagesRef = dbRootRef.child("chat_messages").child(chatRoomId!!)

        toolbarChat = findViewById(R.id.toolbar_chat)
        setSupportActionBar(toolbarChat)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = receiverName ?: getString(R.string.chat_error_no_file_selected)

        rvChatMessages = findViewById(R.id.rvChatMessages)
        etMessageInput = findViewById(R.id.etMessageInput)
        btnSendMessage = findViewById(R.id.btnSendMessage)
        btnAttachImage = findViewById(R.id.btnAttachImage)

        setupRecyclerView()
        attachMessagesListener()

        btnSendMessage.setOnClickListener {
            val messageText = etMessageInput.text.toString().trim()
            if (messageText.isNotEmpty()) {
                sendMessage(messageText, null)
                etMessageInput.setText("")
            }
        }

        btnAttachImage.setOnClickListener {
            openImageChooser()
        }
    }

    private fun fetchCurrentUserName() {
        currentUserId?.let { uid ->
            firestoreDb.collection("users").document(uid).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        currentUserName = document.getString("username") // Asume que el campo se llama 'username' en Firestore
                    }
                    if (currentUserName.isNullOrEmpty()) {
                        Log.w(TAG, "Nombre de usuario actual no encontrado en Firestore o es vacío. Usando email como fallback.")
                        currentUserName = auth.currentUser?.email?.split("@")?.get(0) ?: "Usuario"
                    }
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error al obtener nombre de usuario actual de Firestore", e)
                    currentUserName = auth.currentUser?.email?.split("@")?.get(0) ?: "Usuario"
                }
        } ?: run {
            currentUserName = auth.currentUser?.email?.split("@")?.get(0) ?: "Usuario"
        }
    }

    private fun getChatRoomId(userId1: String, userId2: String): String {
        return if (userId1 < userId2) "${userId1}_${userId2}" else "${userId2}_${userId1}"
    }

    private fun setupRecyclerView() {
        chatAdapter = ChatAdapter(messagesList, currentUserId!!)
        val layoutManager = LinearLayoutManager(this)
        rvChatMessages.layoutManager = layoutManager
        rvChatMessages.adapter = chatAdapter
    }

    private fun sendMessage(text: String?, imageUrl: String?) {
        if (chatRoomId.isNullOrEmpty()) {
            Log.e(TAG, "chatRoomId es null o vacío. No se puede enviar el mensaje.")
            return
        }
        val finalReceiverId = receiverId ?: deriveReceiverIdFromChatRoom(chatRoomId!!, currentUserId!!)

        if (finalReceiverId.isNullOrEmpty()) {
            Toast.makeText(this, getString(R.string.chat_error_no_receiver_info), Toast.LENGTH_SHORT).show()
            Log.e(TAG, "finalReceiverId es null o vacío. No se pudo determinar el destinatario del mensaje.")
            return
        }

        val messageId = dbMessagesRef.push().key ?: return
        val timestamp = System.currentTimeMillis()
        val senderDisplayName = currentUserName ?: auth.currentUser?.email?.split("@")?.get(0) ?: "Usuario"

        val chatMessage = ChatMessage(
            messageId = messageId,
            text = text,
            imageUrl = imageUrl,
            senderId = currentUserId!!,
            receiverId = finalReceiverId,
            timestamp = timestamp,
            senderName = senderDisplayName
        )

        dbMessagesRef.child(messageId).setValue(chatMessage)
            .addOnSuccessListener {
                Log.d(TAG, "Mensaje '$messageId' enviado a la sala '$chatRoomId'")
                updateLastMessageInfo(text ?: (if (imageUrl != null) "Imagen" else "Mensaje"), timestamp, finalReceiverId)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, getString(R.string.chat_error_send_message), Toast.LENGTH_SHORT).show()
                Log.e(TAG, "Error al enviar mensaje a la sala '$chatRoomId'", e)
            }
    }

    private fun deriveReceiverIdFromChatRoom(roomId: String, currentUid: String): String? {
        val ids = roomId.split("_")
        if (ids.size == 2) {
            return when (currentUid) {
                ids[0] -> ids[1]
                ids[1] -> ids[0]
                else -> null
            }
        }
        return null
    }

    private fun updateLastMessageInfo(lastMsg: String, timestamp: Long, actualReceiverId: String) {
        if (chatRoomId.isNullOrEmpty() || currentUserId.isNullOrEmpty()) {
            Log.w(TAG, "No se pudo actualizar lastMessageInfo: chatRoomId o currentUserId es null.")
            return
        }

        val chatRoomMetaRef = dbRootRef.child("chat_rooms_metadata").child(chatRoomId!!)
        val roomUpdate = mapOf(
            "lastMessage" to lastMsg,
            "lastMessageTimestamp" to timestamp,
            "participants/${currentUserId!!}" to true,
            "participants/${actualReceiverId}" to true
        )
        chatRoomMetaRef.updateChildren(roomUpdate)

        val currentUserChatInfoRef = dbRootRef.child("user_chats").child(currentUserId!!).child(actualReceiverId)
        val currentUserUpdate = mapOf(
            "chatRoomId" to chatRoomId,
            "lastMessage" to lastMsg,
            "timestamp" to timestamp,
            "withUserId" to actualReceiverId,
            "withUserName" to (receiverName ?: "Desconocido")
        )
        currentUserChatInfoRef.setValue(currentUserUpdate)

        val receiverChatInfoRef = dbRootRef.child("user_chats").child(actualReceiverId).child(currentUserId!!)
        val receiverUpdate = mapOf(
            "chatRoomId" to chatRoomId,
            "lastMessage" to lastMsg,
            "timestamp" to timestamp,
            "withUserId" to currentUserId,
            "withUserName" to (currentUserName ?: "Usuario")
        )
        receiverChatInfoRef.setValue(receiverUpdate)
    }

    private fun attachMessagesListener() {
        if (messagesListener != null) {
            dbMessagesRef.removeEventListener(messagesListener!!)
        }
        messagesList.clear()
        chatAdapter.notifyDataSetChanged()

        messagesListener = object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                try {
                    val message = snapshot.getValue(ChatMessage::class.java)
                    if (message != null && messagesList.none { it.messageId == message.messageId }) {
                        chatAdapter.addMessage(message)
                        rvChatMessages.smoothScrollToPosition(chatAdapter.itemCount - 1)
                    }
                } catch (e: DatabaseException) {
                    Log.e(TAG, "Error al deserializar mensaje en onChildAdded: ${snapshot.key}", e)
                }
            }
            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) { /* ... */ }
            override fun onChildRemoved(snapshot: DataSnapshot) { /* ... */ }
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) { /* ... */ }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ChatActivity, getString(R.string.chat_error_load_messages), Toast.LENGTH_LONG).show()
                Log.e(TAG, "Error en Firebase ChildEventListener: ${error.message}", error.toException())
            }
        }
        dbMessagesRef.orderByChild("timestamp").limitToLast(50).addChildEventListener(messagesListener!!)
    }

    private fun openImageChooser() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickImageLauncher.launch(intent)
    }
    private fun uploadImageAndSendMessage(filePath: Uri) {
        if (chatRoomId.isNullOrEmpty() || currentUserId.isNullOrEmpty()) {
            Toast.makeText(this, getString(R.string.chat_error_no_file_selected), Toast.LENGTH_SHORT).show()
            return
        }
        showProgressDialog(getString(R.string.chat_image_upload_progress))

        val imageFileName = "${UUID.randomUUID()}.jpg"
        val imageRef = storage.reference.child("chat_images/$chatRoomId/$imageFileName")

        imageRef.putFile(filePath)
            .addOnSuccessListener { taskSnapshot ->
                dismissProgressDialog()
                taskSnapshot.storage.downloadUrl.addOnSuccessListener { uri ->
                    sendMessage(null, uri.toString())
                } .addOnFailureListener { e_url ->
                    dismissProgressDialog()
                    Toast.makeText(this, getString(R.string.chat_error_image_upload), Toast.LENGTH_SHORT).show()
                    Log.e(TAG, "Error al obtener URL de descarga", e_url)
                }
            }
            .addOnFailureListener { e_upload ->
                dismissProgressDialog()
                Toast.makeText(this, getString(R.string.chat_error_image_upload), Toast.LENGTH_SHORT).show()
                Log.e(TAG, "Error al subir imagen", e_upload)
            }
    }

    private fun showProgressDialog(message: String) {
        val builder = AlertDialog.Builder(this)
        val inflater = this.layoutInflater
        val dialogView: View = inflater.inflate(R.layout.dialog_progress, null) // Asegúrate que R.layout.dialog_progress exista
        val tvMessage = dialogView.findViewById<TextView>(R.id.tv_progress_message) // Asegúrate que R.id.tv_progress_message exista en dialog_progress.xml
        tvMessage.text = message
        builder.setView(dialogView)
        builder.setCancelable(false)
        progressDialog = builder.create()
        progressDialog?.show()
    }

    private fun dismissProgressDialog() {
        if (progressDialog != null && progressDialog!!.isShowing) {
            progressDialog?.dismiss()
        }
        progressDialog = null
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressedDispatcher.onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        super.onDestroy()
        messagesListener?.let { listener ->
            if (this::dbMessagesRef.isInitialized) {
                dbMessagesRef.removeEventListener(listener)
            }
        }
        dismissProgressDialog()
    }
}
