package com.example.deeplearningsample.view

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.deeplearningsample.R


abstract class BasePreviewActivity : AppCompatActivity() {
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.preview_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.camera_capture -> {
                startActivity(Intent(this, CameraCaptureActivity::class.java))
            }
            R.id.frame_capture -> {
                startActivity(Intent(this, CameraCaptureActivity::class.java))
            }
        }
        return super.onOptionsItemSelected(item)
    }

    protected fun setupToolbar() {
        val toolBar: Toolbar = findViewById(R.id.tool_bar)
        toolBar.inflateMenu(R.menu.preview_menu)
        setSupportActionBar(toolBar)
    }
}