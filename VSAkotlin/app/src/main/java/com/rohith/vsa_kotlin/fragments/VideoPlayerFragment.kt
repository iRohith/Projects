package com.rohith.vsa_kotlin.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.*
import android.view.GestureDetector.SimpleOnGestureListener
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.rohith.vsa_kotlin.R
import com.rohith.vsa_kotlin.activities.MainActivity
import com.rohith.vsa_kotlin.databinding.PlayerViewBinding
import com.rohith.vsa_kotlin.network.CacheDataSourceFactory
import com.rohith.vsa_kotlin.network.ServerHandler
import com.rohith.vsa_kotlin.network.VideoStats
import com.rohith.vsa_kotlin.util.DataParcel
import com.rohith.vsa_kotlin.util.parseInt


class VideoPlayerFragment() : Fragment(), View.OnClickListener {

    constructor(c: Context) : this() {
        mContext = c
    }

    private lateinit var videoId: String
    private lateinit var mActivity: MainActivity
    lateinit var videoStats: VideoStats
    var mediaSource: ProgressiveMediaSource? = null
    private lateinit var mContext : Context
    lateinit var binding: PlayerViewBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mContext = context ?: mContext
        mActivity = activity as MainActivity
        binding = PlayerViewBinding.inflate(inflater)
        videoId = arguments?.getString("videoId")!!

        ServerHandler.Content.getVideoStats(videoId).addOnSuccessListener {
            videoStats = it
            binding.like.text = parseInt(it.numLikes)
            binding.share.text = parseInt(it.numShares)
            binding.comment.text = parseInt(it.numComments)
            ServerHandler.Content.getVideoThumbUri(it.videoId, it.userId).addOnSuccessListener { uri ->
                Glide.with(this).load(uri).into(binding.imageView)
            }`
            ServerHandler.Content.getVideoUri(it.videoId, it.userId).addOnSuccessListener { uri ->
                mediaSource = ProgressiveMediaSource.Factory(
                    CacheDataSourceFactory(
                        mContext,
                        500 * 1024 * 1024,
                        10 * 1024 * 1024
                    )
                ).createMediaSource(uri)
                if (isResumed) {
                    getDataParcel().data = it.userId
                    binding.player.player = mActivity.exoPlayer
                    mActivity.exoPlayer.prepare(mediaSource!!)
                    mActivity.exoPlayer.playWhenReady = true
                }
                isInitialized = true
            }.addOnFailureListener{
                Toast.makeText(mContext, "Error", Toast.LENGTH_SHORT).show()
            }
        }

        return binding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.like.setOnClickListener(this)
        binding.share.setOnClickListener(this)
        binding.comment.setOnClickListener(this)
        binding.playButton.alpha = 0f
        val gd = GestureDetector(mContext, MyGestureListener())
        view.setOnTouchListener { _, e ->  gd.onTouchEvent(e) }
    }

    private fun getDataParcel() : DataParcel {
        return (activity as MainActivity).dataParcel
    }

    fun pauseVideo(playAnimation: Boolean) {
        mActivity.exoPlayer.playWhenReady = false
        if (playAnimation) binding.playButton.animate().alpha(0.6f).start()
    }

    fun resumeVideo(playAnimation: Boolean) {
        mActivity.exoPlayer.playWhenReady = true
        if (playAnimation) binding.playButton.animate().alpha(0f).start() else binding.playButton.alpha = 0f
    }

    private var liked = false
    fun likePress(forceLike : Boolean = false){
        liked = if (forceLike || !liked) {
            binding.like.setCompoundDrawablesRelativeWithIntrinsicBounds(null, ContextCompat.getDrawable(mContext, R.drawable.ic_heart_red_small), null, null)
            true
        } else {
            binding.like.setCompoundDrawablesRelativeWithIntrinsicBounds(null, ContextCompat.getDrawable(mContext, R.drawable.ic_heart_small), null, null)
            false
        }
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.like -> {
                if (!ServerHandler.Auth.requestSignIn(activity as MainActivity)) return
                likePress()
            }
            R.id.comment -> {
                if (!ServerHandler.Auth.requestSignIn(activity as MainActivity)) return
            }
            R.id.share -> {
                if (!ServerHandler.Auth.requestSignIn(activity as MainActivity)) return
            }
        }
    }

    inner class MyGestureListener : SimpleOnGestureListener() {
        override fun onDown(e: MotionEvent): Boolean = true
        override fun onDoubleTap(e: MotionEvent): Boolean {
            likePress(true)
            return true
        }

        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
            if (mActivity.exoPlayer.isPlaying){
                pauseVideo(true)
            } else {
                resumeVideo(true)
            }
            return true
        }
    }

    companion object {
        var isInitialized = false
    }
}