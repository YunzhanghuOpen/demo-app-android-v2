package io.rong.app.activity;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import io.rong.app.DemoContext;
import io.rong.app.R;
import io.rong.app.fragment.ChatRoomListFragment;
import io.rong.app.fragment.CustomerFragment;
import io.rong.app.fragment.GroupListFragment;
import io.rong.app.fragment.TestFragment;
import io.rong.app.message.DeAgreedFriendRequestMessage;
import io.rong.app.model.Friends;
import io.rong.app.ui.LoadingDialog;
import io.rong.app.utils.Constants;
import io.rong.imkit.RongIM;
import io.rong.imkit.common.RongConst;
import io.rong.imkit.fragment.ConversationFragment;
import io.rong.imkit.fragment.ConversationListFragment;
import io.rong.imkit.fragment.SubConversationListFragment;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.UserInfo;
import com.sea_monster.exception.BaseException;
import com.sea_monster.network.AbstractHttpRequest;

public class MainActivity extends BaseApiActivity implements View.OnClickListener, ViewPager.OnPageChangeListener, ActionBar.OnMenuVisibilityListener, Handler.Callback {
    private static final String TAG = MainActivity.class.getSimpleName();

    public static final String ACTION_DMEO_RECEIVE_MESSAGE = "action_demo_receive_message";
    public static final String ACTION_DMEO_AGREE_REQUEST = "action_demo_agree_request";
    private RelativeLayout mMainConversationLiner;
    private RelativeLayout mMainGroupLiner;
    private RelativeLayout mMainChatroomLiner;
    private RelativeLayout mMainCustomerLiner;

    /**
     * 聊天室的fragment
     */
    private Fragment mChatroomFragment = null;

    /**
     * 客服的fragment
     */
    private Fragment mCustomerFragment = null;
    /**
     * 会话列表的fragment
     */
    private Fragment mConversationFragment = null;
    /**
     * 群组的fragment
     */
    private Fragment mGroupListFragment = null;
    /**
     * 会话TextView
     */
    private TextView mMainConversationTv;
    /**
     * 群组TextView
     */
    private TextView mMainGroupTv;

    private TextView mUnreadNumView;
    /**
     * 聊天室TextView
     */
    private TextView mMainChatroomTv;
    /**
     * 客服TextView
     */
    private TextView mMainCustomerTv;

    private FragmentManager mFragmentManager;


    private ViewPager mViewPager;
    /**
     * 下划线
     */
    private ImageView mMainSelectImg;

    private DemoFragmentPagerAdapter mDemoFragmentPagerAdapter;

    private LayoutInflater mInflater;
    /**
     * 下划线长度
     */
    int indicatorWidth;
    private LinearLayout mMainShow;

    private boolean hasNewFriends = false;
    private Menu mMenu;
    private ReceiveMessageBroadcastReciver mBroadcastReciver;
    private LoadingDialog mDialog;
    //    private AbstractHttpRequest<Friends> getUserInfoHttpRequest;
    private AbstractHttpRequest<Friends> getFriendsHttpRequest;
    private int mNetNum = 0;
    ActivityManager activityManager;
    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.de_ac_main);
        initView();
        initData();

    }


    protected void initView() {
        mHandler = new Handler(this);
        mDialog = new LoadingDialog(this);
        mFragmentManager = getSupportFragmentManager();
        getSupportActionBar().setTitle(R.string.main_name);
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm); // 获取屏幕信息
        indicatorWidth = dm.widthPixels / 4;// 指示器宽度为屏幕宽度的4/1

        mMainShow = (LinearLayout) findViewById(R.id.main_show);
        mMainConversationLiner = (RelativeLayout) findViewById(R.id.main_conversation_liner);
        mMainGroupLiner = (RelativeLayout) findViewById(R.id.main_group_liner);
        mMainChatroomLiner = (RelativeLayout) findViewById(R.id.main_chatroom_liner);
        mMainCustomerLiner = (RelativeLayout) findViewById(R.id.main_customer_liner);
        mMainConversationTv = (TextView) findViewById(R.id.main_conversation_tv);
        mMainGroupTv = (TextView) findViewById(R.id.main_group_tv);
        mMainChatroomTv = (TextView) findViewById(R.id.main_chatroom_tv);
        mMainCustomerTv = (TextView) findViewById(R.id.main_customer_tv);
        mViewPager = (ViewPager) findViewById(R.id.main_viewpager);
        mMainSelectImg = (ImageView) findViewById(R.id.main_switch_img);
        mUnreadNumView = (TextView) findViewById(R.id.de_num);

        ViewGroup.LayoutParams cursor_Params = mMainSelectImg.getLayoutParams();
        cursor_Params.width = indicatorWidth;// 初始化滑动下标的宽
        mMainSelectImg.setLayoutParams(cursor_Params);
        // 获取布局填充器
        mInflater = (LayoutInflater) this
                .getSystemService(LAYOUT_INFLATER_SERVICE);
        if (getIntent() != null) {
            if (getIntent().hasExtra("PUSH_CONTEXT")) {
                if (getIntent().getStringExtra("PUSH_CONTEXT").equals("push")) {
                    Log.e(TAG, "--------0527---PUSH_CONTEXT------" + getIntent().getStringExtra("PUSH_CONTEXT"));
                    push();
                }
            }
        }

    }

    protected void initData() {
        activityManager = (ActivityManager) this.getSystemService(ACTIVITY_SERVICE);
        mMainChatroomLiner.setOnClickListener(this);
        mMainConversationLiner.setOnClickListener(this);
        mMainGroupLiner.setOnClickListener(this);
        mMainCustomerLiner.setOnClickListener(this);
        mDemoFragmentPagerAdapter = new DemoFragmentPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mDemoFragmentPagerAdapter);
        mViewPager.setOnPageChangeListener(this);
        mViewPager.setOffscreenPageLimit(3);
        //发起获取好友列表的http请求  (注：非融云SDK接口，是demo接口)
        if (DemoContext.getInstance() != null) {

//            getUserInfoHttpRequest = DemoContext.getInstance().getDemoApi().getFriends(MainActivity.this);

            getFriendsHttpRequest = DemoContext.getInstance().getDemoApi().getNewFriendlist(MainActivity.this);
            if (mDialog != null && !mDialog.isShowing()) {
                mDialog.show();
            }
        }

        final Conversation.ConversationType[] conversationTypes = {Conversation.ConversationType.PRIVATE, Conversation.ConversationType.DISCUSSION,
                Conversation.ConversationType.GROUP, Conversation.ConversationType.SYSTEM,
                Conversation.ConversationType.APP_PUBLIC_SERVICE, Conversation.ConversationType.PUBLIC_SERVICE};

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                RongIM.getInstance().setOnReceiveUnreadCountChangedListener(mCountListener, conversationTypes);
            }
        }, 500);


        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_DMEO_RECEIVE_MESSAGE);
        if (mBroadcastReciver == null) {
            mBroadcastReciver = new ReceiveMessageBroadcastReciver();
        }
        this.registerReceiver(mBroadcastReciver, intentFilter);

    }

    public RongIM.OnReceiveUnreadCountChangedListener mCountListener = new RongIM.OnReceiveUnreadCountChangedListener() {
        @Override
        public void onMessageIncreased(int count) {
            if (count == 0) {
                mUnreadNumView.setVisibility(View.GONE);
            } else if (count > 0 && count < 100) {
                mUnreadNumView.setVisibility(View.VISIBLE);
                mUnreadNumView.setText(count + "");
            } else {
                mUnreadNumView.setVisibility(View.VISIBLE);
                mUnreadNumView.setText(R.string.no_read_message);
            }
        }
    };


    public void push() {
        if (getIntent() != null) {
            if (DemoContext.getInstance() != null) {

                String token = DemoContext.getInstance().getSharedPreferences().getString("DEMO_TOKEN", "defult");
                Log.e(TAG,"-------------527----token:"+token);
                reconnect(token);

            }
        }
    }

    /**
     * 收到push消息后做重连，重新连接融云
     *
     * @param token
     */
    private void reconnect(String token) {


//        mDialog.setCancelable(false);
        mDialog.setText("正在连接中...");
        mDialog.show();

        try {
            RongIM.connect(token, new RongIMClient.ConnectCallback()  {
                @Override
                public void onTokenIncorrect() {

                }

                @Override
                public void onSuccess(String userId) {
                    Log.e(TAG, "-------------527--onSuccess--userId:" + userId);
                    if (mDialog != null)
                        mDialog.dismiss();

                    Intent intent = getIntent();
                    if (intent != null)
                        enterFragment(intent);

                }

                @Override
                public void onError(RongIMClient.ErrorCode e) {
                    Log.e(TAG, "-------------527--onError--e:" + e);
                    mDialog.dismiss();

                }
            });
        } catch (Exception e) {
            Log.e(TAG,"-------------527--Exception--e:"+e);
            mDialog.dismiss();

            e.printStackTrace();
        }

    }

    @Override
    public void onMenuVisibilityChanged(boolean b) {

    }


    @Override
    public void onPageScrolled(int i, float v, int i2) {

    }

    @Override
    public void onPageSelected(int i) {

        switch (i) {
            case 0:
                selectNavSelection(0);
                break;
            case 1:
                selectNavSelection(1);
                break;
            case 2:
                selectNavSelection(2);
                break;
            case 3:
                selectNavSelection(3);
                break;
        }
    }

    @Override
    public void onPageScrollStateChanged(int i) {

    }

    @Override
    public boolean handleMessage(Message message) {
        return false;
    }


    private class DemoFragmentPagerAdapter extends android.support.v4.app.FragmentPagerAdapter {

        public DemoFragmentPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            Fragment fragment = null;
            switch (i) {
                case 0:
                    mMainConversationTv.setTextColor(getResources().getColor(R.color.de_title_bg));
                    //TODO
                    if (mConversationFragment == null) {
                        ConversationListFragment listFragment = ConversationListFragment.getInstance();
                        Uri uri = Uri.parse("rong://" + getApplicationInfo().packageName).buildUpon()
                                .appendPath("conversationlist")
                                .appendQueryParameter(Conversation.ConversationType.PRIVATE.getName(), "false") //设置私聊会话是否聚合显示
                                .appendQueryParameter(Conversation.ConversationType.GROUP.getName(), "true")//群组
                                .appendQueryParameter(Conversation.ConversationType.DISCUSSION.getName(), "false")//讨论组
//                                .appendQueryParameter(Conversation.ConversationType.CHATROOM.getName(), "false")//聊天室
//                                .appendQueryParameter(Conversation.ConversationType.CUSTOMER_SERVICE.getName(), "false")//客服
                                .appendQueryParameter(Conversation.ConversationType.APP_PUBLIC_SERVICE.getName(), "false")//应用公众服务。
                                .appendQueryParameter(Conversation.ConversationType.PUBLIC_SERVICE.getName(), "false")//公共服务号
                                .appendQueryParameter(Conversation.ConversationType.SYSTEM.getName(), "false")//系统
                                .build();
                        listFragment.setUri(uri);
                        fragment = listFragment;
                    } else {
                        fragment = mConversationFragment;

//                        fragment = new TestFragment();
                    }
                    break;
                case 1:
                    if (mGroupListFragment == null) {
                        mGroupListFragment = new GroupListFragment();
                    }

                    fragment = mGroupListFragment;

                    break;

                case 2:
                    if (mChatroomFragment == null) {
                        fragment = new ChatRoomListFragment();
                    } else {
                        fragment = mChatroomFragment;
                    }
                    break;
                case 3:
                    if (mCustomerFragment == null) {
                        fragment = new CustomerFragment();
                    } else {
                        fragment = mCustomerFragment;
                    }
                    break;
            }
            return fragment;
        }

        @Override
        public int getCount() {
            return 4;
        }
    }

    private void selectNavSelection(int index) {
        clearSelection();
        switch (index) {
            case 0:
                mMainConversationTv.setTextColor(getResources().getColor(R.color.de_title_bg));
                TranslateAnimation animation = new TranslateAnimation(0, 0,
                        0f, 0f);
                animation.setInterpolator(new LinearInterpolator());
                animation.setDuration(100);
                animation.setFillAfter(true);
                mMainSelectImg.startAnimation(animation);

                break;
            case 1:
                mMainGroupTv.setTextColor(getResources().getColor(R.color.de_title_bg));
                TranslateAnimation animation1 = new TranslateAnimation(
                        indicatorWidth, indicatorWidth,
                        0f, 0f);
                animation1.setInterpolator(new LinearInterpolator());
                animation1.setDuration(100);
                animation1.setFillAfter(true);
                mMainSelectImg.startAnimation(animation1);

                break;
            case 2:
                mMainChatroomTv.setTextColor(getResources().getColor(R.color.de_title_bg));
                TranslateAnimation animation2 = new TranslateAnimation(
                        2 * indicatorWidth, indicatorWidth * 2,
                        0f, 0f);
                animation2.setInterpolator(new LinearInterpolator());
                animation2.setDuration(100);
                animation2.setFillAfter(true);
                mMainSelectImg.startAnimation(animation2);

                break;
            case 3:
                mMainCustomerTv.setTextColor(getResources().getColor(R.color.de_title_bg));
                TranslateAnimation animation3 = new TranslateAnimation(
                        3 * indicatorWidth, indicatorWidth * 3,
                        0f, 0f);
                animation3.setInterpolator(new LinearInterpolator());
                animation3.setDuration(100);
                animation3.setFillAfter(true);
                mMainSelectImg.startAnimation(animation3);
                break;
        }
    }

    private void clearSelection() {
        mMainConversationTv.setTextColor(getResources().getColor(R.color.black_textview));
        mMainGroupTv.setTextColor(getResources().getColor(R.color.black_textview));
        mMainChatroomTv.setTextColor(getResources().getColor(R.color.black_textview));
        mMainCustomerTv.setTextColor(getResources().getColor(R.color.black_textview));
    }

    private class ReceiveMessageBroadcastReciver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            //收到好友添加的邀请，需要更新 Actionbar
            if (action.equals(ACTION_DMEO_RECEIVE_MESSAGE)) {
                hasNewFriends = intent.getBooleanExtra("has_message", false);
                supportInvalidateOptionsMenu();
            }
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.main_conversation_liner:
                mViewPager.setCurrentItem(0);
                break;
            case R.id.main_group_liner:
                mViewPager.setCurrentItem(1);
                break;
            case R.id.main_chatroom_liner:
                mViewPager.setCurrentItem(2);
                break;
            case R.id.main_customer_liner:
                mViewPager.setCurrentItem(3);
                break;
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        this.mMenu = menu;
        inflater.inflate(R.menu.de_main_menu, menu);
        if (hasNewFriends) {
            mMenu.getItem(0).setIcon(getResources().getDrawable(R.drawable.de_ic_add_hasmessage));
            mMenu.getItem(0).getSubMenu().getItem(2).setIcon(getResources().getDrawable(R.drawable.de_btn_main_contacts_select));
        } else {
            mMenu.getItem(0).setIcon(getResources().getDrawable(R.drawable.de_ic_add));
            mMenu.getItem(0).getSubMenu().getItem(2).setIcon(getResources().getDrawable(R.drawable.de_btn_main_contacts));
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add_item1://发起聊天
                startActivity(new Intent(this, FriendListActivity.class));
                break;
            case R.id.add_item2://选择群组
//                sendMessage();
//                startActivity(new Intent(MainActivity.this,TestActivity.class));

                if(RongIM.getInstance() != null)
                    RongIM.getInstance().startSubConversationList(this, Conversation.ConversationType.GROUP);


                break;
            case R.id.add_item3://通讯录
                startActivity(new Intent(MainActivity.this, DeAdressListActivity.class));
                break;
            case R.id.set_item1://我的账号
                startActivity(new Intent(MainActivity.this, MyAccountActivity.class));
                break;
            case R.id.set_item2://新消息提醒
                startActivity(new Intent(MainActivity.this, NewMessageRemindActivity.class));
                break;
            case R.id.set_item3://隐私
                startActivity(new Intent(MainActivity.this, PrivacyActivity.class));
                break;
            case R.id.set_item4://关于融云
                startActivity(new Intent(MainActivity.this, AboutRongCloudActivity.class));
                break;
            case R.id.set_item5://退出

                final AlertDialog.Builder alterDialog = new AlertDialog.Builder(this);
                alterDialog.setMessage("确定退出应用？");
                alterDialog.setCancelable(true);

                alterDialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {


                        if (RongIM.getInstance() != null) {
                            RongIM.getInstance().disconnect(false);
                        }
                        killThisPackageIfRunning(MainActivity.this, "io.rong.imlib.ipc");
                        Process.killProcess(Process.myPid());
                    }
                });
                alterDialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                alterDialog.show();

                break;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onCallApiSuccess(AbstractHttpRequest request, Object obj) {

        if (getFriendsHttpRequest == request) {
            if (obj instanceof Friends) {
                final Friends friends = (Friends) obj;
                if (friends.getCode() == 200) {
                    ArrayList<UserInfo> friendreslut = new ArrayList<UserInfo>();
                    //status : 1 好友, 2 请求添加, 3 请求被添加, 4 请求被拒绝, 5 我被对方删除
                    for (int i = 0; i < friends.getResult().size(); i++) {
                        //此处定义的好友为：1 好友，3 请求被添加, 5 我被对方删除
                        if (friends.getResult().get(i).getStatus() == 1 || friends.getResult().get(i).getStatus() == 3 || friends.getResult().get(i).getStatus() == 5) {
                            UserInfo info = new UserInfo(String.valueOf(friends.getResult().get(i).getId()), friends.getResult().get(i).getUsername(), friends.getResult().get(i).getPortrait() == null ? null : Uri.parse(friends.getResult().get(i).getPortrait()));
                            friendreslut.add(info);
                        }
                    }
                    if (DemoContext.getInstance() != null)

                        DemoContext.getInstance().setFriends(friendreslut);

                    if (mDialog != null)
                        mDialog.dismiss();

                }
            }

        }
    }

    @Override
    public void onCallApiFailure(AbstractHttpRequest request, BaseException e) {

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (KeyEvent.KEYCODE_BACK == event.getKeyCode()) {


            final AlertDialog.Builder alterDialog = new AlertDialog.Builder(this);
            alterDialog.setMessage("确定退出应用？");
            alterDialog.setCancelable(true);

            alterDialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (RongIM.getInstance() != null)
                        RongIM.getInstance().disconnect(true);

                    killThisPackageIfRunning(MainActivity.this, "io.rong.imlib.ipc");
                    Process.killProcess(Process.myPid());

                }
            });
            alterDialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            alterDialog.show();
        }

        return false;
    }

    public static void killThisPackageIfRunning(final Context context, String packageName) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        activityManager.killBackgroundProcesses(packageName);
    }


    /**
     * 消息分发，选择跳转到哪个fragment
     *
     * @param intent
     */
    private void enterFragment(Intent intent) {
        String tag = null;
        if (intent != null) {
            Fragment fragment = null;

            if (intent.getExtras() != null && intent.getExtras().containsKey(RongConst.EXTRA.CONTENT)) {
                String fragmentName = intent.getExtras().getString(RongConst.EXTRA.CONTENT);
                fragment = Fragment.instantiate(this, fragmentName);
            } else if (intent.getData() != null) {
                if (intent.getData().getPathSegments().get(0).equals("conversation")) {
                    tag = "conversation";
                    if (intent.getData().getLastPathSegment().equals("system")) {
                        //注释掉的代码为不加输入框的聊天页面（此处作为示例）
//                        String fragmentName = MessageListFragment.class.getCanonicalName();
//                        fragment = Fragment.instantiate(this, fragmentName);
                        startActivity(new Intent(MainActivity.this, NewFriendListActivity.class));
                        finish();
                        List<Conversation> conversations = RongIM.getInstance().getRongIMClient().getConversationList(Conversation.ConversationType.SYSTEM);
                        for (int i = 0; i < conversations.size(); i++) {
                            RongIM.getInstance().getRongIMClient().clearMessagesUnreadStatus(Conversation.ConversationType.SYSTEM, conversations.get(i).getSenderUserId());
                        }
                    } else {
                        String fragmentName = ConversationFragment.class.getCanonicalName();
                        fragment = Fragment.instantiate(this, fragmentName);
                    }
                } else if (intent.getData().getLastPathSegment().equals("conversationlist")) {
                    tag = "conversationlist";
                    String fragmentName = ConversationListFragment.class.getCanonicalName();
                    fragment = Fragment.instantiate(this, fragmentName);
                } else if (intent.getData().getLastPathSegment().equals("subconversationlist")) {
                    tag = "subconversationlist";
                    String fragmentName = SubConversationListFragment.class.getCanonicalName();
                    fragment = Fragment.instantiate(this, fragmentName);
                }
            }

            if (fragment != null) {
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.add(R.id.de_content, fragment, tag);
                transaction.addToBackStack(null).commitAllowingStateLoss();
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (mBroadcastReciver != null) {
            this.unregisterReceiver(mBroadcastReciver);
        }
        super.onDestroy();
    }
    /**
     * 添加好友成功后，向对方发送一条消息
     *
     */
    private void sendMessage() {
//        26590   114
       String id = "26590";
        final DeAgreedFriendRequestMessage message = new DeAgreedFriendRequestMessage(id, "agree");
        if (DemoContext.getInstance() != null) {
            //获取当前用户的 userid
            String userid = DemoContext.getInstance().getSharedPreferences().getString("DEMO_USERID", "defalte");
            UserInfo userInfo = DemoContext.getInstance().getUserInfoById(userid);
            //把用户信息设置到消息体中，直接发送给对方，可以不设置，非必选项
            message.setUserInfo(userInfo);
            if (RongIM.getInstance() != null) {

                //发送一条添加成功的自定义消息，此条消息不会在ui上展示
                RongIM.getInstance().getRongIMClient().sendMessage(Conversation.ConversationType.PRIVATE, id, message, null, new RongIMClient.SendMessageCallback() {
                    @Override
                    public void onError(Integer messageId, RongIMClient.ErrorCode e) {
                        Log.e(TAG, Constants.DEBUG + "------DeAgreedFriendRequestMessage----onError--");
                        if (mDialog != null)
                            mDialog.dismiss();
                    }

                    @Override
                    public void onSuccess(Integer integer) {
                        Log.e(TAG, Constants.DEBUG + "------DeAgreedFriendRequestMessage----onSuccess--" + message.getMessage());
                        if (mDialog != null)
                            mDialog.dismiss();
                    }
                });
            }
        }
    }

}
