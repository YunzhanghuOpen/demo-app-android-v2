package io.rong.app;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import io.rong.app.db.DBManager;
import io.rong.app.db.Friend;
import io.rong.app.message.AgreedFriendRequestMessage;
import io.rong.app.server.broadcast.BroadcastManager;
import io.rong.app.server.network.http.HttpException;
import io.rong.app.server.response.ContactNotificationMessageData;
import io.rong.app.server.response.GroupNotificationMessageData;
import io.rong.app.server.utils.NLog;
import io.rong.app.server.utils.json.JsonMananger;
import io.rong.app.ui.activity.NewFriendListActivity;
import io.rong.imkit.RongContext;
import io.rong.imkit.RongIM;
import io.rong.imkit.model.GroupUserInfo;
import io.rong.imkit.model.UIConversation;
import io.rong.imkit.widget.provider.CameraInputProvider;
import io.rong.imkit.widget.provider.InputProvider;
import io.rong.imkit.widget.provider.LocationInputProvider;
import io.rong.imkit.widget.provider.VoIPInputProvider;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Group;
import io.rong.imlib.model.Message;
import io.rong.imlib.model.MessageContent;
import io.rong.imlib.model.UserInfo;
import io.rong.message.ContactNotificationMessage;
import io.rong.message.GroupNotificationMessage;

/**
 * Created by AMing on 16/1/7.
 * Company RongCloud
 */
public class RongCloudEvent implements RongIM.ConversationListBehaviorListener, RongIMClient.OnReceiveMessageListener, RongIM.UserInfoProvider, RongIM.GroupInfoProvider, RongIM.GroupUserInfoProvider {


    public static final String UPDATEFRIEND = "updatefriend";
    public static final java.lang.String UPDATEREDDOT = "updatereddot";
    public static String NETUPDATEGROUP = "netupdategroup";
    private Context mContext;

    private static RongCloudEvent mRongCloudInstance;

    public RongCloudEvent(Context mContext) {
        this.mContext = mContext;
        //初始化不需要 connect 就能 监听的 Listener
        initListener();
        UserInfoEngine.getInstance(mContext).setListener(new UserInfoEngine.UserInfoListener() {
            @Override
            public void onResult(UserInfo info) {
                if (info != null && RongIM.getInstance() != null) {
                    RongIM.getInstance().refreshUserInfoCache(info);
                }
            }
        });
        GroupInfoEngine.getInstance(mContext).setmListener(new GroupInfoEngine.GroupInfoListeners() {
            @Override
            public void onResult(Group info) {
                if (info != null && RongIM.getInstance() != null) {
                    NLog.e("GroupInfoEngine:"+info.getId()+"----"+info.getName()+"----"+info.getPortraitUri());
                    RongIM.getInstance().refreshGroupInfoCache(info);
                }
            }
        });
    }

    /**
     * 初始化 RongCloud.
     *
     * @param context 上下文。
     */
    public static void init(Context context) {

        if (mRongCloudInstance == null) {

            synchronized (RongCloudEvent.class) {

                if (mRongCloudInstance == null) {
                    mRongCloudInstance = new RongCloudEvent(context);
                }
            }
        }

    }

    /**
     * 获取RongCloud 实例。
     *
     * @return RongCloud。
     */
    public static RongCloudEvent getInstance() {
        return mRongCloudInstance;
    }

    /**
     * init 后就能设置的监听
     */
    private void initListener() {
//        RongIM.setConversationBehaviorListener(this);//设置会话界面操作的监听器。
        RongIM.setConversationListBehaviorListener(this);
        RongIM.setUserInfoProvider(this, true);
        RongIM.setGroupInfoProvider(this, true);
        RongIM.setGroupUserInfoProvider(this, true);
    }

    /**
     * 需要 rongcloud connect 成功后设置的 listener
     */
    public void setConnectedListener() {
        RongIM.getInstance().getRongIMClient().setOnReceiveMessageListener(this);

        //        扩展功能自定义  singleProvider 语音 voip 只支持单对单
        InputProvider.ExtendProvider[] singleProvider = {
//                new PhotoInputProvider(RongContext.getInstance()),//图片
                new CameraInputProvider(RongContext.getInstance()),//相机
                new LocationInputProvider(RongContext.getInstance()),//地理位置
                new VoIPInputProvider(RongContext.getInstance()),// 语音通话
        };

        InputProvider.ExtendProvider[] muiltiProvider = {
//                new PhotoInputProvider(RongContext.getInstance()),//图片
                new CameraInputProvider(RongContext.getInstance()),//相机
                new LocationInputProvider(RongContext.getInstance()),//地理位置
        };

        RongIM.getInstance().resetInputExtensionProvider(Conversation.ConversationType.PRIVATE, singleProvider);
        RongIM.getInstance().resetInputExtensionProvider(Conversation.ConversationType.DISCUSSION, muiltiProvider);
        RongIM.getInstance().resetInputExtensionProvider(Conversation.ConversationType.CUSTOMER_SERVICE, muiltiProvider);
        RongIM.getInstance().resetInputExtensionProvider(Conversation.ConversationType.GROUP, muiltiProvider);
    }

    @Override
    public boolean onConversationPortraitClick(Context context, Conversation.ConversationType conversationType, String s) {
        return false;
    }

    @Override
    public boolean onConversationPortraitLongClick(Context context, Conversation.ConversationType conversationType, String s) {
        return false;
    }

    @Override
    public boolean onConversationLongClick(Context context, View view, UIConversation uiConversation) {
        return false;
    }

    @Override
    public boolean onConversationClick(Context context, View view, UIConversation uiConversation) {
        MessageContent messageContent = uiConversation.getMessageContent();
        if (messageContent instanceof ContactNotificationMessage) {
            ContactNotificationMessage contactNotificationMessage = (ContactNotificationMessage) messageContent;
            if (contactNotificationMessage.getOperation().equals("AcceptResponse")) {
                // 被加方同意请求后
                if (RongIM.getInstance() != null && RongIM.getInstance().getRongIMClient() != null) {
                    if (contactNotificationMessage.getExtra() != null) {
                        ContactNotificationMessageData bean = null;
                        try {
                            bean = JsonMananger.jsonToBean(contactNotificationMessage.getExtra(), ContactNotificationMessageData.class);
                        } catch (HttpException e) {
                            e.printStackTrace();
                        }
                        RongIM.getInstance().startPrivateChat(context, uiConversation.getConversationSenderId(), bean.getSourceUserNickname());

                    }
                }
            } else {
                context.startActivity(new Intent(context, NewFriendListActivity.class));
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean onReceived(Message message, int i) {
        MessageContent messageContent = message.getContent();
        if (messageContent instanceof ContactNotificationMessage) {
            ContactNotificationMessage contactNotificationMessage = (ContactNotificationMessage) messageContent;
            if (contactNotificationMessage.getOperation().equals("Request")) {
                //对方发来好友邀请
                BroadcastManager.getInstance(mContext).sendBroadcast(RongCloudEvent.UPDATEREDDOT);
            }else if (contactNotificationMessage.getOperation().equals("AcceptResponse")) {
                //对方同意我的好友请求
                BroadcastManager.getInstance(mContext).sendBroadcast(UPDATEFRIEND);
                BroadcastManager.getInstance(mContext).sendBroadcast(RongCloudEvent.UPDATEREDDOT);
            }
//                // 发广播通知更新好友列表
            BroadcastManager.getInstance(mContext).sendBroadcast(UPDATEREDDOT);
//            }
        } else if (messageContent instanceof AgreedFriendRequestMessage) {//好友添加成功消息
            AgreedFriendRequestMessage agreedFriendRequestMessage = (AgreedFriendRequestMessage) messageContent;
            if (agreedFriendRequestMessage.getUserInfo() != null) {
                UserInfo bean = agreedFriendRequestMessage.getUserInfo();
                DBManager.getInstance(mContext).getDaoSession().getFriendDao().insertOrReplace(
                        new Friend(bean.getUserId(), bean.getName(), String.valueOf(bean.getPortraitUri()), null, null, null)
                );
            } else {
                DBManager.getInstance(mContext).getDaoSession().getFriendDao().insertOrReplace(
                        new Friend(agreedFriendRequestMessage.getFriendId())
                );
                RongIM.getInstance().refreshUserInfoCache(new UserInfo(agreedFriendRequestMessage.getUserInfo().getUserId(),
                        agreedFriendRequestMessage.getUserInfo().getName(),
                        agreedFriendRequestMessage.getUserInfo().getPortraitUri()
                ));
            }
        } else if (messageContent instanceof GroupNotificationMessage) {
            GroupNotificationMessage groupNotificationMessage = (GroupNotificationMessage) messageContent;
            NLog.e("" + groupNotificationMessage.getMessage());
            if (groupNotificationMessage.getOperation().equals("Kicked")) {
                //TODO 被移除群组
            } else if (groupNotificationMessage.getOperation().equals("Add")) {
                //TODO 被添加到群组
            } else if (groupNotificationMessage.getOperation().equals("Quit")) {
                //TODO 群中有用户退出群组 或者 当前用户自行退出群组？
            } else if (groupNotificationMessage.getOperation().equals("Rename")) {
                //TODO 群组中有用户改名 或者 当前用户自己改名 还是群主更改群昵称？
            }

            BroadcastManager.getInstance(mContext).sendBroadcast(RongCloudEvent.NETUPDATEGROUP);
        }
        return false;
    }

    @Override
    public UserInfo getUserInfo(String s) {
        return UserInfoEngine.getInstance(mContext).startEngine(s);
    }

    @Override
    public Group getGroupInfo(String s) {
        NLog.e("getGroupInfo:"+s);
        return GroupInfoEngine.getInstance(mContext).startEngine(s);
    }

    @Override
    public GroupUserInfo getGroupUserInfo(String groupId, String userId) {
        //TODO 服务端查询
        return null;
    }
}
