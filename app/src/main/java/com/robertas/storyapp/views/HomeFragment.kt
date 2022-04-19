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
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.paging.LoadState
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.robertas.storyapp.R
import com.robertas.storyapp.abstractions.IOnItemClickListener
import com.robertas.storyapp.adapters.LoadingStateAdapter
import com.robertas.storyapp.adapters.StoryListAdapter
import com.robertas.storyapp.databinding.FragmentHomeBinding
import com.robertas.storyapp.databinding.StoryCardBinding
import com.robertas.storyapp.models.domain.Story
import com.robertas.storyapp.viewmodels.StoryViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


@AndroidEntryPoint
class HomeFragment : Fragment(), View.OnClickListener,
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


        val storyStatusObserver = Observer<Boolean> { invalid ->
            if (invalid) {
                storyListAdapter.refresh()

                storyViewModel.validateStories()
            }
        }

        storyViewModel.isStoriesInvalid.observe(viewLifecycleOwner, storyStatusObserver)

        binding?.storyList?.apply {
            adapter = storyListAdapter.withLoadStateFooter(
                footer = LoadingStateAdapter {
                    storyListAdapter.retry()
                }
            )

            postponeEnterTransition()

            viewTreeObserver
                .addOnPreDrawListener {
                    startPostponedEnterTransition()
                    true
                }
        }

        lifecycleScope.launch {
            storyListAdapter.loadStateFlow.collectLatest { loadStates ->
                swipeRefreshLayout?.isRefreshing = loadStates.refresh is LoadState.Loading
            }
        }

        lifecycleScope.launch {
            storyViewModel.getPaginatedStories().collectLatest {
                storyListAdapter.submitData(lifecycle, it)
            }
        }

        swipeRefreshLayout?.setOnRefreshListener {
            storyListAdapter.refresh()
        }
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