package com.app.pawcare

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage

class ConfiguracionCuidador_Activity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_configuracion_cuidador)

        // Inicializar Firebase Auth
        auth = Firebase.auth

        // 1. Botón de Cerrar Sesión
        val btn_logout2 = findViewById<TextView>(R.id.btn_logout2)
        btn_logout2.setOnClickListener {
            cerrarSesion()
        }

        // 2. Botón de Regresar a perfil cuidador
        val imageBack = findViewById<ImageView>(R.id.perrificado)
        imageBack.setOnClickListener {
            startActivity(Intent(this, perfilCuidadorActivity::class.java))
        }

        // 3. Botón Editar Perfil (existente)
        val editarperfil3 = findViewById<TextView>(R.id.editarperfil3)
        editarperfil3.setOnClickListener {
            startActivity(Intent(this, EditarPerfilCuidador_Activity::class.java))
        }

        // 4. Configurar botón Eliminar Cuenta
        val btnEliminar = findViewById<Button>(R.id.btnEliminar4)
        btnEliminar.setOnClickListener {
            mostrarDialogoConfirmacion()
        }

        // 5. Configurar botón Chat con destinatario fijo
        val btnChat = findViewById<Button>(R.id.btnProbarChatConfig)
        btnChat.setOnClickListener {
            val intent = Intent(this, ChatActivity::class.java)
            // Enviar el id del destinatario (email en este caso)
            intent.putExtra("destinatarioId", "alejo5@main.com")
            startActivity(intent)
        }
    }

    private fun cerrarSesion() {
        auth.signOut() // Cierra sesión en Firebase

        // Redirige al Login y limpia el back stack
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()

        Toast.makeText(this, "Sesión cerrada", Toast.LENGTH_SHORT).show()
    }

    private fun mostrarDialogoConfirmacion() {
        AlertDialog.Builder(this)
            .setTitle("Confirmar eliminación")
            .setMessage("¿Estás seguro? Todos tus datos (mascotas, reservas, etc.) se borrarán permanentemente.")
            .setPositiveButton("Eliminar") { _, _ ->
                if (auth.currentUser != null) {
                    // Solicitar autenticación reciente primero
                    solicitarReautenticacion()
                }
            }
            .setNegativeButton("Cancelar", null)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show()
    }

    private fun solicitarReautenticacion() {
        val user = auth.currentUser ?: return

        val dialogView = layoutInflater.inflate(R.layout.dialog_password, null)
        val etPassword = dialogView.findViewById<EditText>(R.id.et_password)

        AlertDialog.Builder(this)
            .setTitle("Verificación de seguridad")
            .setMessage("Por favor ingresa tu contraseña para confirmar:")
            .setView(dialogView)
            .setPositiveButton("Confirmar") { _, _ ->
                val password = etPassword.text.toString()
                val credential = EmailAuthProvider.getCredential(user.email!!, password)

                user.reauthenticate(credential)
                    .addOnSuccessListener {
                        eliminarCuentaCompleta()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Contraseña incorrecta", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Cancelar", null)
            .create()
            .show()
    }

    private fun eliminarCuentaCompleta() {
        val user = auth.currentUser ?: return
        val userId = user.uid
        val progressDialog = mostrarProgressDialog("Eliminando cuenta...")

        val deleteTasks = mutableListOf<Task<*>>()

        // Eliminar usuario de la colección "users"
        deleteTasks.add(db.collection("users").document(userId).delete())

        // Eliminar documento de la colección "caregivers"
        deleteTasks.add(db.collection("caregivers").document(userId).delete())

        // Eliminar mascotas asociadas
        deleteTasks.add(
            db.collection("pets")
                .whereEqualTo("userId", userId)
                .get()
                .continueWithTask { task ->
                    val batch = db.batch()
                    task.result?.forEach { doc ->
                        batch.delete(doc.reference)
                    }
                    batch.commit()
                }
        )

        // Eliminar imagen de perfil si existe
        deleteTasks.add(storage.reference.child("profile_images/$userId.jpg").delete())

        // Ejecutar todas las tareas
        Tasks.whenAllComplete(deleteTasks)
            .addOnSuccessListener {
                user.delete()
                    .addOnSuccessListener {
                        progressDialog.dismiss()
                        Toast.makeText(this, "Cuenta eliminada", Toast.LENGTH_SHORT).show()
                        redirigirALogin()
                    }
                    .addOnFailureListener { e ->
                        progressDialog.dismiss()
                        Toast.makeText(this, "Error al eliminar cuenta: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(this, "Error al borrar datos: ${e.message}", Toast.LENGTH_SHORT).show()
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
