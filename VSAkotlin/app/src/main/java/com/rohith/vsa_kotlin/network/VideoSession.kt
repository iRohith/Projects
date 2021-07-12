package com.rohith.vsa_kotlin.network

import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource

class VideoSession {

    var videoIds: Set<String>? = null


    fun populateRandom() : VideoSession{
        videoIds = (0..7).shuffled().map { it.toString() }.toSet()
        return this
    }
/*
    fun populateFromProfile(userId: String) : Task<Set<String>> {
        val tcs = TaskCompletionSource<Set<String>>()
        ServerHandler.getProfileStats(userId).addOnSuccessListener {
            videoIds = it.videoIds.toSet()
            tcs.trySetResult(videoIds)
        }.addOnFailureListener{
            tcs.trySetException(it)
        }
        return tcs.task
    }
*/
    fun refresh(){
        videoIds = videoIds?.shuffled()?.toSet()
    }
}