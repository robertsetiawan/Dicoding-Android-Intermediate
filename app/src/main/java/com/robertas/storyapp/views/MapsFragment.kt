package com.robertas.storyapp.views

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import com.robertas.storyapp.R
import com.robertas.storyapp.databinding.FragmentMapsBinding
import com.robertas.storyapp.models.domain.Story
import com.robertas.storyapp.models.enums.NetworkResult
import com.robertas.storyapp.viewmodels.MapsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MapsFragment : Fragment(), OnMapReadyCallback, Toolbar.OnMenuItemClickListener {

    private var _binding: FragmentMapsBinding? = null

    private val binding get() = _binding

    private lateinit var navController: NavController

    private lateinit var mMap: GoogleMap

    private val mapsViewModel by viewModels<MapsViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMapsBinding.inflate(inflater, container, false)

        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupMap()

        setupNavigation()
    }

    private fun setupNavigation() {
        navController = findNavController()

        val appBarConfiguration =
            AppBarConfiguration(setOf(R.id.homeFragment))

        binding?.toolbarFragment?.apply {
            setupWithNavController(navController, appBarConfiguration)

            setNavigationOnClickListener { navController.navigateUp() }

            inflateMenu(R.menu.maps_menu)

            setOnMenuItemClickListener(this@MapsFragment)
        }
    }

    private fun setupMap() {
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?

        mapFragment?.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {

        mMap = googleMap

        mMap.uiSettings.apply {
            isZoomControlsEnabled = true

            isIndoorLevelPickerEnabled = true
        }

        loadData()

        setMapStyle()
    }

    private fun setMapStyle() {
        try {
            mMap.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    requireContext(),
                    R.raw.map_style
                )
            )

        } catch (e: Exception) {
            Log.e(TAG, e.message.toString())
        }
    }

    private fun loadData() {
        val storyListObserver = Observer<NetworkResult<List<Story>?>> { result ->
            when (result) {
                is NetworkResult.Loading -> {}

                is NetworkResult.Success -> {
                    result.data?.forEachIndexed { index, story -> loadMarker(index, story) }
                }
                is NetworkResult.Error -> {
                    binding?.root?.let {
                        Snackbar.make(it, result.message, Snackbar.LENGTH_SHORT).show()
                    }
                }
            }

        }

        mapsViewModel.loadStoryState.observe(viewLifecycleOwner, storyListObserver)
    }

    private fun loadMarker(index: Int, story: Story) {
        if (story.lat != null && story.lon != null) {
            Glide.with(requireContext())
                .asBitmap()
                .load(story.photoUrl)
                .apply(RequestOptions().centerCrop())
                .into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(
                        resource: Bitmap,
                        transition: Transition<in Bitmap>?
                    ) {
                        val resizedBitmap = resizeBitmap(resource, 100, 100)

                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(story.lat, story.lon))
                                .title(story.name)
                                .icon(BitmapDescriptorFactory.fromBitmap(resizedBitmap))
                        )

                        if (index == 0) mMap.animateCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                LatLng(story.lat, story.lon),
                                0f
                            )
                        )
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {}
                })
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.normal_type -> {
                mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
                true
            }

            R.id.hybrid_type -> {
                mMap.mapType = GoogleMap.MAP_TYPE_HYBRID
                true
            }

            R.id.terrain_type -> {
                mMap.mapType = GoogleMap.MAP_TYPE_TERRAIN
                true
            }

            R.id.satellite_type -> {
                mMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun resizeBitmap(bitmap: Bitmap, width: Int, height: Int): Bitmap {

        return Bitmap.createScaledBitmap(bitmap, width, height, false)
    }

    companion object {
        private const val TAG = "MapsFragment"
    }
}