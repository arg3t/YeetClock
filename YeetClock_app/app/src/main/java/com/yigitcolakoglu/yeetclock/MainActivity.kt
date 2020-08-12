package com.yigitcolakoglu.yeetclock

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import androidx.viewpager.widget.ViewPager
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import com.yigitcolakoglu.yeetclock.ui.main.SectionsPagerAdapter
import kotlinx.android.synthetic.main.activity_settings_popup.*
import java.io.File

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val file = File(applicationContext?.filesDir, "ip")
        if(! file.exists()){
            this.applicationContext.openFileOutput("ip", Context.MODE_PRIVATE).use {
                it.write("google.com".toByteArray())
            }
        }

        val sectionsPagerAdapter = SectionsPagerAdapter(this, supportFragmentManager)
        val viewPager: ViewPager = findViewById(R.id.view_pager)
        viewPager.adapter = sectionsPagerAdapter
        val tabs: TabLayout = findViewById(R.id.tabs)
        tabs.setupWithViewPager(viewPager)
        findViewById<ImageView>(R.id.settings_image).setOnClickListener {
            val intent = Intent(this, SettingsPopup::class.java)
            startActivity(intent)
        }
    }
}