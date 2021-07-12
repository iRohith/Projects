package com.rohith.vsa_kotlin.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.rohith.vsa_kotlin.databinding.ActivityUploadBinding

class UploadActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUploadBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUploadBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val string: String = intent.getStringExtra("file")!!
        Glide.with(this).load(string).skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.NONE).into(binding.imageView)
    }
}