package com.windnsoft.tbus.Common;

import android.content.Context;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

/**
 * Created by Windn(Windnsoft@gmail.com) on 2016.
 */
public class ImageLoaderInit {
    public static void initIfNeeded(Context context) {
        ImageLoader imageLoader = ImageLoader.getInstance();
        if (imageLoader.isInited()) return;
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .resetViewBeforeLoading(true)
                .displayer(new FadeInBitmapDisplayer(500))
                .build();
        ImageLoaderConfiguration configuration = new ImageLoaderConfiguration.Builder(context)
                .defaultDisplayImageOptions(defaultOptions)
                .build();
        imageLoader.init(configuration);

    }

}
