package io.rong.app.ui.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import io.rong.app.App;
import io.rong.app.R;
import io.rong.app.server.response.UserRelationshipResponse;
import io.rong.app.server.utils.AMGenerate;
import io.rong.imkit.widget.AsyncImageView;

/**
 * Created by Bob on 2015/3/26.
 */

public class NewFriendListAdapter extends BaseAdapters {
    private ViewHoler holer;

    public NewFriendListAdapter(Context context) {
        super(context);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            holer = new ViewHoler();
            convertView = mInflater.inflate(R.layout.rs_ada_user_ship, null);
            holer.mName = (TextView) convertView.findViewById(R.id.ship_name);
            holer.mMessage = (TextView) convertView.findViewById(R.id.ship_message);
            holer.mHead = (AsyncImageView) convertView.findViewById(R.id.new_header);
            holer.mState = (TextView) convertView.findViewById(R.id.ship_state);
            convertView.setTag(holer);
        } else {
            holer = (ViewHoler) convertView.getTag();
        }
        final UserRelationshipResponse.ResultEntity bean = (UserRelationshipResponse.ResultEntity) dataSet.get(position);
        holer.mName.setText(bean.getUser().getNickname());
        if (TextUtils.isEmpty(bean.getUser().getPortraitUri())) {
            ImageLoader.getInstance().displayImage(AMGenerate.generateDefaultAvatar(bean.getUser().getNickname(),bean.getUser().getId()), holer.mHead, App.getOptions());
        }else {
            ImageLoader.getInstance().displayImage(bean.getUser().getPortraitUri(), holer.mHead, App.getOptions());
        }
        holer.mMessage.setText(bean.getMessage());
        holer.mState.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnItemButtonClick != null) {
                    mOnItemButtonClick.onButtonClick(position, v, bean.getStatus());
                }
            }
        });

        switch (bean.getStatus()) {
            case 11: //收到了好友邀请
                holer.mState.setText("接受");
                holer.mState.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.de_add_friend_selector));
                break;
            case 10: // 发出了好友邀请
                holer.mState.setText("已请求");
                holer.mState.setBackgroundDrawable(null);
                break;
            case 21: // 忽略好友邀请
                holer.mState.setText("已忽略");
                holer.mState.setBackgroundDrawable(null);
                break;
            case 20: // 已是好友
                holer.mState.setText("已添加");
                holer.mState.setBackgroundDrawable(null);
                break;
            case 30: // 删除了好友关系
                holer.mState.setText("已删除");
                holer.mState.setBackgroundDrawable(null);
                break;
        }
        return convertView;
    }

    /**
     * displayName :
     * message : 手机号:18622222222昵称:的用户请求添加你为好友
     * status : 11
     * updatedAt : 2016-01-07T06:22:55.000Z
     * user : {"id":"i3gRfA1ml","nickname":"nihaoa","portraitUri":""}
     */

    class ViewHoler {
        AsyncImageView mHead;
        TextView mName;
        TextView mState;
        TextView mtime;
        TextView mMessage;
    }

    OnItemButtonClick mOnItemButtonClick;

    public OnItemButtonClick getOnItemButtonClick() {
        return mOnItemButtonClick;
    }

    public void setOnItemButtonClick(OnItemButtonClick onItemButtonClick) {
        this.mOnItemButtonClick = onItemButtonClick;
    }

    public interface OnItemButtonClick {
        boolean onButtonClick(int position, View view, int status);

    }
}
