package com.robertas.storyapp.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isVisible
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
import com.google.android.material.snackbar.Snackbar
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

                if (loadStates.refresh is LoadState.NotLoading && loadStates.prepend is LoadState.Loading) {
                    binding?.storyList?.scrollToPosition(0)
                }

                binding?.storyList?.isVisible =
                    loadStates.source.refresh is LoadState.NotLoading || loadStates.mediator?.refresh is LoadState.NotLoading

                binding?.emptyLayout?.isVisible =
                    loadStates.mediator?.refresh is LoadState.Error && storyListAdapter.itemCount == 0

                val errorState = loadStates.source.append as? LoadState.Error
                    ?: loadStates.source.prepend as? LoadState.Error
                    ?: loadStates.append as? LoadState.Error
                    ?: loadStates.prepend as? LoadState.Error

                errorState?.let { state ->

                    binding?.root?.let {
                        Snackbar.make(it, state.error.toString(), Snackbar.LENGTH_SHORT).show()
                    }
                }
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