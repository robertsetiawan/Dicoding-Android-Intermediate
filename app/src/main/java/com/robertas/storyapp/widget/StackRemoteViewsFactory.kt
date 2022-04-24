package com.robertas.storyapp.widget

import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import androidx.core.os.bundleOf
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.robertas.storyapp.R
import com.robertas.storyapp.abstractions.StoryRepository
import com.robertas.storyapp.abstractions.UserRepository
import com.robertas.storyapp.models.domain.Story
import com.robertas.storyapp.models.enums.NetworkResult
import kotlinx.coroutines.runBlocking


class StackRemoteViewsFactory(private val context: Context,
                              private val userAccountRepository: UserRepository,
                              private val storyRepository: StoryRepository): RemoteViewsService.RemoteViewsFactory {

    private val listStory = ArrayList<Story>()

    override fun onCreate(){}

    override fun onDataSetChanged() {
        runBlocking {
            try {
                val storyList = storyRepository.getAllStories(userAccountRepository.getBearerToken(), false)

                storyList.collect { result ->

                    when(result){
                        is NetworkResult.Loading -> listStory.clear()

                        is NetworkResult.Success -> listStory.addAll(result.data)

                        is NetworkResult.Error -> {}
                    }
                }

            } catch (e: Exception){
                e.printStackTrace()
            }
        }
    }

    override fun onDestroy() {}

    override fun getCount() = listStory.size

    override fun getViewAt(pos: Int): RemoteViews {
        val rv = RemoteViews(context.packageName, R.layout.widget_item)

        val bitmap = Glide.with(context)
            .asBitmap()
            .load(listStory[pos].photoUrl)
            .apply(RequestOptions().centerCrop())
            .submit()
            .get()

        rv.setImageViewBitmap(R.id.imageView, bitmap)

        val extras = bundleOf(
            StackStoryWidget.EXTRA_ITEM to pos
        )

        val fillInIntent = Intent()

        fillInIntent.putExtras(extras)

        rv.setOnClickFillInIntent(R.id.imageView, fillInIntent)

        return rv
    }

    override fun getLoadingView(): RemoteViews? = null

    override fun getViewTypeCount(): Int = 1

    override fun getItemId(p0: Int): Long = 0L

    override fun hasStableIds(): Boolean = false
}