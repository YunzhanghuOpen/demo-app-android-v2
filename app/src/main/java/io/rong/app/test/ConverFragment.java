package io.rong.app.test;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import io.rong.app.R;
import io.rong.imkit.fragment.ConversationFragment;

/**
 * Created by Bob_ge on 15/8/4.
 */
public class ConverFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.a_test1,container,false);
        ConversationFragment fragment = (ConversationFragment) getChildFragmentManager().findFragmentById(R.id.conversation);

        Uri uri = Uri.parse("rong://" + getActivity().getApplicationInfo().packageName).buildUpon()
                .appendPath("conversation").appendPath(io.rong.imlib.model.Conversation.ConversationType.PRIVATE.getName().toLowerCase())
                .appendQueryParameter("targetId", "10000").appendQueryParameter("title", "hello").build();
        fragment.setUri(uri);

        return view;
    }
}
