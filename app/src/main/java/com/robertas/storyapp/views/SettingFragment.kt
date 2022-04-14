package com.robertas.storyapp.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.robertas.storyapp.R
import com.robertas.storyapp.databinding.FragmentSettingBinding
import com.robertas.storyapp.models.enums.CameraMode
import com.robertas.storyapp.viewmodels.StoryViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingFragment : Fragment() {

    private var _binding: FragmentSettingBinding? = null

    private val binding get() = _binding

    private lateinit var navController: NavController

    private val storyViewModel by activityViewModels<StoryViewModel>()

    private var cameraDropDown: AutoCompleteTextView? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSettingBinding.inflate(inflater, container, false)

        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupNavigation()

        setupCameraDropdown()
    }

    private fun setupCameraDropdown() {
        val items = listOf(CameraMode.CAMERA_X, CameraMode.SYSTEM)

        val adapter = ArrayAdapter(requireContext(), R.layout.list_item_settings, items)

        cameraDropDown = (binding?.cameraTv?.editText as? AutoCompleteTextView)

        cameraDropDown?.setAdapter(adapter)

        cameraDropDown?.setText(
            cameraDropDown?.adapter?.getItem(
                items.indexOf(storyViewModel.getCameraMode())
            ).toString(), false
        )

        cameraDropDown?.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, pos, _ -> storyViewModel.setCameraMode(items[pos]) }
    }

    private fun setupNavigation() {
        navController = findNavController()

        val appBarConfiguration =
            AppBarConfiguration(setOf(R.id.homeFragment))

        binding?.toolbarFragment?.apply {
            setupWithNavController(navController, appBarConfiguration)

            setNavigationOnClickListener { navController.navigateUp() }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null

        cameraDropDown = null
    }

}