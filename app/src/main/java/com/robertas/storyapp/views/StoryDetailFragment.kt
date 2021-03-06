package com.robertas.storyapp.views

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.transition.TransitionInflater
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.robertas.storyapp.R
import com.robertas.storyapp.databinding.FragmentStoryDetailBinding

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

        startEnterTransition()

        postponeEnterTransition()

        setupNavigation()

        bindParamToFragment()
    }

    private fun bindParamToFragment() {
        val story = arg.story

        binding?.apply {

            storyDetailImg.transitionName = "picture_${story.id}"

            storyDetailImg.contentDescription = story.description

            nameDetailTv.transitionName = "name_${story.id}"

            fullDescDetailTv.transitionName = "desc_${story.id}"

            timeDetailTv.transitionName = "time_${story.id}"

            loadImage(storyDetailImg, story.photoUrl)

            nameDetailTv.text = story.name

            fullDescDetailTv.text = story.description

            timeDetailTv.text = story.createdAt
        }
    }

    private fun loadImage(imageView: ImageView, url: String) {

        Glide.with(requireContext())
            .load(url)
            .dontAnimate()
            .listener(object: RequestListener<Drawable>{
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    startPostponedEnterTransition()
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    startPostponedEnterTransition()
                    return false
                }
            })
            .fitCenter()
            .placeholder(R.drawable.ic_baseline_image)
            .into(imageView)

    }

    private fun setupNavigation() {
        navController = findNavController()

        val appBarConfiguration =
            AppBarConfiguration(setOf(R.id.homeFragment))

        binding?.storyDetailToolbarFragment?.apply {
            setupWithNavController(navController, appBarConfiguration)

            setNavigationOnClickListener { navController.navigateUp() }
        }
    }

    private fun startEnterTransition(){
        sharedElementEnterTransition = TransitionInflater.from(requireContext())
            .inflateTransition(android.R.transition.move)
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }
}