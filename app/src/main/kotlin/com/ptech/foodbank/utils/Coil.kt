package com.ptech.foodbank.utils

import android.content.Context
import android.widget.ImageView
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.ImageRequest

const val MEMORY_CACHE = 0.25
const val DISK_CACHE = 0.05

/**
 * Coil image loading library utilities
 *
 * @see <a href="https://coil-kt.github.io/coil/getting_started/">Coil</a>
 */
internal object Coil {
    fun imageLoader(context: Context): ImageLoader {
        return ImageLoader.Builder(context)
            .memoryCache {
                MemoryCache.Builder(context)
                    .maxSizePercent(MEMORY_CACHE)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(context.cacheDir.resolve("image_cache"))
                    .maxSizePercent(DISK_CACHE)
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
