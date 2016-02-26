package io.rong.app.ui.activity;


import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import io.rong.app.App;
import io.rong.app.R;
import io.rong.app.server.network.http.HttpException;
import io.rong.app.server.response.ChangePasswordResponse;
import io.rong.app.server.response.SetNameResponse;
import io.rong.app.server.response.SetPortraitResponse;
import io.rong.app.server.utils.NLog;
import io.rong.app.server.utils.NToast;
import io.rong.app.server.utils.photo.PhotoUtils;
import io.rong.app.server.widget.BottomMenuDialog;
import io.rong.app.server.widget.DialogWithYesOrNoUtils;
import io.rong.app.server.widget.LoadDialog;
import io.rong.imkit.RongIM;
import io.rong.imkit.widget.AsyncImageView;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Message;
import io.rong.imlib.model.UserInfo;
import io.rong.message.ImageMessage;

/**
 * Created by Administrator on 2015/3/2.
 */
public class MyAccountActivity extends BaseActionBarActivity implements View.OnClickListener {

    private static final int RESULTCODE = 10;
    private static final int UPDATENAME = 7;
    private static final int UPLOADPORTRAIT = 8;
    private static final int UPDATEPASSWORD = 15;

    private RelativeLayout portraitItem, nameItem, passwordItem;

    private SharedPreferences sp;

    private SharedPreferences.Editor editor;

    private AsyncImageView mImageView;

    private TextView mName;
    private String newName;

    private String portraitUrl;
    private PhotoUtils photoUtils;
    private BottomMenuDialog dialog;
    private String mOldPassword, mNewPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.de_ac_myaccount);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.de_actionbar_back);
        getSupportActionBar().setTitle(R.string.de_actionbar_myacc);
        sp = getSharedPreferences("config", MODE_PRIVATE);
        editor = sp.edit();

        initView();


    }

    private void initView() {
        portraitItem = (RelativeLayout) findViewById(R.id.rl_my_portrait);
        nameItem = (RelativeLayout) findViewById(R.id.rl_my_username);
        passwordItem = (RelativeLayout) findViewById(R.id.rl_my_password);
        mImageView = (AsyncImageView) findViewById(R.id.img_my_portrait);
        mName = (TextView) findViewById(R.id.tv_my_username);
        portraitItem.setOnClickListener(this);
        nameItem.setOnClickListener(this);
        passwordItem.setOnClickListener(this);
        String cacheName = sp.getString("loginnickname", "");
        String cachePortrait = sp.getString("loginPortrait", "");
        if (!TextUtils.isEmpty(cacheName)) {
            mName.setText(cacheName);
            ImageLoader.getInstance().displayImage(cachePortrait, mImageView, App.getOptions());
        }
        setPortraitChangeListener();
    }

    private void setPortraitChangeListener() {
        photoUtils = new PhotoUtils(new PhotoUtils.OnPhotoResultListener() {
            @Override
            public void onPhotoResult(Uri uri) {
                if (uri != null && !TextUtils.isEmpty(uri.getPath())) {
                    LoadDialog.show(mContext);
                    final ImageMessage imageMessage = ImageMessage.obtain();
                    imageMessage.setLocalUri(uri);
                    Message message = Message.obtain("uuuuuuuTest", Conversation.ConversationType.PRIVATE, imageMessage);
                    if (RongIM.getInstance().getRongIMClient() != null) {
                        RongIMClient.getInstance().sendImageMessage(message, null, null, new RongIMClient.SendImageMessageCallback() {
                            @Override
                            public void onAttached(Message message) {

                            }

                            @Override
                            public void onError(Message message, RongIMClient.ErrorCode errorCode) {
                                NLog.e("imageMessage", errorCode.getMessage() + errorCode.getValue());
                            }

                            @Override
                            public void onSuccess(Message message) {
                                if (message.getContent() instanceof ImageMessage) {
                                    ImageMessage imsg = (ImageMessage) message.getContent();
                                    NLog.e("imageMessage", "imsg.getRemoteUri()" + imsg.getRemoteUri());
                                    portraitUrl = imsg.getRemoteUri().toString();
                                    if (!TextUtils.isEmpty(portraitUrl)) {
                                        request(UPLOADPORTRAIT);
                                    } else {
                                        NLog.e("imageMessage", "返回远程头像路径为空");
                                    }


                                }
                            }

                            @Override
                            public void onProgress(Message message, int i) {

                            }
                        });
                    }


                }
            }

            @Override
            public void onPhotoCancel() {

            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rl_my_portrait:
                showPhotoDialog();
                break;

            case R.id.rl_my_username:
                DialogWithYesOrNoUtils.getInstance().showEditDialog(mContext, "新昵称", "确认修改", new DialogWithYesOrNoUtils.DialogCallBack() {
                    @Override
                    public void exectEvent() {

                    }

                    @Override
                    public void exectEditEvent(String editText) {
                        if (!TextUtils.isEmpty(editText)) {
                            newName = editText;
                            LoadDialog.show(mContext);
                            request(UPDATENAME, true);
                        } else {
                            NToast.shortToast(mContext, "昵称不能为空");
                        }
                    }

                    @Override
                    public void updatePassword(String oldPassword, String newPassword) {

                    }
                });
                break;
            case R.id.rl_my_password:
                DialogWithYesOrNoUtils.getInstance().showUpdatePasswordDialog(mContext, new DialogWithYesOrNoUtils.DialogCallBack() {
                    @Override
                    public void exectEvent() {

                    }

                    @Override
                    public void exectEditEvent(String editText) {

                    }

                    @Override
                    public void updatePassword(String oldPassword, String newPassword) {
                        mOldPassword = oldPassword;
                        mNewPassword = newPassword;
                        LoadDialog.show(mContext);
                        request(UPDATEPASSWORD, true);
                    }
                });
                break;
        }
    }


    @Override
    public Object doInBackground(int requestCode) throws HttpException {
        switch (requestCode) {
            case UPDATENAME:
                return action.setName(newName);
            case UPLOADPORTRAIT:
                return action.setPortrait(portraitUrl);
            case UPDATEPASSWORD:
                return action.changePassword(mOldPassword, mNewPassword);
        }
        return super.doInBackground(requestCode);
    }

    @Override
    public void onSuccess(int requestCode, Object result) {
        if (result != null) {
            switch (requestCode) {
                case UPDATENAME:
                    SetNameResponse sRes = (SetNameResponse) result;
                    if (sRes.getCode() == 200) {
                        editor.putString("loginnickname", newName);
                        editor.commit();
                        mName.setText(newName);

                        if (RongIM.getInstance() != null) {
                            RongIM.getInstance().refreshUserInfoCache(new UserInfo(sp.getString("loginid", ""), newName, Uri.parse(sp.getString("loginPortrait", ""))));
                            RongIM.getInstance().setCurrentUserInfo(new UserInfo(sp.getString("loginid", ""), newName, Uri.parse(sp.getString("loginPortrait", ""))));
                        }

                        LoadDialog.dismiss(mContext);
                        NToast.shortToast(mContext, "昵称更改成功");
                    }
                    break;
                case UPLOADPORTRAIT:
                    SetPortraitResponse spRes = (SetPortraitResponse) result;
                    if (spRes.getCode() == 200) {
                        editor.putString("loginPortrait", portraitUrl);
                        editor.commit();
                        ImageLoader.getInstance().displayImage(portraitUrl, mImageView, App.getOptions());

                        RongIM.getInstance().getRongIMClient().removeConversation(Conversation.ConversationType.PRIVATE, "uuuuuuuTest");
                        RongIM.getInstance().getRongIMClient().clearMessages(Conversation.ConversationType.PRIVATE, "uuuuuuuTest");

                        if (RongIM.getInstance() != null) {
                            RongIM.getInstance().refreshUserInfoCache(new UserInfo(sp.getString("loginid", ""), sp.getString("loginnickname", ""), Uri.parse(portraitUrl)));
                            RongIM.getInstance().setCurrentUserInfo(new UserInfo(sp.getString("loginid", ""), sp.getString("loginnickname", ""), Uri.parse(portraitUrl)));
                        }

                        NToast.shortToast(mContext, "头像更新成功");
                        LoadDialog.dismiss(mContext);
                    }
                    break;
                case UPDATEPASSWORD:
                    ChangePasswordResponse cpRes = (ChangePasswordResponse) result;
                    if (cpRes.getCode() == 200) {
                        NToast.shortToast(mContext, "修改成功");
                        editor.putString("loginpassword", mNewPassword);
                        editor.commit();
                        LoadDialog.dismiss(mContext);
                    } else if (cpRes.getCode() == 1000) {
                        NToast.shortToast(mContext, "初始密码有误:" + cpRes.getCode());
                        LoadDialog.dismiss(mContext);
                    } else {
                        NToast.shortToast(mContext, "修改密码失败:" + cpRes.getCode());
                        LoadDialog.dismiss(mContext);
                    }
                    break;
            }
        }
    }

    @Override
    public void onFailure(int requestCode, int state, Object result) {
        switch (requestCode) {
            case UPDATENAME:
                LoadDialog.dismiss(mContext);

                NToast.shortToast(mContext, "更名请求失败");
                break;
            case UPLOADPORTRAIT:
                NToast.shortToast(mContext, "设置头像请求失败");
                LoadDialog.dismiss(mContext);
                break;
            case UPDATEPASSWORD:
                NToast.shortToast(mContext, "修改密码请求失败");
                break;
        }
    }


    /**
     * 弹出底部框
     */
    private void showPhotoDialog() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }

        dialog = new BottomMenuDialog(mContext);
        dialog.setConfirmListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (dialog != null && dialog.isShowing()) {
                    dialog.dismiss();
                }
                photoUtils.takePicture(MyAccountActivity.this);
            }
        });
        dialog.setMiddleListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (dialog != null && dialog.isShowing()) {
                    dialog.dismiss();
                }
                photoUtils.selectPicture(MyAccountActivity.this);
            }
        });
        dialog.show();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case PhotoUtils.INTENT_CROP:
            case PhotoUtils.INTENT_TAKE:
            case PhotoUtils.INTENT_SELECT:
                photoUtils.onActivityResult(MyAccountActivity.this, requestCode, resultCode, data);
                break;
        }
    }
}
