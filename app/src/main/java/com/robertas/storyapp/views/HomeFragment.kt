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
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.snackbar.Snackbar
import com.robertas.storyapp.R
import com.robertas.storyapp.abstractions.IOnItemClickListener
import com.robertas.storyapp.adapters.LoadingStateAdapter
import com.robertas.storyapp.adapters.StoryListAdapter
import com.robertas.storyapp.databinding.FragmentHomeBinding
import com.robertas.storyapp.databinding.StoryCardBinding
import com.robertas.storyapp.models.domain.Story
import com.robertas.storyapp.utils.EspressoIdlingResource
import com.robertas.storyapp.utils.wrapEspressoIdlingResource
import com.robertas.storyapp.viewmodels.StoryViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


@AndroidEntryPoint
class HomeFragment : Fragment(), View.OnClickListener,
    Toolbar.OnMenuItemClickListener {

    private var _binding: FragmentHomeBinding? = null

    private val binding get() = _binding

    private lateinit var navController: NavController

    private val storyViewModel by activityViewModels<StoryViewModel>()

    private lateinit var storyListAdapter: StoryListAdapter

    private var swipeRefreshLayout: SwipeRefreshLayout? = null

    private var storyList: RecyclerView? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        storyList = binding?.storyList

        setupNavigation()

        setupSwipeRefreshLayout()

        setupRecyclerView()

        loadStories()

        binding?.floatingBtn?.setOnClickListener(this)
    }

    private fun loadStories() {

        storyViewModel.isStoriesInvalid.observe(viewLifecycleOwner) { isInvalid ->
            if (isInvalid) {

                storyListAdapter.refresh()

                storyViewModel.validateStories()
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            storyViewModel.getPaginatedData().observe(viewLifecycleOwner) { pagingData ->
                storyListAdapter.submitData(viewLifecycleOwner.lifecycle, pagingData)
            }
        }
    }

    private fun showSnackBar(message: String){
        binding?.root?.let {
            Snackbar.make(it, message, Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun setupSwipeRefreshLayout() {

        swipeRefreshLayout = binding?.swipeRefresh

        swipeRefreshLayout?.setOnRefreshListener {
            storyListAdapter.refresh()
        }
    }

    private fun setupRecyclerView() {

        storyListAdapter = StoryListAdapter()

        val linearLayoutManager = LinearLayoutManager(requireContext())

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

        viewLifecycleOwner.lifecycleScope.launch {

                storyListAdapter.loadStateFlow.collect { loadStates ->

                    swipeRefreshLayout?.isRefreshing = loadStates.refresh is LoadState.Loading

                    if (loadStates.refresh is LoadState.NotLoading && loadStates.prepend is LoadState.Loading) {
                        storyList?.scrollToPosition(0)
                    }

                    val showEmptyList = (loadStates.mediator?.refresh is LoadState.Error && storyListAdapter.itemCount == 0) || (loadStates.mediator?.refresh is LoadState.NotLoading && storyListAdapter.itemCount == 0)

                    binding?.emptyLayout?.isVisible = showEmptyList

                    storyList?.isVisible = !showEmptyList

                    val errorState = loadStates.source.append as? LoadState.Error
                        ?: loadStates.source.prepend as? LoadState.Error
                        ?: loadStates.append as? LoadState.Error
                        ?: loadStates.prepend as? LoadState.Error

                    errorState?.let { state ->

                        showSnackBar(state.error.toString())
                    }
                }
        }

        storyList?.apply {
            adapter = storyListAdapter.withLoadStateFooter(
                footer = LoadingStateAdapter {
                    storyListAdapter.retry()
                }
            )

            layoutManager = linearLayoutManager

            postponeEnterTransition()

            viewTreeObserver
                .addOnPreDrawListener {
                    startPostponedEnterTransition()
                    true
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

        storyList = null
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