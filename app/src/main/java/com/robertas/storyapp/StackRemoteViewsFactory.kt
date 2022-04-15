package com.robertas.storyapp

import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import androidx.core.os.bundleOf
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.robertas.storyapp.abstractions.StoryRepository
import com.robertas.storyapp.models.domain.Story
import kotlinx.coroutines.runBlocking


class StackRemoteViewsFactory(private val context: Context, private val storyRepository: StoryRepository): RemoteViewsService.RemoteViewsFactory {

    private val listStory = ArrayList<Story>()

    override fun onCreate(){}

    override fun onDataSetChanged() {
        runBlocking {
            try {
                val storyList = storyRepository.getAllStories()

                storyList?.let {
                    listStory.clear()

                    listStory.addAll(it)
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