package com.rohith.vsa_kotlin.network

import android.content.Context
import com.google.android.exoplayer2.database.ExoDatabaseProvider
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.google.android.exoplayer2.upstream.FileDataSource
import com.google.android.exoplayer2.upstream.cache.CacheDataSink
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import com.google.android.exoplayer2.util.Util
import java.io.File


internal class CacheDataSourceFactory (
    private val context: Context,
    private val maxCacheSize: Long,
    private val maxFileSize: Long
) :
    DataSource.Factory {

    companion object {
        var simpleCache : SimpleCache? = null
    }

    private val defaultDataSourceFactory: DefaultHttpDataSourceFactory =
        DefaultHttpDataSourceFactory(Util.getUserAgent(context, "VisualShare"))

    override fun createDataSource(): DataSource {
        val evictor = LeastRecentlyUsedCacheEvictor(maxCacheSize)
        simpleCache = simpleCache
            ?: SimpleCache(File(context.cacheDir, "media"), evictor, ExoDatabaseProvider(context))
        return CacheDataSource(
            simpleCache, defaultDataSourceFactory.createDataSource(),
            FileDataSource(), CacheDataSink(simpleCache, maxFileSize),
            CacheDataSource.FLAG_BLOCK_ON_CACHE or CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR, null
        )
    }

}