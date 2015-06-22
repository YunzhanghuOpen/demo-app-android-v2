package io.rong.app.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sea_monster.exception.BaseException;
import com.sea_monster.network.AbstractHttpRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.rong.app.DeleteEvent;
import io.rong.app.DemoContext;
import io.rong.app.R;
import io.rong.app.RongCloudEvent;
import io.rong.app.model.ApiResult;
import io.rong.app.model.Friends;
import io.rong.app.model.Groups;
import io.rong.app.model.User;
import io.rong.app.ui.EditTextHolder;
import io.rong.app.ui.LoadingDialog;
import io.rong.app.ui.WinToast;
import io.rong.imkit.RongIM;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Group;
import io.rong.imlib.model.UserInfo;


/**
 * Created by Bob on 2015/1/30.
 */
public class LoginActivity extends BaseApiActivity implements View.OnClickListener, Handler.Callback, EditTextHolder.OnEditTextFocusChangeListener {
    private static final String TAG = "LoginActivity";

    /**
     * 用户账户
     */
    private EditText mUserNameEt;
    /**
     * 密码
     */
    private EditText mPassWordEt;
    /**
     * 登录button
     */
    private Button mSignInBt;
    /**
     * 设备id
     */
    private String mDeviceId;
    /**
     * 忘记密码
     */
    private TextView mFogotPassWord;
    /**
     * 注册
     */
    private TextView mRegister;
    /**
     * 输入用户名删除按钮
     */
    private FrameLayout mFrUserNameDelete;
    /**
     * 输入密码删除按钮
     */
    private FrameLayout mFrPasswordDelete;
    /**
     * logo
     */
    private ImageView mLoginImg;
    /**
     * 软键盘的控制
     */
    private InputMethodManager mSoftManager;
    /**
     * 是否展示title
     */
    private RelativeLayout mIsShowTitle;
    /**
     * 左侧title
     */
    private TextView mLeftTitle;
    /**
     * 右侧title
     */
    private TextView mRightTitle;

    private static final int REQUEST_CODE_REGISTER = 200;
    public static final String INTENT_IMAIL = "intent_email";
    public static final String INTENT_PASSWORD = "intent_password";
    private static final int HANDLER_LOGIN_SUCCESS = 1;
    private static final int HANDLER_LOGIN_FAILURE = 2;
    private static final int HANDLER_LOGIN_HAS_FOCUS = 3;
    private static final int HANDLER_LOGIN_HAS_NO_FOCUS = 4;


    private LoadingDialog mDialog;
    private AbstractHttpRequest<User> loginHttpRequest;
    private AbstractHttpRequest<User> getTokenHttpRequest;
    private AbstractHttpRequest<Friends> getUserInfoHttpRequest;
    private AbstractHttpRequest<Groups> mGetMyGroupsRequest;

    private Handler mHandler;
    private List<User> mUserList;
    private List<ApiResult> mResultList;
    private ImageView mImgBackgroud;
    EditTextHolder mEditUserNameEt;
    EditTextHolder mEditPassWordEt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.de_ac_login);
        initView();
        initData();
    }

    protected void initView() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();//隐藏ActionBar
        mSoftManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        mLoginImg = (ImageView) findViewById(R.id.de_login_logo);
        mUserNameEt = (EditText) findViewById(R.id.app_username_et);
        mPassWordEt = (EditText) findViewById(R.id.app_password_et);
        mSignInBt = (Button) findViewById(R.id.app_sign_in_bt);
        mRegister = (TextView) findViewById(R.id.de_login_register);
        mFogotPassWord = (TextView) findViewById(R.id.de_login_forgot);
        mImgBackgroud = (ImageView) findViewById(R.id.de_img_backgroud);
        mFrUserNameDelete = (FrameLayout) findViewById(R.id.fr_username_delete);
        mFrPasswordDelete = (FrameLayout) findViewById(R.id.fr_pass_delete);
        mIsShowTitle = (RelativeLayout) findViewById(R.id.de_merge_rel);
        mLeftTitle = (TextView) findViewById(R.id.de_left);
        mRightTitle = (TextView) findViewById(R.id.de_right);
        mUserList = new ArrayList<User>();
        mResultList = new ArrayList<ApiResult>();

        mSignInBt.setOnClickListener(this);
        mRegister.setOnClickListener(this);
        mLeftTitle.setOnClickListener(this);
        mRightTitle.setOnClickListener(this);
        mHandler = new Handler(this);
        mDialog = new LoadingDialog(this);

        mEditUserNameEt = new EditTextHolder(mUserNameEt, mFrUserNameDelete, null);
        mEditPassWordEt = new EditTextHolder(mPassWordEt, mFrPasswordDelete, null);

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Animation animation = AnimationUtils.loadAnimation(LoginActivity.this, R.anim.translate_anim);
                mImgBackgroud.startAnimation(animation);
            }
        });


    }

    protected void initData() {

        if (DemoContext.getInstance() != null) {
            String email = DemoContext.getInstance().getSharedPreferences().getString(INTENT_IMAIL, "");
            String password = DemoContext.getInstance().getSharedPreferences().getString(INTENT_PASSWORD, "");
            mUserNameEt.setText(email);
            mPassWordEt.setText(password);
        }

        TelephonyManager mTelephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        mDeviceId = mTelephonyManager.getDeviceId();

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mUserNameEt.setOnClickListener(LoginActivity.this);
                mPassWordEt.setOnClickListener(LoginActivity.this);
                mEditPassWordEt.setmOnEditTextFocusChangeListener(LoginActivity.this);
                mEditUserNameEt.setmOnEditTextFocusChangeListener(LoginActivity.this);
            }
        }, 200);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (getCurrentFocus() != null && getCurrentFocus().getWindowToken() != null) {
                mSoftManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                Message mess = Message.obtain();
                mess.what = HANDLER_LOGIN_HAS_NO_FOCUS;
                mHandler.sendMessage(mess);
            }
        }
        return super.onTouchEvent(event);
    }


    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        event.getKeyCode();
        switch (event.getKeyCode()) {
            case KeyEvent.KEYCODE_BACK:
            case KeyEvent.KEYCODE_ESCAPE:
                Message mess = Message.obtain();
                mess.what = HANDLER_LOGIN_HAS_NO_FOCUS;
                mHandler.sendMessage(mess);
                break;
        }
        return super.dispatchKeyEvent(event);
    }


    protected void onPause() {
        super.onPause();
        if (mSoftManager == null) {
            mSoftManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        }
        if (getCurrentFocus() != null) {
            mSoftManager.hideSoftInputFromWindow(getCurrentFocus()
                    .getWindowToken(), 0);// 隐藏软键盘
        }
    }


    @Override
    public boolean handleMessage(Message msg) {

        if (msg.what == HANDLER_LOGIN_FAILURE) {

            if (mDialog != null)
                mDialog.dismiss();
            WinToast.toast(LoginActivity.this, R.string.login_failure);


        } else if (msg.what == HANDLER_LOGIN_SUCCESS) {
            if (mDialog != null)
                mDialog.dismiss();
            WinToast.toast(LoginActivity.this, R.string.login_success);
//            initTest();

            startActivity(new Intent(this, MainActivity.class));

            finish();

        } else if (msg.what == HANDLER_LOGIN_HAS_FOCUS) {
            mLoginImg.setVisibility(View.GONE);
            mRegister.setVisibility(View.GONE);
            mFogotPassWord.setVisibility(View.GONE);
            mIsShowTitle.setVisibility(View.VISIBLE);
            mLeftTitle.setText(R.string.app_sign_up);
            mRightTitle.setText(R.string.app_fogot_password);
        } else if (msg.what == HANDLER_LOGIN_HAS_NO_FOCUS) {
            mLoginImg.setVisibility(View.VISIBLE);
            mRegister.setVisibility(View.VISIBLE);
            mFogotPassWord.setVisibility(View.VISIBLE);
            mIsShowTitle.setVisibility(View.GONE);
        }

        return false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.app_sign_in_bt://登录

                String userName = mUserNameEt.getEditableText().toString();
                String passWord = mPassWordEt.getEditableText().toString();

                if (TextUtils.isEmpty(userName) || TextUtils.isEmpty(passWord)) {
                    WinToast.toast(this, R.string.login_erro_is_null);
                    return;
                }

                if (mDialog != null && !mDialog.isShowing()) {
                    mDialog.show();
                }
                //发起登录 http请求 (注：非融云SDK接口，是demo接口)

                if (DemoContext.getInstance() != null) {

//                    String cookie = DemoContext.getInstance().getSharedPreferences().getString("DEMO_COOKIE", "DEFAULT");
//                    if (!TextUtils.isEmpty(cookie)) {
//                        httpGetTokenSuccess(cookie);
//                    } else {
                    loginHttpRequest = DemoContext.getInstance().getDemoApi().login(userName, passWord, this);
//                    }
                }

                break;
            case R.id.de_left://注册
            case R.id.de_login_register://注册
                Intent intent = new Intent(this, RegisterActivity.class);
                startActivityForResult(intent, REQUEST_CODE_REGISTER);
                break;
            case R.id.de_login_forgot://忘记密码
                WinToast.toast(this, "忘记密码");
                break;
            case R.id.de_right://忘记密码
                Intent intent1 = new Intent(this, RegisterActivity.class);
                startActivityForResult(intent1, REQUEST_CODE_REGISTER);
                break;

            case R.id.app_username_et:
            case R.id.app_password_et:
                Message mess = Message.obtain();
                mess.what = HANDLER_LOGIN_HAS_FOCUS;
                mHandler.sendMessage(mess);
                break;

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQUEST_CODE_REGISTER && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                mUserNameEt.setText(data.getStringExtra(INTENT_IMAIL));
                mPassWordEt.setText(data.getStringExtra(INTENT_PASSWORD));
            }
        }

    }

    private void httpLoginSuccess(User user, boolean isFirst) {

        if (user.getCode() == 200) {
            Log.e(TAG, "-----get token----");
            getTokenHttpRequest = DemoContext.getInstance().getDemoApi().getToken(this);
        }

    }


    private void httpGetTokenSuccess(String token) {

        try {
            /**
             * IMKit SDK调用第二步
             *
             * 建立与服务器的连接
             *
             * 详见API
             * http://docs.rongcloud.cn/api/android/imkit/index.html
             */
            Log.e("LoginActivity", "---------onSuccess gettoken----------:" + token);
//            token = "dNcIdu8Eqtu7iNca1gMhzs2yq+hfEluLjZ78E1qo4hGRHcB01HLt4SCyc1P/x3rYpMLVNO7rD0vC99se33P+Aw==";
            RongIM.connect(token, new RongIMClient.ConnectCallback() {
                        @Override
                        public void onTokenIncorrect() {

                        }

                        @Override
                        public void onSuccess(String userId) {
                            Log.e("LoginActivity", "---------onSuccess userId----------:" + userId);

                            getUserInfoHttpRequest = DemoContext.getInstance().getDemoApi().getFriends(LoginActivity.this);

                            SharedPreferences.Editor edit = DemoContext.getInstance().getSharedPreferences().edit();
                            edit.putString("DEMO_USERID", userId);
                            edit.apply();

                            RongCloudEvent.getInstance().setOtherListener();
                            DeleteEvent.init(LoginActivity.this);
                        }

                        @Override
                        public void onError(RongIMClient.ErrorCode e) {
                            mHandler.obtainMessage(HANDLER_LOGIN_FAILURE).sendToTarget();
                            Log.e("LoginActivity", "---------onError ----------:" + e);
                        }
                    }
            );

        } catch (Exception e) {
            e.printStackTrace();
        }

        //发起获取好友列表的http请求  (注：非融云SDK接口，是demo接口)
        if (DemoContext.getInstance() != null) {


//                getFriendsHttpRequest = DemoContext.getInstance().getDemoApi().getNewFriendlist(LoginActivity.this);
            mGetMyGroupsRequest = DemoContext.getInstance().getDemoApi().getMyGroups(LoginActivity.this);

        }

        if (DemoContext.getInstance() != null) {
            SharedPreferences.Editor editor = DemoContext.getInstance().getSharedPreferences().edit();
            editor.putString(INTENT_PASSWORD, mPassWordEt.getText().toString());
            editor.putString(INTENT_IMAIL, mUserNameEt.getText().toString());
            editor.apply();
        }
    }


    @Override
    public void onCallApiSuccess(AbstractHttpRequest request, Object obj) {
        //登录成功  返回数据
        if (loginHttpRequest.equals(request)) {

            if (obj instanceof User) {

                final User user = (User) obj;

                if (user.getCode() == 200) {
                    if (DemoContext.getInstance() != null && user.getResult() != null) {
                        SharedPreferences.Editor edit = DemoContext.getInstance().getSharedPreferences().edit();
                        edit.putString("DEMO_USER_ID", user.getResult().getId());
                        edit.putString("DEMO_USER_NAME", user.getResult().getUsername());
                        edit.putString("DEMO_USER_PORTRAIT", user.getResult().getPortrait());
                        edit.apply();
                        Log.e(TAG, "-------login success------");

                        httpLoginSuccess(user, true);
                    }
                } else if (user.getCode() == 103) {

                    if (mDialog != null)
                        mDialog.dismiss();

                    WinToast.toast(LoginActivity.this, "密码错误");
                } else if (user.getCode() == 104) {

                    if (mDialog != null)
                        mDialog.dismiss();

                    WinToast.toast(LoginActivity.this, "账号错误");
                }
            }
        } else if (getTokenHttpRequest.equals(request)) {
            if (obj instanceof User) {
                final User user = (User) obj;
                if (user.getCode() == 200) {
                    httpGetTokenSuccess(user.getResult().getToken());
                    SharedPreferences.Editor edit = DemoContext.getInstance().getSharedPreferences().edit();
                    edit.putString("DEMO_TOKEN", user.getResult().getToken());
                    edit.apply();
                    Log.e(TAG, "------getTokenHttpRequest -success--" + user.getResult().getToken());
                } else if (user.getCode() == 110) {

                    WinToast.toast(LoginActivity.this, user.getMessage());
                } else if (user.getCode() == 111) {
                    WinToast.toast(LoginActivity.this, user.getMessage());
                }
            }
        } else if (mGetMyGroupsRequest.equals(request)) {
            if (obj instanceof Groups) {
                final Groups groups = (Groups) obj;

                if (groups.getCode() == 200) {
                    List<Group> grouplist = new ArrayList<>();
                    if (groups.getResult() != null) {
                        for (int i = 0; i < groups.getResult().size(); i++) {

                            String id = groups.getResult().get(i).getId();
                            String name = groups.getResult().get(i).getName();
                            if (groups.getResult().get(i).getPortrait() != null) {
                                Uri uri = Uri.parse(groups.getResult().get(i).getPortrait());
                                grouplist.add(new Group(id, name, uri));
                            } else {
                                grouplist.add(new Group(id, name, null));
                            }


                        }
                        HashMap<String, Group> groupM = new HashMap<String, Group>();
                        for (int i = 0; i < grouplist.size(); i++) {
                            groupM.put(groups.getResult().get(i).getId(), grouplist.get(i));
                            Log.e("login", "------get Group id---------" + groups.getResult().get(i).getId());
                        }

                        if (DemoContext.getInstance() != null)
                            DemoContext.getInstance().setGroupMap(groupM);
                    }
                } else {
//                    WinToast.toast(this, groups.getCode());
                }
            }
        } else if (getUserInfoHttpRequest.equals(request)) {
            //获取好友列表接口  返回好友数据  (注：非融云SDK接口，是demo接口)
            if (obj instanceof Friends) {
                final Friends friends = (Friends) obj;
                if (friends.getCode() == 200) {
                    ArrayList<UserInfo> friendResults = new ArrayList<UserInfo>();

                    for (int i = 0; i < friends.getResult().size(); i++) {
                        UserInfo info = new UserInfo(String.valueOf(friends.getResult().get(i).getId()), friends.getResult().get(i).getUsername(), friends.getResult().get(i).getPortrait() == null ? null : Uri.parse(friends.getResult().get(i).getPortrait()));
                        friendResults.add(info);
                    }
                    friendResults.add(new UserInfo("10000", "新好友消息", Uri.parse("test")));
                    friendResults.add(new UserInfo("kefu114", "客服", Uri.parse("http://jdd.kefu.rongcloud.cn/image/service_80x80.png")));
                    if (DemoContext.getInstance() != null)
                        //将数据提供给用户信息提供者
                        DemoContext.getInstance().setUserInfos(friendResults);
                    mHandler.obtainMessage(HANDLER_LOGIN_SUCCESS).sendToTarget();
                }
            }
        }

    }


    @Override
    public void onCallApiFailure(AbstractHttpRequest request, BaseException e) {

        if (loginHttpRequest == request) {
            if (mDialog != null)
                mDialog.dismiss();
        } else if (getTokenHttpRequest == request) {
            if (mDialog != null)
                mDialog.dismiss();
        }
    }


    @Override
    public void onEditTextFocusChange(View v, boolean hasFocus) {
        Message mess = Message.obtain();
        switch (v.getId()) {
            case R.id.app_username_et:
            case R.id.app_password_et:
                if (hasFocus) {
                    mess.what = HANDLER_LOGIN_HAS_FOCUS;
                }
                mHandler.sendMessage(mess);
                break;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mDialog != null) {
            mDialog.dismiss();
            mDialog = null;
        }
    }

}
