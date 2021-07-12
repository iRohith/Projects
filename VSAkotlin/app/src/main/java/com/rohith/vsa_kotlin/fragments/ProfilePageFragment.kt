package com.rohith.vsa_kotlin.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.material.tabs.TabLayoutMediator.TabConfigurationStrategy
import com.rohith.vsa_kotlin.util.DataParcel
import com.rohith.vsa_kotlin.R
import com.rohith.vsa_kotlin.activities.MainActivity
import com.rohith.vsa_kotlin.activities.RESULT_BACK_PRESSED
import com.rohith.vsa_kotlin.databinding.LayoutProfileBinding
import com.rohith.vsa_kotlin.network.ProfileStats
import com.rohith.vsa_kotlin.network.ServerHandler
import com.rohith.vsa_kotlin.util.parseInt
import com.rohith.vsa_kotlin.views.VideoListView


class ProfilePageFragment : Fragment() {

    lateinit var videoList1 : VideoListView
    lateinit var videoList2 : VideoListView
    private lateinit var binding: LayoutProfileBinding

    var currentProfileStats : ProfileStats? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = LayoutProfileBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.more.setOnClickListener{
            ServerHandler.Auth.signOut()
        }

        binding.viewPager.adapter = MyAdapter()
        binding.viewPager.offscreenPageLimit = 2

        TabLayoutMediator(binding.tabLayout, binding.viewPager,
            TabConfigurationStrategy { tab: TabLayout.Tab, position: Int -> tab.setIcon(if (position == 0) R.drawable.ic_home else R.drawable.ic_heart) }
        ).attach()
    }

    private fun getDataParcel() : DataParcel {
        return (activity as MainActivity).dataParcel
    }

    @SuppressLint("SetTextI18n")
    override fun onResume() {
        super.onResume()
        val dataParcel = getDataParcel()
        if (dataParcel.data != null)
        ServerHandler.Profile.getProfileStats(dataParcel.data as String).addOnSuccessListener { profile ->
            binding.userId.text = "@" + profile.userID
            binding.userName.text = profile.name
            binding.views.text =
                parseInt(profile.numViews)
            binding.likes.text =
                parseInt(profile.numLikes)
            binding.follows.text =
                parseInt(profile.numFollowers)
            binding.following.text =
                parseInt(profile.numFollowing)

            ServerHandler.Profile.getProfileUri(profile.userID!!).addOnSuccessListener { uri ->
                Glide.with(this).load(uri).into(binding.profileImageView)
            }

            currentProfileStats = profile
            (videoList1.adapter as BaseAdapter).notifyDataSetChanged()
            (videoList2.adapter as BaseAdapter).notifyDataSetChanged()
        }.addOnFailureListener {
            binding.userId.text = """@user_name"""
            binding.userName.text = "User name"
            binding.views.text = "0"
            binding.likes.text = "0"
            binding.follows.text = "0"
            binding.following.text = "0"
        }
    }

    inner class MyAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        override fun getItemCount(): Int = 2

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return object : RecyclerView.ViewHolder(VideoListView(parent.context, VideoListAdapter())){}
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            if (position == 0) {
                videoList1 = holder.itemView as VideoListView
                (videoList1.adapter as VideoListAdapter).type = 0
            } else {
                videoList2 = holder.itemView as VideoListView
                (videoList2.adapter as VideoListAdapter).type = 1
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_BACK_PRESSED)
            (activity as MainActivity).binding.viewPager.currentItem = 0
    }

    inner class VideoListAdapter : BaseAdapter() {
        var type: Int = 0
        private val layoutInflater: LayoutInflater = LayoutInflater.from(context)

        override fun getCount(): Int {
            return if (type == 0) currentProfileStats?.numVideos ?: 0 else 0
        }

        override fun getItem(position: Int): Any? = null
        override fun getItemId(position: Int): Long = position.toLong()

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val v = convertView ?: layoutInflater.inflate(R.layout.layout_video_grid_node, parent, false)
            ServerHandler.Content.getVideoStats(currentProfileStats!!.videoIds[position]).addOnSuccessListener {
                v.findViewById<TextView>(R.id.views).text =
                    parseInt(it.numViews)
                v.findViewById<TextView>(R.id.likes).text =
                    parseInt(it.numLikes)

                ServerHandler.Content.getVideoThumbUri(it.videoId!!, it.userId!!).addOnSuccessListener { uri ->
                    Glide.with(this@ProfilePageFragment).load(uri).into(v.findViewById(R.id.imageView))
                }
            }
            v.setOnClickListener {
                val intent = Intent(context, MainActivity::class.java)
                intent.putExtra("profile_vids", true)
                intent.putExtra("user_name", currentProfileStats?.userID)
                intent.putExtra("index", position)
                intent.putExtra("videoIds", currentProfileStats?.videoIds)
                startActivityForResult(intent, RESULT_BACK_PRESSED)
            }
            return v
        }
    }

}