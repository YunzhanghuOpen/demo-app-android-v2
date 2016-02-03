package io.rong.app.ui.activity;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import io.rong.app.R;
import io.rong.app.server.network.http.HttpException;
import io.rong.app.server.response.CheckPhoneResponse;
import io.rong.app.server.utils.AMUtils;
import io.rong.app.server.utils.NToast;
import io.rong.app.server.widget.ClearWriteEditText;

/**
 * Created by AMing on 16/2/2.
 * Company RongCloud
 */
public class ForgetPasswordActivity extends BaseActivity implements View.OnClickListener {

    private static final int CHECKPHONE = 31;
    private ClearWriteEditText mPhone, mCode, mPassword1, mPassword2;

    private Button mGetCode, mOK;

    private String phone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.de_ac_forget);
        ActionBar actionBar = getSupportActionBar();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.de_actionbar_back);
        actionBar.setTitle("忘记密码");
        initView();

    }

    private void initView() {
        mPhone = (ClearWriteEditText) findViewById(R.id.forget_phone);
        mCode = (ClearWriteEditText) findViewById(R.id.forget_code);
        mPassword1 = (ClearWriteEditText) findViewById(R.id.forget_password);
        mPassword2 = (ClearWriteEditText) findViewById(R.id.forget_password1);
        mGetCode = (Button) findViewById(R.id.forget_getcode);
        mOK = (Button) findViewById(R.id.forget_button);
        mGetCode.setOnClickListener(this);
        mOK.setOnClickListener(this);
        mPhone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 11) {
                    if (AMUtils.isMobile(s.toString().trim())) {
                        Toast.makeText(mContext, "正则验证通过", Toast.LENGTH_SHORT).show();
                        phone = mPhone.getText().toString().trim();
                        request(CHECKPHONE, true);
                        AMUtils.onInactive(mContext, mPhone);
                    } else {
                        Toast.makeText(mContext, "正则验证无效,请检查手机号", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    mGetCode.setClickable(false);
                    mGetCode.setBackgroundDrawable(getResources().getDrawable(R.drawable.rs_select_btn_gray));
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
                return action.checkPhoneAvailable("86", phone);
        }
        return super.doInBackground(requestCode);
    }

    @Override
    public void onSuccess(int requestCode, Object result) {
        if (result != null) {
            switch (requestCode) {
                case CHECKPHONE:
                    CheckPhoneResponse response = (CheckPhoneResponse) result;
                    if (response.getCode() == 200) {
                        if (response.isResult() == true) {
                            NToast.shortToast(mContext, "此号码未被注册");
                            mGetCode.setClickable(false);
                            mGetCode.setBackgroundDrawable(getResources().getDrawable(R.drawable.rs_select_btn_gray));
                        } else {
                            mGetCode.setClickable(true);
                            mGetCode.setBackgroundDrawable(getResources().getDrawable(R.drawable.rs_select_btn_blue));
                        }
                    }
                    break;
            }
        }
    }

    @Override
    public void onFailure(int requestCode, int state, Object result) {
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.forget_getcode:

                break;

            case R.id.forget_button:

                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);
    }


}
