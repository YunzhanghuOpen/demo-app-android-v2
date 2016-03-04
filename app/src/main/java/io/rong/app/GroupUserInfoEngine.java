package io.rong.app;

import android.content.Context;
import android.text.TextUtils;

import java.util.List;

import io.rong.app.server.SealAction;
import io.rong.app.server.network.async.AsyncTaskManager;
import io.rong.app.server.network.async.OnDataListener;
import io.rong.app.server.network.http.HttpException;
import io.rong.app.server.response.GetGroupMemberResponse;
import io.rong.app.server.utils.NLog;
import io.rong.imkit.RongIM;
import io.rong.imkit.model.GroupUserInfo;

/**
 * Created by AMing on 16/2/29.
 * Company RongCloud
 */
public class GroupUserInfoEngine implements OnDataListener {

    private String groupId, userId;

    private static final int REQUESTGROUPUSERINFO = 50;
    private static GroupUserInfoEngine instance;

    private GroupUserInfoEngine(Context context) {
        this.context = context;
    }

    private static Context context;

    private GroupUserInfo groupUserInfo;

    public GroupUserInfo getGroupUserInfo() {
        return groupUserInfo;
    }

    public void setGroupUserInfo(GroupUserInfo groupUserInfo) {
        this.groupUserInfo = groupUserInfo;
    }

    public static GroupUserInfoEngine getInstance(Context context) {
        if (instance == null) {
            instance = new GroupUserInfoEngine(context);
        }
        return instance;
    }

    public GroupUserInfo startEngine(String groupId, String userid) {
        if (!TextUtils.isEmpty(groupId) && !TextUtils.isEmpty(userid)) {
            NLog.e("-------groupId-----userid 同时不为空");
            this.groupId = groupId;
            this.userId = userid;
            AsyncTaskManager.getInstance(context).request(REQUESTGROUPUSERINFO, this);
        }
        return getGroupUserInfo();
    }

    @Override
    public Object doInBackground(int requsetCode) throws HttpException {
        return new SealAction(context).getGroupMember(groupId);
    }

    private List<GetGroupMemberResponse.ResultEntity> mGroupMember;

    @Override
    public void onSuccess(int requestCode, Object result) {
        if (result != null) {
            GetGroupMemberResponse res = (GetGroupMemberResponse) result;
            if (res.getCode() == 200) {
                mGroupMember = res.getResult();
                for (GetGroupMemberResponse.ResultEntity g : mGroupMember) {
                    if (g.getUser().getId().equals(userId)) {
                        if (RongIM.getInstance() != null && RongIM.getInstance().getRongIMClient() != null) {
                            RongIM.getInstance().refreshGroupUserInfoCache(new GroupUserInfo(groupId,userId,g.getDisplayName()));
                            setGroupUserInfo(new GroupUserInfo(groupId,userId,g.getDisplayName()));
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onFailure(int requestCode, int state, Object result) {

    }
}