package com.robertas.storyapp.views

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavArgs
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import com.robertas.storyapp.R
import com.robertas.storyapp.databinding.FragmentStoryDetailBinding
import com.robertas.storyapp.utils.DATETIME_UI_FORMAT
import com.robertas.storyapp.utils.formatTime
import com.robertas.storyapp.utils.parseTime

class StoryDetailFragment : Fragment() {

    private var _binding: FragmentStoryDetailBinding? = null

    private val binding get() = _binding

    private lateinit var navController: NavController

    private val arg by navArgs<StoryDetailFragmentArgs>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentStoryDetailBinding.inflate(inflater, container, false)

        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupNavigation()

        bindParamToFragment()
    }

    private fun bindParamToFragment() {
        val story = arg.story

        binding?.apply {

            Glide.with(requireContext())
                .load(story.photoUrl)
                .fitCenter()
                .placeholder(R.drawable.ic_baseline_image)
                .into(storyImg)

            nameTv.text = story.name

            fullDescTv.text = story.description

            val parsedDate = parseTime(story.createdAt)

            parsedDate?.time?.let { timeTv.text = formatTime(it, DATETIME_UI_FORMAT) }
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
}