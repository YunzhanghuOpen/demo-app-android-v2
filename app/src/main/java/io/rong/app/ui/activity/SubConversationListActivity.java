package io.rong.app.ui.activity;

import android.content.Intent;
import android.os.Bundle;

import io.rong.app.R;
import io.rong.app.ui.adapter.SubConversationListAdapterEx;
import io.rong.imkit.fragment.SubConversationListFragment;

/**
 * Created by Bob on 15/11/3.
 * 聚合会话列表
 */
public class SubConversationListActivity extends BaseActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.subconversationlist);

        SubConversationListFragment fragment = (SubConversationListFragment) getSupportFragmentManager()
                .findFragmentById(R.id.subconversationlist);
        fragment.setAdapter(new SubConversationListAdapterEx(this));

        Intent intent = getIntent();
        //聚合会话参数
        String type = intent.getData().getQueryParameter("type");

        if(type == null )
            return;

        if (type.equals("group")) {
            getSupportActionBar().setTitle(R.string.de_actionbar_sub_group);
        } else if (type.equals("private")) {
            getSupportActionBar().setTitle(R.string.de_actionbar_sub_private);
        } else if (type.equals("discussion")) {
            getSupportActionBar().setTitle(R.string.de_actionbar_sub_discussion);
        } else if (type.equals("system")) {
            getSupportActionBar().setTitle(R.string.de_actionbar_sub_system);
        } else {
            getSupportActionBar().setTitle(R.string.de_actionbar_sub_defult);
        }
    }

}
