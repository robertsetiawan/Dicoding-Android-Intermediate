package com.robertas.storyapp.views

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.snackbar.Snackbar
import com.robertas.storyapp.R
import com.robertas.storyapp.adapters.StoryListAdapter
import com.robertas.storyapp.databinding.FragmentHomeBinding
import com.robertas.storyapp.models.domain.Story
import com.robertas.storyapp.models.enums.NetworkResult
import com.robertas.storyapp.viewmodels.StoryViewModel
import com.robertas.storyapp.views.components.BottomSheetAction
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class HomeFragment : Fragment(), SwipeRefreshLayout.OnRefreshListener, View.OnClickListener {

    private var _binding: FragmentHomeBinding? = null

    private val binding get() = _binding

    private lateinit var navController: NavController

    private val storyViewModel by viewModels<StoryViewModel>()

    private var swipeRefreshLayout: SwipeRefreshLayout? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        swipeRefreshLayout = binding?.swipeRefresh

        setupNavigation()

        setupStoryListObserver()
    }

    private fun toggleSwipeRefresh(isLoading: Boolean) {
        swipeRefreshLayout?.isRefreshing = !isLoading
    }

    private fun setupStoryListObserver() {

        val storyListAdapter = StoryListAdapter()

        val storyListObserver = Observer<NetworkResult<List<Story>?>> { result ->
            when (result) {
                is NetworkResult.Loading -> toggleSwipeRefresh(false)

                is NetworkResult.Success -> {
                    result.data?.let { storyListAdapter.submitList(it) }

                    toggleSwipeRefresh(true)
                }

                is NetworkResult.Error -> {
                    binding?.root?.let {
                        Snackbar.make(it, result.message, Snackbar.LENGTH_SHORT).show()
                    }

                    toggleSwipeRefresh(true)
                }
            }
        }

        storyViewModel.loadStoryState.observe(viewLifecycleOwner, storyListObserver)

        binding?.storyList?.adapter = storyListAdapter

        swipeRefreshLayout?.setOnRefreshListener(this)
    }

    private fun setupNavigation() {
        navController = findNavController()

        val appBarConfiguration =
            AppBarConfiguration(setOf(R.id.homeFragment))

        binding?.toolbarFragment?.apply {
            setupWithNavController(navController, appBarConfiguration)

            setNavigationOnClickListener { navController.navigateUp() }
        }

        binding?.floatingBtn?.setOnClickListener(this)
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null

        swipeRefreshLayout = null
    }

    override fun onRefresh() {
        storyViewModel.getAllStories()
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.floating_btn -> {
                if (childFragmentManager.findFragmentByTag(BottomSheetAction.TAG) == null) {

                    BottomSheetAction().show(childFragmentManager, BottomSheetAction.TAG)
                }
            }

            R.id.camera_btn -> {
                val actionToCameraFragment =
                    HomeFragmentDirections.actionHomeFragmentToCameraFragment()

                navController.navigate(actionToCameraFragment)
            }

            R.id.upload_btn -> {
            }
        }
    }
}