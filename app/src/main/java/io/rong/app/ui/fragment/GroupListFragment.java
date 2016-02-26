package io.rong.app.ui.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.Serializable;
import java.util.List;

import io.rong.app.App;
import io.rong.app.R;
import io.rong.app.RongCloudEvent;
import io.rong.app.db.DBManager;
import io.rong.app.db.Qun;
import io.rong.app.server.SealAction;
import io.rong.app.server.broadcast.BroadcastManager;
import io.rong.app.server.network.async.AsyncTaskManager;
import io.rong.app.server.network.async.OnDataListener;
import io.rong.app.server.network.http.HttpException;
import io.rong.app.server.response.GetGroupResponse;
import io.rong.app.server.utils.NLog;
import io.rong.app.server.utils.NToast;
import io.rong.app.ui.activity.CreateGroupActivity;
import io.rong.app.ui.activity.NewGroupDetailActivity;

/**
 * Created by Bob on 2015/1/25.
 */
public class GroupListFragment extends Fragment {
    private static final int REFRESHGROUPUI = 22;

    private ListView mGroupListView;

//    private GroupListAdapter mGroupListAdapter;

    private GroupAdapter adapter;

    private TextView mNoGroups;


    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.de_fr_group_list, container, false);
        mGroupListView = (ListView) view.findViewById(R.id.group_listview);
        mNoGroups = (TextView) view.findViewById(R.id.show_no_group);
        initData();
        refreshUIListener();
        initNetUpdateUI();
        return view;
    }

    private void initNetUpdateUI() {
        BroadcastManager.getInstance(getActivity()).addAction(RongCloudEvent.NETUPDATEGROUP, new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String command = intent.getAction();
                if (!TextUtils.isEmpty(command)) {
                    AsyncTaskManager.getInstance(getActivity()).request(REFRESHGROUPUI, new OnDataListener() {
                        @Override
                        public Object doInBackground(int requsetCode) throws HttpException {
                            return new SealAction(getActivity()).getGroups();
                        }

                        @Override
                        public void onSuccess(int requestCode, Object result) {
                            if (result != null) {
                                GetGroupResponse response = (GetGroupResponse) result;
                                if (response.getCode() == 200) {
                                    DBManager.getInstance(getActivity()).getDaoSession().getQunDao().deleteAll();
                                    List<GetGroupResponse.ResultEntity> list = response.getResult();
                                    if (list.size() > 0 && list != null) { //服务端上也没有群组数据
                                        for (GetGroupResponse.ResultEntity g : list) {
                                            DBManager.getInstance(getActivity()).getDaoSession().getQunDao().insertOrReplace(
                                                    new Qun(g.getGroup().getId(), g.getGroup().getName(), g.getGroup().getPortraitUri(), String.valueOf(g.getRole()))
                                            );
                                        }
                                    }
                                    new android.os.Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            List<Qun> list = DBManager.getInstance(getActivity()).getDaoSession().getQunDao().loadAll();
                                                if (adapter != null) {
                                                    adapter.updateListView(list);
                                                }else {
                                                    GroupAdapter gAdapter = new GroupAdapter(getActivity(),list);
                                                    mGroupListView.setAdapter(gAdapter);
                                                }
                                            NLog.e(RongCloudEvent.NETUPDATEGROUP, "数据刷新成功");
                                        }
                                    }, 500);
                                }
                            }
                        }

                        @Override
                        public void onFailure(int requestCode, int state, Object result) {
                            NToast.shortToast(getActivity(), "刷新群组数据请求失败");
                        }
                    });
                }
            }
        });
    }


    private void initData() {
        List<Qun> list = DBManager.getInstance(getActivity()).getDaoSession().getQunDao().loadAll();
        if (list != null && list.size() > 0) {
            adapter = new GroupAdapter(getActivity(), list);
            mGroupListView.setAdapter(adapter);
            mNoGroups.setVisibility(View.GONE);
            mGroupListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent intent = new Intent(getActivity(), NewGroupDetailActivity.class);
                    intent.putExtra("QunBean", (Serializable) adapter.getItem(position));
                    startActivityForResult(intent, 99);
                }
            });
        } else {
            mNoGroups.setVisibility(View.VISIBLE);
        }

    }

    private void refreshUIListener() {
        BroadcastManager.getInstance(getActivity()).addAction(CreateGroupActivity.REFRESHGROUPUI, new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String command = intent.getAction();
                if (!TextUtils.isEmpty(command)) {
                    initData();
                }
            }
        });
    }


    class GroupAdapter extends BaseAdapter {

        private Context context;

        private List<Qun> list;

        public GroupAdapter(Context context, List<Qun> list) {
            this.context = context;
            this.list = list;
        }

        /**
         * 传入新的数据 刷新UI的方法
         */
        public void updateListView(List<Qun> list) {
            this.list = list;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder = null;
            final Qun mContent = list.get(position);
            if (convertView == null) {
                viewHolder = new ViewHolder();
                convertView = LayoutInflater.from(context).inflate(R.layout.group_item_new, null);
                viewHolder.tvTitle = (TextView) convertView.findViewById(R.id.groupname);
                viewHolder.mImageView = (ImageView) convertView.findViewById(R.id.groupuri);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            viewHolder.tvTitle.setText(mContent.getName());
            ImageLoader.getInstance().displayImage(mContent.getPortraitUri(), viewHolder.mImageView, App.getOptions());
            return convertView;
        }


        class ViewHolder {
            /**
             * 昵称
             */
            TextView tvTitle;
            /**
             * 头像
             */
            ImageView mImageView;
            /**
             * userid
             */
//        TextView tvUserId;
        }
    }


    @Override
    public void onDestroy() {
        BroadcastManager.getInstance(getActivity()).destroy(CreateGroupActivity.REFRESHGROUPUI);
        BroadcastManager.getInstance(getActivity()).destroy(RongCloudEvent.NETUPDATEGROUP);
        super.onDestroy();
    }

}
