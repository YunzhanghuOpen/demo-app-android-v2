package io.rong.app;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import java.util.List;

import io.rong.app.db.DBManager;
import io.rong.app.db.Friend;
import io.rong.app.db.Qun;
import io.rong.app.message.AgreedFriendRequestMessage;
import io.rong.app.server.broadcast.BroadcastManager;
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
        RongIM.setGroupUserInfoProvider(this,true);
//        RongIM.setGroupInfoProvider(this, true);
    }

    /**
     * 需要 rongcloud connect 成功后设置的 listener
     */
    public void setConnectedListener() {
        RongIM.getInstance().enableUnreadMessageIcon(true);
        RongIM.getInstance().enableNewComingMessageIcon(true);

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
//            ContactNotificationMessage contactNotificationMessage = (ContactNotificationMessage) messageContent;
//            if (contactNotificationMessage.getOperation().equals("AcceptResponse")) {
//                if (RongIM.getInstance()!=null && RongIM.getInstance().getRongIMClient() != null) {
//                    RongIM.getInstance().startPrivateChat(context,uiConversation.getConversationSenderId(),null);
//                }
//            }else {
            context.startActivity(new Intent(context, NewFriendListActivity.class));
//            }


//            BroadcastManager.getInstance(context).sendBroadcast(GONEREDDOT);
            return true;
        }
        return false;
    }

    @Override
    public boolean onReceived(Message message, int i) {
        MessageContent messageContent = message.getContent();
        if (messageContent instanceof ContactNotificationMessage) {
            ContactNotificationMessage contactNotificationMessage = (ContactNotificationMessage) messageContent;
//            if (contactNotificationMessage.getOperation().equals("AcceptResponse")) {
//                if (contactNotificationMessage.getUserInfo() != null) {
//                    UserInfo bean = contactNotificationMessage.getUserInfo();
//                    DBManager.getInstance(mContext).getDaoSession().getFriendDao().insertOrReplace(
//                            new Friend(bean.getUserId(), bean.getName(), String.valueOf(bean.getPortraitUri()), null, null, Long.parseLong(contactNotificationMessage.getExtra()))
//                    );
//                } else {
//                    DBManager.getInstance(mContext).getDaoSession().getFriendDao().insertOrReplace(
//                            new Friend(contactNotificationMessage.getSourceUserId())
//                    );
//                }
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

            //TODO 对方发来的 同意加你为好友的自定义消息
            BroadcastManager.getInstance(mContext).sendBroadcast(UPDATEFRIEND);
        }
        return false;
    }

    @Override
    public UserInfo getUserInfo(String s) {
        return UserInfoEngine.getInstance(mContext).startEngine(s);
    }

    @Override
    public Group getGroupInfo(String s) {
        return GroupInfoEngine.getInstance(mContext).startEngine(s);
    }

    @Override
    public GroupUserInfo getGroupUserInfo(String groupId, String userId) {
       //TODO 服务端查询
        return null;
    }
}
