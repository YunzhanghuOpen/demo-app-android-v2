package io.rong.app.message.provider;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import io.rong.app.R;
import io.rong.app.server.network.async.OnDataListener;
import io.rong.app.server.network.http.HttpException;
import io.rong.app.server.response.GroupNotificationMessageData;
import io.rong.app.server.utils.json.JsonMananger;
import io.rong.imkit.RongContext;
import io.rong.imkit.RongIM;
import io.rong.imkit.model.ProviderTag;
import io.rong.imkit.model.UIMessage;
import io.rong.imkit.widget.provider.IContainerItemProvider;
import io.rong.imlib.model.UserInfo;
import io.rong.message.GroupNotificationMessage;

/**
 * Created by AMing on 16/2/18.
 * Company RongCloud
 */
@ProviderTag(messageContent = GroupNotificationMessage.class, showPortrait = false, centerInHorizontal = true, showProgress = false, showSummaryWithName = false)
public class GroupNotificationMessageProvider extends IContainerItemProvider.MessageProvider<GroupNotificationMessage> implements OnDataListener {

    @Override
    public void bindView(View view, int i, GroupNotificationMessage groupNotificationMessage, UIMessage uiMessage) {
        ViewHolder viewHolder = (ViewHolder) view.getTag();

        if (groupNotificationMessage != null && uiMessage != null) {

            GroupNotificationMessageData data = null;
            try {
                data = JsonMananger.jsonToBean(groupNotificationMessage.getData(), GroupNotificationMessageData.class);
            } catch (HttpException e) {
                e.printStackTrace();
            }

            String operatorNickname = data.getData().getOperatorNickname();
            List<String> memberList = data.getData().getTargetUserDisplayNames();
            String memberName = null;
            if (memberList != null && memberList.size() == 1) {
                memberName = memberList.get(0);
            } else if (memberList != null) {
                StringBuilder sb = new StringBuilder();
                for (String s : memberList) {
                    sb.append(s);
                    sb.append(",");
                }
                String str = sb.toString();
                memberName = str.substring(0, str.length() - 1);
            }
            if (!TextUtils.isEmpty(groupNotificationMessage.getOperation()))
                if (groupNotificationMessage.getOperation().equals("Add")) {
                    viewHolder.contentTextView.setText(operatorNickname + " 邀请 " + memberName + " 加入本群");
                } else if (groupNotificationMessage.getOperation().equals("Kicked")) {
                    viewHolder.contentTextView.setText(operatorNickname + " 将 " + memberName + " 移出本群");
                } else if (groupNotificationMessage.getOperation().equals("Create")) {
                    viewHolder.contentTextView.setText(operatorNickname + " 创建本群");
                } else if (groupNotificationMessage.getOperation().equals("Dismiss")) {
                    viewHolder.contentTextView.setText(operatorNickname + " 解散本群");
                } else if (groupNotificationMessage.getOperation().equals("Quit")) {
                    viewHolder.contentTextView.setText(operatorNickname+" 退出本群");
                }
        }
    }

    @Override
    public Spannable getContentSummary(GroupNotificationMessage groupNotificationMessage) {
        GroupNotificationMessageData data = null;
        try {
            data = JsonMananger.jsonToBean(groupNotificationMessage.getData(), GroupNotificationMessageData.class);
        } catch (HttpException e) {
            e.printStackTrace();
        }
//        String operatorNickname = data.getData().getOperatorNickname();
        List<String> memberList = data.getData().getTargetUserDisplayNames();
        String memberName;
        if (memberList != null && memberList.size() == 1) {
            memberName = memberList.get(0);
        } else {
            StringBuilder sb = new StringBuilder();
            for (String s : memberList) {
                sb.append(s);
                sb.append(",");
            }
            String str = sb.toString();
            memberName = str.substring(0, str.length() - 1);
        }
        if (groupNotificationMessage.getOperation().equals("Add")) {
            return new SpannableString("邀请 " + memberName + " 加入该群");
        } else if (groupNotificationMessage.getOperation().equals("Kicked")) {
            return new SpannableString("将 " + memberName + " 移除该群");
        } else if (groupNotificationMessage.getOperation().equals("Create")) {
            return new SpannableString("创建该群");
        } else if (groupNotificationMessage.getOperation().equals("Dismiss")) {
            return new SpannableString("解散该群");
        } else if (groupNotificationMessage.getOperation().equals("Quit")) {
            return new SpannableString("退出本群");
        }
        return new SpannableString("[群组通知]");
    }

    @Override
    public void onItemClick(View view, int i, GroupNotificationMessage groupNotificationMessage, UIMessage uiMessage) {

    }

    @Override
    public void onItemLongClick(View view, int i, GroupNotificationMessage groupNotificationMessage, UIMessage uiMessage) {

    }

    @Override
    public View newView(Context context, ViewGroup viewGroup) {
        View view = LayoutInflater.from(context).inflate(R.layout.de_item_information_notification_message, null);
        ViewHolder viewHolder = new ViewHolder();
        viewHolder.contentTextView = (TextView) view.findViewById(R.id.rc_msg);
        viewHolder.contentTextView.setMovementMethod(LinkMovementMethod.getInstance());
        view.setTag(viewHolder);
        return view;
    }

    @Override
    public Object doInBackground(int requsetCode) throws HttpException {
        return null;
    }

    @Override
    public void onSuccess(int requestCode, Object result) {

    }

    @Override
    public void onFailure(int requestCode, int state, Object result) {

    }

    class ViewHolder {
        TextView contentTextView;
    }
}
