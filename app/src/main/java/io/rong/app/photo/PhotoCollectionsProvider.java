package io.rong.app.photo;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.View;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

import io.rong.imkit.R;
import io.rong.imkit.RLog;
import io.rong.imkit.RongContext;
import io.rong.imkit.RongIM;
import io.rong.imkit.widget.provider.InputProvider;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Message;
import io.rong.message.ImageMessage;

public class PhotoCollectionsProvider extends InputProvider.ExtendProvider {
    private RongContext mContext;

    public PhotoCollectionsProvider(RongContext context) {
        super(context);
        this.mContext = context;
    }

    @Override
    public Drawable obtainPluginDrawable(Context arg0) {
        // TODO Auto-generated method stub
        return arg0.getResources().getDrawable(R.drawable.rc_ic_picture);
    }

    @Override
    public CharSequence obtainPluginTitle(Context arg0) {

        return arg0.getString(io.rong.app.R.string.de_plugins_image);
    }

    @Override
    public void onPluginClick(View arg0) {
        // TODO Auto-generated method stub
        // 点击跳转至图片选择界面

        Intent intent = new Intent(mContext, ChoosePictureActivity.class);
        intent.putExtra("max", 9);
        startActivityForResult(intent, 86);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        // 根据选择完毕的图片返回值，直接上传文件
        if (requestCode == 86 && data != null) {

            ArrayList<String> pathList = data.getStringArrayListExtra("data");
            if (pathList != null && pathList.size() > 0) {
                int intSize = pathList.size();
                for (int i = 0; i <= intSize - 1; i++) {
                    String localStrPath = pathList.get(i);
                    localStrPath = "file://" + localStrPath;
                    Uri pathUri = Uri.parse(localStrPath);
                    getContext().executorBackground(new AttachRunnable(pathUri));
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 用于显示文件的异步线程
     *
     * @ClassName: MyRunnable
     * @Description: 用于显示文件的异步线程
     *
     */
    class AttachRunnable implements Runnable {

        Uri mUri;

        public AttachRunnable(Uri uri) {
            mUri = uri;
        }

        @Override
        public void run() {
            RLog.d(this, "AttachRunnable", "insert image and save to db, uri = " + mUri);
            final ImageMessage content = ImageMessage.obtain(mUri, mUri);
            if(RongIM.getInstance() != null && RongIM.getInstance().getRongIMClient() != null);
            RongIM.getInstance().getRongIMClient().insertMessage(getCurrentConversation().getConversationType(), getCurrentConversation().getTargetId(), null, content, new RongIMClient.ResultCallback<Message>() {
                @Override
                public void onSuccess(Message message) {
                    RLog.d(this, "AttachRunnable", "onSuccess insert image");
                    message.setSentStatus(Message.SentStatus.SENDING);
                    RongIM.getInstance().getRongIMClient().setMessageSentStatus(message.getMessageId(), Message.SentStatus.SENDING, null);
                    getContext().executorBackground(new UploadRunnable(message));
                }

                @Override
                public void onError(RongIMClient.ErrorCode e) {
                    RLog.d(this, "AttachRunnable", "onError insert image, error = " + e);
                }
            });
        }
    }

    class UploadRunnable implements Runnable {
        Message msg;
        CountDownLatch mLock;

        public UploadRunnable(Message msg) {
            this.msg = msg;
        }

        @Override
        public void run() {
            mLock = new CountDownLatch(1);
            RLog.d(this, "UploadRunnable", "sendImageMessage");
            RongIM.getInstance().getRongIMClient().sendImageMessage(msg, null, null, new RongIMClient.SendImageMessageCallback() {
                @Override
                public void onAttached(Message message) {
                    mLock.countDown();
                }

                @Override
                public void onError(Message message, RongIMClient.ErrorCode code) {
                    mLock.countDown();
                }

                @Override
                public void onSuccess(Message message) {
                    mLock.countDown();
                }

                @Override
                public void onProgress(Message message, int progress) {

                }
            });
            try {
                mLock.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


}
