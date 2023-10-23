package com.nacare.capture.data.service;

import android.content.Context;


import okhttp3.Interceptor;

public class FlipperManager {

    public static Interceptor setUp(Context appContext) {
     /*   if (BuildConfig.DEBUG && FlipperUtils.shouldEnableFlipper(appContext)) {
//            NetworkFlipperPlugin networkPlugin = new NetworkFlipperPlugin();
//            SoLoader.init(appContext, false);
//            FlipperClient client = AndroidFlipperClient.getInstance(appContext);
//            client.addPlugin(networkPlugin);
//            client.addPlugin(new DatabasesFlipperPlugin(appContext));
//            client.addPlugin(new InspectorFlipperPlugin(appContext, DescriptorMapping.withDefaults()));
//            client.start();
            return null;// new FlipperOkhttpInterceptor(networkPlugin);
        } else {*/
            return null;
        /*}*/
    }
}
