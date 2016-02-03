package io.rong.app.database;

import android.content.Context;

public class DBManager {

    private static DBManager instance;
    private DaoMasters daoMasters;
    private DaoSession daoSession;

    public static DBManager getInstance(Context context) {
        if (instance == null) {
            synchronized (DBManager.class) {
                if (instance == null) {
                    instance = new DBManager(context);
                }
            }
        }
        return instance;
    }

    private DBManager(Context context) {
        if (daoSession == null) {
            if (daoMasters == null) {
                DaoMasters.OpenHelper helper = new DaoMasters.DevOpenHelper(context, context.getPackageName(), null);
                daoMasters = new DaoMasters(helper.getWritableDatabase());
            }
            daoSession = daoMasters.newSession();
        }
    }

    public DaoMasters getDaoMasters() {
        return daoMasters;
    }

    public void setDaoMasters(DaoMasters daoMasters) {
        this.daoMasters = daoMasters;
    }

    public DaoSession getDaoSession() {
        return daoSession;
    }

    public void setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
    }
}
