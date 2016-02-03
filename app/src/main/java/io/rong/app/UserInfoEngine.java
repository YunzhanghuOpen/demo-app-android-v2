package io.rong.app;

import android.content.Context;
import android.net.Uri;



import io.rong.app.server.SealAction;
import io.rong.app.server.network.async.AsyncTaskManager;
import io.rong.app.server.network.async.OnDataListener;
import io.rong.app.server.network.http.HttpException;
import io.rong.app.server.response.GetUserInfoByIdResponse;
import io.rong.imlib.model.UserInfo;

/**
 * Created by AMing on 15/12/10.
 * Company RongCloud
 */
public class UserInfoEngine implements OnDataListener {


    private static UserInfoEngine instance;
    private UserInfoListener mListener;

    public static UserInfoEngine getInstance(Context context) {
        if (instance == null) {
            instance = new UserInfoEngine(context);
        }
        return instance;
    }

    private UserInfoEngine(Context context) {
        this.context = context;
    }

    private static Context context;

    private String userid;

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    private static final int REQUSERINFO = 4234;


    private UserInfo userInfo;

    public UserInfo getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
    }

    public UserInfo startEngine(String userid) {
        setUserid(userid);
        AsyncTaskManager.getInstance(context).request(REQUSERINFO, this);
        return getUserInfo();
    }


    @Override
    public Object doInBackground(int requsetCode) throws HttpException {
        return new SealAction(context).getUserInfoById(getUserid());
    }

    @Override
    public void onSuccess(int requestCode, Object result) {
        if (result != null) {
            GetUserInfoByIdResponse res = (GetUserInfoByIdResponse) result;
            if (res.getCode() == 200) {
                userInfo = new UserInfo(res.getResult().getId(), res.getResult().getNickname(), Uri.parse(res.getResult().getPortraitUri()));
                if (mListener != null) {
                    mListener.onResult(userInfo);
                }
            }
        }
    }

    @Override
    public void onFailure(int requestCode, int state, Object result) {
        if (mListener != null) {
            mListener.onResult(null);
        }
    }

    public void setListener(UserInfoListener listener) {
        this.mListener = listener;
    }

    public interface UserInfoListener {
        void onResult(UserInfo info);
    }
}
