package com.ptech.foodbank.utils

import android.content.Context
import android.widget.ImageView
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.ImageRequest

/**
 * Coil image loading library utilities
 *
 * @see <a href="https://coil-kt.github.io/coil/getting_started/">Coil</a>
 */
object Coil {
    fun imageLoader(context: Context): ImageLoader {
        return ImageLoader.Builder(context)
            .memoryCache {
                MemoryCache.Builder(context)
                    .maxSizePercent(0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(context.cacheDir.resolve("image_cache"))
                    .maxSizePercent(0.05)
                    .build()
            }
            .build()
    }

    fun imageRequest(image: String, target: ImageView, context: Context): ImageRequest {
        return ImageRequest.Builder(context)
            .data(image)
            .target(target)
            .crossfade(true)
            .build()
    }
}
