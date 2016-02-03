package io.rong.app;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;

import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

import io.rong.app.message.AgreedFriendRequestMessage;
import io.rong.app.message.provider.ContactNotificationMessageProvider;
import io.rong.app.message.provider.NewDiscussionConversationProvider;
import io.rong.app.message.provider.RealTimeLocationMessageProvider;
import io.rong.app.server.utils.NLog;
import io.rong.imkit.RongContext;
import io.rong.imkit.RongIM;
import io.rong.imlib.ipc.RongExceptionHandler;


/**
 * Created by bob on 2015/1/30.
 */
public class App extends Application {

    private static DisplayImageOptions options;

    @Override
    public void onCreate() {

        super.onCreate();
        /**
         * 注意：
         *
         * IMKit SDK调用第一步 初始化
         *
         * context上下文
         *
         * 只有两个进程需要初始化，主进程和 push 进程
         */
        if (getApplicationInfo().packageName.equals(getCurProcessName(getApplicationContext())) ||
                "io.rong.push".equals(getCurProcessName(getApplicationContext()))) {

            RongIM.init(this);
            NLog.setDebug(true); //打印参数
            /**
             * 融云SDK事件监听处理
             *
             * 注册相关代码，只需要在主进程里做。
             */
            if (getApplicationInfo().packageName.equals(getCurProcessName(getApplicationContext()))) {

                RongCloudEvent.init(this);
                DemoContext.init(this);
                Thread.setDefaultUncaughtExceptionHandler(new RongExceptionHandler(this));

                try {
                    RongIM.registerMessageType(AgreedFriendRequestMessage.class);

                    RongIM.registerMessageTemplate(new ContactNotificationMessageProvider());
                    RongIM.registerMessageTemplate(new RealTimeLocationMessageProvider());
                    //@ 消息模板展示
                    RongContext.getInstance().registerConversationTemplate(new NewDiscussionConversationProvider());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }


        options = new DisplayImageOptions.Builder()
                .showImageForEmptyUri(R.drawable.de_default_portrait)
                .showImageOnFail(R.drawable.de_default_portrait)
                .showImageOnLoading(R.drawable.de_default_portrait)
                .displayer(new FadeInBitmapDisplayer(300))
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .build();

        //初始化图片下载组件
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())
                .threadPriority(Thread.NORM_PRIORITY - 2)
                .denyCacheImageMultipleSizesInMemory()
                .diskCacheSize(50 * 1024 * 1024)
                .diskCacheFileCount(200)
                .diskCacheFileNameGenerator(new Md5FileNameGenerator())
                .defaultDisplayImageOptions(options)
                .build();

        //Initialize ImageLoader with configuration.
        ImageLoader.getInstance().init(config);
    }

    public static String getCurProcessName(Context context) {
        int pid = android.os.Process.myPid();
        ActivityManager activityManager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo appProcess : activityManager
                .getRunningAppProcesses()) {
            if (appProcess.pid == pid) {
                return appProcess.processName;
            }
        }
        return null;
    }



    public static DisplayImageOptions getOptions() {
        return options;
    }

    public static void setOptions(DisplayImageOptions options) {
        App.options = options;
    }
}
