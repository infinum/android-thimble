package com.infinum.thimble.sample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.infinum.thimble.Thimble
import com.infinum.thimble.sample.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ActivityMainBinding.inflate(layoutInflater)
            .also { setContentView(it.root) }
            .also {
                it.showButton.setOnClickListener {
                    Thimble.show()
                }
            }
    }
}