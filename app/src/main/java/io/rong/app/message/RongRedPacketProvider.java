package io.rong.app.message;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;

import com.easemob.luckymoneylibrary.bean.MoneyInfo;
import com.easemob.luckymoneylibrary.constant.LmConstant;
import com.easemob.luckymoneylibrary.ui.activity.LuckyMoneyActivity;

import io.rong.app.R;
import io.rong.app.utils.Constants;
import io.rong.imkit.RongContext;
import io.rong.imkit.RongIM;
import io.rong.imkit.widget.provider.InputProvider;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;

/**
 *
 * @author lsy
 * @date 2015-11-19
 */
public class RongRedPacketProvider extends InputProvider.ExtendProvider {
    private static final String TAG=RongRedPacketProvider.class.getSimpleName();
    HandlerThread mWorkThread;
    Handler mUploadHandler;
    private int REQUEST_CONTACT = 20;
    private Context mContext;


    public RongRedPacketProvider(RongContext context) {
        super(context);
        this.mContext = context;
        mWorkThread = new HandlerThread("RongDemo");
        mWorkThread.start();
        mUploadHandler = new Handler(mWorkThread.getLooper());
    }

    /**
     * 设置展示的图标
     * @param context
     * @return
     */
    @Override
    public Drawable obtainPluginDrawable(Context context) {
        return context.getResources().getDrawable(R.drawable.ease_chat_money_selector);
    }

    /**
     * 设置图标下的title
     * @param context
     * @return
     */
    @Override
    public CharSequence obtainPluginTitle(Context context) {
        return context.getString(R.string.red_packet);
    }

    /**
     * click 事件，在这里做跳转
     * @param view
     */
    @Override
    public void onPluginClick(View view) {
        Intent intent = new Intent(mContext, LuckyMoneyActivity.class);
        //发送者头像url
       String fromAvatarUrl = "";
        //发送者昵称 设置了昵称就传昵称 否则传id
        String fromNickname = "";
       fromNickname= PreferenceManager.getDefaultSharedPreferences(mContext).getString(Constants.APP_USER_NAME,"default");
        fromAvatarUrl= PreferenceManager.getDefaultSharedPreferences(mContext).getString(Constants.APP_USER_PORTRAIT,"default");

//        EaseUser easeUser = EaseUserUtils.getUserInfo(EMChatManager.getInstance().getCurrentUser());
//        if (easeUser != null) {
//            fromAvatarUrl = TextUtils.isEmpty(easeUser.getAvatar()) ? "none" : easeUser.getAvatar();
//            fromNickname = TextUtils.isEmpty(easeUser.getNick()) ? easeUser.getUsername() : easeUser.getNick();
////        }
        MoneyInfo moneyInfo = new MoneyInfo();
        moneyInfo.fromAvatarUrl = fromAvatarUrl;
        moneyInfo.fromNickName = fromNickname;
        //接收者Id或者接收的群Id
        if (getCurrentConversation().getConversationType() == Conversation.ConversationType.PRIVATE) {
            moneyInfo.toUserId = getCurrentConversation().getTargetId();
            moneyInfo.chatType = LmConstant.CHATTYPE_SINGLE;
        } else if(getCurrentConversation().getConversationType()== Conversation.ConversationType.GROUP){
            Log.e(TAG,"--group--send redpacket");
           // EMGroup group = EMGroupManager.getInstance().getGroup(toChatUsername);
              moneyInfo.toGroupId = getCurrentConversation().getTargetId();;
            //group.getAffiliationsCount()方法可能返回-1，需要在进入ChatFragment时异步获取群人数.
           // moneyInfo.groupMemberCount = group.getAffiliationsCount();
            moneyInfo.chatType = LmConstant.CHATTYPE_GROUP;
        }
        intent.putExtra(LmConstant.EXTRA_MONEY_INFO, moneyInfo);
        startActivityForResult(intent, Constants.REQUEST_CODE_SEND_MONEY);

//        if(RongIM.getInstance()!=null && RongIM.getInstance().getRongIMClient()!=null){
//
//            String userid =  PreferenceManager.getDefaultSharedPreferences(mContext).getString("userId","default");
//
//            RongRedPacketMessage rongRedPacketMessage = RongRedPacketMessage.obtain(userid,"给你一个大红包");
//
//            RongIM.getInstance().getRongIMClient().sendMessage(getCurrentConversation().getConversationType(), getCurrentConversation().getTargetId(), rongRedPacketMessage, null, null, new RongIMClient.SendMessageCallback() {
//                @Override
//                public void onError(Integer integer, RongIMClient.ErrorCode errorCode) {
//                    Log.e("RongRedPacketProvider", "-----onError--"+errorCode);
//                }
//
//                @Override
//                public void onSuccess(Integer integer) {
//                    Log.e("RongRedPacketProvider", "-----onSuccess--"+integer);
//                }
//            });
//        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode != Activity.RESULT_OK)
            return;

        if (data.getData() != null && "content".equals(data.getData().getScheme())) {
            mUploadHandler.post(new MyRunnable(data.getData()));
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    class MyRunnable implements Runnable {

        Uri mUri;

        public MyRunnable(Uri uri) {
            mUri = uri;
        }

        @Override
        public void run() {

        }
    }

}
