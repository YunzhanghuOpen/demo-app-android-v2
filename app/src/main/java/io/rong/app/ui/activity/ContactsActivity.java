package io.rong.app.ui.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.rong.app.R;
import io.rong.app.RongCloudEvent;
import io.rong.app.db.DBManager;
import io.rong.app.db.FriendDao;
import io.rong.app.server.broadcast.BroadcastManager;
import io.rong.app.server.pinyin.CharacterParser;
import io.rong.app.server.pinyin.Friend;
import io.rong.app.server.pinyin.PinyinComparator;
import io.rong.app.server.pinyin.SideBar;
import io.rong.app.server.utils.NToast;
import io.rong.app.ui.adapter.FriendAdapter;
import io.rong.imkit.RongIM;

/**
 * Created by Administrator on 2015/3/26.
 */
public class ContactsActivity extends BaseActivity implements View.OnClickListener {

    private String TAG = ContactsActivity.class.getSimpleName();

    private EditText mSearch;

    private ListView mListView;

    private List<Friend> dataLsit = new ArrayList<>();

    private List<Friend> sourceDataList = new ArrayList<>();


    /**
     * 好友列表的 adapter
     */
    private FriendAdapter adapter;
    /**
     * 右侧好友指示 Bar
     */
    private SideBar mSidBar;
    /**
     * 中部展示的字母提示
     */
    public TextView dialog;

    /**
     * 汉字转换成拼音的类
     */
    private CharacterParser characterParser;
    /**
     * 根据拼音来排列ListView里面的数据类
     */
    private PinyinComparator pinyinComparator;

    private LayoutInflater infalter;

    private TextView mNoFriends;
    private TextView unread;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.de_ac_address_fragment);
        getSupportActionBar().setTitle(R.string.add_contacts);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.de_actionbar_back);
        initView();
        initData();

        if (dataLsit != null && dataLsit.size() > 0) {
            sourceDataList = filledData(dataLsit); //过滤数据为有字母的字段  现在有字母 别的数据没有
        } else {
            mNoFriends.setVisibility(View.VISIBLE);
            NToast.shortToast(mContext, "暂无好友数据");
        }

        //还原除了带字母字段的其他数据
        for (int i = 0; i < dataLsit.size(); i++) {
            sourceDataList.get(i).setName(dataLsit.get(i).getName());
            sourceDataList.get(i).setUserId(dataLsit.get(i).getUserId());
            sourceDataList.get(i).setPortraitUri(dataLsit.get(i).getPortraitUri());
            sourceDataList.get(i).setDisplayName(dataLsit.get(i).getDisplayName());
        }

        // 根据a-z进行排序源数据
        Collections.sort(sourceDataList, pinyinComparator);

        infalter = LayoutInflater.from(this);
        View headView = infalter.inflate(R.layout.item_contact_list_header,
                null);
        unread = (TextView)headView.findViewById(R.id.tv_unread);
        RelativeLayout re_newfriends = (RelativeLayout) headView.findViewById(R.id.re_newfriends);
        re_newfriends.setOnClickListener(this);
        adapter = new FriendAdapter(this, sourceDataList);
        mListView.addHeaderView(headView);
        mListView.setAdapter(adapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position != 0) {
                    if (RongIM.getInstance() != null) {
                        Friend bean = sourceDataList.get(position - 1);
                        RongIM.getInstance().startPrivateChat(mContext, bean.getUserId(), bean.getName());
                    }
                }
            }
        });
        mSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //当输入框里面的值为空，更新为原来的列表，否则为过滤数据列表
                filterData(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        refreshUIListener();
    }

    private void initData() {
        List<io.rong.app.db.Friend> list = DBManager.getInstance(mContext).getDaoSession().getFriendDao().loadAll();
        if (list != null && list.size() > 0) {
            for (io.rong.app.db.Friend friend : list) {
                dataLsit.add(new Friend(friend.getUserId(), friend.getName(), friend.getPortraitUri(), friend.getDisplayName(), null, null));
            }

        }
    }

    private void initView() {
        //实例化汉字转拼音类
        characterParser = CharacterParser.getInstance();
        pinyinComparator = PinyinComparator.getInstance();
        mSearch = (EditText) findViewById(R.id.search);
        mListView = (ListView) findViewById(R.id.listview);
        mNoFriends = (TextView) findViewById(R.id.show_no_friend);
        mSidBar = (SideBar) findViewById(R.id.sidrbar);
        dialog = (TextView) findViewById(R.id.dialog);
        mSidBar.setTextView(dialog);
        //设置右侧触摸监听
        mSidBar.setOnTouchingLetterChangedListener(new SideBar.OnTouchingLetterChangedListener() {

            @Override
            public void onTouchingLetterChanged(String s) {
                //该字母首次出现的位置
                int position = adapter.getPositionForSection(s.charAt(0));
                if (position != -1) {
                    mListView.setSelection(position);
                }

            }
        });
    }


    public TextView getDialog() {
        return dialog;
    }

    public void setDialog(TextView dialog) {
        this.dialog = dialog;
    }


    /**
     * 根据输入框中的值来过滤数据并更新ListView
     *
     * @param filterStr
     */
    private void filterData(String filterStr) {
        List<Friend> filterDateList = new ArrayList<Friend>();

        if (TextUtils.isEmpty(filterStr)) {
            filterDateList = sourceDataList;
        } else {
            filterDateList.clear();
            for (Friend friendModel : sourceDataList) {
                String name = friendModel.getName();
                if (name.indexOf(filterStr.toString()) != -1 || characterParser.getSelling(name).startsWith(filterStr.toString())) {
                    filterDateList.add(friendModel);
                }
            }
        }

        // 根据a-z进行排序
        Collections.sort(filterDateList, pinyinComparator);
        adapter.updateListView(filterDateList);
    }


    /**
     * 为ListView填充数据
     *
     * @param
     * @return
     */
    private List<Friend> filledData(List<Friend> lsit) {
        List<Friend> mFriendList = new ArrayList<Friend>();

        for (int i = 0; i < lsit.size(); i++) {
            Friend friendModel = new Friend();
            friendModel.setName(lsit.get(i).getName());
            //汉字转换成拼音
            String pinyin = characterParser.getSelling(lsit.get(i).getName());
            String sortString = pinyin.substring(0, 1).toUpperCase();

            // 正则表达式，判断首字母是否是英文字母
            if (sortString.matches("[A-Z]")) {
                friendModel.setLetters(sortString.toUpperCase());
            } else {
                friendModel.setLetters("#");
            }

            mFriendList.add(friendModel);
        }
        return mFriendList;

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.re_newfriends:
                unread.setVisibility(View.GONE);
                Intent intent = new Intent(this, NewFriendListActivity.class);
                startActivityForResult(intent, 20);
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);
    }

    private void refreshUIListener() {
        BroadcastManager.getInstance(mContext).addAction(RongCloudEvent.UPDATEFRIEND, new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String command = intent.getAction();
                if (!TextUtils.isEmpty(command)) {
                    List<io.rong.app.db.Friend> list = DBManager.getInstance(mContext).getDaoSession().getFriendDao().loadAll();
                    if (list != null && list.size() > 0) {
                        if (dataLsit!= null ) {
                            dataLsit.clear();
                        }
                        if (sourceDataList != null) {
                            sourceDataList.clear();
                        }
                        for (io.rong.app.db.Friend friend : list) {
                            dataLsit.add(new Friend(friend.getUserId(), friend.getName(), friend.getPortraitUri()));
                        }

                    }
                    if (dataLsit != null && dataLsit.size() > 0) {
                        sourceDataList = filledData(dataLsit); //过滤数据为有字母的字段  现在有字母 别的数据没有
                    } else {
                        mNoFriends.setVisibility(View.VISIBLE);
                        NToast.shortToast(mContext, "暂无好友数据");
                    }

                    //还原除了带字母字段的其他数据
                    for (int i = 0; i < dataLsit.size(); i++) {
                        sourceDataList.get(i).setName(dataLsit.get(i).getName());
                        sourceDataList.get(i).setUserId(dataLsit.get(i).getUserId());
                        sourceDataList.get(i).setPortraitUri(dataLsit.get(i).getPortraitUri());
                        sourceDataList.get(i).setDisplayName(dataLsit.get(i).getDisplayName());
                    }

                    // 根据a-z进行排序源数据
                    Collections.sort(sourceDataList, pinyinComparator);
                    adapter.updateListView(sourceDataList);


                }
            }
        });

        BroadcastManager.getInstance(mContext).addAction(RongCloudEvent.UPDATEREDDOT, new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String command = intent.getAction();
                if (!TextUtils.isEmpty(command)) {
                    unread.setVisibility(View.VISIBLE);
                }
            }
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        BroadcastManager.getInstance(mContext).destroy(RongCloudEvent.UPDATEFRIEND);
        BroadcastManager.getInstance(mContext).destroy(RongCloudEvent.UPDATEREDDOT);
    }
}
