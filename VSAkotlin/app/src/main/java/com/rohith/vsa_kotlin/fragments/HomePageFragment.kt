package com.rohith.vsa_kotlin.fragments

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.exoplayer2.Player
import com.rohith.vsa_kotlin.R
import com.rohith.vsa_kotlin.activities.CameraActivity
import com.rohith.vsa_kotlin.activities.MainActivity
import com.rohith.vsa_kotlin.activities.RESULT_BACK_PRESSED
import com.rohith.vsa_kotlin.databinding.LayoutHomeBinding
import com.rohith.vsa_kotlin.network.VideoSession
import com.rohith.vsa_kotlin.util.DataParcel


class HomePageFragment : Fragment(R.layout.layout_home), View.OnClickListener {

    private lateinit var mVideoSession: VideoSession
    private lateinit var mActivity: MainActivity
    private lateinit var binding: LayoutHomeBinding

    fun refresh(){
        if (mActivity.binding.swipeContainer.isRefreshing || !VideoPlayerFragment.isInitialized) return
        mActivity.binding.swipeContainer.isRefreshing = true
        mVideoSession.refresh()
        binding.viewPager.adapter?.notifyDataSetChanged()
        binding.viewPager.adapter = binding.viewPager.adapter
        Handler().postDelayed({
            mActivity.binding.swipeContainer.isRefreshing = false
        }, 1000)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mActivity = activity as MainActivity
        binding = LayoutHomeBinding.inflate(inflater)
        mVideoSession = if (arguments == null || arguments?.containsKey("user_name") != true) VideoSession().populateRandom() else {
            val vs = VideoSession()
            vs.videoIds = arguments?.getStringArrayList("videoIds")?.toSet()
            vs
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mActivity.exoPlayer.addListener(object : Player.EventListener{
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                if (isPlaying) getCurrentVideoPlayerFragment().binding.imageView.visibility = View.GONE
            }
        })

        binding.home.setOnClickListener(this)
        binding.more.setOnClickListener(this)
        binding.search.setOnClickListener(this)
        binding.create.setOnClickListener(this)

        binding.viewPager.adapter = MyAdapter(this)
        binding.viewPager.offscreenPageLimit = 5

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            var prevFragPos : Int = -1

            override fun onPageSelected(position: Int) {
                val currFrag = getVideoPlayerFragmentAt(position)
                if (currFrag.mediaSource == null) { prevFragPos = position; return }
                getDataParcel().data = currFrag.videoStats.userId

                if (prevFragPos >= 0) {
                    val prevFrag = getVideoPlayerFragmentAt(prevFragPos)
                    if (prevFrag != currFrag) {
                        prevFrag.binding.player.player = null
                        prevFrag.binding.imageView.visibility = View.VISIBLE
                        prevFrag.binding.playButton.alpha = 0f
                    }
                }

                currFrag.binding.player.player = mActivity.exoPlayer
                mActivity.exoPlayer.prepare(currFrag.mediaSource!!)
                mActivity.exoPlayer.playWhenReady = true
                prevFragPos = position
            }
        })

        if (arguments != null && arguments?.containsKey("index") == true)
            binding.viewPager.postDelayed({ binding.viewPager.setCurrentItem(arguments?.getInt("index")?:0, false) }, 100)
    }

    override fun onPause() {
        super.onPause()
        mActivity.exoPlayer.playWhenReady = false
    }

    override fun onResume() {
        super.onResume()
        if (getDataParcel().data == null) return
        val frag = getCurrentVideoPlayerFragment()
        getDataParcel().data = frag.videoStats.userId
        if (frag.binding.playButton.alpha == 0f){
            frag.binding.player.player = mActivity.exoPlayer
            mActivity.exoPlayer.playWhenReady = true
        }
    }

    private fun getDataParcel() : DataParcel {
        return (activity as MainActivity).dataParcel
    }

    fun getVideoPlayerFragmentAt(pos: Int) : VideoPlayerFragment {
        return childFragmentManager.findFragmentByTag("f$pos") as VideoPlayerFragment
    }

    fun getCurrentVideoPlayerFragment() : VideoPlayerFragment {
        return getVideoPlayerFragmentAt(binding.viewPager.currentItem)
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.home -> {
                if (arguments == null || arguments?.containsKey("user_name") != true){
                    refresh()
                } else {
                    activity?.setResult(RESULT_BACK_PRESSED)
                    activity?.finish()
                }
            }
            R.id.more -> Toast.makeText(activity, "More pressed", Toast.LENGTH_SHORT).show()
            R.id.search -> Toast.makeText(activity, "Search pressed", Toast.LENGTH_SHORT).show()
            R.id.create -> {
                val intent = Intent(context, CameraActivity::class.java)
                startActivity(intent)
            }
        }
    }

    inner class MyAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
        override fun createFragment(position: Int): Fragment {
            val args = Bundle()
            args.putString("videoId", mVideoSession.videoIds?.elementAt(position))
            val ret = VideoPlayerFragment(requireContext())
            ret.arguments = args
            return ret
        }

        override fun getItemCount(): Int {
            return mVideoSession.videoIds?.size ?: 0
        }

    }
}