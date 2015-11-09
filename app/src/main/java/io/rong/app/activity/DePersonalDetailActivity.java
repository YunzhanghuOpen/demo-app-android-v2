package io.rong.app.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.sea_monster.exception.BaseException;
import com.sea_monster.network.AbstractHttpRequest;
import com.sea_monster.resource.Resource;

import io.rong.app.DemoContext;
import io.rong.app.R;
import io.rong.app.database.DBManager;
import io.rong.app.database.UserInfos;
import io.rong.app.database.UserInfosDao;
import io.rong.app.model.User;
import io.rong.app.ui.LoadingDialog;
import io.rong.app.ui.WinToast;
import io.rong.app.utils.Constants;
import io.rong.imkit.RongIM;
import io.rong.imkit.widget.AsyncImageView;
import io.rong.imlib.model.UserInfo;


/**
 * Created by Bob on 2015/3/26.
 *
 * 搜索好友点详情
 */
public class DePersonalDetailActivity extends BaseApiActivity implements View.OnClickListener {


    private AsyncImageView mFriendImg;
    private TextView mFriendName;
    private Button mAddFriend;
    private AbstractHttpRequest<User> mUserHttpRequest;
    private LoadingDialog mDialog;
    private UserInfo user;
    String userID;
    private AbstractHttpRequest<User> getUserInfoByUserIdHttpRequest;
    private UserInfosDao mUserInfosDao;

    String targetId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.de_ac_personal_detail);
        initView();
        initData();
    }

    protected void initView() {
        getSupportActionBar().setTitle(R.string.public_add_address);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.de_actionbar_back);
        mFriendImg = (AsyncImageView) findViewById(R.id.friend_adapter_img);
        mFriendName = (TextView) findViewById(R.id.de_name);
        mAddFriend = (Button) findViewById(R.id.de_add_friend);
        mUserInfosDao = DBManager.getInstance(this).getDaoSession().getUserInfosDao();
    }

    protected void initData() {
        mAddFriend.setOnClickListener(this);
        mDialog = new LoadingDialog(this);
        userID = DemoContext.getInstance().getSharedPreferences().getString(Constants.APP_USER_ID,Constants.DEFAULT);

        if (getIntent().hasExtra("SEARCH_USERID")&&getIntent().hasExtra("SEARCH_USERNAME")&&getIntent().hasExtra("SEARCH_PORTRAIT")) {
            String userid = getIntent().getStringExtra("SEARCH_USERID");
            if(userid.equals(userID)){
                mAddFriend.setVisibility(View.GONE);
            }
            mFriendName.setText(getIntent().getStringExtra("SEARCH_USERNAME"));
            mFriendImg.setResource(new Resource(getIntent().getStringExtra("SEARCH_PORTRAIT")));
        }

        if (getIntent().hasExtra("USER")) {
            user = getIntent().getParcelableExtra("USER");
            targetId = user.getUserId();
            mFriendName.setText(user.getName());
            mFriendImg.setResource(new Resource(user.getPortraitUri()));
            if(user.getUserId().equals(userID)){
                mAddFriend.setVisibility(View.GONE);
            }else if(user.getUserId().equals("kefu114")){
                mAddFriend.setVisibility(View.GONE);
            }

            if (DemoContext.getInstance() != null && DemoContext.getInstance().searcheUserInfosById(user.getUserId())) {
                mAddFriend.setVisibility(View.GONE);
            }
        }

        getUserInfoByUserIdHttpRequest = DemoContext.getInstance().getDemoApi().getUserInfoByUserId(targetId, this);

    }

    @Override
    public void onCallApiSuccess(AbstractHttpRequest request, Object obj) {
        if (mUserHttpRequest == request) {
            if (mDialog != null)
                mDialog.dismiss();
            final User user = (User) obj;
            if (user.getCode() == 200) {

                    WinToast.toast(this,R.string.friend_send_success);
                    Intent intent = new Intent();
                    this.setResult( Constants.PERSONAL_REQUESTCODE, intent);
            }else if(user.getCode() == 301){
                WinToast.toast(this,R.string.friend_send);
            }
        }else if (getUserInfoByUserIdHttpRequest != null && getUserInfoByUserIdHttpRequest.equals(request)) {
            if (obj instanceof User) {
                final User user = (User) obj;

                if (user.getCode() == 200) {
                    UserInfos addFriend = new UserInfos();
                    addFriend.setUsername(user.getResult().getUsername());
                    addFriend.setUserid(user.getResult().getId());
                    addFriend.setPortrait(user.getResult().getPortrait());
                    addFriend.setStatus("0");
                    mUserInfosDao.insertOrReplace(addFriend);

                    mFriendName.setText(user.getResult().getUsername());

                    Log.e("-onCallApiSuccess--","--refreshUserInfoCache---"+user.getResult().getId()+"---name-"+user.getResult().getName());
                    RongIM.getInstance().refreshUserInfoCache(new UserInfo(user.getResult().getId(), user.getResult().getUsername(), Uri.parse(user.getResult().getPortrait())));

                    if (mDialog != null)
                        mDialog.dismiss();
                }
            }}
        }

    @Override
    public void onCallApiFailure(AbstractHttpRequest request, BaseException e) {
        if (mUserHttpRequest == request) {
            if (mDialog != null)
                mDialog.dismiss();
        }
    }

    @Override
    public void onClick(View v) {
        String targetid = getIntent().getStringExtra("SEARCH_USERID");

        if (DemoContext.getInstance() != null && !"".equals(targetid)) {
            if (DemoContext.getInstance() != null) {
//                String targetname = DemoContext.getInstance().getUserInfoById(targetid).getName().toString();
//                mUserHttpRequest = DemoContext.getInstance().getDemoApi().sendFriendInvite(targetid,"请添加我为好友，I'm "+targetname, this);
                mUserHttpRequest = DemoContext.getInstance().getDemoApi().sendFriendInvite(targetid, "请添加我为好友 ", this);

                if (mDialog != null && !mDialog.isShowing()) {
                    mDialog.show();
                }
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);
    }
}
