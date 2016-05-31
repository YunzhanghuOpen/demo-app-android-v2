package com.easemob.redpacketui.provider;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.easemob.redpacketsdk.bean.RedPacketInfo;
import com.easemob.redpacketsdk.constant.RPConstant;
import com.easemob.redpacketui.R;
import com.easemob.redpacketui.RPContext;
import com.easemob.redpacketui.callback.GetGroupInfoCallback;
import com.easemob.redpacketui.callback.ToRedPacketActivity;
import com.easemob.redpacketui.message.RongRedPacketMessage;
import com.easemob.redpacketui.ui.activity.RPRedPacketActivity;

import io.rong.imkit.RongContext;
import io.rong.imkit.RongIM;
import io.rong.imkit.widget.provider.InputProvider;
import io.rong.imlib.RongIMClient;

/**
 * 自定义群红包提供者
 * @author desert
 * @date 2016-05-23
 */
public class RongGroupRedPacketProvider extends InputProvider.ExtendProvider implements ToRedPacketActivity {
    private static final String TAG = RongGroupRedPacketProvider.class.getSimpleName();
    HandlerThread mWorkThread;
    Handler mUploadHandler;
    private static Context mContext;
    private GetGroupInfoCallback callback;
    private static RedPacketInfo redPacketInfo;

    public RongGroupRedPacketProvider(RongContext context, GetGroupInfoCallback callback) {
        super(context);
        this.callback = callback;
        this.mContext = context;
        mWorkThread = new HandlerThread("YZHRedPacket");
        mWorkThread.start();
        mUploadHandler = new Handler(mWorkThread.getLooper());
    }

    /**
     * 设置展示的图标
     *
     * @param context
     * @return
     */
    @Override
    public Drawable obtainPluginDrawable(Context context) {
        return context.getResources().getDrawable(R.drawable.yzh_chat_money_provider);
    }

    /**
     * 设置图标下的title
     *
     * @param context
     * @return
     */
    @Override
    public CharSequence obtainPluginTitle(Context context) {
        return context.getString(R.string.red_packet);
    }

    /**
     * click 事件，在这里做跳转
     *
     * @param view
     */
    @Override
    public void onPluginClick(View view) {

        redPacketInfo = new RedPacketInfo();
        redPacketInfo.fromAvatarUrl = RPContext.getInstance().getUserAvatar(); //发送者头像url
        redPacketInfo.fromNickName = RPContext.getInstance().getUserName();//发送者昵称 设置了昵称就传昵称 否则传id
        redPacketInfo.toGroupId = getCurrentConversation().getTargetId();//群ID
        redPacketInfo.chatType = RPConstant.CHATTYPE_GROUP;
        if (callback != null) {
            callback.getGroupPersonNumber(redPacketInfo.toGroupId, this);
        } else {
            Toast.makeText(mContext, "回调函数不能为空", Toast.LENGTH_SHORT).show();
        }


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode != Activity.RESULT_OK)
            return;
        //接受返回的红包信息,并发送红包消息
        if (data != null && requestCode == RPContext.REQUEST_CODE_SEND_MONEY) {
            String greeting = data.getStringExtra(RPConstant.EXTRA_MONEY_GREETING);
            String moneyID = data.getStringExtra(RPConstant.EXTRA_CHECK_MONEY_ID);
            String userId = RPContext.getInstance().getUserID();
            String userName = RPContext.getInstance().getUserName();
            RongRedPacketMessage message = RongRedPacketMessage.obtain(userId, userName, greeting, moneyID,"1","融云红包");
            Log.e("yzh", "--红包界面返回--" + "-moneyID-" + moneyID + "-greeting-" + greeting);
            mUploadHandler.post(new MyRunnable(message));
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void toRedPacketActivity(int number) {
        Intent intent = new Intent(mContext, RPRedPacketActivity.class);
        redPacketInfo.groupMemberCount = number;
        intent.putExtra(RPConstant.EXTRA_MONEY_INFO, redPacketInfo);
        startActivityForResult(intent, RPContext.REQUEST_CODE_SEND_MONEY);
    }


    class MyRunnable implements Runnable {

        RongRedPacketMessage mMessage;

        public MyRunnable(RongRedPacketMessage message) {
            mMessage = message;
        }

        @Override
        public void run() {
            if (RongIM.getInstance() != null && RongIM.getInstance().getRongIMClient() != null) {

                RongIM.getInstance().getRongIMClient().sendMessage(getCurrentConversation().getConversationType(), getCurrentConversation().getTargetId(), mMessage, null, null, new RongIMClient.SendMessageCallback() {
                    @Override
                    public void onError(Integer integer, RongIMClient.ErrorCode errorCode) {
                        Log.e("PacketProvider", "-----onError--" + errorCode);
                    }

                    @Override
                    public void onSuccess(Integer integer) {
                        Log.e("PacketProvider", "-----onSuccess--" + integer);
                    }
                });
            }

        }
    }

}
