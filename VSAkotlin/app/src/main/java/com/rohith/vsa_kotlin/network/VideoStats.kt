package com.rohith.vsa_kotlin.network

import com.google.firebase.firestore.PropertyName

data class VideoStats (
    @set:PropertyName("uid") @get:PropertyName("uid")
    var userId: String? = null,
    @set:PropertyName("vid") @get:PropertyName("vid")
    var videoId: String? = null,
    @set:PropertyName("name") @get:PropertyName("name")
    var userName: String? = null,
    @set:PropertyName("caption") @get:PropertyName("caption")
    var caption: String? = null,
    @set:PropertyName("views") @get:PropertyName("views")
    var numViews: Long = 0,
    @set:PropertyName("likes") @get:PropertyName("likes")
    var numLikes: Long = 0,
    @set:PropertyName("shares") @get:PropertyName("shares")
    var numShares: Long = 0,
    @set:PropertyName("comments") @get:PropertyName("comments")
    var numComments: Long = 0
)
