package io.rong.app.ui.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import io.rong.app.R;
import io.rong.app.RongCloudEvent;
import io.rong.app.db.DBManager;
import io.rong.app.db.Qun;
import io.rong.app.server.network.async.AsyncTaskManager;
import io.rong.app.server.network.http.HttpException;
import io.rong.app.server.response.GetGroupResponse;
import io.rong.app.server.response.GetTokenResponse;
import io.rong.app.server.response.GetUserInfoByIdResponse;
import io.rong.app.server.response.LoginResponse;
import io.rong.app.server.utils.AMUtils;
import io.rong.app.server.utils.NLog;
import io.rong.app.server.utils.NToast;
import io.rong.app.server.widget.ClearWriteEditText;
import io.rong.app.server.widget.LoadDialog;
import io.rong.imkit.RongIM;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.UserInfo;
import io.rong.message.RichContentMessage;

/**
 * Created by AMing on 16/1/15.
 * Company RongCloud
 */
public class LoginActivity extends BaseActivity implements View.OnClickListener {

    private static final int LOGIN = 5;
    private static final int GETTOKEN = 6;
    private static final int SYNCUSERINFO = 9;
    private static final int SYNCGROUP = 17;
    private ImageView mImgBackgroud;

    private ClearWriteEditText mPhoneEdit, mPasswordEdit;

    private Button mConfirm;

    private TextView mRegist, forgetPassword;

    private String phoneString, passwordString, loginToken, connectResultId;

    private SharedPreferences sp;

    SharedPreferences.Editor e;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.de_ac_login);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        sp = getSharedPreferences("config", MODE_PRIVATE);
        e = sp.edit();

        initView();

    }

    private void initView() {
        mPhoneEdit = (ClearWriteEditText) findViewById(R.id.de_login_phone);
        mPasswordEdit = (ClearWriteEditText) findViewById(R.id.de_login_password);
        mConfirm = (Button) findViewById(R.id.de_login_sign);
        mRegist = (TextView) findViewById(R.id.de_login_register);
        forgetPassword = (TextView) findViewById(R.id.de_login_forgot);
        forgetPassword.setOnClickListener(this);
        mConfirm.setOnClickListener(this);
        mRegist.setOnClickListener(this);
        mImgBackgroud = (ImageView) findViewById(R.id.de_img_backgroud);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Animation animation = AnimationUtils.loadAnimation(LoginActivity.this, R.anim.translate_anim);
                mImgBackgroud.startAnimation(animation);
            }
        }, 200);
        mPhoneEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 11) {
                    AMUtils.onInactive(mContext, mPhoneEdit);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        String oldPhone = sp.getString("loginphone", "");
        String oldPassword = sp.getString("loginpassword", "");
        if (oldPhone.equals(mPhoneEdit.getText().toString().trim())) {//和上次登录账户一致

        } else {
            //和上次登录账户不一致 或者 换设备登录  重新网络拉取好友 和 群组数据
            DBManager.getInstance(mContext).getDaoSession().getFriendDao().deleteAll();//清空上个用户的数据库
            DBManager.getInstance(mContext).getDaoSession().getQunDao().deleteAll();
        }
        if (!TextUtils.isEmpty(oldPhone) && !TextUtils.isEmpty(oldPassword)) {
            mPhoneEdit.setText(oldPhone);
            mPasswordEdit.setText(oldPassword);
        }


    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.de_login_sign:
                phoneString = mPhoneEdit.getText().toString().trim();
                passwordString = mPasswordEdit.getText().toString().trim();

                if (TextUtils.isEmpty(phoneString)) {
                    NToast.shortToast(mContext, "手机号不能为空");
                    mPhoneEdit.setShakeAnimation();
                    return;
                }

                if (!AMUtils.isMobile(phoneString)) {
                    NToast.shortToast(mContext, "非法手机号");
                    mPhoneEdit.setShakeAnimation();
                    return;
                }

                if (TextUtils.isEmpty(passwordString)) {
                    NToast.shortToast(mContext, "密码不能为空");
                    mPasswordEdit.setShakeAnimation();
                    return;
                }
                if (passwordString.contains(" ")) {
                    NToast.shortToast(mContext, "密码不能包含空格");
                    mPasswordEdit.setShakeAnimation();
                    return;
                }
                LoadDialog.show(mContext);
                request(LOGIN);
                break;
            case R.id.de_login_register:
                startActivityForResult(new Intent(this, RegisterActivity.class), 1);
                break;
            case R.id.de_login_forgot:
                startActivityForResult(new Intent(this, ForgetPasswordActivity.class), 2);
                break;
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 2 && data != null) { //TODO 服务端不在 目前还未测试通过
            String phone = data.getStringExtra("phone");
            String password = data.getStringExtra("password");
            mPhoneEdit.setText(phone);
            mPasswordEdit.setText(password);
        } else if (data != null && requestCode == 1) {
            String phone = data.getStringExtra("phone");
            String password = data.getStringExtra("password");
            String id = data.getStringExtra("id");
            String nickname = data.getStringExtra("nickname");
            if (!TextUtils.isEmpty(phone) && !TextUtils.isEmpty(password) && !TextUtils.isEmpty(id) && !TextUtils.isEmpty(nickname)) {
                mPhoneEdit.setText(phone);
                mPasswordEdit.setText(password);
                e.putString("loginphone", phone);
                e.putString("loginpassword", password);
                e.putString("loginid", id);
                e.putString("loginnickname", nickname);
                e.commit();
            }

        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    public Object doInBackground(int requestCode) throws HttpException {
        switch (requestCode) {
            case LOGIN:
                return action.login("86", phoneString, passwordString);
            case GETTOKEN:
                return action.getToken();
            case SYNCUSERINFO:
                return action.getUserInfoById(connectResultId);
            case SYNCGROUP:
                return action.getGroups();
        }
        return super.doInBackground(requestCode);
    }

    @Override
    public void onSuccess(int requestCode, Object result) {
        if (result != null) {
            switch (requestCode) {
                case LOGIN:
                    LoginResponse lrres = (LoginResponse) result;
                    if (lrres.getCode() == 200) {
                        request(GETTOKEN);
                    } else if (lrres.getCode() == 100) {
                        LoadDialog.dismiss(mContext);
                        NToast.shortToast(mContext, "手机或者密码错误");
                    } else if (lrres.getCode() == 1000) {
                        LoadDialog.dismiss(mContext);
                        NToast.shortToast(mContext, "手机或者密码错误");
                    }
                    break;
                case GETTOKEN:
                    GetTokenResponse gtres = (GetTokenResponse) result;
                    if (gtres.getCode() == 200) {
                        loginToken = gtres.getResult().getToken();
                        if (!TextUtils.isEmpty(loginToken)) {
                            e.putString("loginToken", loginToken);
                            e.putString("loginphone", phoneString);
                            e.putString("loginpassword", passwordString);
                            e.commit();

                            RongIM.connect(loginToken, new RongIMClient.ConnectCallback() {
                                @Override
                                public void onTokenIncorrect() {
                                    NLog.e("connect", "onTokenIncorrect");
                                }

                                @Override
                                public void onSuccess(String s) {
                                    connectResultId = s;
                                    NLog.e("connect", "onSuccess userid:" + s);
                                    e.putString("loginid", s);
                                    e.commit();

                                    request(SYNCUSERINFO, true);
                                }

                                @Override
                                public void onError(RongIMClient.ErrorCode errorCode) {
                                    NLog.e("connect", "onError errorcode:" + errorCode.getValue());
                                }
                            });
                        }
                    }
                    break;
                case SYNCUSERINFO:
                    GetUserInfoByIdResponse guRes = (GetUserInfoByIdResponse) result;
                    if (guRes.getCode() == 200) {
                        e.putString("loginnickname", guRes.getResult().getNickname());
                        e.putString("loginPortrait", guRes.getResult().getPortraitUri());
                        e.commit();

                        if (RongIM.getInstance() != null) {
                            RongCloudEvent.getInstance().setConnectedListener();
                            RongIM.getInstance().setCurrentUserInfo(new UserInfo(guRes.getResult().getId(), guRes.getResult().getNickname(), Uri.parse(guRes.getResult().getPortraitUri())));
                            RongIM.getInstance().setMessageAttachedUserInfo(true);


                            List<Qun> groupList = DBManager.getInstance(mContext).getDaoSession().getQunDao().loadAll();
                            if (groupList.size() == 0 || groupList == null) {
                                //检查本地是否有群组数据  如果没有 可能是换设备登录 从服务端再次去查询 有无群组数据
//                                NToast.shortToast(mContext, "同步群组数据");
                                request(SYNCGROUP);
                            } else {
                                LoadDialog.dismiss(mContext);
                                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                NToast.shortToast(mContext, "login success!");
                                finish();
                            }
                        }
                    }
                    break;
                case SYNCGROUP:
                    GetGroupResponse ggRes = (GetGroupResponse) result;
                    if (ggRes.getCode() == 200) {
                        List<GetGroupResponse.ResultEntity> list = ggRes.getResult();
                        if (list.size() > 0 && list != null) { //服务端上也没有群组数据
                            for (GetGroupResponse.ResultEntity g : list) {
                                DBManager.getInstance(mContext).getDaoSession().getQunDao().insertOrReplace(
                                        new Qun(g.getGroup().getId(), g.getGroup().getName(), g.getGroup().getPortraitUri(), String.valueOf(g.getRole()))
                                );
                            }
                        }
                        LoadDialog.dismiss(mContext);
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        NToast.shortToast(mContext, "login success!");
                        finish();
                    }
                    break;
            }
        }
    }

    @Override
    public void onFailure(int requestCode, int state, Object result) {
        if (state == AsyncTaskManager.HTTP_NULL_CODE || state == AsyncTaskManager.HTTP_ERROR_CODE) {
            LoadDialog.dismiss(mContext);
            NToast.shortToast(mContext,"当前网络不可用");
            return;
        }
        switch (requestCode) {
            case LOGIN:
                LoadDialog.dismiss(mContext);
                NToast.shortToast(mContext, "登录请求失败");
                break;
            case SYNCUSERINFO:
                LoadDialog.dismiss(mContext);
                NToast.shortToast(mContext, "同步个人数据请求失败");
                break;
            case GETTOKEN:
                LoadDialog.dismiss(mContext);
                NToast.shortToast(mContext, "获取token请求失败");
                break;
            case SYNCGROUP:
                NToast.shortToast(mContext, "同步群组数据请求失败");
                break;
        }
    }
}
