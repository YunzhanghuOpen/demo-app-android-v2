package io.rong.app;

import android.content.Context;
import android.util.Log;
import android.view.View;

import io.rong.imkit.RongIM;
import io.rong.imkit.model.UIConversation;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Message;
import io.rong.imlib.model.MessageContent;
import io.rong.imlib.model.UserInfo;
import io.rong.message.TextMessage;
import io.rong.notification.PushNotificationMessage;

/**
 * Created by Bob_ge on 15/6/16.
 */
public final class DeleteEvent {

    private static DeleteEvent sDeleteEvent;

    private Context mContext;

    public void initDefaultListener() {
//        initSendMessage();
//        initReciveMessage();
//        initConnectionStatusListener();
//        inittest1();
//        inittest2();
//        inittest3();
        inittest4();
    }


    private void inittest4() {


        if (RongIM.getInstance() != null) {
            /**
             * 接收未读消息的监听器。
             *
             * @param listener          接收未读消息消息的监听器。
             */
            RongIM.getInstance().setOnReceiveUnreadCountChangedListener(new MyReceiveUnreadCountChangedListener());

            /**
             * 设置接收未读消息的监听器。
             *
             * @param listener          接收未读消息消息的监听器。
             * @param conversationTypes 接收未读消息的会话类型。
             */
            RongIM.getInstance().setOnReceiveUnreadCountChangedListener(new MyReceiveUnreadCountChangedListener(), Conversation.ConversationType.PRIVATE);
        }
    }

    /**
     * 接收未读消息的监听器。
     */
    private class MyReceiveUnreadCountChangedListener implements RongIM.OnReceiveUnreadCountChangedListener {

        @Override
        public void onMessageIncreased(int count) {

        }
    }

    private void inittest3() {

        /**
         * 设置接收 push 消息的监听器。
         */
        RongIM.setOnReceivePushMessageListener(new MyReceivePushMessageListener());

    }


    private class MyReceivePushMessageListener implements RongIMClient.OnReceivePushMessageListener {
        /**
         * 获得 push 通知的监听函数。
         *
         * @return push 通知的监听函数。
         */
        @Override
        public boolean onReceivePushMessage(PushNotificationMessage pushNotificationMessage) {
            return false;
        }
    }


    private void inittest2() {
        /**
         * 设置会话列表界面操作的监听器。
         */
        RongIM.setConversationListBehaviorListener(new MyConversationListBehaviorListener());

    }

    /**
     * 实现 ConversationListBehaviorListener 接口
     */
    private class MyConversationListBehaviorListener implements RongIM.ConversationListBehaviorListener {
        /**
         * 长按会话列表中的 item 时执行。
         *
         * @param context        上下文。
         * @param view           触发点击的 View。
         * @param uiConversation 长按时的会话条目。
         * @return 如果用户自己处理了长按会话后的逻辑处理，则返回 true， 否则返回 false，false 走融云默认处理方式。
         */
        @Override
        public boolean onConversationLongClick(Context context, View view, UIConversation uiConversation) {
            return false;
        }

        /**
         * 点击会话列表中的 item 时执行。
         *
         * @param context        上下文。
         * @param view           触发点击的 View。
         * @param uiConversation 会话条目。
         * @return 如果用户自己处理了点击会话后的逻辑处理，则返回 true， 否则返回 false，false 走融云默认处理方式。
         */
        @Override
        public boolean onConversationClick(Context context, View view, UIConversation uiConversation) {
            return false;
        }
    }

    private void inittest1() {
        /**
         * 设置会话界面操作的监听器。
         */
        RongIM.setConversationBehaviorListener(new MyConversationBehaviorListener());

    }

    /**
     * 实现 ConversationBehaviorListener 接口
     */
    private class MyConversationBehaviorListener implements RongIM.ConversationBehaviorListener {

        /**
         * 当点击用户头像后执行。
         *
         * @param context          上下文。
         * @param conversationType 会话类型。
         * @param userInfo         被点击的用户的信息。
         * @return 如果用户自己处理了点击后的逻辑处理，则返回 true，否则返回 false，false 走融云默认处理方式。
         */
        @Override
        public boolean onUserPortraitClick(Context context, Conversation.ConversationType conversationType, UserInfo userInfo) {
            return false;
        }

        /**
         * 当长按用户头像后执行。
         *
         * @param context          上下文。
         * @param conversationType 会话类型。
         * @param userInfo         被点击的用户的信息。
         * @return 如果用户自己处理了点击后的逻辑处理，则返回 true，否则返回 false，false 走融云默认处理方式。
         */
        @Override
        public boolean onUserPortraitLongClick(Context context, Conversation.ConversationType conversationType, UserInfo userInfo) {
            return false;
        }

        /**
         * 当点击消息时执行。
         *
         * @param context 上下文。
         * @param view    触发点击的 View。
         * @param message 被点击的消息的实体信息。
         * @return 如果用户自己处理了点击后的逻辑处理，则返回 true， 否则返回 false, false 走融云默认处理方式。
         */
        @Override
        public boolean onMessageClick(Context context, View view, Message message) {
            return false;
        }

        /**
         * 当长按消息时执行。
         *
         * @param context 上下文。
         * @param view    触发点击的 View。
         * @param message 被长按的消息的实体信息。
         * @return 如果用户自己处理了长按后的逻辑处理，则返回 true，否则返回 false，false 走融云默认处理方式。
         */
        @Override
        public boolean onMessageLongClick(Context context, View view, Message message) {
            return false;
        }
    }


    private void initConnectionStatusListener() {

        if (RongIM.getInstance() != null && RongIM.getInstance().getRongIMClient() != null) {
            /**
             * 设置连接状态变化的监听器.
             */
            RongIM.getInstance().getRongIMClient().setConnectionStatusListener(new MyConnectionStatusListener());
        }

    }

    /**
     * 连接状态监听器，以获取连接相关状态。
     */
    private class MyConnectionStatusListener implements RongIMClient.ConnectionStatusListener {

        @Override
        public void onChanged(ConnectionStatus connectionStatus) {

            switch (connectionStatus) {

                case CONNECTED://连接成功。

                    break;
                case DISCONNECTED://断开连接。

                    break;
                case CONNECTING://连接中。

                    break;
                case NETWORK_UNAVAILABLE://网络不可用。

                    break;
                case KICKED_OFFLINE_BY_OTHER_CLIENT://用户账户在其他设备登录，本机会被踢掉线

                    break;
            }
        }
    }


    private void initReciveMessage() {
        if (RongIM.getInstance() != null && RongIM.getInstance().getRongIMClient() != null) {
            // 设置接收消息的监听器。
            RongIM.getInstance().getRongIMClient().setOnReceiveMessageListener(new MyReceiveMessageListener());
        }

    }

    /**
     * 设置接收消息的监听器。
     * <p/>
     * 所有接收到的消息、通知、状态都经由此处设置的监听器处理。包括私聊消息、讨论组消息、群组消息、聊天室消息以及各种状态。
     */
    private class MyReceiveMessageListener implements RongIMClient.OnReceiveMessageListener {

        /**
         * 收到消息的处理。
         *
         * @param message 收到的消息实体。
         * @param left    剩余未拉取消息数目。
         * @return 收到消息是否处理完成。
         */
        @Override
        public boolean onReceived(Message message, int left) {

            return false;
        }
    }


    private void initSendMessage() {

        if (RongIM.getInstance() != null) {
            //获取自己发出的消息监听器。
            RongIM.getInstance().setSendMessageListener(new MySendMessageListener());
        }
    }


    /**
     * 实现 OnSendMessageListener 接口
     */
    private class MySendMessageListener implements RongIM.OnSendMessageListener {

        /**
         * 消息发送前监听器处理接口（是否发送成功可以从SentStatus属性获取）。
         *
         * @param message 发送的消息实例。
         * @return 处理后的消息实例。
         */
        @Override
        public Message onSend(Message message) {
            //开发者根据自己需求自行处理

            return message;
        }

        /**
         * 消息发送后回调接口。
         *
         * @param message 消息实例。
         */
        @Override
        public void onSent(Message message) {

        }
    }


    private DeleteEvent(Context context) {
        this.mContext = context;
        initDefaultListener();
    }

    /**
     * 初始化 RongCloud.
     *
     * @param context 上下文。
     */
    public static void init(Context context) {

        if (sDeleteEvent == null) {

            synchronized (RongCloudEvent.class) {

                if (sDeleteEvent == null) {
                    sDeleteEvent = new DeleteEvent(context);
                }
            }
        }
    }
}
