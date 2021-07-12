package com.rohith.vsa_kotlin.network

import com.google.firebase.firestore.PropertyName

data class ProfileStats(
    @set:PropertyName("id") @get:PropertyName("id")
    var userID: String? = null,
    @set:PropertyName("name") @get:PropertyName("name")
    var name:String? = null,
    @set:PropertyName("views") @get:PropertyName("views")
    var numViews: Long = 0,
    @set:PropertyName("likes") @get:PropertyName("likes")
    var numLikes: Long = 0,
    @set:PropertyName("followers") @get:PropertyName("followers")
    var numFollowers: Long = 0,
    @set:PropertyName("following") @get:PropertyName("following")
    var numFollowing: Long = 0,
    @set:PropertyName("vids") @get:PropertyName("vids")
    var numVideos: Int = 0,
    @set:PropertyName("videos") @get:PropertyName("videos")
    var videoIds: ArrayList<String> = ArrayList()
)
