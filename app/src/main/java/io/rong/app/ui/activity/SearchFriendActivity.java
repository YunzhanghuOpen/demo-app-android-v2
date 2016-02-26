package io.rong.app.ui.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import io.rong.app.App;
import io.rong.app.R;
import io.rong.app.server.network.http.HttpException;
import io.rong.app.server.response.FriendInvitationResponse;
import io.rong.app.server.response.GetUserInfoByPhoneResponse;
import io.rong.app.server.utils.AMUtils;
import io.rong.app.server.utils.NToast;
import io.rong.app.server.widget.DialogWithYesOrNoUtils;
import io.rong.app.server.widget.LoadDialog;

/**
 * Created by Bob on 2015/3/26.
 */
public class SearchFriendActivity extends BaseActivity {

    private static final int SEARCHPHONE = 10;
    private static final int ADDFRIEND = 11;
    private EditText mEtSearch;

    private Button mBtSearch;

    private LinearLayout searchItem;

    private TextView searchName;

    private ImageView searchImage;

    private String mPhone, addFriendMessage, mFriendId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.de_ac_search);
        getSupportActionBar().setTitle(R.string.public_account_search);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.de_actionbar_back);

        mEtSearch = (EditText) findViewById(R.id.de_ui_search);
        mBtSearch = (Button) findViewById(R.id.de_search);
        searchItem = (LinearLayout) findViewById(R.id.search_result);
        searchName = (TextView) findViewById(R.id.search_name);
        searchImage = (ImageView) findViewById(R.id.search_header);


        mBtSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPhone = mEtSearch.getText().toString().trim();
                if (TextUtils.isEmpty(mPhone)) {
                    NToast.shortToast(mContext, "手机号不能为空");
                    return;
                }

                if (!AMUtils.isMobile(mPhone)) {
                    NToast.shortToast(mContext, "手机号正则验证失败");
                    return;
                }

                LoadDialog.show(mContext);
                request(SEARCHPHONE, true);
            }
        });

    }

    @Override
    public Object doInBackground(int requestCode) throws HttpException {
        switch (requestCode) {
            case SEARCHPHONE:
                return action.getUserInfoFromPhone("86", mPhone);
            case ADDFRIEND:
                return action.sendFriendInvitation(mFriendId, addFriendMessage);
        }
        return super.doInBackground(requestCode);
    }

    @Override
    public void onSuccess(int requestCode, Object result) {
        if (result != null) {
            switch (requestCode) {
                case SEARCHPHONE:
                    final GetUserInfoByPhoneResponse guifres = (GetUserInfoByPhoneResponse) result;
                    if (guifres.getCode() == 200) {
                        LoadDialog.dismiss(mContext);
                        NToast.shortToast(mContext, "success");
                        mFriendId = guifres.getResult().getId();
                        searchItem.setVisibility(View.VISIBLE);
                        ImageLoader.getInstance().displayImage(guifres.getResult().getPortraitUri(), searchImage, App.getOptions());
                        searchName.setText(guifres.getResult().getNickname());
                        searchItem.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (getSharedPreferences("config", MODE_PRIVATE).getString("loginphone", "").equals(mEtSearch.getText().toString().trim())) {
                                    NToast.shortToast(mContext, "自己不能添加自己为好友");
                                    return;
                                }

                                DialogWithYesOrNoUtils.getInstance().showEditDialog(mContext, "加好友信息...", "加为好友", new DialogWithYesOrNoUtils.DialogCallBack() {
                                    @Override
                                    public void exectEvent() {

                                    }

                                    @Override
                                    public void updatePassword(String oldPassword, String newPassword) {

                                    }

                                    @Override
                                    public void exectEditEvent(String editText) {
                                        String name = getSharedPreferences("config", MODE_PRIVATE).getString("loginnickname", "");
                                        addFriendMessage = editText;
                                        if (TextUtils.isEmpty(editText)) {
                                            addFriendMessage = name + "请求添加你为好友";
                                        }
                                        if (!TextUtils.isEmpty(mFriendId)) {
                                            LoadDialog.show(mContext);
                                            request(ADDFRIEND);
                                        } else {
                                            NToast.shortToast(mContext, "id is null");
                                        }
                                    }
                                });
                            }
                        });

                    }
                    break;
                case ADDFRIEND:
                    FriendInvitationResponse fres = (FriendInvitationResponse) result;
                    if (fres.getCode() == 200) {
                        NToast.shortToast(mContext, "请求成功");
                        LoadDialog.dismiss(mContext);
                    } else {
                        NToast.shortToast(mContext, "请求失败 错误码:" + fres.getCode());
                        LoadDialog.dismiss(mContext);
                    }
                    break;
            }
        }
    }

    @Override
    public void onFailure(int requestCode, int state, Object result) {
        switch (requestCode) {
            case ADDFRIEND:
                NToast.shortToast(mContext, "好友邀请请求失败");
                LoadDialog.dismiss(mContext);
                break;
            case SEARCHPHONE:
                NToast.shortToast(mContext, "用户不存在");
                LoadDialog.dismiss(mContext);
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);
    }
}
