package com.robertas.storyapp.views

import android.Manifest
import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_GET_CONTENT
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.robertas.storyapp.R
import com.robertas.storyapp.databinding.FragmentPreviewBinding
import com.robertas.storyapp.models.enums.CameraMode
import com.robertas.storyapp.models.enums.NetworkResult
import com.robertas.storyapp.utils.EspressoIdlingResource
import com.robertas.storyapp.utils.createTempFile
import com.robertas.storyapp.utils.rotateBitmap
import com.robertas.storyapp.utils.uriToFile
import com.robertas.storyapp.viewmodels.StoryViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class PreviewFragment : Fragment(), View.OnClickListener {

    private var _binding: FragmentPreviewBinding? = null

    private val binding get() = _binding

    private val navArgs by navArgs<PreviewFragmentArgs>()

    private var myPhoto: File? = null

    private lateinit var navController: NavController

    private val storyViewModel by activityViewModels<StoryViewModel>()

    private lateinit var currentPhotoPath: String

    private var descEditText: TextInputEditText? = null

    private var rotationDegree = 0f

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private lateinit var locationRequest: LocationRequest

    private var lastLocation: Location? = null

    private var uploadJob: Job = Job()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentPreviewBinding.inflate(inflater, container, false)

        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        setupNavigation()

        bindParamToFragment()

        descEditText = binding?.descPortraitEt

        binding?.cameraBtn?.setOnClickListener(this)

        binding?.uploadBtn?.setOnClickListener(this)

        binding?.galleryBtn?.setOnClickListener(this)

        binding?.rotateBtn?.setOnClickListener(this)

        createLocationRequest()
    }

    private fun requestCameraPermission() {
        if (isCameraPermissionGranted()) {

            startCamera()
        } else {

            EspressoIdlingResource.increment()

            requestPermission.launch(REQUIRED_CAMERA_PERMISSION)
        }
    }

    private fun isCameraPermissionGranted() = ContextCompat.checkSelfPermission(
        requireContext(),
        REQUIRED_CAMERA_PERMISSION
    ) == PackageManager.PERMISSION_GRANTED


    private val requestPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { permission ->
            if (permission == true) {
                startCamera()

                EspressoIdlingResource.decrement()
            } else {
                binding?.root?.let {
                    Snackbar.make(
                        it,
                        getString(R.string.can_not_receive_permission),
                        Snackbar.LENGTH_SHORT
                    ).show()
                }

                EspressoIdlingResource.decrement()
            }
        }

    private val requestMultiplePermission =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions())
        { permissions ->
            when {
                permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false -> {
                    getLastLocation()
                }
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false -> {
                    getLastLocation()
                }
                else -> {}
            }

            EspressoIdlingResource.decrement()

        }

    private fun getLastLocation() {

        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            EspressoIdlingResource.increment()

            requestMultiplePermission.launch(REQUIRED_LOCATION_PERMISSIONS)

        } else {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->

                lastLocation = location

                if (location == null) {
                    lastLocation = null

                    showSnackBar(getString(R.string.last_location_is_null))
                }
            }
        }
    }

    private val resolutionLauncher =
        registerForActivityResult(
            ActivityResultContracts.StartIntentSenderForResult()
        ) { result ->
            when (result.resultCode) {
                RESULT_OK ->
                    Log.i(TAG, "onActivityResult: All location settings are satisfied.")
                Activity.RESULT_CANCELED-> showSnackBar(getString(R.string.please_turn_on_gps))
            }
        }

    private fun createLocationRequest() {

        locationRequest = LocationRequest.create().apply {
            interval = TimeUnit.SECONDS.toMillis(1)
            maxWaitTime = TimeUnit.SECONDS.toMillis(1)
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)

        val client = LocationServices.getSettingsClient(requireActivity())

        client.checkLocationSettings(builder.build())
            .addOnSuccessListener {

                getLastLocation()
            }
            .addOnFailureListener { exception ->
                if (exception is ResolvableApiException) {
                    try {
                        resolutionLauncher.launch(
                            IntentSenderRequest.Builder(exception.resolution).build()
                        )
                    } catch (sendEx: IntentSender.SendIntentException) {

                        sendEx.message?.let { showSnackBar(it) }
                    }
                }
            }
    }

    private fun startCamera() {
        when (storyViewModel.getCameraMode()) {
            CameraMode.CAMERA_X -> {
                val actionToCameraFragment =
                    PreviewFragmentDirections.actionPreviewFragmentToCameraFragment()

                navController.navigate(actionToCameraFragment)
            }

            CameraMode.SYSTEM -> startCameraSystem()
        }
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
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null

        descEditText = null

        myPhoto = null

        rotationDegree = 0f

        lastLocation = null

        hideKeyBoard()
    }

    private fun startGallery() {
        val intent = Intent()

        intent.action = ACTION_GET_CONTENT

        intent.type = "image/*"

        val chooser = Intent.createChooser(intent, getString(R.string.choose_a_picture))

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

    private fun startCameraSystem() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        val packageManager = activity?.packageManager

        packageManager?.let {
            intent.resolveActivity(it)

            createTempFile(requireContext()).also { file ->
                val photoURI: Uri = FileProvider.getUriForFile(
                    requireActivity(),
                    "com.robertas.storyapp",
                    file
                )
                currentPhotoPath = file.absolutePath

                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)

                launcherIntentCamera.launch(intent)
            }
        }
    }

    private val launcherIntentCamera = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == RESULT_OK) {
            val myFile = File(currentPhotoPath)

            myPhoto = myFile

            val result = BitmapFactory.decodeFile(myPhoto?.path)

            binding?.previewImg?.setImageBitmap(result)
        }
    }


    private fun setButtonVisibility(isVisible: Boolean){
        binding?.apply {
            rotateBtn.isEnabled = isVisible

            uploadBtn.isVisible = isVisible
        }
    }


    private fun setLoadingStatus(isLoading: Boolean){
        binding?.progressLoading?.isVisible = isLoading
    }

    private fun showSnackBar(message: String){
        binding?.root?.let {
            Snackbar.make(
                it,
                message,
                Snackbar.LENGTH_SHORT
            ).show()
        }
    }

    private fun processUpload(file: File, desc: String, rotation: Float, location: Location?) {

        viewLifecycleOwner.lifecycleScope.launchWhenResumed {

            if (uploadJob.isActive) uploadJob.cancel()

            uploadJob = launch {

                val uploadFlow = if (location != null) storyViewModel.uploadImage(
                    file, desc, rotation,
                    LatLng(location.latitude, location.longitude)
                ) else storyViewModel.uploadImage(file, desc, rotation)

                uploadFlow.collect { result ->

                    when (result) {
                        is NetworkResult.Loading -> {
                            setButtonVisibility(false)

                            setLoadingStatus(true)
                        }

                        is NetworkResult.Success -> {

                            setButtonVisibility(true)

                            setLoadingStatus(false)

                            showSnackBar(getString(R.string.success_to_create_story))

                            storyViewModel.invalidateStories()

                            navController.navigateUp()
                        }

                        is NetworkResult.Error -> {
                            setButtonVisibility(true)

                            setLoadingStatus(false)

                            showSnackBar(result.message)
                        }
                    }
                }
            }
        }
    }

    override fun onClick(view: View) {
        when (view.id) {

            R.id.camera_btn -> requestCameraPermission()

            R.id.gallery_btn -> startGallery()

            R.id.upload_btn -> {

                hideKeyBoard()

                if (TextUtils.isEmpty(descEditText?.text)) {

                    descEditText?.error = getString(R.string.error_caption)

                } else {

                    descEditText?.error = null

                    if (myPhoto != null) {

                        processUpload(myPhoto!!, descEditText?.text.toString().trim(), rotationDegree, lastLocation)

                    } else {
                        binding?.root?.let {
                            Snackbar.make(
                                it,
                                getString(R.string.picture_unavailable),
                                Snackbar.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }

            R.id.rotate_btn -> {

                myPhoto?.let { file ->

                    incrementRotation()

                    viewLifecycleOwner.lifecycleScope.launch {
                        val rotated = rotateBitmap(file, rotationDegree)

                        binding?.previewImg?.setImageBitmap(rotated)
                    }
                }
            }

            else -> return
        }
    }

    private fun incrementRotation() {
        rotationDegree += 90f
    }


    private fun hideKeyBoard() {
        if (requireActivity().currentFocus == null) {
            return
        }
        val inputMethodManager =
            requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        inputMethodManager.hideSoftInputFromWindow(requireActivity().currentFocus!!.windowToken, 0)
    }

    companion object {
        private const val REQUIRED_CAMERA_PERMISSION = Manifest.permission.CAMERA

        private val REQUIRED_LOCATION_PERMISSIONS = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        const val TAG = "PreviewFragment"
    }

}