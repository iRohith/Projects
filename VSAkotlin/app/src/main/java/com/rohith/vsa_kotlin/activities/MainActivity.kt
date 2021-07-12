package com.rohith.vsa_kotlin.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.firebase.ui.auth.IdpResponse
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.material.snackbar.Snackbar
import com.rohith.vsa_kotlin.databinding.ActivityMainBinding
import com.rohith.vsa_kotlin.fragments.HomePageFragment
import com.rohith.vsa_kotlin.fragments.ProfilePageFragment
import com.rohith.vsa_kotlin.network.ServerHandler
import com.rohith.vsa_kotlin.util.DataParcel

const val RESULT_BACK_PRESSED = 2

class MainActivity : AppCompatActivity() {



    val dataParcel = DataParcel()
    lateinit var exoPlayer: SimpleExoPlayer
    lateinit var binding : ActivityMainBinding
    private val snackBar : Snackbar by lazy { Snackbar.make(binding.swipeContainer, "Press back again to exit", Snackbar.LENGTH_SHORT) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        binding.swipeContainer.isEnabled = intent.extras?.getBoolean("profile_vids") != true

        exoPlayer = SimpleExoPlayer.Builder(this).build()
        exoPlayer.repeatMode = ExoPlayer.REPEAT_MODE_ONE

        if (binding.swipeContainer.isEnabled) binding.swipeContainer.setOnRefreshListener {
            binding.swipeContainer.isRefreshing = false
            dataParcel.data = null
            (binding.viewPager.adapter?.instantiateItem(binding.viewPager, 0) as HomePageFragment).refresh()
        }

        binding.viewPager.adapter = PageAdapter(supportFragmentManager)
        binding.viewPager.offscreenPageLimit = 2
        if (binding.swipeContainer.isEnabled) binding.viewPager.addOnPageChangeListener(object : OnPageChangeListener {
            override fun onPageScrolled(position: Int, v: Float, i1: Int) {}
            override fun onPageSelected(position: Int) {
                binding.swipeContainer.isEnabled = position == 0
            }
            override fun onPageScrollStateChanged(state: Int) {
                binding.swipeContainer.isEnabled = state == ViewPager.SCROLL_STATE_IDLE && binding.viewPager.currentItem == 0
            }
        })
    }

    override fun onDestroy() {
        exoPlayer.release()
        super.onDestroy()
    }

    override fun onBackPressed() {
        if (binding.viewPager.currentItem == 1) binding.viewPager.currentItem = 0 else {
            if (snackBar.isShown || intent.extras?.getBoolean("profile_vids") == true) super.onBackPressed() else snackBar.show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ServerHandler.RC_SIGN_IN) {
            if (resultCode != Activity.RESULT_OK) {
                Toast.makeText(this, "Sign in failed", Toast.LENGTH_SHORT).show()
            } else {
                val response = IdpResponse.fromResultIntent(data) //if (response?.isNewUser == true){
                ServerHandler.Profile.checkUserExists(response?.email).addOnSuccessListener { oldUser ->
                    if (!oldUser){
                        val intent = Intent(this, SignUpActivity::class.java)
                        intent.putExtra("name", ServerHandler.Auth.displayName)
                        val email = ServerHandler.Auth.email
                        intent.putExtra("user_id", email?.substring(0, email.indexOf('@')))
                        startActivity(intent)
                    }
                }
            }
        }
    }

    inner class PageAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
        override fun getItem(position: Int): Fragment {
            return if (position == 0) {
                if (intent.extras?.getBoolean("profile_vids") == true) {
                    val args = Bundle()
                    args.putString("user_name", intent.extras?.getString("user_name"))
                    args.putInt("index", intent.extras?.getInt("index")?:0)
                    args.putStringArrayList("videoIds", intent.extras?.getStringArrayList("videoIds"))
                    val frag = HomePageFragment()
                    frag.arguments = args
                    frag
                } else HomePageFragment()
            } else ProfilePageFragment()
        }

        override fun getCount(): Int {
            return if (intent.extras?.getBoolean("profile_vids") == true) 1 else 2
        }
    }

}