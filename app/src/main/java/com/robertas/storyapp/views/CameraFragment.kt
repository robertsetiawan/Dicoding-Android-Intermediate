package com.robertas.storyapp.views

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.robertas.storyapp.R
import com.robertas.storyapp.StoryApp
import com.robertas.storyapp.databinding.FragmentCameraBinding
import com.robertas.storyapp.utils.createFile
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraFragment : Fragment(), View.OnClickListener {

    private var _binding: FragmentCameraBinding? = null

    private val binding get() = _binding

    private var cameraSelector: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

    private var imageCapture: ImageCapture? = null

    private lateinit var cameraExecutor: ExecutorService

    private lateinit var navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCameraBinding.inflate(inflater, container, false)

        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (allPermissionsGranted()) {

            startCamera()
        } else {

            requestPermission.launch(REQUIRED_PERMISSIONS)
        }

        binding?.captureBtn?.setOnClickListener(this)

        binding?.switchBtn?.setOnClickListener(this)

        cameraExecutor = Executors.newSingleThreadExecutor()

        navController = findNavController()
    }

    private val requestPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { permission ->
            if (permission == true) {
                startCamera()
            } else {
                binding?.root?.let {
                    Snackbar.make(it, "Tidak mendapatkan permission", Snackbar.LENGTH_SHORT).show()
                }
            }
        }

    private fun startCamera() {
        val cameraFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding?.viewFinder?.surfaceProvider)
            }

            imageCapture = ImageCapture.Builder().build()

            try {
                cameraProvider.unbindAll()

                cameraProvider.bindToLifecycle(
                    viewLifecycleOwner,
                    cameraSelector,
                    preview,
                    imageCapture
                )

            } catch (e: Exception) {

                binding?.root?.let {
                    Snackbar.make(it, "Gagal menjalankan kamera", Snackbar.LENGTH_SHORT).show()
                }
            }

        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun allPermissionsGranted() = ContextCompat.checkSelfPermission(
        requireContext(),
        REQUIRED_PERMISSIONS
    ) == PackageManager.PERMISSION_GRANTED

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null

        cameraExecutor.shutdown()
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.capture_btn -> takePhoto()

            R.id.switch_btn -> {
                cameraSelector =
                    if (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) CameraSelector.DEFAULT_FRONT_CAMERA
                    else CameraSelector.DEFAULT_BACK_CAMERA
                startCamera()
            }

            else -> return
        }
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return

        val photoFile = createFile(activity?.application as StoryApp)

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    val actionToPreviewFragment =
                        CameraFragmentDirections.actionCameraFragmentToPreviewFragment(photoFile)

                    navController.navigate(actionToPreviewFragment)
                }

                override fun onError(exception: ImageCaptureException) {
                    binding?.root?.let {
                        Snackbar.make(it, "Gagal mengambil gambar", Snackbar.LENGTH_SHORT).show()
                    }
                }
            }
        )
    }

    companion object {
        private const val REQUIRED_PERMISSIONS = Manifest.permission.CAMERA
    }
}