package io.rong.app.provider;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Date;
import java.util.List;

import io.rong.app.R;
import io.rong.app.activity.MainActivity;
import io.rong.app.model.ApiResult;
import io.rong.imkit.RongContext;
import io.rong.imkit.model.ConversationKey;
import io.rong.imkit.model.ConversationProviderTag;
import io.rong.imkit.model.ProviderTag;
import io.rong.imkit.model.UIConversation;
import io.rong.imkit.util.AndroidEmoji;
import io.rong.imkit.util.RongDateUtils;
import io.rong.imkit.widget.provider.IContainerItemProvider;

@ConversationProviderTag(conversationType = "discussion", portraitPosition = 1)
public class NewDiscussionConversationProvider implements IContainerItemProvider.ConversationProvider<UIConversation> {

    private static final String TAG = NewDiscussionConversationProvider.class.getSimpleName();
    private ReceiveMessageBroadcastReciver mBroadcastReciver;
    private boolean hasRead = false;
    private boolean ifFirst = false;
    String targetid = null;
    private List<ApiResult> list;

    class ViewHolder {

        TextView title;
        TextView time;
        TextView content;
        ImageView notificationBlockImage;
        TextView atMe;
        final NewDiscussionConversationProvider provider;

        ViewHolder() {
            provider = NewDiscussionConversationProvider.this;
        }
    }

    public NewDiscussionConversationProvider() {

    }

    private class ReceiveMessageBroadcastReciver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(MainActivity.ACTION_DMEO_AT_ME)) {
                hasRead = true;
                targetid = intent.getStringExtra("DEMO_TARGETID");
            }
        }
    }

    @Override
    public void bindView(View view, int position, UIConversation data) {

        ViewHolder holder = (ViewHolder) view.getTag();
        ProviderTag tag = null;

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MainActivity.ACTION_DMEO_AT_ME);
        if (mBroadcastReciver == null) {
            mBroadcastReciver = new ReceiveMessageBroadcastReciver();
        }
        view.getContext().registerReceiver(mBroadcastReciver, intentFilter);

        if (data == null) {
            holder.title.setText(null);
            holder.time.setText(null);
            holder.content.setText(null);
        } else {
            //设置会话标题
            holder.title.setText(data.getUIConversationTitle());
            //设置会话时间
            String time = RongDateUtils.getConversationListFormatDate(new Date(data.getUIConversationTime()));
            holder.time.setText(time);
            //设置内容
            if (data.getShowDraftFlag()) {
                SpannableStringBuilder builder = new SpannableStringBuilder();
                SpannableString string = new SpannableString(view.getContext().getString(R.string.de_message_content_draft));
                string.setSpan(new ForegroundColorSpan(view.getContext().getResources().getColor(R.color.de_draft_color)), 0, string.length(), 33);
                builder.append(string).append(" : ").append(data.getDraft());
                AndroidEmoji.ensure(builder);
                holder.content.setText(builder);
                Log.e("Tag", "-------草稿-----" + data.getDraft());
            } else {
                setDateView3(holder, view, position, data);
            }
            if (RongContext.getInstance() != null && data.getMessageContent() != null)
                tag = RongContext.getInstance().getMessageProviderTag(data.getMessageContent().getClass());
            if (data.getSentStatus() != null && (data.getSentStatus() == io.rong.imlib.model.Message.SentStatus.FAILED || data.getSentStatus() == io.rong.imlib.model.Message.SentStatus.SENDING) && tag != null && tag.showWarning()) {
                int width = (int) view.getContext().getResources().getDimension(R.dimen.de_message_send_status_image_size);
                Drawable drawable = null;
                if (data.getSentStatus() == io.rong.imlib.model.Message.SentStatus.FAILED)
                    drawable = view.getContext().getResources().getDrawable(R.drawable.de_conversation_list_msg_send_failure);
                else if (data.getSentStatus() == io.rong.imlib.model.Message.SentStatus.SENDING)
                    drawable = view.getContext().getResources().getDrawable(R.drawable.de_conversation_list_msg_sending);
                if (drawable != null) {
                    drawable.setBounds(0, 0, width, width);
                    holder.content.setCompoundDrawablePadding(10);
                    holder.content.setCompoundDrawables(drawable, null, null, null);
                }
            } else {
                holder.content.setCompoundDrawables(null, null, null, null);
            }
            ConversationKey key = ConversationKey.obtain(data.getConversationTargetId(), data.getConversationType());
            io.rong.imlib.model.Conversation.ConversationNotificationStatus status = RongContext.getInstance().getConversationNotifyStatusFromCache(key);
            if (status != null && status.equals(io.rong.imlib.model.Conversation.ConversationNotificationStatus.DO_NOT_DISTURB))
                holder.notificationBlockImage.setVisibility(View.VISIBLE);
            else
                holder.notificationBlockImage.setVisibility(View.GONE);
        }
    }

    /**
     * 设置 View 数据
     *
     * @param holder   ViewHolder
     * @param view     view
     * @param position 位置
     * @param data     UIConversation
     */
    private void setDateView3(ViewHolder holder, View view, int position, UIConversation data) {

//        String userId = null;
//        String username = null;
//
//        Map<String, Boolean> hasReadMap = new HashMap<String, Boolean>();
//
//        if (DemoContext.getInstance().getSharedPreferences() != null) {
//            userId = DemoContext.getInstance().getSharedPreferences().getString("DEMO_USERID", "default");
//            username = DemoContext.getInstance().getSharedPreferences().getString("DEMO_USER_NAME", "default");
//        }
//
//        MessageContent messageContent = data.getMessageContent();
//
//        if (messageContent instanceof TextMessage) {
//
//            if (((TextMessage) messageContent).getContent().contains("@" + userId)
//                    || ((TextMessage) messageContent).getContent().contains("@" + username)) {
//                ifFirst = true;
//                if (data.getUnReadMessageCount() == 0) {
//                    holder.atMe.setVisibility(View.GONE);
//                    data.setIsRead(true);
//                    Log.e(TAG, "1-------全部已读------");
//                } else if (data.getUnReadMessageCount() > 0) {
//                    Log.e(TAG, "1-------未读消息= ------data.isRead()--" + data.isRead());
//                    if(data.isRead()){
//
//                        holder.atMe.setVisibility(View.GONE);
//                    }else{
//                        holder.atMe.setVisibility(View.VISIBLE);
//                    }
//                    Log.e(TAG, "1-------未读消息= 11111111------position--" + position);
//                    data.setIsRead(false);
////                    hasReadMap.put(data.getConversationTargetId(), false);
//                }
//            } else {
//                if (ifFirst) {
//                    if (hasRead) {
//                        Log.e(TAG, "1------已点击-------position-" + position);
////                    hasReadMap.put(position, true);
//                        data.setIsRead(true);
//                        hasRead = false;
//                    }
//
//                    if (data.isRead()) {
//                        holder.atMe.setVisibility(View.GONE);
//                    } else {
//                        holder.atMe.setVisibility(View.VISIBLE);
//                    }
//
//                } else {
//                    Log.e(TAG, "1-------------不是第一次-");
//                    holder.atMe.setVisibility(View.GONE);
//                }
//            }
//            holder.content.setText(data.getConversationContent());
//        } else {
//            holder.content.setText(data.getConversationContent());
//        }

    }

//    /**
//     * 设置 View 数据
//     *
//     * @param holder   ViewHolder
//     * @param view     view
//     * @param position 位置
//     * @param data     UIConversation
//     */
//    private void setDateView2(ViewHolder holder, View view, int position, UIConversation data) {
//
//        String userId = null;
//        String username = null;
//
//        Map<Integer, Boolean> hasReadMap = new HashMap<Integer, Boolean>();
//
//        if (DemoContext.getInstance().getSharedPreferences() != null) {
//            userId = DemoContext.getInstance().getSharedPreferences().getString("DEMO_USERID", "default");
//            username = DemoContext.getInstance().getSharedPreferences().getString("DEMO_USER_NAME", "default");
//        }
//
//        MessageContent messageContent = data.getMessageContent();
//
//        if (messageContent instanceof TextMessage) {
//
//            if (((TextMessage) messageContent).getContent().contains("@" + userId)
//                    || ((TextMessage) messageContent).getContent().contains("@" + username)) {
//                TextMessage textMessage = (TextMessage) messageContent;
//
//                data.getSentStatus();
//                twoRead = true;
//                if (data.getUnReadMessageCount() == 0) {
//                    holder.atMe.setVisibility(View.GONE);
//
//                    Log.e(TAG, "1-------全部已读------");
//                } else if (data.getUnReadMessageCount() > 0) {
//                    holder.atMe.setVisibility(View.VISIBLE);
//                    Log.e(TAG, "1-------hasRead= 11111111------position--" + position);
//                    hasReadMap.put(position, false);
//
//                }
//
//            } else {
//
//            }
//            holder.content.setText(data.getConversationContent());
//        } else {
//            holder.content.setText(data.getConversationContent());
//        }
//
//    }
//
//    /**
//     * 设置 View 数据
//     *
//     * @param holder   ViewHolder
//     * @param view     view
//     * @param position 位置
//     * @param data     UIConversation
//     */
//    private void setDateView1(ViewHolder holder, View view, int position, UIConversation data) {
//
//        String userId = null;
//        String username = null;
//
//        Map<Integer, Boolean> hasReadMap = new HashMap<Integer, Boolean>();
//
//        if (DemoContext.getInstance().getSharedPreferences() != null) {
//            userId = DemoContext.getInstance().getSharedPreferences().getString("DEMO_USERID", "default");
//            username = DemoContext.getInstance().getSharedPreferences().getString("DEMO_USER_NAME", "default");
//        }
//
//        MessageContent messageContent = data.getMessageContent();
//
//        if (messageContent instanceof TextMessage) {
//
//            if (((TextMessage) messageContent).getContent().contains("@" + userId)
//                    || ((TextMessage) messageContent).getContent().contains("@" + username)) {
//                twoRead = true;
//
//                if (data.getUnReadMessageCount() == 0) {
//                    holder.atMe.setVisibility(View.GONE);
//
//                    Log.e(TAG, "1-------全部已读------");
//                } else if (data.getUnReadMessageCount() > 0) {
//                    holder.atMe.setVisibility(View.VISIBLE);
//                    Log.e(TAG, "1-------hasRead= 11111111------position--" + position);
//                    hasReadMap.put(position, false);
//
//                }
//
//            } else {
//                if (twoRead) {
//                    if (hasRead) {
//                        Log.e(TAG, "1------已点击-------position-" + position);
//                        hasReadMap.put(position, true);
//                        hasRead = false;
//                    }
//
//                    if (hasReadMap.containsKey(position)) {
//
//                        if (hasReadMap.get(position)) {
//
//                            Log.e(TAG, "1------已点击-----hasReadMap---" + hasReadMap.get(position) + "---position:" + position);
//                            holder.atMe.setVisibility(View.GONE);
//
//                        } else {
//                            Log.e(TAG, "1------未点击-----hasReadMap---" + hasReadMap.get(position) + "---position:" + position);
//                            holder.atMe.setVisibility(View.VISIBLE);
//                            if (data.getUnReadMessageCount() == 0) {
//                                holder.atMe.setVisibility(View.GONE);
//                            } else if (data.getUnReadMessageCount() > 0) {
//                                holder.atMe.setVisibility(View.VISIBLE);
//                                Log.e(TAG, "1-------hasRead= position------position--" + position);
//                            }
//                        }
//                    }
//                }else{
//                    Log.e(TAG, "1-------hasRead= isTwoRead-------" );
//                }
//            }
//            holder.content.setText(data.getConversationContent());
//        } else {
//            holder.content.setText(data.getConversationContent());
//        }
//
//    }

//    /**
//     * 设置 View 数据
//     *
//     * @param holder   ViewHolder
//     * @param view     view
//     * @param position 位置
//     * @param data     UIConversation
//     */
//    private void setDateView(ViewHolder holder, View view, int position, UIConversation data) {
//
//        String userId = null;
//        String username = null;
//
//        if (DemoContext.getInstance().getSharedPreferences() != null) {
//            userId = DemoContext.getInstance().getSharedPreferences().getString("DEMO_USERID", "default");
//            username = DemoContext.getInstance().getSharedPreferences().getString("DEMO_USER_NAME", "default");
//        }
//        //接收消息
////                if (data.getConversationSenderId() != userId) {
//
//
//        MessageContent messageContent = data.getMessageContent();
//        String targetId = data.getConversationTargetId();
//
//        if (messageContent instanceof TextMessage) {
//
//            if (((TextMessage) messageContent).getContent().contains("@" + userId)
//                    || ((TextMessage) messageContent).getContent().contains("@" + username)) {
//
//                if (data.getUnReadMessageCount() == 0) {
//                    holder.atMe.setVisibility(View.GONE);
//
//                    Log.e("Tag", "-------全部已读------");
//                } else if (data.getUnReadMessageCount() > 0) {
//                    holder.atMe.setVisibility(View.VISIBLE);
//                    Log.e("Tag", "-------hasRead= 11111111--------");
//                    hasRead = false;
//                }
//            } else {
//
//                if (hasRead) {
//                    Log.e("Tag", "-------hasRead= true--------");
//                    holder.atMe.setVisibility(View.GONE);
//                } else {
//                    Log.e("Tag", "-------hasRead= false--------");
//                    if (data.getUnReadMessageCount() == 0) {
//                        holder.atMe.setVisibility(View.GONE);
//                    } else if (data.getUnReadMessageCount() > 0) {
//                        Log.e("Tag", "-------hasRead= false----getUnReadMessageCount> 0----" + ((TextMessage) messageContent).getContent());
//                        if (((TextMessage) messageContent).getContent().contains("@" + userId)
//                                || ((TextMessage) messageContent).getContent().contains("@" + username)) {
//                            Log.e("Tag", "-------hasRead= 444444--------");
//                            if (data.getUnReadMessageCount() == 0) {
//                                holder.atMe.setVisibility(View.GONE);
//                                Log.e("Tag", "-------hasRead= 555555--------");
//                            } else if (data.getUnReadMessageCount() > 0) {
//                                holder.atMe.setVisibility(View.VISIBLE);
//                                Log.e("Tag", "-------hasRead= 333333--------");
//                                twoRead = true;
//                            }
//                        }
//                    }
//                }
//
//                if (twoRead) {
//                    holder.atMe.setVisibility(View.VISIBLE);
//
//                }
//                if (isRead) {
//                    Log.e("Tag", "-----已经点击查看此条消息------");
//
//                    holder.atMe.setVisibility(View.GONE);
//                    isRead = false;
//                    hasRead = true;
//                    twoRead = false;
//                }
//            }
//            holder.content.setText(data.getConversationContent());
//        } else {
//            holder.content.setText(data.getConversationContent());
//        }
////                } else {
////                    holder.content.setText(data.getConversationContent());
////                }
//
//    }

    @Override
    public View newView(Context context, ViewGroup viewgroup) {
        // TODO Auto-generated method stub
        View result = LayoutInflater.from(context).inflate(R.layout.de_item_base_conversation, null);
        ViewHolder holder = new ViewHolder();
        holder.title = (TextView) result.findViewById(R.id.de_conversation_title);
        holder.time = (TextView) result.findViewById(R.id.de_conversation_time);
        holder.content = (TextView) result.findViewById(R.id.de_conversation_content);
        holder.notificationBlockImage = (ImageView) result.findViewById(R.id.de_conversation_msg_block);
        holder.atMe = (TextView) result.findViewById(R.id.de_at_me);
        result.setTag(holder);
        return result;
    }

    @Override
    public String getTitle(String s) {
        // TODO Auto-generated method stub
        String name;
        if (RongContext.getInstance().getDiscussionInfoFromCache(s) == null)
            name = RongContext.getInstance().getResources().getString(R.string.de_group_list_default_discussion_name);
        else
            name = RongContext.getInstance().getDiscussionInfoFromCache(s).getName();
        return name;
    }

    @Override
    public Uri getPortraitUri(String s) {
        // TODO Auto-generated method stub
        return null;
    }

}
