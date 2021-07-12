package com.rohith.vsa_kotlin.network

import android.net.Uri
import com.firebase.ui.auth.AuthUI
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Source
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.rohith.vsa_kotlin.activities.MainActivity

object ServerHandler {

    object Auth {
        val emptyTask: Task<Void?> by lazy {
            val tcs = TaskCompletionSource<Void?>()
            tcs.setResult(null)
            tcs.task
        }
        private val fireAuth = FirebaseAuth.getInstance()
        private val currentUser get() = fireAuth.currentUser
        private val providers = arrayListOf(AuthUI.IdpConfig.GoogleBuilder().build())
        private var signInSnackbar: Snackbar? = null

        val email get() = currentUser?.email
        val displayName get() = currentUser?.displayName

        fun signOut(){
            AuthUI.getInstance().signOut(fireAuth.app.applicationContext)
        }

        fun requestSignIn(activity: MainActivity) : Boolean{
            if (currentUser != null) return true
            if (signInSnackbar == null){
                signInSnackbar = Snackbar.make(activity.binding.swipeContainer, "Sign in to continue", Snackbar.LENGTH_LONG)
                signInSnackbar?.setAction("Sign In") {
                    activity.startActivityForResult(
                        AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setAvailableProviders(providers)
                            .build(),
                        RC_SIGN_IN)
                }
            }
            signInSnackbar?.show()
            return false
        }

        fun createUser(emailId: String?, id : String?, name : String?) : Task<Void?>{
            if (emailId == null || id == null || name == null) return emptyTask
            val ps = ProfileStats(id, name)
            return db.collection("all_users").document(emailId).set(hashMapOf(
                "id" to id,
                "name" to name
            )).continueWithTask { db.collection("users").document(id).set(ps) }
        }
    }

    object Profile {
        fun checkUserExists(emailId: String?) : Task<Boolean> {
            val tcs = TaskCompletionSource<Boolean>()
            if (emailId == null) {
                tcs.setResult(false)
                return tcs.task
            }
            db.collection("all_users").document(emailId).get().addOnSuccessListener {
                tcs.trySetResult(it.exists())
            }.addOnFailureListener { tcs.trySetException(it) }
            return tcs.task
        }

        fun getProfileStats(userId : String, cacheFirst: Boolean = true) : Task<ProfileStats> {
            val tcs = TaskCompletionSource<ProfileStats>()
            val docRef = db.collection("users").document(userId)
            (if (cacheFirst) requestCacheFirst(docRef) else docRef.get()).addOnSuccessListener { document ->
                if (document == null) tcs.trySetException(Exception("No such document")) else tcs.trySetResult(document.toObject<ProfileStats>())
            }.addOnFailureListener { tcs.trySetException(it) }
            return tcs.task
        }

        fun getProfileUri(userId: String, backCoverPic : Boolean = false) : Task<Uri> {
            val tcs = TaskCompletionSource<Uri>()
            val picRef = storage.getReference(if (backCoverPic) "" else "$userId/profile.jpg")
            picRef.downloadUrl.addOnSuccessListener { uri ->
                if (uri == null) tcs.trySetException(Exception("No such uri")) else tcs.trySetResult(uri)
            }.addOnFailureListener { tcs.trySetException(it) }

            return tcs.task
        }
    }

    object Content {
        fun getVideoStats(vidId : String?, cacheFirst: Boolean = true) : Task<VideoStats> {
            val tcs = TaskCompletionSource<VideoStats>()
            if (vidId == null){
                tcs.setException(NullPointerException())
                return tcs.task
            }
            val docRef = db.collection("videos").document(vidId)
            (if (cacheFirst) requestCacheFirst(docRef) else docRef.get()).addOnSuccessListener { document ->
                if (document == null) tcs.trySetException(Exception("No such document")) else tcs.trySetResult(document.toObject<VideoStats>())
            }.addOnFailureListener { tcs.trySetException(it) }
            return tcs.task
        }

        fun getVideoThumbUri(vidId: String?, userId: String?) : Task<Uri> {
            val tcs = TaskCompletionSource<Uri>()
            if (vidId == null || userId == null){
                tcs.setException(NullPointerException())
                return tcs.task
            }
            val picRef = storage.getReference("$userId/$vidId.jpg")
            picRef.downloadUrl.addOnSuccessListener { uri ->
                tcs.trySetResult(Uri.parse("https://player.vimeo.com/external/221163277.hd.mp4?s=28ec836aaa65693dd2c683e2a2c407e6f8ac8998&profile_id=174"))
                //if (uri == null) tcs.trySetException(Exception("No such uri")) else tcs.trySetResult(uri)
            }.addOnFailureListener {
                //tcs.trySetResult(Uri.parse("https://player.vimeo.com/external/221163277.hd.mp4?s=28ec836aaa65693dd2c683e2a2c407e6f8ac8998&profile_id=174"))
                tcs.trySetException(it)
            }
            return tcs.task
        }

        fun getVideoUri(vidId: String?, userId: String?) : Task<Uri> {
            val tcs = TaskCompletionSource<Uri>()
            if (vidId == null || userId == null){
                tcs.setException(NullPointerException())
                return tcs.task
            }
            val vidRef = storage.getReference("$userId/$vidId.mp4")
            vidRef.downloadUrl.addOnSuccessListener { uri ->
                tcs.trySetResult(Uri.parse("https://player.vimeo.com/external/221163277.hd.mp4?s=28ec836aaa65693dd2c683e2a2c407e6f8ac8998&profile_id=174"))
                //if (uri == null) tcs.trySetException(Exception("No such uri")) else tcs.trySetResult(uri)
            }.addOnFailureListener {
                //tcs.trySetResult(Uri.parse("https://player.vimeo.com/external/221163277.hd.mp4?s=28ec836aaa65693dd2c683e2a2c407e6f8ac8998&profile_id=174"))
                tcs.trySetException(it)
            }
            return tcs.task
        }
    }

    private val db = Firebase.firestore
    private val storage = Firebase.storage

    private fun requestCacheFirst(docRef: DocumentReference) : Task<DocumentSnapshot> {
        return docRef.get(Source.CACHE).continueWithTask { if (it.isSuccessful) it else docRef.get(Source.SERVER) }
    }

    const val RC_SIGN_IN = 123
}