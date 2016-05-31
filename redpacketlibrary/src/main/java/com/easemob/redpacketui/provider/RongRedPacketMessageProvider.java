package com.easemob.redpacketui.provider;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v4.app.FragmentActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.easemob.redpacketsdk.bean.RedPacketInfo;
import com.easemob.redpacketsdk.constant.RPConstant;
import com.easemob.redpacketui.R;
import com.easemob.redpacketui.RPContext;
import com.easemob.redpacketui.message.RongEmptyMessage;
import com.easemob.redpacketui.message.RongNotificationMessage;
import com.easemob.redpacketui.message.RongRedPacketMessage;
import com.easemob.redpacketui.utils.RPOpenPacketUtil;

import io.rong.imkit.RongIM;
import io.rong.imkit.model.ProviderTag;
import io.rong.imkit.model.UIMessage;
import io.rong.imkit.widget.ArraysDialogFragment;
import io.rong.imkit.widget.provider.IContainerItemProvider;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Message;


/**
 * 自定义单聊红包提供者
 *
 * @author desert
 * @date 2016-05-23
 */
@ProviderTag(messageContent = RongRedPacketMessage.class, showPortrait = true, showProgress = false, centerInHorizontal = false)
// 会话界面自定义UI注解
public class RongRedPacketMessageProvider extends IContainerItemProvider.MessageProvider<RongRedPacketMessage> {
    private Context mContext;

    public RongRedPacketMessageProvider(Context mContext) {
        super();
        this.mContext = mContext;
    }

    /**
     * 初始化View
     */
    @Override
    public View newView(Context context, ViewGroup group) {
        View view = LayoutInflater.from(context).inflate(R.layout.yzh_customize_message_red_packet, null);
        ViewHolder holder = new ViewHolder();
        holder.greeting = (TextView) view.findViewById(R.id.tv_money_greeting);
        holder.sponsor = (TextView) view.findViewById(R.id.tv_sponsor_name);
        holder.view = view.findViewById(R.id.bubble);
        view.setTag(holder);
        this.mContext = context;
        return view;
    }

    @Override
    public void bindView(View v, int position, RongRedPacketMessage content, UIMessage message) {
        ViewHolder holder = (ViewHolder) v.getTag();

        // 更改气泡样式
        if (message.getMessageDirection() == UIMessage.MessageDirection.SEND) {
            // 消息方向，自己发送的
            holder.view.setBackgroundResource(R.drawable.yzh_money_chatto_bg);
        } else {
            // 消息方向，别人发送的
            holder.view.setBackgroundResource(R.drawable.yzh_money_chatfrom_bg);
        }
        holder.greeting.setText(content.getMessage()); // 设置问候语
        holder.sponsor.setText(content.getSponsorName()); // 设置赞助商
    }

    @Override
    public Spannable getContentSummary(RongRedPacketMessage data) {
        if (data != null && !TextUtils.isEmpty(data.getMessage()))
            return new SpannableString(data.getMessage());
        return null;
    }

    @Override
    public void onItemClick(View view, int position, final RongRedPacketMessage content, final UIMessage message) {
        final ProgressDialog progressDialog = new ProgressDialog(mContext);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCanceledOnTouchOutside(false);
        RedPacketInfo packetInfo = new RedPacketInfo();
        //获取红包id
        packetInfo.moneyID = content.getMoneyID();
        Log.e("yzh", "-打开红包成功-getMoneyID-" + content.getMoneyID());
        //获取名字和头像url
        packetInfo.toAvatarUrl = RPContext.getInstance().getUserAvatar();
        packetInfo.toNickName = RPContext.getInstance().getUserName();
        //判断发送方还是接收方
        if (message.getMessageDirection() == UIMessage.MessageDirection.SEND) {
            packetInfo.moneyMsgDirect = RPConstant.MESSAGE_DIRECT_SEND;
        } else {
            packetInfo.moneyMsgDirect = RPConstant.MESSAGE_DIRECT_RECEIVE;
        }
        //获取聊天类型/单聊群聊
        if (message.getConversationType() == Conversation.ConversationType.PRIVATE) {//单聊
            packetInfo.chatType = RPConstant.CHATTYPE_SINGLE;
        } else {//群聊
            packetInfo.chatType = RPConstant.CHATTYPE_GROUP;
        }
        RPOpenPacketUtil.getInstance().openRedPacket(packetInfo, (FragmentActivity) mContext, new RPOpenPacketUtil.RPOpenPacketCallBack() {
            @Override
            public void onSuccess(String s, String s1) {
                //打开红包消息成功,然后发送回执消息例如"你领取了XX的红包"
                sendAckMsg(content, message, RPContext.getInstance().getUserName());
            }

            @Override
            public void showLoading() {
                progressDialog.show();

            }

            @Override
            public void hideLoading() {
                progressDialog.dismiss();
            }

            @Override
            public void onError(String s, String s1) {
                //错误处理
                Toast.makeText(mContext, s, Toast.LENGTH_SHORT).show();
            }
        });


    }

    @Override
    public void onItemLongClick(View view, int position, RongRedPacketMessage content, final UIMessage message) {

        String[] items;
        items = new String[]{view.getContext().getResources().getString(R.string.yzh_dialog_item_delete)};
        ArraysDialogFragment.newInstance("", items).setArraysDialogItemListener(new ArraysDialogFragment.OnArraysDialogItemListener() {
            @Override
            public void OnArraysDialogItemClick(DialogInterface dialog, int which) {
                if (which == 0)
                    RongIM.getInstance().getRongIMClient().deleteMessages(new int[]{message.getMessageId()}, null);

            }
        }).show(((FragmentActivity) view.getContext()).getSupportFragmentManager());
    }

    public void sendAckMsg(RongRedPacketMessage content, UIMessage message, String receiveName) {
        String receiveID = RPContext.getInstance().getUserID();
        RongNotificationMessage rongNotificationMessage = RongNotificationMessage.obtain(content.getSendUserID(), content.getSendUserName(), receiveID, receiveName,"1");
        final RongEmptyMessage rongEmptyMessage = RongEmptyMessage.obtain(content.getSendUserID(), content.getSendUserName(), receiveID, receiveName,"1");
        if (message.getConversationType() == Conversation.ConversationType.PRIVATE) {
            RongIM.getInstance().getRongIMClient().sendMessage(message.getConversationType(), content.getSendUserID(), rongNotificationMessage, null, null, new RongIMClient.SendMessageCallback() {
                @Override
                public void onError(Integer integer, RongIMClient.ErrorCode errorCode) {
                    Log.e("yzh", "-单聊发送回执消息失败-");

                }

                @Override
                public void onSuccess(Integer integer) {

                    Log.e("yzh", "-单聊发送回执消息成功-");
                }
            }, null);
        } else {//群聊讨论组
            if (content.getSendUserID().equals(receiveID)) {//自己领取了自己的红包
                RongIM.getInstance().getRongIMClient().insertMessage(message.getConversationType(), message.getTargetId(), receiveID, rongNotificationMessage, null);
            } else {
                RongIM.getInstance().getRongIMClient().sendMessage(message.getConversationType(), message.getTargetId(), rongEmptyMessage, null, null, new RongIMClient.SendMessageCallback() {
                    @Override
                    public void onError(Integer integer, RongIMClient.ErrorCode errorCode) {
                        Log.e("yzh", "-发送空消息通知类失败-");

                    }

                    @Override
                    public void onSuccess(Integer integer) {

                        Log.e("yzh", "-发送空消息通知类成功-");
                    }
                }, new RongIMClient.ResultCallback<Message>() {
                    @Override
                    public void onSuccess(Message message) {
                        Log.e("dxf","--message--"+message.toString());
                        RongEmptyMessage message1=(RongEmptyMessage)message.getContent();
                        Log.e("dxf","--message--"+message1.toString());

                    }

                    @Override
                    public void onError(RongIMClient.ErrorCode errorCode) {

                    }
                });
                RongIM.getInstance().getRongIMClient().insertMessage(message.getConversationType(), message.getTargetId(), receiveID, rongNotificationMessage, null);
            }
        }


    }


    class ViewHolder {
        TextView greeting, sponsor;
        View view;
    }

}
