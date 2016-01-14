package io.rong.app.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import io.rong.app.R;
import io.rong.app.server.SealAction;
import io.rong.app.server.network.async.AsyncTaskManager;
import io.rong.app.server.network.async.OnDataListener;
import io.rong.app.server.network.http.HttpException;
import io.rong.app.server.utils.NToast;

public abstract class BaseActivity extends ActionBarActivity implements OnDataListener{

    protected Context mContext;
    private AsyncTaskManager mAsyncTaskManager;
    protected SealAction action;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setVolumeControlStream(AudioManager.STREAM_MUSIC);// 使得音量键控制媒体声音
        getSupportActionBar().setLogo(R.drawable.de_bar_logo);//actionbar 添加logo
        mContext = this;

        mAsyncTaskManager = AsyncTaskManager.getInstance(mContext);
        // Activity管理
        action = new SealAction(mContext);

    }

    protected void onResume() {
        super.onResume();
//        MobclickAgent.onResume(this);
    }

    protected void onPause() {
        super.onPause();
//        MobclickAgent.onPause(this);
    }


    /**
     * 发送请求（需要检查网络）
     *
     * @param requsetCode 请求码
     */
    public void request(int requsetCode) {
        if (mAsyncTaskManager != null) {
            mAsyncTaskManager.request(requsetCode, this);
        }
    }

    /**
     * 发送请求
     *
     * @param requsetCode    请求码
     * @param isCheckNetwork 是否需检查网络，true检查，false不检查
     */
    public void request(int requsetCode, boolean isCheckNetwork) {
        if (mAsyncTaskManager != null) {
            mAsyncTaskManager.request(requsetCode, isCheckNetwork, this);
        }
    }

    /**
     * 取消所有请求
     */
    public void cancelRequest() {
        if (mAsyncTaskManager != null) {
            mAsyncTaskManager.cancelRequest();
        }
    }

    @Override
    public Object doInBackground(int requestCode) throws HttpException {
        return null;
    }

    @Override
    public void onSuccess(int requestCode, Object result) {

    }

    @Override
    public void onFailure(int requestCode, int state, Object result) {
        switch (state) {
            // 网络不可用给出提示
            case AsyncTaskManager.HTTP_NULL_CODE:
                NToast.shortToast(mContext, "当前网络不可用");
                break;

            // 网络有问题给出提示
            case AsyncTaskManager.HTTP_ERROR_CODE:
                NToast.shortToast(mContext, "网络问题请稍后重试");
                break;

            // 请求有问题给出提示
            case AsyncTaskManager.REQUEST_ERROR_CODE:
                // NToast.shortToast(mContext, R.string.common_request_error);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }



}
