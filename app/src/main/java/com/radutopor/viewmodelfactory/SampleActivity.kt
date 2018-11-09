package com.radutopor.viewmodelfactory

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import kotlinx.android.synthetic.main.activity_sample.*
import javax.inject.Inject

class SampleActivity : AppCompatActivity() {
    @Inject
    lateinit var sampleViewModelFactory2: SampleViewModelFactory2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sample)
        appComponent.inject(this)

        val userName = intent.getStringExtra("USER_NAME") ?: "Billy"
        val viewModel = ViewModelProviders.of(this, sampleViewModelFactory2.create(userName))
            .get(SampleViewModel::class.java)

        viewModel.getGreeting().observe(this, Observer { greeting ->
            greetingTextView.text = greeting
        })
    }
}