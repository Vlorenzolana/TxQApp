package com.sugaarklang.txqapp_jelly_bean

import android.app.Activity
import android.os.Bundle

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        val gridView = GridViewCanvas(this)
        setContentView(gridView)
    }
}

