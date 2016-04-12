package io.rong.app.ui.fragment;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.rong.app.R;
import io.rong.app.db.DBManager;
import io.rong.app.db.Qun;
import io.rong.app.server.SealAction;
import io.rong.app.server.network.async.AsyncTaskManager;
import io.rong.app.server.network.async.OnDataListener;
import io.rong.app.server.network.http.HttpException;
import io.rong.app.server.network.http.SyncHttpClient;
import io.rong.app.server.response.DefaultConversationResponse;
import io.rong.app.server.response.DeleteFriendResponse;
import io.rong.app.server.response.GetGroupResponse;
import io.rong.app.server.response.JoinGroupResponse;
import io.rong.app.server.utils.NToast;
import io.rong.app.server.widget.LoadDialog;
import io.rong.app.ui.activity.NewGroupDetailActivity;
import io.rong.imkit.RongIM;
import io.rong.imkit.widget.AsyncImageView;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;

/**
 * Created by Administrator on 2015/3/6.
 */
public class ChatRoomListFragment extends Fragment implements OnDataListener, View.OnClickListener {


    private static final int GETDEFCONVERSATION = 333;
    private static final int JOINGROUP = 334;
    private static final int JOINGROUP1 = 335;
    private static final int JOINGROUP2 = 336;
    private static final int GETGROUPS = 337;
    private TextView chatRoom1, chatRoom2, chatRoom3, chatRoom4, groupNumber1, groupNumber2, groupNumber3, groupName1, groupName2, groupName3;

    private Button groupState1, groupState2, groupState3, groupStateChat1, groupStateChat2, groupStateChat3;

    private AsyncTaskManager atm = AsyncTaskManager.getInstance(getActivity());

    private ArrayList<DefaultConversationResponse.ResultEntity> groupList;

    private ArrayList<DefaultConversationResponse.ResultEntity> chatroomList;

    private LinearLayout groupItem1,groupItem2,groupItem3;

    private boolean isJoin1, isJoin2,isJoin3;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.de_fr_chatroom_list, container, false);
        initViews(view);
        atm.request(GETDEFCONVERSATION, this);
        return view;
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.def_chatroom1:
                if (RongIM.getInstance() != null && RongIM.getInstance().getRongIMClient() != null && chatroomList != null && chatroomList.get(0) != null) {
                    RongIM.getInstance().startConversation(getActivity(), Conversation.ConversationType.CHATROOM, chatroomList.get(0).getId(), "聊天室1");
                }
                break;
            case R.id.def_chatroom2:
                if (RongIM.getInstance() != null && RongIM.getInstance().getRongIMClient() != null && chatroomList != null && chatroomList.get(1) != null) {
                    RongIM.getInstance().startConversation(getActivity(), Conversation.ConversationType.CHATROOM, chatroomList.get(1).getId(), "聊天室2");
                }
                break;
            case R.id.def_chatroom3:
                if (RongIM.getInstance() != null && RongIM.getInstance().getRongIMClient() != null && chatroomList != null && chatroomList.get(2) != null) {
                    RongIM.getInstance().startConversation(getActivity(), Conversation.ConversationType.CHATROOM, chatroomList.get(2).getId(), "聊天室3");
                }
                break;
            case R.id.def_chatroom4:
                if (RongIM.getInstance() != null && RongIM.getInstance().getRongIMClient() != null && chatroomList != null && chatroomList.get(3) != null) {
                    RongIM.getInstance().startConversation(getActivity(), Conversation.ConversationType.CHATROOM, chatroomList.get(3).getId(), "聊天室4");
                }
                break;
            case R.id.def_state_group1_chat:
                RongIM.getInstance().startConversation(getActivity(), Conversation.ConversationType.GROUP, groupList.get(0).getId(), "技术交流群1");
                break;
            case R.id.def_state_group2_chat:
                RongIM.getInstance().startConversation(getActivity(), Conversation.ConversationType.GROUP, groupList.get(1).getId(), "技术交流群2");
                break;
            case R.id.def_state_group3_chat:
                RongIM.getInstance().startConversation(getActivity(), Conversation.ConversationType.GROUP, groupList.get(2).getId(), "技术交流群3");
                break;
            case R.id.def_state_group1:
                if (RongIM.getInstance() != null && RongIM.getInstance().getRongIMClient() != null && groupList.get(0) != null) {
                    LoadDialog.show(getActivity());
                    atm.request(JOINGROUP, this);
                }
                break;
            case R.id.def_state_group2:
                if (RongIM.getInstance() != null && RongIM.getInstance().getRongIMClient() != null && groupList.get(1) != null) {
                    LoadDialog.show(getActivity());
                    atm.request(JOINGROUP1, this);
                }
                break;
            case R.id.def_state_group3:
                if (RongIM.getInstance() != null && RongIM.getInstance().getRongIMClient() != null && groupList.get(2) != null) {
                    LoadDialog.show(getActivity());
                    atm.request(JOINGROUP2, this);
                }
                break;
            case R.id.group_item1:
                if (!isJoin1) {
                    NToast.shortToast(getActivity(),"非群组成员不能查看群组详情");
                    return;
                }
                Intent intent = new Intent(getActivity(), NewGroupDetailActivity.class);
                intent.putExtra("TargetId", groupList.get(0).getId());
                startActivity(intent);
                break;
            case R.id.group_item2:
                if (!isJoin2) {
                    NToast.shortToast(getActivity(),"非群组成员不能查看群组详情");
                    return;
                }
                Intent intent1 = new Intent(getActivity(), NewGroupDetailActivity.class);
                intent1.putExtra("TargetId", groupList.get(1).getId());
                startActivity(intent1);

                break;
            case R.id.group_item3:
                if (!isJoin3) {
                    NToast.shortToast(getActivity(),"非群组成员不能查看群组详情");
                    return;
                }
                Intent intent2 = new Intent(getActivity(), NewGroupDetailActivity.class);
                intent2.putExtra("TargetId", groupList.get(2).getId());
                startActivity(intent2);
                break;
        }
    }


    @Override
    public Object doInBackground(int requsetCode, String id) throws HttpException {
        switch (requsetCode) {
            case GETDEFCONVERSATION:
                return new SealAction(getActivity()).getDefaultConversation();
            case JOINGROUP:
                return new SealAction(getActivity()).JoinGroup(groupList.get(0).getId());
            case JOINGROUP1:
                return new SealAction(getActivity()).JoinGroup(groupList.get(1).getId());
            case JOINGROUP2:
                return new SealAction(getActivity()).JoinGroup(groupList.get(2).getId());
            case GETGROUPS:
                return new SealAction(getActivity()).getGroups();
        }
        return null;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onSuccess(int requestCode, Object result) {
        if (result != null) {
            switch (requestCode) {
                case GETDEFCONVERSATION:
                    DefaultConversationResponse response = (DefaultConversationResponse) result;
                    if (response.getCode() == 200) {


                        groupList = new ArrayList();
                        chatroomList = new ArrayList();
                        if (response.getResult().size() > 0) {
                            groupList.clear();
                            chatroomList.clear();
                            for (DefaultConversationResponse.ResultEntity d : response.getResult()) {
                                if (d.getType().equals("group")) {
                                    groupList.add(d);
                                } else {
                                    chatroomList.add(d);
                                }
                            }

                            atm.request(GETGROUPS, this);

                            if (chatroomList != null && chatroomList.size()>0) {
                                chatRoom1.setText(chatroomList.get(0).getName());
                                chatRoom2.setText(chatroomList.get(1).getName());
                                chatRoom3.setText(chatroomList.get(2).getName());
                                chatRoom4.setText(chatroomList.get(3).getName());
                            }
                            chatRoom1.setText(chatroomList.get(0).getName());
                            if (groupList.size() != 0) {
                                if (groupList.get(0) != null) {
                                    groupNumber1.setText(groupList.get(0).getMemberCount() + "/" + groupList.get(0).getMaxMemberCount());
                                    groupName1.setText(groupList.get(0).getName());
                                }
                                if (groupList.get(1) != null) {
                                    groupNumber2.setText(groupList.get(1).getMemberCount() + "/" + groupList.get(1).getMaxMemberCount());
                                    groupName2.setText(groupList.get(1).getName());
                                }
                                if (groupList.get(2) != null) {
                                    groupNumber3.setText(groupList.get(2).getMemberCount() + "/" + groupList.get(2).getMaxMemberCount());
                                    groupName3.setText(groupList.get(2).getName());
                                }
                            }
                        }

                    }
                    break;

                case JOINGROUP:
                    JoinGroupResponse j1 = (JoinGroupResponse) result;
                    if (j1.getCode() == 200) {
                        refresh();
                        NToast.shortToast(getActivity(), "加入成功");
                        LoadDialog.dismiss(getActivity());
                    }
                    break;
                case JOINGROUP1:
                    JoinGroupResponse j2 = (JoinGroupResponse) result;
                    if (j2.getCode() == 200) {
                        refresh();
                        NToast.shortToast(getActivity(), "加入成功");
                        LoadDialog.dismiss(getActivity());
                    }

                    break;
                case JOINGROUP2:
                    JoinGroupResponse j3 = (JoinGroupResponse) result;
                    if (j3.getCode() == 200) {
                        refresh();
                        NToast.shortToast(getActivity(), "加入成功");
                        LoadDialog.dismiss(getActivity());
                    }
                    break;
                case GETGROUPS:
                    GetGroupResponse getGroupResponse = (GetGroupResponse) result;
                    if (getGroupResponse.getCode() == 200) {
//                        DBManager.getInstance(getActivity()).getDaoSession().getQunDao().deleteAll();
                        for (GetGroupResponse.ResultEntity g : getGroupResponse.getResult()) {
                            DBManager.getInstance(getActivity()).getDaoSession().getQunDao().insertOrReplace(
                                    new Qun(g.getGroup().getId(), g.getGroup().getName(), g.getGroup().getPortraitUri(), String.valueOf(g.getRole()))
                            );
                        }

                        isJoin1 = false;
                        isJoin2 = false;
                        isJoin3 = false;

                        List<Qun> list = DBManager.getInstance(getActivity()).getDaoSession().getQunDao().loadAll();
                        if (list != null && list.size() > 0) {
                            for (GetGroupResponse.ResultEntity g : getGroupResponse.getResult()) {
                                if (g.getGroup().getId().equals(groupList.get(0).getId())) {
                                    isJoin1 = true;
                                }
                                if (g.getGroup().getId().equals(groupList.get(1).getId())) {
                                    isJoin2 = true;
                                }
                                if (g.getGroup().getId().equals(groupList.get(2).getId())) {
                                    isJoin3 = true;
                                }
                            }
                            if (isJoin1) {
                                groupState1.setVisibility(View.GONE);
                                groupStateChat1.setVisibility(View.VISIBLE);
                            } else {
                                groupState1.setVisibility(View.VISIBLE);
                                groupStateChat1.setVisibility(View.GONE);
                            }
                            if (isJoin2) {
                                groupState2.setVisibility(View.GONE);
                                groupStateChat2.setVisibility(View.VISIBLE);
                            } else {
                                groupState2.setVisibility(View.VISIBLE);
                                groupStateChat2.setVisibility(View.GONE);
                            }
                            if (isJoin3) {
                                groupState3.setVisibility(View.GONE);
                                groupStateChat3.setVisibility(View.VISIBLE);
                            } else {
                                groupState3.setVisibility(View.VISIBLE);
                                groupStateChat3.setVisibility(View.GONE);
                            }

                        }
                    }
                    break;
            }
        }
    }

    @Override
    public void onFailure(int requestCode, int state, Object result) {
        switch (requestCode) {
            case GETDEFCONVERSATION:
                break;
        }
    }

    public void initViews(View view) {
        chatRoom1 = (TextView) view.findViewById(R.id.def_chatroom1);
        chatRoom2 = (TextView) view.findViewById(R.id.def_chatroom2);
        chatRoom3 = (TextView) view.findViewById(R.id.def_chatroom3);
        chatRoom4 = (TextView) view.findViewById(R.id.def_chatroom4);
        groupState1 = (Button) view.findViewById(R.id.def_state_group1);
        groupState2 = (Button) view.findViewById(R.id.def_state_group2);
        groupState3 = (Button) view.findViewById(R.id.def_state_group3);
        groupStateChat1 = (Button) view.findViewById(R.id.def_state_group1_chat);
        groupStateChat2 = (Button) view.findViewById(R.id.def_state_group2_chat);
        groupStateChat3 = (Button) view.findViewById(R.id.def_state_group3_chat);
        groupNumber1 = (TextView) view.findViewById(R.id.group_number1);
        groupNumber2 = (TextView) view.findViewById(R.id.group_number2);
        groupNumber3 = (TextView) view.findViewById(R.id.group_number3);
        groupName1 = (TextView) view.findViewById(R.id.def_group_name1);
        groupName2 = (TextView) view.findViewById(R.id.def_group_name2);
        groupName3 = (TextView) view.findViewById(R.id.def_group_name3);
        groupItem1 = (LinearLayout) view.findViewById(R.id.group_item1);
        groupItem2 = (LinearLayout) view.findViewById(R.id.group_item2);
        groupItem3 = (LinearLayout) view.findViewById(R.id.group_item3);
        chatRoom1.setOnClickListener(this);
        chatRoom2.setOnClickListener(this);
        chatRoom3.setOnClickListener(this);
        chatRoom4.setOnClickListener(this);
        groupState1.setOnClickListener(this);
        groupState2.setOnClickListener(this);
        groupState3.setOnClickListener(this);
        groupStateChat1.setOnClickListener(this);
        groupStateChat2.setOnClickListener(this);
        groupStateChat3.setOnClickListener(this);
        groupItem1.setOnClickListener(this);
        groupItem2.setOnClickListener(this);
        groupItem3.setOnClickListener(this);
    }


    @Override
    public void onResume() {
        super.onResume();
        refresh();
    }

    public void refresh() {
        atm.request(GETDEFCONVERSATION, this);
    }

}
