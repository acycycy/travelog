package com.acycycy.travelog

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.acycycy.travelog.databinding.ActivityMainBinding
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 처음 화면
        replaceFragment(HomeFragment())

        // 아래 탭 클릭 이벤트
        binding.bottomNavigation.setOnItemSelectedListener {

            when (it.itemId) {

                R.id.menu_home -> {
                    replaceFragment(HomeFragment())
                    true
                }

                R.id.menu_stats -> {
                    replaceFragment(StatsFragment())
                    true
                }

                else -> false
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_option, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        val currentFragment =
            supportFragmentManager.findFragmentById(R.id.fragment_container)

        when (item.itemId) {

            R.id.menu_latest -> {
                if (currentFragment is HomeFragment) {
                    currentFragment.loadTravelsLatest()
                }

                Toast.makeText(this, "최신순 정렬", Toast.LENGTH_SHORT).show()
                return true
            }

            R.id.menu_oldest -> {
                if (currentFragment is HomeFragment) {
                    currentFragment.loadTravelsOldest()
                }

                Toast.makeText(this, "오래된순 정렬", Toast.LENGTH_SHORT).show()
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun replaceFragment(fragment: Fragment) {

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}