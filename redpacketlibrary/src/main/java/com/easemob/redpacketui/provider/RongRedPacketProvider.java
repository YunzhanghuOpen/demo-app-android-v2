package com.easemob.redpacketui.provider;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.View;

import com.easemob.redpacketsdk.bean.RedPacketInfo;
import com.easemob.redpacketsdk.constant.RPConstant;
import com.easemob.redpacketui.R;
import com.easemob.redpacketui.RPContext;
import com.easemob.redpacketui.message.RongRedPacketMessage;
import com.easemob.redpacketui.ui.activity.RPRedPacketActivity;

import io.rong.imkit.RongContext;
import io.rong.imkit.RongIM;
import io.rong.imkit.widget.provider.InputProvider;
import io.rong.imlib.RongIMClient;

/**
 * 自定义扩展栏红包提供者
 *
 * @author desert
 * @date 2016-05-17
 */
public class RongRedPacketProvider extends InputProvider.ExtendProvider {
    private static final String TAG = RongRedPacketProvider.class.getSimpleName();
    HandlerThread mWorkThread;
    Handler mUploadHandler;
    private Context mContext;

    public RongRedPacketProvider(RongContext context) {
        super(context);
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
        final Intent intent = new Intent(mContext, RPRedPacketActivity.class);
        final RedPacketInfo redPacketInfo = new RedPacketInfo();
        redPacketInfo.fromAvatarUrl = RPContext.getInstance().getUserAvatar();//发送者头像
        redPacketInfo.fromNickName = RPContext.getInstance().getUserName();//发送者名字
        //接收者Id或者接收的群Id
        redPacketInfo.toUserId = getCurrentConversation().getTargetId(); //接受者id
        redPacketInfo.chatType = RPConstant.CHATTYPE_SINGLE;
        intent.putExtra(RPConstant.EXTRA_MONEY_INFO, redPacketInfo);
        startActivityForResult(intent, RPContext.REQUEST_CODE_SEND_MONEY);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.e(TAG, "--红包界面返回--" + "-requestCode-" + requestCode + "-resultCode-" + resultCode + "-data-" + data);

        //接受返回的红包信息,并发送红包消息
        if (resultCode==Activity.RESULT_OK&&data != null && requestCode == RPContext.REQUEST_CODE_SEND_MONEY) {
            String greeting = data.getStringExtra(RPConstant.EXTRA_MONEY_GREETING);
            String moneyID = data.getStringExtra(RPConstant.EXTRA_CHECK_MONEY_ID);

            RongRedPacketMessage message = RongRedPacketMessage.obtain(RPContext.getInstance().getUserID(), RPContext.getInstance().getUserName(), greeting,moneyID,"1","融云红包");
            Log.e(TAG, "--红包界面返回--" + "-moneyID-" + moneyID + "-greeting-" + greeting);
            mUploadHandler.post(new MyRunnable(message));
        }
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
                        Log.e("RedPacketProvider", "-----onError--" + errorCode);
                    }

                    @Override
                    public void onSuccess(Integer integer) {
                        Log.e("RedPacketProvider", "-----onSuccess--" + integer);
                    }
                });
            }

        }
    }

}
