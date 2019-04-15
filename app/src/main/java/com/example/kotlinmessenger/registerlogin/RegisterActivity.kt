package com.example.kotlinmessenger.registerlogin

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import com.example.kotlinmessenger.R
import com.example.kotlinmessenger.messages.LatestMessagesActivity
import com.example.kotlinmessenger.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_register.*
import java.util.*

class RegisterActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    var selectedPhotoUri: Uri? = null
    val TAG = "RegisterActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()

        register_button_resgiste.setOnClickListener {
            performRegister()

        }

        text_view_already_have_account.setOnClickListener{

            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        selectphoto_button_register.setOnClickListener {
            Log.d(TAG, "Tentando selecionar foto")
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 0)

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null){
            Log.d(TAG, "Foto selecionada")

            selectedPhotoUri = data.data

            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedPhotoUri)

            selectedphoto_imageview_register.setImageBitmap(bitmap)
            selectphoto_button_register.alpha = 0f


//            val bmpDrawable =  BitmapDrawable(bitmap)
//            selectphoto_button_register.setBackgroundDrawable(bmpDrawable)
        }
    }
    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        //updateUI(currentUser)
    }
    private fun performRegister(){
        val email = email_edittext_register.text.toString()
        val password = password_edittext_register.text.toString()

        Log.d(TAG,  "Password: $password")

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(baseContext, "Email ou senha vazio",
                Toast.LENGTH_SHORT).show()
            return
        }

        //Firebase Authentication to create a user with email and password
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "createUserWithEmail:success")
                   uploadImageToFirebaseStorage()
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "Falha ao criar usuário: ${task.exception}")
                    Toast.makeText(baseContext, "Falha ao criar usuário: ${task.exception}",
                        Toast.LENGTH_LONG).show()
                }

                // ...
            }
    }

    private fun uploadImageToFirebaseStorage(){

        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("images/$filename")

        ref.putFile(selectedPhotoUri!!)
            .addOnSuccessListener {
                Log.d(TAG, "Upload da foto concluído! Foto:${it.metadata?.path}")

                ref.downloadUrl.addOnSuccessListener {
                    Log.d(TAG, "File location:${it}")

                    saveUserToFirebaseDatabase(it.toString())
                }
            }
            .addOnFailureListener{
                Log.d(TAG, "Falha ao salvar foto! Erro:${it.message}")
            }

    }

    private fun saveUserToFirebaseDatabase(profileImageUrl: String){

        val uid = auth.uid ?: ""
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
        val user = User(uid, edittext_username_register.text.toString(), profileImageUrl )
        ref.setValue(user)
            .addOnSuccessListener {
                Log.d(TAG, "Usuário salvo com sucesso!!")

                val intent = Intent(this, LatestMessagesActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
            .addOnFailureListener {
                Log.d(TAG, "Falha ao salvar usuário! Erro:${it.message}")

            }

    }
}

