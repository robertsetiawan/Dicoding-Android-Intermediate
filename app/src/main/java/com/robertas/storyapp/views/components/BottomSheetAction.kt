package com.robertas.storyapp.views.components

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.robertas.storyapp.databinding.BottomSheetActionBinding

class BottomSheetAction: BottomSheetDialogFragment() {

    private var _binding: BottomSheetActionBinding?= null

    private val binding get() = _binding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = BottomSheetActionBinding.inflate(inflater)

        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding?.cameraBtn?.setOnClickListener(parentFragment as View.OnClickListener)

        binding?.uploadBtn?.setOnClickListener(parentFragment as View.OnClickListener)
    }

    companion object {
        const val TAG = "bottom_sheet_action"
    }
}