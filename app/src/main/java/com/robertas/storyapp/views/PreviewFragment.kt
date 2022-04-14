package com.robertas.storyapp.views

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_GET_CONTENT
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.robertas.storyapp.R
import com.robertas.storyapp.databinding.FragmentPreviewBinding
import com.robertas.storyapp.models.enums.NetworkResult
import com.robertas.storyapp.utils.rotateBitmap
import com.robertas.storyapp.utils.uriToFile
import com.robertas.storyapp.viewmodels.StoryViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.File


@AndroidEntryPoint
class PreviewFragment : Fragment(), View.OnClickListener {

    private var _binding: FragmentPreviewBinding? = null

    private val binding get() = _binding

    private val navArgs by navArgs<PreviewFragmentArgs>()

    private var myPhoto: File? = null

    private lateinit var navController: NavController

    private val storyViewModel by activityViewModels<StoryViewModel>()

    private var descEditText: TextInputEditText? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentPreviewBinding.inflate(inflater, container, false)

        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupNavigation()

        bindParamToFragment()

        descEditText = binding?.descEt

        binding?.cameraBtn?.setOnClickListener(this)

        binding?.uploadBtn?.setOnClickListener(this)

        binding?.galleryBtn?.setOnClickListener(this)

        binding?.rotateBtn?.setOnClickListener(this)
    }

    private fun bindParamToFragment() {

        if (navArgs.picture != null) {
            myPhoto = navArgs.picture

            val result = BitmapFactory.decodeFile(myPhoto?.path)

            binding?.previewImg?.setImageBitmap(result)
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

        val uploadObserver = Observer<NetworkResult<Boolean?>> { result ->
            when (result) {
                is NetworkResult.Loading -> {}

                is NetworkResult.Success -> {

                    binding?.root?.let {
                        Snackbar.make(it, "Story berhasil dibuat", Snackbar.LENGTH_SHORT).show()
                    }

                    storyViewModel.getAllStories()

                    navController.navigateUp()

                    binding?.uploadBtn?.visibility = View.VISIBLE

                    binding?.progressLoading?.visibility = View.GONE

                    storyViewModel.doneNavigating()
                }

                is NetworkResult.Error -> {
                    binding?.root?.let {
                        Snackbar.make(it, "Gagal membuat story", Snackbar.LENGTH_SHORT).show()
                    }

                    binding?.uploadBtn?.visibility = View.VISIBLE

                    binding?.progressLoading?.visibility = View.GONE

                    storyViewModel.doneNavigating()
                }
            }
        }

        storyViewModel.uploadStoryState.observe(viewLifecycleOwner, uploadObserver)
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null

        descEditText = null

        myPhoto = null

        storyViewModel.resetRotation()

        hideKeyBoard()
    }

    private fun startGallery() {
        val intent = Intent()

        intent.action = ACTION_GET_CONTENT

        intent.type = "image/*"

        val chooser = Intent.createChooser(intent, "Choose a Picture")

        launcherIntentGallery.launch(chooser)
    }

    private val launcherIntentGallery = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val selectedImg: Uri = result.data?.data as Uri

            myPhoto = uriToFile(selectedImg, requireContext())

            binding?.previewImg?.setImageURI(selectedImg)
        }
    }

    override fun onClick(view: View) {
        when (view.id) {

            R.id.camera_btn -> {
                val actionToCameraFragment =
                    PreviewFragmentDirections.actionPreviewFragmentToCameraFragment()

                navController.navigate(actionToCameraFragment)
            }

            R.id.gallery_btn -> {
                Log.i(PreviewFragment::class.java.simpleName, "button gallery clicked")
                startGallery()
            }

            R.id.upload_btn -> {

                if (TextUtils.isEmpty(descEditText?.text)) {

                    descEditText?.error = getString(R.string.error_caption)

                } else {

                    descEditText?.error = null

                    if (myPhoto != null) {

                        storyViewModel.uploadImage(myPhoto!!, descEditText?.text.toString())

                        binding?.uploadBtn?.visibility = View.GONE

                        binding?.progressLoading?.visibility = View.VISIBLE

                    } else {
                        binding?.root?.let {
                            Snackbar.make(it, "Gambar tidak tersedia", Snackbar.LENGTH_SHORT).show()
                        }
                    }
                }

            }

            R.id.rotate_btn -> {

                myPhoto?.let { file ->

                    storyViewModel.incrementRotation()

                    lifecycleScope.launch {
                        val rotated = rotateBitmap(file, storyViewModel.rotationDegree)

                        binding?.previewImg?.setImageBitmap(rotated)
                    }
                }
            }

            else -> return
        }
    }

    private fun hideKeyBoard() {
        if (requireActivity().currentFocus == null) {
            return
        }
        val inputMethodManager =
            requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        inputMethodManager.hideSoftInputFromWindow(requireActivity().currentFocus!!.windowToken, 0)
    }

}