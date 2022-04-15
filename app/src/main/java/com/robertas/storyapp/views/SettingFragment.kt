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
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.snackbar.Snackbar
import com.robertas.storyapp.R
import com.robertas.storyapp.databinding.FragmentSettingBinding
import com.robertas.storyapp.models.enums.CameraMode
import com.robertas.storyapp.models.enums.LanguageMode
import com.robertas.storyapp.viewmodels.LoginViewModel
import com.robertas.storyapp.viewmodels.StoryViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingFragment : Fragment(), View.OnClickListener {

    private var _binding: FragmentSettingBinding? = null

    private val binding get() = _binding

    private lateinit var navController: NavController

    private val storyViewModel by activityViewModels<StoryViewModel>()

    private val loginViewModel by viewModels<LoginViewModel>()

    private var cameraDropdown: AutoCompleteTextView? = null

    private var languageDropdown: AutoCompleteTextView? = null

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

        setupLanguageDropdown()

        binding?.logoutBtn?.setOnClickListener(this)
    }

    private fun setupLanguageDropdown() {
        val languages = listOf(LanguageMode.DEFAULT, LanguageMode.ID, LanguageMode.EN)

        val adapter = ArrayAdapter(requireContext(), R.layout.list_item_settings, languages)

        languageDropdown = (binding?.languageTv?.editText as? AutoCompleteTextView)

        languageDropdown?.apply {
            setAdapter(adapter)

            setText(
                languageDropdown?.adapter?.getItem(
                    languages.indexOf(loginViewModel.getLanguageMode())
                ).toString(), false
            )

            onItemClickListener = AdapterView.OnItemClickListener { _, _, pos, _ -> 
                loginViewModel.setLanguageMode(languages[pos])

                binding?.root?.let {
                    Snackbar.make(it, getString(R.string.restart_to_change_language), Snackbar.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupCameraDropdown() {
        val items = listOf(CameraMode.CAMERA_X, CameraMode.SYSTEM)

        val adapter = ArrayAdapter(requireContext(), R.layout.list_item_settings, items)

        cameraDropdown = (binding?.cameraTv?.editText as? AutoCompleteTextView)

        cameraDropdown?.apply {
            setAdapter(adapter)

            setText(
                cameraDropdown?.adapter?.getItem(
                    items.indexOf(storyViewModel.getCameraMode())
                ).toString(), false
            )

            onItemClickListener =
                AdapterView.OnItemClickListener { _, _, pos, _ -> storyViewModel.setCameraMode(items[pos]) }
        }
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

        cameraDropdown = null
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.logout_btn -> {

                loginViewModel.logOut()

                val actionToLoginFragment =
                    SettingFragmentDirections.actionSettingFragmentToLoginFragment()

                navController.navigate(actionToLoginFragment)
            }

            else -> return
        }

    }

}