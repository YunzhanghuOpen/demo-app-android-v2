package io.rong.app.ui.activity;

import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;

import io.rong.app.R;
import io.rong.app.server.network.http.HttpException;
import io.rong.app.server.widget.LoadDialog;

/**
 * Created by Bob on 2015/4/9.
 */
public class BlackListActivity extends BaseActionBarActivity {

    private static final int GETBLACKLIST = 66;
    private String TAG = BlackListActivity.class.getSimpleName();

    private TextView isShowData;

    private ListView blackList;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.de_fr_black);
        getSupportActionBar().setTitle(R.string.the_blacklist);
        initView();
        requestData();
    }

    private void requestData() {
        LoadDialog.show(mContext);
        request(GETBLACKLIST);
    }

    private void initView() {
        isShowData = (TextView) findViewById(R.id.blacklsit_show_data);
        blackList = (ListView) findViewById(R.id.blacklsit_list);
    }

    @Override
    public Object doInBackground(int requestCode) throws HttpException {
        return action.getBlackList();
    }

    @Override
    public void onSuccess(int requestCode, Object result) {
        super.onSuccess(requestCode, result);
    }
}
