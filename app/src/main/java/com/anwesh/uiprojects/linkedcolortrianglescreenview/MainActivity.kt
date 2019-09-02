package com.anwesh.uiprojects.linkedcolortrianglescreenview

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.anwesh.uiprojects.colortrainglescreenview.ColorTriangleScreenView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ColorTriangleScreenView.create(this)
    }
}
