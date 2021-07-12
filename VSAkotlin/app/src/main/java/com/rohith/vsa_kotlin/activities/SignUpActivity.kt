package com.rohith.vsa_kotlin.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.rohith.vsa_kotlin.databinding.ActivitySignUpBinding
import com.rohith.vsa_kotlin.network.ServerHandler

class SignUpActivity : AppCompatActivity() {
    private lateinit var binding : ActivitySignUpBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.name.setText(intent.getStringExtra("name")?:"")
        binding.userId.setText(intent.getStringExtra("user_id")?:"")

        binding.ok.setOnClickListener {
            val name = binding.name.text.toString()
            val id = binding.userId.text.toString()

            if (name.isBlank() || id.isBlank()) {
                Toast.makeText(this, "Name and id cannot be blank", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            ServerHandler.Auth.createUser(ServerHandler.Auth.email, binding.userId.text.toString(), binding.name.text.toString())
                .addOnSuccessListener {
                    Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Create user failed", Toast.LENGTH_SHORT).show()
                }
        }

    }
}