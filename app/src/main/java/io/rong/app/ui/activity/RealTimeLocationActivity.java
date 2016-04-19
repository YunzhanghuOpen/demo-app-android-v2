package io.rong.app.ui.activity;

import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.model.LatLng;

import java.util.List;

import de.greenrobot.event.EventBus;
import io.rong.app.R;
import io.rong.app.UserInfoEngine;
import io.rong.app.db.DBManager;
import io.rong.app.db.Friend;
import io.rong.app.model.RongEvent;
import io.rong.app.server.utils.AMGenerate;
import io.rong.app.ui.widget.RealTimeLocationHorizontalScrollView;
import io.rong.imkit.RongContext;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.UserInfo;

/**
 * Created by zhjchen on 8/12/15.
 */
public class RealTimeLocationActivity extends LocationMapActivity implements View.OnClickListener {

    private RealTimeLocationHorizontalScrollView horizontalScrollView;
    private ImageView mExitImageView;
    private ImageView mCloseImageView;
    private TextView mParticipantTextView;

    private RelativeLayout mLayout;

    private List<Friend> list;

    private SharedPreferences sp;


    @Override
    protected int getContentView() {
        return R.layout.activity_share_location;
    }

    @Override
    protected MapView initView(Bundle savedInstanceState) {

        MapView mapView = (MapView) findViewById(R.id.map);
        horizontalScrollView = (RealTimeLocationHorizontalScrollView) findViewById(R.id.scroll_view);

        mExitImageView = (ImageView) findViewById(android.R.id.icon);
        mCloseImageView = (ImageView) findViewById(android.R.id.icon1);

        mExitImageView.setOnClickListener(this);
        mCloseImageView.setOnClickListener(this);

        mParticipantTextView = (TextView) findViewById(android.R.id.text1);

        mLayout = (RelativeLayout) findViewById(R.id.layout);
        mLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
//                Log.d("RealTimeLocation", "--onTouch-------------");
                return true;
            }
        });

        EventBus.getDefault().register(this);

        int type = getIntent().getIntExtra("conversationType", 0);
        targetId = getIntent().getStringExtra("targetId");

        conversationType = Conversation.ConversationType.setValue(type);

        list = DBManager.getInstance(this).getDaoSession().getFriendDao().loadAll();
        sp = getSharedPreferences("config", MODE_PRIVATE);
        UserInfoEngine.getInstance(this).setListener(new UserInfoEngine.UserInfoListener() {
            @Override
            public void onResult(UserInfo info) {
                if (info != null) {
                    if (TextUtils.isEmpty(info.getPortraitUri().toString())) {
                        info.setPortraitUri(Uri.parse(AMGenerate.generateDefaultAvatar(info.getName(), info.getUserId())));
                    }
                    horizontalScrollView.addUserToView(info);
                    setParticipantTextView(-1);
                }
            }
        });

        return mapView;
    }


    @Override
    protected void initData() {
        super.initData();

        final List<String> userIds = RongIMClient.getInstance().getRealTimeLocationParticipants(conversationType, targetId);

        if (userIds != null && userIds.size() > 0) {

            for (String userId : userIds) {
                addUserInfoToScrollView(userId);
            }

            setParticipantTextView(userIds.size());
        }

    }

    private void setParticipantTextView(int count) {

        if (count == -1) {
            final List<String> userIds = RongIMClient.getInstance().getRealTimeLocationParticipants(conversationType, targetId);

            if (userIds != null && userIds.size() > 0) {
                count = userIds.size();
            }
        }

        mParticipantTextView.setText(String.format(" %1$d人在共享位置", count));
    }

    @Override
    public void onClick(View v) {

        if (v == mExitImageView) {
            RongIMClient.getInstance().quitRealTimeLocation(conversationType, targetId);
//            Log.d("RealTimeLocationActivity", "--quitRealTimeLocation---");
            finish();
        } else if (v == mCloseImageView) {
            finish();
        }

    }


    public void onEventMainThread(final RongEvent.RealTimeLocationReceiveEvent event) {
        String userId = event.getUserId();
        UserInfo userInfo = getCacheUserInfoById(userId);
        if (userInfo != null) {
            moveMarker(new LatLng(event.getLatitude(), event.getLongitude()), userInfo);
        }
    }


    public void onEventMainThread(RongEvent.RealTimeLocationQuitEvent event) {

        String userId = event.getUserId();

        removeMarker(userId);

        horizontalScrollView.removeUserFromView(userId);
        setParticipantTextView(-1);
    }

    public void onEventMainThread(RongEvent.RealTimeLocationJoinEvent event) {
        String userId = event.getUserId();
        addUserInfoToScrollView(userId);
    }

    @Override
    public boolean handleMessage(Message msg) {

        return false;
    }

    private void addUserInfoToScrollView(final String userId) {
        UserInfo userInfo = getCacheUserInfoById(userId);
        if (userInfo != null) {
            if (TextUtils.isEmpty(userInfo.getPortraitUri().toString())) {
                userInfo.setPortraitUri(Uri.parse(AMGenerate.generateDefaultAvatar(userInfo.getName(), userInfo.getUserId())));
            }
            horizontalScrollView.addUserToView(userInfo);
            setParticipantTextView(-1);
        } else {
            UserInfoEngine.getInstance(this).startEngine(userId);
        }
    }


    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }


    private UserInfo getCacheUserInfoById(String userId) {
        UserInfo info = RongContext.getInstance().getUserInfoFromCache(userId);
        if (info != null) {
            return info;
        } else {
            if (list != null && list.size() > 0) {
                for (Friend f : list) {
                    if (userId.equals(f.getUserId())) {
                        return new UserInfo(f.getUserId(), f.getName(), Uri.parse(f.getPortraitUri()));
                    }
                }
            }
        }
        String id = sp.getString("loginid", "");
        if (!TextUtils.isEmpty(id) && id.equals(userId)) {
            return new UserInfo(id, sp.getString("loginnickname", ""), Uri.parse(sp.getString("loginPortrait", "")));
        }
        return null;
    }
}
