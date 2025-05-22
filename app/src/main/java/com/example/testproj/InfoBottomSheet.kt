package com.example.testproj

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class InfoBottomSheet(private val title: String, private val content: String) : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.bottom_sheet_info, container, false)
        view.findViewById<TextView>(R.id.infoTitle).text = title
        view.findViewById<TextView>(R.id.infoContent).text = content
        return view
    }
}
