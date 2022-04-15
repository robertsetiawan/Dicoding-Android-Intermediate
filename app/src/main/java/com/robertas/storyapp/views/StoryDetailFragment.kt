package com.robertas.storyapp.views

import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.navigation.NavArgs
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

        startEnterTransition()

        postponeEnterTransition()

        setupNavigation()

        bindParamToFragment()
    }

    private fun bindParamToFragment() {
        val story = arg.story

        binding?.apply {

            storyImg.transitionName = "picture_${story.id}"

            nameTv.transitionName = "name_${story.id}"

            fullDescTv.transitionName = "desc_${story.id}"

            timeTv.transitionName = "time_${story.id}"

            loadImage(storyImg, story.photoUrl)

            nameTv.text = story.name

            fullDescTv.text = story.description

            val parsedDate = parseTime(story.createdAt)

            parsedDate?.time?.let { timeTv.text = formatTime(it, DATETIME_UI_FORMAT) }
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

        binding?.toolbarFragment?.apply {
            setupWithNavController(navController, appBarConfiguration)

            setNavigationOnClickListener { navController.navigateUp() }
        }
    }

    private fun startEnterTransition(){
        sharedElementEnterTransition = TransitionInflater.from(requireContext())
            .inflateTransition(android.R.transition.move)
    }
}