package com.anwesh.uiprojects.linkedptlupdownview

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.anwesh.uiprojects.ptlupdownview.PTLUpDownView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PTLUpDownView.create(this)
    }
}
