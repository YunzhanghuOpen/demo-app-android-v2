package io.rong.app.ui.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.qiniu.android.http.ResponseInfo;
import com.qiniu.android.storage.UpCompletionHandler;
import com.qiniu.android.storage.UploadManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.rong.app.App;
import io.rong.app.R;
import io.rong.app.db.DBManager;
import io.rong.app.db.Qun;
import io.rong.app.server.broadcast.BroadcastManager;
import io.rong.app.server.network.http.HttpException;
import io.rong.app.server.pinyin.Friend;
import io.rong.app.server.response.CreateGroupResponse;
import io.rong.app.server.response.QiNiuTokenResponse;
import io.rong.app.server.response.SetGroupPortraitResponse;
import io.rong.app.server.utils.AMGenerate;
import io.rong.app.server.utils.NToast;
import io.rong.app.server.utils.photo.PhotoUtils;
import io.rong.app.server.widget.BottomMenuDialog;
import io.rong.app.server.widget.CircleImageView;
import io.rong.app.server.widget.ClearWriteEditText;
import io.rong.app.server.widget.HorizontalListView;
import io.rong.app.server.widget.LoadDialog;
import io.rong.imkit.RongIM;
import io.rong.imkit.widget.AsyncImageView;
import io.rong.imlib.model.Conversation;

/**
 * Created by AMing on 16/1/25.
 * Company RongCloud
 */
public class CreateGroupActivity extends BaseActivity implements View.OnClickListener {


    private static final int CREATEGROUP = 16;
    private static final int SETGROUPPORTRAITURI = 17;
    public static final java.lang.String REFRESHGROUPUI = "REFRESHGROUPUI";
    private List<Friend> memberList;

    private TextView memberSize;

    private HorizontalListView mListView;

    private GroupMemberAdapter adapter;

    private RelativeLayout mGroupHeader;

    private AsyncImageView asyncImageView;

    private PhotoUtils photoUtils;

    private BottomMenuDialog dialog;

    private String mGroupName, mGroupId;

    private Button mButton;

    private ClearWriteEditText mGroupNameEdit;

    private List<String> groupIds = new ArrayList<>();

    private Uri selectUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.de_ac_create_group);
        getSupportActionBar().setTitle("创建群组");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.de_actionbar_back);
        memberList = (List<Friend>) getIntent().getSerializableExtra("GroupMember");
        initView();
        setPortraitChangeListener();
        if (memberList != null && memberList.size() > 0) {
            groupIds.add(getSharedPreferences("config", MODE_PRIVATE).getString("loginid", ""));
            for (Friend f : memberList) {
                groupIds.add(f.getUserId());
            }
            adapter = new GroupMemberAdapter(memberList);
            mListView.setAdapter(adapter);
        }
    }

    private void setPortraitChangeListener() {
        photoUtils = new PhotoUtils(new PhotoUtils.OnPhotoResultListener() {
            @Override
            public void onPhotoResult(Uri uri) {
                if (uri != null && !TextUtils.isEmpty(uri.getPath())) {
                    selectUri = uri;
                    LoadDialog.show(mContext);
                    request(131);
                }
            }

            @Override
            public void onPhotoCancel() {

            }
        });
    }

    private void initView() {
        memberSize = (TextView) findViewById(R.id.create_member);
        memberSize.setText("已邀请成员(" + memberList.size() + ")");
        mListView = (HorizontalListView) findViewById(R.id.create_listview);
        mGroupHeader = (RelativeLayout) findViewById(R.id.rl_Group_portrait);
        asyncImageView = (AsyncImageView) findViewById(R.id.img_Group_portrait);
        mGroupHeader.setOnClickListener(this);
        mButton = (Button) findViewById(R.id.create_ok);
        mButton.setOnClickListener(this);
        mGroupNameEdit = (ClearWriteEditText) findViewById(R.id.create_groupname);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rl_Group_portrait:
                showPhotoDialog();
                break;
            case R.id.create_ok:
                if (TextUtils.isEmpty(imageUrl)) {
                    NToast.shortToast(mContext, "群组头像未设置");
                    break;
                }
                mGroupName = mGroupNameEdit.getText().toString().trim();
                if (TextUtils.isEmpty(mGroupName)) {
                    NToast.shortToast(mContext, "群组昵称不能为空");
                    break;
                }
                if (groupIds.size() > 1) {
                    LoadDialog.show(mContext);
                    request(CREATEGROUP, true);
                }

                break;
        }
    }


    @Override
    public Object doInBackground(int requestCode, String id) throws HttpException {
        switch (requestCode) {
            case CREATEGROUP:
                return action.createGroup(mGroupName, groupIds);
            case SETGROUPPORTRAITURI:
                return action.setGroupPortrait(mGroupId, imageUrl);
            case 131:
                return action.getQiNiuToken();
        }
        return null;
    }

    @Override
    public void onSuccess(int requestCode, Object result) {
        if (result != null) {
            switch (requestCode) {
                case CREATEGROUP:
                    CreateGroupResponse cgRes = (CreateGroupResponse) result;
                    if (cgRes.getCode() == 200) {
                        mGroupId = cgRes.getResult().getId(); //id == null
                        if (!TextUtils.isEmpty(mGroupId)) {
                            request(SETGROUPPORTRAITURI);
                        }
                    }
                    break;
                case SETGROUPPORTRAITURI:
                    SetGroupPortraitResponse spRes = (SetGroupPortraitResponse) result;
                    if (spRes.getCode() == 200) {
                        DBManager.getInstance(mContext).getDaoSession().getQunDao().insertOrReplace(new Qun(mGroupId, mGroupName, imageUrl, String.valueOf(0)));
                        BroadcastManager.getInstance(mContext).sendBroadcast(REFRESHGROUPUI);
                        if (RongIM.getInstance() != null) {
                            RongIM.getInstance().getRongIMClient().removeConversation(Conversation.ConversationType.PRIVATE, "uuuuuuuTest");
                            RongIM.getInstance().getRongIMClient().clearMessages(Conversation.ConversationType.PRIVATE, "uuuuuuuTest");
                        }

                        LoadDialog.dismiss(mContext);
                        NToast.shortToast(mContext, "群组创建成功");
                        finish();
                    }
                case 131:
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
            case CREATEGROUP:
                LoadDialog.dismiss(mContext);
                NToast.shortToast(mContext, "群组创建请求失败");
                break;
            case SETGROUPPORTRAITURI:
                LoadDialog.dismiss(mContext);
                NToast.shortToast(mContext, "群组头像请求失败");
                break;
        }
    }

    class GroupMemberAdapter extends BaseAdapter {

        List<Friend> memberList;

        GroupMemberAdapter(List<Friend> memberList) {
            this.memberList = memberList;
        }

        @Override
        public int getCount() {
            return memberList.size();
        }

        @Override
        public Object getItem(int position) {
            return memberList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Holder holder;
            Friend data = memberList.get(position);
            if (convertView == null) {
                holder = new Holder();
                convertView = LayoutInflater.from(mContext).inflate(R.layout.crate_group_item, null);
                holder.memberHeader = (CircleImageView) convertView.findViewById(R.id.create_show_title);
                holder.memberName = (TextView) convertView.findViewById(R.id.create_show_name);
                convertView.setTag(holder);
            } else {
                holder = (Holder) convertView.getTag();
            }
            holder.memberName.setText(data.getName());
            if (TextUtils.isEmpty(data.getPortraitUri())) {
                ImageLoader.getInstance().displayImage(AMGenerate.generateDefaultAvatar(data.getName(),data.getUserId()), holder.memberHeader, App.getOptions());
            }else {
                ImageLoader.getInstance().displayImage(data.getPortraitUri(), holder.memberHeader, App.getOptions());
            }
            return convertView;
        }
    }

    class Holder {
        CircleImageView memberHeader;
        TextView memberName;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);
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
                photoUtils.takePicture(CreateGroupActivity.this);
            }
        });
        dialog.setMiddleListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (dialog != null && dialog.isShowing()) {
                    dialog.dismiss();
                }
                photoUtils.selectPicture(CreateGroupActivity.this);
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
                photoUtils.onActivityResult(CreateGroupActivity.this, requestCode, resultCode, data);
                break;
        }
    }

    private UploadManager uploadManager;

    private String imageUrl;

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
                            ImageLoader.getInstance().displayImage(imageUrl, asyncImageView);
                            LoadDialog.dismiss(mContext);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, null);
    }
}
