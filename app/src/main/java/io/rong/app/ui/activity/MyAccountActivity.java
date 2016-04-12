package io.rong.app.ui.activity;


import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.qiniu.android.http.ResponseInfo;
import com.qiniu.android.storage.UpCompletionHandler;
import com.qiniu.android.storage.UploadManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

import io.rong.app.App;
import io.rong.app.R;
import io.rong.app.server.network.http.HttpException;
import io.rong.app.server.response.ChangePasswordResponse;
import io.rong.app.server.response.QiNiuTokenResponse;
import io.rong.app.server.response.SetNameResponse;
import io.rong.app.server.response.SetPortraitResponse;
import io.rong.app.server.utils.AMGenerate;
import io.rong.app.server.utils.NToast;
import io.rong.app.server.utils.photo.PhotoUtils;
import io.rong.app.server.widget.BottomMenuDialog;
import io.rong.app.server.widget.DialogWithYesOrNoUtils;
import io.rong.app.server.widget.LoadDialog;
import io.rong.imkit.RongIM;
import io.rong.imkit.widget.AsyncImageView;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.UserInfo;

/**
 * Created by Administrator on 2015/3/2.
 */
public class MyAccountActivity extends BaseActionBarActivity implements View.OnClickListener {

    private static final int UPDATENAME = 7;
    private static final int UPLOADPORTRAIT = 8;
    private static final int UPDATEPASSWORD = 15;

    private RelativeLayout portraitItem, nameItem, passwordItem;

    private SharedPreferences sp;

    private SharedPreferences.Editor editor;

    private AsyncImageView mImageView;

    private TextView mName ,mPhone;
    private String newName;

    private PhotoUtils photoUtils;
    private BottomMenuDialog dialog;
    private String mOldPassword, mNewPassword;

    private UploadManager uploadManager;

    private String imageUrl;

    private Uri selectUri;



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
        mPhone = (TextView) findViewById(R.id.tv_my_phone);
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
        String cachePhone = sp.getString("loginphone","");
        if (!TextUtils.isEmpty(cachePhone)) {
            mPhone.setText("+86 "+cachePhone);
        }
        if (!TextUtils.isEmpty(cacheName)) {
            mName.setText(cacheName);
            if (TextUtils.isEmpty(cachePortrait)) {
                ImageLoader.getInstance().displayImage(AMGenerate.generateDefaultAvatar(cacheName,sp.getString("loginid","a")), mImageView, App.getOptions());
            }else {
                ImageLoader.getInstance().displayImage(cachePortrait, mImageView, App.getOptions());
            }
        }
        setPortraitChangeListener();
    }

    private void setPortraitChangeListener() {
        photoUtils = new PhotoUtils(new PhotoUtils.OnPhotoResultListener() {
            @Override
            public void onPhotoResult(Uri uri) {
                if (uri != null && !TextUtils.isEmpty(uri.getPath())) {
                    selectUri = uri;
                    LoadDialog.show(mContext);
                    request(128);
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
    public Object doInBackground(int requestCode, String id) throws HttpException {
        switch (requestCode) {
            case UPDATENAME:
                return action.setName(newName);
            case UPLOADPORTRAIT:
                return action.setPortrait(imageUrl);
            case UPDATEPASSWORD:
                return action.changePassword(mOldPassword, mNewPassword);
            case 128:
                return action.getQiNiuToken();
        }
        return super.doInBackground(requestCode, id);
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
                        editor.putString("loginPortrait", imageUrl);
                        editor.commit();
                        ImageLoader.getInstance().displayImage(imageUrl, mImageView, App.getOptions());

                        RongIM.getInstance().getRongIMClient().removeConversation(Conversation.ConversationType.PRIVATE, "uuuuuuuTest");
                        RongIM.getInstance().getRongIMClient().clearMessages(Conversation.ConversationType.PRIVATE, "uuuuuuuTest");

                        if (RongIM.getInstance() != null) {
                            RongIM.getInstance().refreshUserInfoCache(new UserInfo(sp.getString("loginid", ""), sp.getString("loginnickname", ""), Uri.parse(imageUrl)));
                            RongIM.getInstance().setCurrentUserInfo(new UserInfo(sp.getString("loginid", ""), sp.getString("loginnickname", ""), Uri.parse(imageUrl)));
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
                case 128:
                    QiNiuTokenResponse response = (QiNiuTokenResponse) result;
                    if (response.getCode() == 200) {
                        uploadImage(response.getResult().getDomain(), response.getResult().getToken(), selectUri);
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

    public void onTestClick(View v) {
        //TODO test use
    }




    public void uploadImage(final String domain, String imageToken, Uri imagePath) {
        if (TextUtils.isEmpty(domain) && TextUtils.isEmpty(imageToken) && TextUtils.isEmpty(imagePath.toString())) {
            throw new RuntimeException("upload parameter is null!");
        }
        File imageFile = new File(imagePath.getPath());

        if (this.uploadManager == null) {
            this.uploadManager = new UploadManager();
        }
        this.uploadManager.put(imageFile, null, imageToken, new UpCompletionHandler() {

            @Override
            public void complete(String s, ResponseInfo responseInfo, JSONObject jsonObject) {
                if (responseInfo.isOK()) {
                    try {
                        String key = (String) jsonObject.get("key");
                        imageUrl = "http://" + domain + "/" + key;
                        Log.e("uploadImage", imageUrl);
                        if (!TextUtils.isEmpty(imageUrl)) {
                            request(UPLOADPORTRAIT);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, null);
    }
}
