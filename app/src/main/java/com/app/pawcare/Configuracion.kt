package com.app.pawcare // Asumiendo que Configuracion.kt está en este paquete

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import android.widget.Button // IMPORTANTE: Añadir esta importación si no estaba
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
// Como ChatActivity.kt está en el mismo paquete com.app.pawcare,
// una importación explícita como 'import com.app.pawcare.ChatActivity' no es estrictamente necesaria,
// pero no hace daño y Android Studio podría añadirla automáticamente.

import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage

class Configuracion : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_configuracion)

        auth = Firebase.auth

        val btnLogout = findViewById<TextView>(R.id.btn_logout) // Renombré para seguir convención (opcional)
        btnLogout.setOnClickListener {
            cerrarSesion()
        }

        val imageBack = findViewById<ImageView>(R.id.imageView36)
        imageBack.setOnClickListener {
            finish()
        }

        val editarPerfilDueno = findViewById<TextView>(R.id.editarperfil) // Renombré (opcional)
        editarPerfilDueno.setOnClickListener {
            Log.d("Configuracion", "Accediendo a Editar Perfil Dueño")
            startActivity(Intent(this, EditarPerfilDueno::class.java))
        }

        val btnEliminar = findViewById<Button>(R.id.btnEliminar)
        btnEliminar.setOnClickListener {
            mostrarDialogoConfirmacion()
        }

        // --- PASO 5: INICIAR CHATACTIVITY DESDE AQUÍ (BOTÓN DE PRUEBA) ---
        // Asegúrate de que el ID R.id.btnProbarChatConfig coincida con el de tu activity_configuracion.xml
        val btnProbarChat = findViewById<Button>(R.id.btnProbarChatConfig) // Usa el ID que definiste en el XML
        btnProbarChat.setOnClickListener {
            val currentUser = auth.currentUser
            if (currentUser != null) {
                val currentUserId = currentUser.uid
                // Para esta prueba, el "receiver" será el mismo usuario.
                // En un chat real, este sería el UID de OTRO usuario.
                val receiverIdParaPrueba = currentUserId

                // Intentar obtener el nombre de usuario desde Firestore
                db.collection("users").document(currentUserId).get()
                    .addOnSuccessListener { document ->
                        var receiverNameParaPrueba = if (document != null && document.exists()) {
                            document.getString("username") // Asume que el campo se llama "username"
                        } else {
                            null
                        }

                        if (receiverNameParaPrueba.isNullOrEmpty()) {
                            // Fallback si no hay 'username' en Firestore o está vacío
                            receiverNameParaPrueba = currentUser.email?.split("@")?.get(0) ?: getString(R.string.user_default_name)
                            Log.w("Configuracion", getString(R.string.username_not_found_firestore))
                        }
                        Log.d("Configuracion", "Iniciando chat (prueba): ID Receptor=$receiverIdParaPrueba, Nombre Receptor=$receiverNameParaPrueba")
                        launchChatActivity(receiverIdParaPrueba, receiverNameParaPrueba)
                    }
                    .addOnFailureListener { exception ->
                        Log.e("Configuracion", "Error al obtener nombre de usuario desde Firestore", exception)
                        // Fallback si hay error al obtener de Firestore
                        val receiverNameParaPrueba = currentUser.email?.split("@")?.get(0) ?: getString(R.string.user_default_name)
                        launchChatActivity(receiverIdParaPrueba, receiverNameParaPrueba)
                    }
            } else {
                Toast.makeText(this, getString(R.string.login_required_for_chat_test), Toast.LENGTH_SHORT).show()
            }
        }
        // --- FIN DE LA LÓGICA DEL BOTÓN DE PRUEBA DE CHAT ---
    }

    // --- NUEVA FUNCIÓN PARA LANZAR ChatActivity ---
    private fun launchChatActivity(receiverId: String, receiverName: String) {
        // Como ChatActivity.kt está en el mismo paquete com.app.pawcare que Configuracion.kt,
        // la referencia ChatActivity::class.java debería funcionar directamente.
        val intent = Intent(this, ChatActivity::class.java).apply {
            putExtra(ChatActivity.EXTRA_RECEIVER_ID, receiverId)
            putExtra(ChatActivity.EXTRA_RECEIVER_NAME, receiverName)
            // Opcional: podrías pre-generar y pasar el chatRoomId también
            // val currentUserId = auth.currentUser?.uid
            // if (currentUserId != null) {
            //    val chatRoomId = if (currentUserId < receiverId) "${currentUserId}_${receiverId}" else "${receiverId}_${currentUserId}"
            //    putExtra(ChatActivity.EXTRA_CHAT_ROOM_ID, chatRoomId)
            // }
        }
        startActivity(intent)
    }

    private fun cerrarSesion() {
        auth.signOut()
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
        Toast.makeText(this, getString(R.string.logout), Toast.LENGTH_SHORT).show()
    }

    private fun mostrarDialogoConfirmacion() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.confirm))
            .setMessage(getString(R.string.confirm_delete_message))
            .setPositiveButton(getString(R.string.delete)) { _, _ ->
                if (auth.currentUser != null) {
                    solicitarReautenticacion()
                }
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show()
    }

    private fun solicitarReautenticacion() {
        val user = auth.currentUser ?: return
        val dialogView = layoutInflater.inflate(R.layout.dialog_password, null) // Asegúrate que R.layout.dialog_password exista
        val etPassword = dialogView.findViewById<EditText>(R.id.et_password) // Asegúrate que R.id.et_password exista en dialog_password.xml

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.password_verification))
            .setMessage(getString(R.string.enter_password_to_confirm))
            .setView(dialogView)
            .setPositiveButton(getString(R.string.confirm_button)) { _, _ ->
                val password = etPassword.text.toString()
                if (password.isEmpty()){
                    Toast.makeText(this, "La contraseña no puede estar vacía.", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                user.email?.let { email ->
                    val credential = EmailAuthProvider.getCredential(email, password)
                    user.reauthenticate(credential)
                        .addOnSuccessListener {
                            Log.d("Configuracion", "Reautenticación exitosa.")
                            eliminarCuentaCompleta()
                        }
                        .addOnFailureListener { e ->
                            Log.e("Configuracion", "Fallo en reautenticación.", e)
                            Toast.makeText(this, getString(R.string.incorrect_password), Toast.LENGTH_SHORT).show()
                        }
                } ?: run {
                    Toast.makeText(this, "Email de usuario no disponible para reautenticación.", Toast.LENGTH_LONG).show()
                    Log.e("Configuracion", "Email del usuario es null durante reautenticación.")
                }
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .create()
            .show()
    }

    private fun eliminarCuentaCompleta() {
        val user = auth.currentUser ?: return
        val userId = user.uid
        // Uso de string resource para el mensaje del diálogo
        val progressDialogInstance = mostrarProgressDialog(getString(R.string.processing))

        val deleteTasks = mutableListOf<Task<*>>()
        deleteTasks.add(db.collection("users").document(userId).delete())
        deleteTasks.add(
            db.collection("pets") // Asumiendo que tienes una colección "pets"
                .whereEqualTo("userId", userId)
                .get()
                .continueWithTask { task ->
                    val batch = db.batch()
                    task.result?.forEach { doc -> batch.delete(doc.reference) }
                    batch.commit()
                }
        )
        // La eliminación de la imagen de perfil puede fallar si no existe, no debería ser un error bloqueante
        val profileImageRef = storage.reference.child("profile_images/$userId.jpg")
        deleteTasks.add(profileImageRef.delete()
            .addOnSuccessListener {
                Log.d("Configuracion", "Imagen de perfil eliminada si existía.")
            }
            .addOnFailureListener { e ->
                // No tratar esto como un error que impida borrar la cuenta si la imagen simplemente no existe
                if ((e as? com.google.firebase.storage.StorageException)?.errorCode == com.google.firebase.storage.StorageException.ERROR_OBJECT_NOT_FOUND) {
                    Log.w("Configuracion", "No se encontró la imagen de perfil para eliminar (profile_images/$userId.jpg), lo cual es normal si no se subió una.")
                } else {
                    Log.e("Configuracion", "Error al intentar eliminar la imagen de perfil (profile_images/$userId.jpg): ${e.message}", e)
                }
            }
        )

        Tasks.whenAllComplete(deleteTasks.filterNotNull()) // Filtrar tareas nulas si alguna falló inmediatamente
            .addOnCompleteListener { taskCompletion ->
                // Incluso si alguna tarea de borrado de datos falló (ej. la imagen no existía),
                // intentamos borrar la cuenta de Auth si el borrado de 'users' fue exitoso (o al menos se intentó).
                // Para ser más robusto, podrías verificar específicamente el éxito de db.collection("users").document(userId).delete()
                if (taskCompletion.isSuccessful || deleteTasks.first().isSuccessful) { // Chequea si todas las tareas completaron (incluso con fallos individuales no críticos) o al menos la primera (borrado de user)
                    Log.d("Configuracion", "Datos de Firestore y Storage (intentos) eliminados.")
                    user.delete()
                        .addOnSuccessListener {
                            progressDialogInstance.dismiss()
                            Toast.makeText(this, getString(R.string.account_deleted_successfully), Toast.LENGTH_SHORT).show()
                            redirigirALogin()
                        }
                        .addOnFailureListener { e ->
                            progressDialogInstance.dismiss()
                            Toast.makeText(this, getString(R.string.error_deleting_account, e.message ?: "Error desconocido"), Toast.LENGTH_LONG).show()
                            Log.e("Configuracion", "Error al eliminar cuenta de Auth", e)
                        }
                } else {
                    progressDialogInstance.dismiss()
                    val errorMessage = taskCompletion.exception?.message ?: "Error desconocido"
                    Toast.makeText(this, getString(R.string.error_deleting_data, errorMessage), Toast.LENGTH_LONG).show()
                    Log.e("Configuracion", "Error al borrar datos asociados (Firestore/Storage): $errorMessage")
                }
            }
    }

    private fun redirigirALogin() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }

    private fun mostrarProgressDialog(mensaje: String): AlertDialog {
        // Asegúrate de que R.layout.dialog_progress y R.id.tv_progress_message existan
        val view = layoutInflater.inflate(R.layout.dialog_progress, null)
        val tvMensaje = view.findViewById<TextView>(R.id.tv_progress_message)
        tvMensaje.text = mensaje

        return AlertDialog.Builder(this)
            .setView(view)
            .setCancelable(false)
            .create()
            .also { it.show() }
    }
}