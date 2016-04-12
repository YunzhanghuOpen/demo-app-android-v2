package io.rong.app.ui.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
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
import io.rong.app.RongCloudEvent;
import io.rong.app.db.Qun;
import io.rong.app.server.broadcast.BroadcastManager;
import io.rong.app.server.network.http.HttpException;
import io.rong.app.server.response.QiNiuTokenResponse;
import io.rong.app.server.response.SetGroupNameResponse;
import io.rong.app.server.response.SetGroupPortraitResponse;
import io.rong.app.server.utils.NToast;
import io.rong.app.server.utils.photo.PhotoUtils;
import io.rong.app.server.widget.BottomMenuDialog;
import io.rong.app.server.widget.DialogWithYesOrNoUtils;
import io.rong.app.server.widget.LoadDialog;

/**
 * Created by AMing on 16/1/28.
 * Company RongCloud
 */
public class ChangeGroupInfoActivity extends BaseActivity implements View.OnClickListener {

    private static final int UPDATEGROUPNAME = 24;
    private static final int UPDATEGROUPHEADER = 25;
    public static final java.lang.String UPDATEGROUPINFONAME = "updategroupinfoname";
    public static final java.lang.String UPDATEGROUPINFOIMG = "updategroupinfoimg";
    private Qun mGroup;

    private RelativeLayout updateHeader, updateName;

    private ImageView imageView;

    private TextView mGroupName;

    private String newGroupName;

    private PhotoUtils photoUtils;

    private BottomMenuDialog dialog;

    private UploadManager uploadManager;

    private String imageUrl;

    private Uri selectUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.de_ac_change_groupinfo);
        getSupportActionBar().setTitle("更改群组信息");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.de_actionbar_back);
        mGroup = (Qun) getIntent().getSerializableExtra("GroupInfo");
        initView();
        setPortraitChangeListener();
    }

    private void initView() {
        updateHeader = (RelativeLayout) findViewById(R.id.rl_change_portrait);
        updateName = (RelativeLayout) findViewById(R.id.rl_group_change_username);
        imageView = (ImageView) findViewById(R.id.change_group_portrait);
        mGroupName = (TextView) findViewById(R.id.tv_group_change_username);
        updateHeader.setOnClickListener(this);
        updateName.setOnClickListener(this);
        if (mGroup != null) {
            mGroupName.setText(mGroup.getName());
            ImageLoader.getInstance().displayImage(mGroup.getPortraitUri(), imageView, App.getOptions());
        }

    }

    private void setPortraitChangeListener() {
        photoUtils = new PhotoUtils(new PhotoUtils.OnPhotoResultListener() {
            @Override
            public void onPhotoResult(Uri uri) {
                if (uri != null && !TextUtils.isEmpty(uri.getPath())) {
                    selectUri = uri;
                    LoadDialog.show(mContext);
                    request(133);
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
            case R.id.rl_change_portrait:
                showPhotoDialog();
                break;
            case R.id.rl_group_change_username:
                DialogWithYesOrNoUtils.getInstance().showEditDialog(mContext, "新的群昵称", "确认修改", new DialogWithYesOrNoUtils.DialogCallBack() {
                    @Override
                    public void exectEvent() {

                    }

                    @Override
                    public void exectEditEvent(String editText) {
                        newGroupName = editText;
                        if (TextUtils.isEmpty(editText)) {
                            return;
                        }
                        LoadDialog.show(mContext);
                        request(UPDATEGROUPNAME);
                    }

                    @Override
                    public void updatePassword(String oldPassword, String newPassword) {

                    }
                });
                break;
        }
    }

    @Override
    public Object doInBackground(int requestCode, String id) throws HttpException {
        switch (requestCode) {
            case UPDATEGROUPNAME:
                return action.setGroupName(mGroup.getQunId(), newGroupName);
            case UPDATEGROUPHEADER:
                return action.setGroupPortrait(mGroup.getQunId(), imageUrl);
            case 133:
                return action.getQiNiuToken();
        }
        return null;
    }

    @Override
    public void onSuccess(int requestCode, Object result) {
        if (result != null) {
            switch (requestCode) {
                case UPDATEGROUPNAME:
                    SetGroupNameResponse response = (SetGroupNameResponse) result;
                    if (response.getCode() == 200) {
                        mGroupName.setText(newGroupName);
                        BroadcastManager.getInstance(mContext).sendBroadcast(UPDATEGROUPINFONAME, newGroupName);
                        BroadcastManager.getInstance(mContext).sendBroadcast(RongCloudEvent.NETUPDATEGROUP);
                        LoadDialog.dismiss(mContext);
                        NToast.shortToast(mContext, "修改成功");
                    }
                    break;
                case UPDATEGROUPHEADER:
                    SetGroupPortraitResponse res = (SetGroupPortraitResponse) result;
                    if (res.getCode() == 200) {
                        ImageLoader.getInstance().displayImage(imageUrl, imageView, App.getOptions());
                        BroadcastManager.getInstance(mContext).sendBroadcast(UPDATEGROUPINFOIMG, imageUrl);
                        BroadcastManager.getInstance(mContext).sendBroadcast(RongCloudEvent.NETUPDATEGROUP);
                        LoadDialog.dismiss(mContext);
                        NToast.shortToast(mContext, "修改成功");
                    }

                    break;
                case 133:
                    QiNiuTokenResponse response1 = (QiNiuTokenResponse) result;
                    if (response1.getCode() == 200) {
                        uploadImage(response1.getResult().getDomain(), response1.getResult().getToken(), selectUri);
                    }
                    break;
            }
        }
    }

    @Override
    public void onFailure(int requestCode, int state, Object result) {
        switch (requestCode) {
            case UPDATEGROUPNAME:
                LoadDialog.dismiss(mContext);
                NToast.shortToast(mContext, "更改群组昵称请求失败");
                break;
            case UPDATEGROUPHEADER:
                LoadDialog.dismiss(mContext);
                NToast.shortToast(mContext, "更改群组头像请求失败");
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
                photoUtils.takePicture(ChangeGroupInfoActivity.this);
            }
        });
        dialog.setMiddleListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (dialog != null && dialog.isShowing()) {
                    dialog.dismiss();
                }
                photoUtils.selectPicture(ChangeGroupInfoActivity.this);
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
                photoUtils.onActivityResult(ChangeGroupInfoActivity.this, requestCode, resultCode, data);
                break;
        }
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
                            request(UPDATEGROUPHEADER);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, null);
    }
}
