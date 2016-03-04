package io.rong.app.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import io.rong.app.R;
import io.rong.app.server.network.http.HttpException;
import io.rong.app.server.response.CheckPhoneResponse;
import io.rong.app.server.response.RegisterResponse;
import io.rong.app.server.response.SendCodeResponse;
import io.rong.app.server.response.VerifyCodeResponse;
import io.rong.app.server.utils.AMUtils;
import io.rong.app.server.utils.NToast;
import io.rong.app.server.utils.downtime.DownTimer;
import io.rong.app.server.utils.downtime.DownTimerListener;
import io.rong.app.server.widget.ClearWriteEditText;
import io.rong.app.server.widget.LoadDialog;

/**
 * Created by AMing on 16/1/14.
 * Company RongCloud
 */
public class RegisterActivity extends BaseActivity implements View.OnClickListener, DownTimerListener {

    private static final int CHECKPHONE = 1;
    private static final int SENDCODE = 2;
    private static final int VERIFYCODE = 3;
    private static final int REGISTER = 4;
    private static final int REGIST_BACK = 1001;
    private ImageView mImgBackgroud;

    private TextView goLogin , goForget;

    private ClearWriteEditText mPhoneEdit, mCodeEdit, mNickEdit, mPasswordEdit;

    private Button mGetCode, mConfirm;

    private String mPhone, mCode, mNickName, mPassword, mCodeToken;

    private boolean isRequestCode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.de_ac_register);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        initView();
    }

    private void initView() {
        mPhoneEdit = (ClearWriteEditText) findViewById(R.id.reg_phone);
        mCodeEdit = (ClearWriteEditText) findViewById(R.id.reg_code);
        mNickEdit = (ClearWriteEditText) findViewById(R.id.reg_username);
        mPasswordEdit = (ClearWriteEditText) findViewById(R.id.reg_password);
        mGetCode = (Button) findViewById(R.id.reg_getcode);
        mConfirm = (Button) findViewById(R.id.reg_button);

        mGetCode.setOnClickListener(this);
        mGetCode.setClickable(false);
        mConfirm.setOnClickListener(this);

        goLogin = (TextView) findViewById(R.id.reg_login);
        goForget = (TextView) findViewById(R.id.reg_forget);
        goLogin.setOnClickListener(this);
        goForget.setOnClickListener(this);

        mImgBackgroud = (ImageView) findViewById(R.id.rg_img_backgroud);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Animation animation = AnimationUtils.loadAnimation(RegisterActivity.this, R.anim.translate_anim);
                mImgBackgroud.startAnimation(animation);
            }
        }, 200);

        addEditTextListener();

    }

    private void addEditTextListener() {
        mPhoneEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 11) {
                    if (AMUtils.isMobile(s.toString().trim())) {
                        Toast.makeText(mContext, "正则验证通过", Toast.LENGTH_SHORT).show();
                        mPhone = s.toString().trim();
                        request(CHECKPHONE, true);
                        AMUtils.onInactive(mContext, mPhoneEdit);
                    } else {
                        Toast.makeText(mContext, "正则验证无效,请检查手机号", Toast.LENGTH_SHORT).show();
                    }
                }else {
                    mGetCode.setClickable(false);
                    mGetCode.setBackgroundDrawable(getResources().getDrawable(R.drawable.rs_select_btn_gray));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        mCodeEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 4) {
                    AMUtils.onInactive(mContext, mCodeEdit);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        mPasswordEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 5) {
                    mConfirm.setClickable(true);
                    mConfirm.setBackgroundDrawable(getResources().getDrawable(R.drawable.rs_select_btn_blue));
                } else {
                    mConfirm.setClickable(false);
                    mConfirm.setBackgroundDrawable(getResources().getDrawable(R.drawable.rs_select_btn_gray));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }


    @Override
    public Object doInBackground(int requestCode) throws HttpException {
        switch (requestCode) {
            case CHECKPHONE:
                return action.checkPhoneAvailable("86", mPhone);
            case SENDCODE:
                return action.sendCode("86", mPhone);
            case VERIFYCODE:
                return action.verifyCode("86", mPhone, mCode);
            case REGISTER:
                return action.register(mNickName, mPassword, mCodeToken);
        }
        return super.doInBackground(requestCode);
    }

    @Override
    public void onSuccess(int requestCode, Object result) {
        if (result != null) {
            switch (requestCode) {
                case CHECKPHONE:
                    CheckPhoneResponse cprres = (CheckPhoneResponse) result;
                    if (cprres.getCode() == 200) {
                        if (cprres.isResult() == true) {
                            mGetCode.setClickable(true);
                            mGetCode.setBackgroundDrawable(getResources().getDrawable(R.drawable.rs_select_btn_blue));
                            Toast.makeText(mContext, "手机号可用", Toast.LENGTH_SHORT).show();
                        } else {
                            mGetCode.setClickable(false);
                            mGetCode.setBackgroundDrawable(getResources().getDrawable(R.drawable.rs_select_btn_gray));
                            Toast.makeText(mContext, "手机号已被注册", Toast.LENGTH_SHORT).show();
                        }
                    }
                    break;
                case SENDCODE:
                    SendCodeResponse scrres = (SendCodeResponse) result;
                    if (scrres.getCode() == 200) {
                        NToast.shortToast(mContext, "短信已发出,请注意查收短信");
                    } else if (scrres.getCode() == 5000) {
                        NToast.shortToast(mContext, "短信发送超过频率限制!");
                    }
                    break;

                case VERIFYCODE:
                    VerifyCodeResponse vcres = (VerifyCodeResponse) result;
                    switch (vcres.getCode()) {
                        case 200:
                            mCodeToken = vcres.getResult().getVerification_token();
                            if (!TextUtils.isEmpty(mCodeToken)) {
                                request(REGISTER);
                            } else {
                                NToast.shortToast(mContext, "code token is null");
                                LoadDialog.dismiss(mContext);
                            }
                            break;
                        case 1000:
                            //验证码错误
                            NToast.shortToast(mContext, "验证码错误");
                            LoadDialog.dismiss(mContext);
                            break;
                        case 2000:
                            //验证码过期
                            NToast.shortToast(mContext, "验证码过期请重新请求");
                            LoadDialog.dismiss(mContext);
                            break;
                    }
                    break;

                case REGISTER:
                    RegisterResponse rres = (RegisterResponse) result;
                    switch (rres.getCode()) {
                        case 200:
                            LoadDialog.dismiss(mContext);
                            NToast.shortToast(mContext, "注册成功!");
                            Intent data = new Intent();
                            data.putExtra("phone", mPhone);
                            data.putExtra("password", mPassword);
                            data.putExtra("nickname", mNickName);
                            data.putExtra("id", rres.getResult().getId());
                            setResult(REGIST_BACK, data);
                            this.finish();
                            break;
                        case 400:
                            // 错误的请求
                            break;
                        case 404:
                            //token 不存在
                            break;
                        case 500:
                            //应用服务端内部错误
                            break;
                    }
                    break;
            }
        }
    }

    @Override
    public void onFailure(int requestCode, int state, Object result) {
        switch (requestCode) {
            case CHECKPHONE:
                Toast.makeText(mContext, "手机号可用请求失败", Toast.LENGTH_SHORT).show();
                break;
            case SENDCODE:
                NToast.shortToast(mContext, "获取验证码请求失败");
                break;
            case VERIFYCODE:
                LoadDialog.dismiss(mContext);
                NToast.shortToast(mContext, "验证码是否可用请求失败");
                break;
            case REGISTER:
                LoadDialog.dismiss(mContext);
                NToast.shortToast(mContext, "注册请求失败");
                break;
        }
    }

    @Override
    public android.support.v4.app.FragmentManager getSupportFragmentManager() {
        return null;
    }

    private DownTimer downTimer;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.reg_login:
                startActivity(new Intent(this,LoginActivity.class));
                break;
            case R.id.reg_forget:
                startActivity(new Intent(this,ForgetPasswordActivity.class));
                break;
            case R.id.reg_getcode:
                if (TextUtils.isEmpty(mPhoneEdit.getText().toString().trim())) {
                    NToast.longToast(mContext,"手机号不能为空");
                }else {
                    isRequestCode = true;
                    downTimer = new DownTimer();
                    downTimer.setListener(this);
                    downTimer.startDown(60 * 1000);
                    request(SENDCODE);
                }
                break;
            case R.id.reg_button:
                mPhone = mPhoneEdit.getText().toString().trim();
                mCode = mCodeEdit.getText().toString().trim();
                mNickName = mNickEdit.getText().toString().trim();
                mPassword = mPasswordEdit.getText().toString().trim();


                if (TextUtils.isEmpty(mNickName)) {
                    NToast.shortToast(mContext, "昵称不能为空");
                    mNickEdit.setShakeAnimation();
                    return;
                }
                if (mNickName.contains(" ")) {
                    NToast.shortToast(mContext, "昵称不包含空格");
                    mNickEdit.setShakeAnimation();
                    return;
                }

                if (TextUtils.isEmpty(mPhone)) {
                    NToast.shortToast(mContext, "手机号不能为空");
                    mPhoneEdit.setShakeAnimation();
                    return;
                }
                if (TextUtils.isEmpty(mCode)) {
                    NToast.shortToast(mContext, "验证码不能为空");
                    mCodeEdit.setShakeAnimation();
                    return;
                }
                if (TextUtils.isEmpty(mPassword)) {
                    NToast.shortToast(mContext, "密码不能为空");
                    mPasswordEdit.setShakeAnimation();
                    return;
                }
                if (mPassword.contains(" ")) {
                    NToast.shortToast(mContext, "密码不能包含空格");
                    mPasswordEdit.setShakeAnimation();
                    return;
                }

                if (!isRequestCode) {
                    NToast.shortToast(mContext, "未向服务端获取验证码");
                    return;
                }

                LoadDialog.show(mContext);
                request(VERIFYCODE,true);

                break;
        }
    }

    @Override
    public void onTick(long millisUntilFinished) {
        mGetCode.setText("seconds:" + String.valueOf(millisUntilFinished / 1000));
        mGetCode.setClickable(false);
        mGetCode.setBackgroundDrawable(getResources().getDrawable(R.drawable.rs_select_btn_gray));
    }

    @Override
    public void onFinish() {
        mGetCode.setText("获取验证码");
        mGetCode.setClickable(true);
        mGetCode.setBackgroundDrawable(getResources().getDrawable(R.drawable.rs_select_btn_blue));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);
    }
}
