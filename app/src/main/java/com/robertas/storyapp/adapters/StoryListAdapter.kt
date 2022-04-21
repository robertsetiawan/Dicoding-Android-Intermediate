package com.robertas.storyapp.adapters

import android.transition.AutoTransition
import android.transition.TransitionManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.robertas.storyapp.R
import com.robertas.storyapp.abstractions.IOnItemClickListener
import com.robertas.storyapp.databinding.StoryCardBinding
import com.robertas.storyapp.models.domain.Story

class StoryListAdapter : PagingDataAdapter<Story, StoryListAdapter.ViewHolder>(DIFF_CALLBACK) {

    lateinit var onItemClickListener: IOnItemClickListener<Story, StoryCardBinding>

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding: StoryCardBinding =
            StoryCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val story = getItem(position)

        story?.let { holder.bind(it) }
    }

    inner class ViewHolder(private val binding: StoryCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private fun showExpandedLayout(story: Story){
            binding.fullDescTv.text = story.description

            binding.moreTv.visibility = View.GONE

            binding.smallDescTv.visibility = View.GONE

            binding.expandedLayout.visibility = View.VISIBLE
        }

        private fun hideExpandedLayout(){
            binding.expandedLayout.visibility = View.GONE

            binding.moreTv.visibility = View.VISIBLE

            binding.smallDescTv.visibility = View.VISIBLE
        }

        private fun setTransitionName(story: Story){
            binding.apply {
                storyImg.transitionName = "picture_${story.id}"

                nameTv.transitionName = "name_${story.id}"

                smallDescTv.transitionName = "desc_${story.id}"

                timeTv.transitionName = "time_${story.id}"
            }
        }

        fun bind(story: Story) {

            setTransitionName(story)

            binding.storyImg.contentDescription = story.description

            binding.storyImg.setOnClickListener{ onItemClickListener.onClick(story, binding) }

            Glide.with(itemView.context)
                .load(story.photoUrl)
                .fitCenter()
                .placeholder(R.drawable.ic_baseline_image)
                .into(binding.storyImg)

            binding.nameTv.text = story.name

            binding.expandedLayout.visibility = View.GONE

            binding.timeTv.text = story.createdAt

            if (story.description.length > 50) {

                binding.smallDescTv.text = story.description.substring(0, 14)

                binding.moreTv.visibility = View.VISIBLE

                binding.smallDescTv.visibility = View.VISIBLE

                binding.moreTv.setOnClickListener {
                    if (binding.expandedLayout.visibility == View.VISIBLE) {
                        TransitionManager.beginDelayedTransition(
                            binding.storyCard,
                            AutoTransition()
                        )

                        hideExpandedLayout()

                    } else {
                        TransitionManager.beginDelayedTransition(
                            binding.storyCard,
                            AutoTransition()
                        )

                        showExpandedLayout(story)
                    }
                }
            } else {

                binding.smallDescTv.visibility = View.VISIBLE

                binding.smallDescTv.text = story.description

                binding.moreTv.visibility = View.GONE

                binding.expandedLayout.visibility = View.GONE
            }
        }
    }


    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Story>() {
            override fun areItemsTheSame(oldItem: Story, newItem: Story) = oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: Story, newItem: Story) = oldItem == newItem
        }
    }
}