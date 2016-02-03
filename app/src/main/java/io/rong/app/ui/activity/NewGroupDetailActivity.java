package io.rong.app.ui.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.Serializable;
import java.util.List;

import de.greenrobot.dao.query.QueryBuilder;
import io.rong.app.App;
import io.rong.app.R;
import io.rong.app.RongCloudEvent;
import io.rong.app.database.QunDao;
import io.rong.app.db.DBManager;
import io.rong.app.db.Qun;
import io.rong.app.server.broadcast.BroadcastManager;
import io.rong.app.server.network.http.HttpException;
import io.rong.app.server.pinyin.Friend;
import io.rong.app.server.response.DismissGroupResponse;
import io.rong.app.server.response.GetGroupInfoResponse;
import io.rong.app.server.response.GetGroupMemberResponse;
import io.rong.app.server.response.QuitGroupResponse;
import io.rong.app.server.response.SetGroupDisplayNameResponse;
import io.rong.app.server.utils.NToast;
import io.rong.app.server.widget.DialogWithYesOrNoUtils;
import io.rong.app.server.widget.LoadDialog;
import io.rong.app.ui.widget.DemoGridView;
import io.rong.imkit.RongIM;
import io.rong.imlib.model.Conversation;

/**
 * Created by AMing on 16/1/27.
 * Company RongCloud
 */
public class NewGroupDetailActivity extends BaseActivity implements View.OnClickListener {

    private static final int GETGROUPMEMBER = 20;
    private static final int DISMISSGROUP = 26;
    private static final int QUITGROUP = 27;
    private static final int SETGROUPNAME = 29;
    private static final int GETGROUPINFO = 30;

    private Qun mGroup;

    private boolean isCreated;

    private DemoGridView gridview;

    private List<GetGroupMemberResponse.ResultEntity> mGroupMember;

    private GridAdapter adapter;

    private TextView mTextViewMemberSize, mGroupName, mGroupDisplayNameText;

    private ImageView mGroupHeader, showIcon;

    private LinearLayout mGroupInfo, mGroupDisplayName, mStartGroupChat;

    private Button mQuitBtn, mDismissBtn;
    private String groupDisplayNmae;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.de_ac_detail_group);
        gridview = (DemoGridView) findViewById(R.id.gridview);
        mTextViewMemberSize = (TextView) findViewById(R.id.group_member_size);
        mGroupName = (TextView) findViewById(R.id.group_name);
        mGroupInfo = (LinearLayout) findViewById(R.id.group_info);
        mGroupHeader = (ImageView) findViewById(R.id.group_header);
        showIcon = (ImageView) findViewById(R.id.show_icon);
        mGroupDisplayName = (LinearLayout) findViewById(R.id.group_displayname);
        mGroupDisplayNameText = (TextView) findViewById(R.id.group_displayname_text);
        mStartGroupChat = (LinearLayout) findViewById(R.id.start_group_chat);
        mQuitBtn = (Button) findViewById(R.id.group_quit);
        mDismissBtn = (Button) findViewById(R.id.group_dismiss);
        mGroupDisplayName.setOnClickListener(this);
        mStartGroupChat.setOnClickListener(this);
        mQuitBtn.setOnClickListener(this);
        mDismissBtn.setOnClickListener(this);
        getSupportActionBar().setTitle("群组信息");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.de_actionbar_back);
        mGroup = (Qun) getIntent().getSerializableExtra("QunBean");
        if (mGroup == null) {
            NToast.shortToast(mContext, "GroupBean 数据为 null");
            return;
        }
        if (mGroup.getRole().equals("0")) {
            isCreated = true;
        } else {
            isCreated = false;
        }

        if (mGroup != null) {
            mGroupName.setText(mGroup.getName());
            ImageLoader.getInstance().displayImage(mGroup.getPortraitUri(), mGroupHeader, App.getOptions());
        }


        initData();
        BroadcastManager.getInstance(mContext).addAction(ChangeGroupInfoActivity.UPDATEGROUPINFOIMG, new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String command = intent.getAction();
                if (!TextUtils.isEmpty(command)) {
                    String img = intent.getStringExtra("String");
                    ImageLoader.getInstance().displayImage(img, mGroupHeader, App.getOptions());
                    if (RongIM.getInstance() != null) {
                        RongIM.getInstance().getRongIMClient().removeConversation(Conversation.ConversationType.PRIVATE, "uuuuuuuTest");
                        RongIM.getInstance().getRongIMClient().clearMessages(Conversation.ConversationType.PRIVATE, "uuuuuuuTest");
                    }
                }
            }
        });

        BroadcastManager.getInstance(mContext).addAction(ChangeGroupInfoActivity.UPDATEGROUPINFONAME, new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String command = intent.getAction();
                if (!TextUtils.isEmpty(command)) {
                    String s = intent.getStringExtra("String");
                    mGroupName.setText(s);
                }
            }
        });

    }

    private void initData() {
        LoadDialog.show(mContext);
        request(GETGROUPMEMBER);
    }

    @Override
    public Object doInBackground(int requestCode) throws HttpException {
        switch (requestCode) {
            case GETGROUPMEMBER:
                return action.getGroupMember(mGroup.getQunId());
            case QUITGROUP:
                return action.quitGroup(mGroup.getQunId());
            case DISMISSGROUP:
                return action.dissmissGroup(mGroup.getQunId());
            case SETGROUPNAME:
                return action.setGroupDisplayName(mGroup.getQunId(), groupDisplayNmae);
            case GETGROUPINFO:
                return action.getGroupInfo(mGroup.getQunId());
        }
        return super.doInBackground(requestCode);
    }

    @Override
    public void onSuccess(int requestCode, Object result) {
        if (result != null) {
            switch (requestCode) {
                case GETGROUPMEMBER:
                    GetGroupMemberResponse res = (GetGroupMemberResponse) result;
                    if (res.getCode() == 200) {
                        mGroupMember = res.getResult();
                        if (mGroupMember != null && mGroupMember.size() > 0) {
                            mTextViewMemberSize.setText("群组成员(" + mGroupMember.size() + ")");
                            adapter = new GridAdapter(mContext, mGroupMember);
                            gridview.setAdapter(adapter);
                        }

                        for (GetGroupMemberResponse.ResultEntity g : mGroupMember) {
                            if (g.getUser().getId().equals(getSharedPreferences("config", MODE_PRIVATE).getString("loginid", ""))) {
                                if (!TextUtils.isEmpty(g.getDisplayName())) {
                                    mGroupDisplayNameText.setText(g.getDisplayName());
                                } else {
                                    mGroupDisplayNameText.setText("无");
                                }
                            }
                        }

                        if (isCreated) {
                            mDismissBtn.setVisibility(View.VISIBLE);
                            showIcon.setVisibility(View.VISIBLE);
                            mGroupInfo.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent intent = new Intent(NewGroupDetailActivity.this, ChangeGroupInfoActivity.class);
                                    intent.putExtra("GroupInfo", (Serializable) mGroup);
                                    startActivityForResult(intent, 102);
                                }
                            });
                        }
                        LoadDialog.dismiss(mContext);
                    }
                    break;
                case QUITGROUP:
                    QuitGroupResponse response = (QuitGroupResponse) result;
                    if (response.getCode() == 200) {
                        BroadcastManager.getInstance(mContext).sendBroadcast(RongCloudEvent.NETUPDATEGROUP);
                        if (RongIM.getInstance() != null && RongIM.getInstance().getRongIMClient().getConversation(Conversation.ConversationType.GROUP, mGroup.getQunId()) != null) {
                            RongIM.getInstance().getRongIMClient().removeConversation(Conversation.ConversationType.GROUP, mGroup.getQunId());
                            RongIM.getInstance().getRongIMClient().clearMessages(Conversation.ConversationType.GROUP, mGroup.getQunId());
                        }
                        NToast.shortToast(mContext, "退出成功");
                        LoadDialog.dismiss(mContext);
                        finish();
                    }
                    break;

                case DISMISSGROUP:
                    DismissGroupResponse response1 = (DismissGroupResponse) result;
                    if (response1.getCode() == 200) {
                        BroadcastManager.getInstance(mContext).sendBroadcast(RongCloudEvent.NETUPDATEGROUP);
                        if (RongIM.getInstance() != null && RongIM.getInstance().getRongIMClient().getConversation(Conversation.ConversationType.GROUP, mGroup.getQunId()) != null) {
                            RongIM.getInstance().getRongIMClient().removeConversation(Conversation.ConversationType.GROUP, mGroup.getQunId());
                            RongIM.getInstance().getRongIMClient().clearMessages(Conversation.ConversationType.GROUP, mGroup.getQunId());
                        }
                        NToast.shortToast(mContext, "解散成功");
                        LoadDialog.dismiss(mContext);
                        finish();
                    }
                    break;

                case SETGROUPNAME:
                    SetGroupDisplayNameResponse response2 = (SetGroupDisplayNameResponse) result;
                    if (response2.getCode() == 200) {
                        request(GETGROUPINFO);
                    }
                    break;
                case GETGROUPINFO:
                    GetGroupInfoResponse response3 = (GetGroupInfoResponse) result;
                    if (response3.getCode() == 200) {
                        int i;
                        if (isCreated) {
                            i = 0;
                        } else {
                            i = 1;
                        }
                        GetGroupInfoResponse.ResultEntity bean = response3.getResult();
                        DBManager.getInstance(mContext).getDaoSession().getQunDao().insertOrReplace(
                                new Qun(bean.getId(), bean.getName(), bean.getPortraitUri(), groupDisplayNmae, String.valueOf(i), null)
                        );
                        mGroupDisplayNameText.setText(groupDisplayNmae);
                        LoadDialog.dismiss(mContext);
                        NToast.shortToast(mContext, "修改成功");
                    }
                    break;
            }
        }
    }

    @Override
    public void onFailure(int requestCode, int state, Object result) {
        switch (requestCode) {
            case GETGROUPMEMBER:
                NToast.shortToast(mContext, "获取群组成员请求失败");
                LoadDialog.dismiss(mContext);
                break;
            case QUITGROUP:
                NToast.shortToast(mContext, "退出群组请求失败");
                LoadDialog.dismiss(mContext);
                break;
            case DISMISSGROUP:
                NToast.shortToast(mContext, "解散群组请求失败");
                LoadDialog.dismiss(mContext);
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.start_group_chat:
                if (RongIM.getInstance() != null) {
                    RongIM.getInstance().startGroupChat(mContext, mGroup.getQunId(), mGroup.getName());
                    finish();
                }
                break;
            case R.id.group_displayname:
                DialogWithYesOrNoUtils.getInstance().showEditDialog(mContext, "群昵称", "确认", new DialogWithYesOrNoUtils.DialogCallBack() {
                    @Override
                    public void exectEvent() {

                    }

                    @Override
                    public void exectEditEvent(String editText) {
                        if (TextUtils.isEmpty(mGroup.getQunId()) && TextUtils.isEmpty(editText)) {
                            return;
                        }
                        groupDisplayNmae = editText;
                        LoadDialog.show(mContext);
                        request(SETGROUPNAME);
                    }

                    @Override
                    public void updatePassword(String oldPassword, String newPassword) {

                    }
                });

                break;
            case R.id.group_quit:
                DialogWithYesOrNoUtils.getInstance().showDialog(mContext, "确认退出群组?", new DialogWithYesOrNoUtils.DialogCallBack() {
                    @Override
                    public void exectEvent() {
                        LoadDialog.show(mContext);
                        request(QUITGROUP);
                    }

                    @Override
                    public void exectEditEvent(String editText) {

                    }

                    @Override
                    public void updatePassword(String oldPassword, String newPassword) {

                    }
                });
                break;
            case R.id.group_dismiss:
                DialogWithYesOrNoUtils.getInstance().showDialog(mContext, "确认解散群组?", new DialogWithYesOrNoUtils.DialogCallBack() {
                    @Override
                    public void exectEvent() {
                        LoadDialog.show(mContext);
                        request(DISMISSGROUP);
                    }

                    @Override
                    public void exectEditEvent(String editText) {

                    }

                    @Override
                    public void updatePassword(String oldPassword, String newPassword) {

                    }
                });
                break;
        }
    }


    private class GridAdapter extends BaseAdapter {

        private List<GetGroupMemberResponse.ResultEntity> list;
        Context context;


        public GridAdapter(Context context, List<GetGroupMemberResponse.ResultEntity> list) {
            this.list = list;
            this.context = context;
        }


        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.social_chatsetting_gridview_item, null);
            }
            ImageView iv_avatar = (ImageView) convertView.findViewById(R.id.iv_avatar);
            TextView tv_username = (TextView) convertView.findViewById(R.id.tv_username);
            ImageView badge_delete = (ImageView) convertView.findViewById(R.id.badge_delete);

            // 最后一个item，减人按钮
            if (position == getCount() - 1 && isCreated) {
                tv_username.setText("");
                badge_delete.setVisibility(View.GONE);
                iv_avatar.setImageResource(R.drawable.icon_btn_deleteperson);

                iv_avatar.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(NewGroupDetailActivity.this, SelectFriendsActivity.class);
                        intent.putExtra("DeleteGroupMember", (Serializable) mGroupMember);
                        intent.putExtra("DeleteGroupId", mGroup.getQunId());
                        startActivityForResult(intent, 101);
                    }

                });
            } else if ((isCreated && position == getCount() - 2) || (!isCreated && position == getCount() - 1)) {
                tv_username.setText("");
                badge_delete.setVisibility(View.GONE);
                iv_avatar.setImageResource(R.drawable.jy_drltsz_btn_addperson);

                iv_avatar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(NewGroupDetailActivity.this, SelectFriendsActivity.class);
                        intent.putExtra("AddGroupMember", (Serializable) mGroupMember);
                        intent.putExtra("GroupId", mGroup.getQunId());
                        startActivityForResult(intent, 100);

                    }
                });
            } else { // 普通成员
                GetGroupMemberResponse.ResultEntity bean = list.get(position);
                if (!TextUtils.isEmpty(bean.getDisplayName())) {
                    tv_username.setText(bean.getDisplayName());
                } else {
                    tv_username.setText(bean.getUser().getNickname());
                }
                ImageLoader.getInstance().displayImage(bean.getUser().getPortraitUri(), iv_avatar, App.getOptions());
                iv_avatar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        NToast.shortToast(mContext, "点击用户头像");

                    }

                });

            }

            return convertView;
        }

        @Override
        public int getCount() {
            if (isCreated) {
                return list.size() + 2;
            } else {
                return list.size() + 1;
            }
        }

        @Override
        public Object getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        /**
         * 传入新的数据 刷新UI的方法
         */
        public void updateListView(List<GetGroupMemberResponse.ResultEntity> list) {
            this.list = list;
            notifyDataSetChanged();
        }

    }


    // 拿到新增的成员刷新adapter
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null) {
            List<Friend> newMemberData = (List<Friend>) data.getSerializableExtra("newAddMember");
            List<Friend> deleMember = (List<Friend>) data.getSerializableExtra("deleteMember");
            if (newMemberData != null && newMemberData.size() > 0) {
                request(GETGROUPMEMBER);
            } else if (deleMember != null && deleMember.size() > 0) {
                request(GETGROUPMEMBER);
            }

        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        BroadcastManager.getInstance(mContext).destroy(ChangeGroupInfoActivity.UPDATEGROUPINFONAME);
        BroadcastManager.getInstance(mContext).destroy(ChangeGroupInfoActivity.UPDATEGROUPINFOIMG);
    }
}
