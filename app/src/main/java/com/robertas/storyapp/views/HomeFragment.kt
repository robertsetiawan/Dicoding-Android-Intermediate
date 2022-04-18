package com.robertas.storyapp.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.snackbar.Snackbar
import com.robertas.storyapp.R
import com.robertas.storyapp.abstractions.IOnItemClickListener
import com.robertas.storyapp.adapters.StoryListAdapter
import com.robertas.storyapp.databinding.FragmentHomeBinding
import com.robertas.storyapp.databinding.StoryCardBinding
import com.robertas.storyapp.models.domain.Story
import com.robertas.storyapp.models.enums.NetworkResult
import com.robertas.storyapp.viewmodels.StoryViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class HomeFragment : Fragment(), SwipeRefreshLayout.OnRefreshListener, View.OnClickListener,
    Toolbar.OnMenuItemClickListener {

    private var _binding: FragmentHomeBinding? = null

    private val binding get() = _binding

    private lateinit var navController: NavController

    private val storyViewModel by activityViewModels<StoryViewModel>()

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

        binding?.floatingBtn?.setOnClickListener(this)

        setupNavigation()

        setupStoryListObserver()
    }


    private fun setupStoryListObserver() {

        val storyListAdapter = StoryListAdapter()

        storyListAdapter.onItemClickListener =
            object : IOnItemClickListener<Story, StoryCardBinding> {
                override fun onClick(item: Story, binding: StoryCardBinding) {
                    val actionToDetailFragment =
                        HomeFragmentDirections.actionHomeFragmentToStoryDetailFragment(item)

                    val extras = FragmentNavigatorExtras(
                        binding.storyImg to "picture_${item.id}",

                        binding.nameTv to "name_${item.id}",

                        binding.smallDescTv to "desc_${item.id}",

                        binding.timeTv to "time_${item.id}"
                    )

                    navController.navigate(actionToDetailFragment, extras)
                }
            }

        val storyListObserver = Observer<NetworkResult<List<Story>?>> { result ->
            when (result) {
                is NetworkResult.Loading -> binding?.swipeRefresh?.isRefreshing = true

                is NetworkResult.Success -> {

                    result.data?.let {
                        if (it.isNotEmpty()) {
                            switchRefreshAndList(false)

                            storyListAdapter.submitList(it)
                        } else {
                            switchRefreshAndList(true)
                        }
                    }
                }
                is NetworkResult.Error -> {
                    binding?.root?.let {
                        Snackbar.make(it, result.message, Snackbar.LENGTH_SHORT).show()
                    }

                    binding?.storyList?.adapter?.itemCount?.let {
                        if (it == 0) {
                            switchRefreshAndList(true)
                        } else {
                            switchRefreshAndList(false)
                        }
                    }
                }
            }
        }

        storyViewModel.loadStoryState.observe(viewLifecycleOwner, storyListObserver)

        binding?.storyList?.apply {
            adapter = storyListAdapter

            postponeEnterTransition()

            viewTreeObserver
                .addOnPreDrawListener {
                    startPostponedEnterTransition()
                    true
                }
        }

        swipeRefreshLayout?.setOnRefreshListener(this)
    }

    private fun switchRefreshAndList(isEmptyList: Boolean) {

        if (isEmptyList) {

            binding?.apply {
                swipeRefreshLayout?.isRefreshing = false

                storyList.visibility = View.GONE

                emptyLayout.visibility = View.VISIBLE
            }

        } else {

            binding?.apply {
                swipeRefresh.isRefreshing = false

                storyList.visibility = View.VISIBLE

                emptyLayout.visibility = View.GONE
            }
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

        binding?.toolbarFragment?.inflateMenu(R.menu.home_menu)

        binding?.toolbarFragment?.setOnMenuItemClickListener(this)
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
                val actionToPreviewFragment =
                    HomeFragmentDirections.actionHomeFragmentToPreviewFragment(null)

                navController.navigate(actionToPreviewFragment)
            }

            else -> return
        }
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.settings -> {
                val actionToSettingsFragment =
                    HomeFragmentDirections.actionHomeFragmentToSettingFragment()

                navController.navigate(actionToSettingsFragment)

                true
            }

            R.id.explore -> {
                val actionToMapsFragment = HomeFragmentDirections.actionHomeFragmentToMapsFragment()

                navController.navigate(actionToMapsFragment)

                true
            }
            else -> false
        }
    }
}