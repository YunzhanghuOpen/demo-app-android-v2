package io.rong.app.test;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import io.rong.app.R;
import io.rong.imkit.fragment.ConversationListFragment;
import io.rong.imlib.model.Conversation;

/**
 * Created by Bob_ge on 15/7/21.
 */
public class TestConversationListActivity  extends FragmentActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.atest_conlist);

        ConversationListFragment fragment =  (ConversationListFragment)getSupportFragmentManager().findFragmentById(R.id.conversationlist);

        Uri uri = Uri.parse("rong://" + getApplicationInfo().packageName).buildUpon()
                .appendPath("conversationlist")
                .appendQueryParameter(Conversation.ConversationType.PRIVATE.getName(), "false") //设置私聊会话是否聚合显示
                .appendQueryParameter(Conversation.ConversationType.GROUP.getName(), "true")
                .appendQueryParameter(Conversation.ConversationType.DISCUSSION.getName(), "false")
                .appendQueryParameter(Conversation.ConversationType.SYSTEM.getName(), "true")
                .build();

        fragment.setUri(uri);


    }
}
