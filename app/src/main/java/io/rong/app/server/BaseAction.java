package io.rong.app.server;

import android.content.Context;

import java.util.List;

import io.rong.app.server.network.http.HttpException;
import io.rong.app.server.network.http.SyncHttpClient;
import io.rong.app.server.utils.json.JsonMananger;

/**
 * Created by AMing on 16/1/14.
 * Company RongCloud
 */
public class BaseAction {
    //正式环境  api.sealtalk.im
    //测试 http://api.hitalk.im/
        private static final String DOMAIN = "http://api.sealtalk.im/";
//        private static final String DOMAIN = "http://api.hitalk.im/";
    protected Context mContext;
    protected SyncHttpClient httpManager;



    /**
     * 构造方法
     * @param context
     */
    public BaseAction(Context context) {
        this.mContext = context;
        this.httpManager = SyncHttpClient.getInstance(context);
    }

    /**
     * JSON转JAVA对象方法
     * @param json
     * @param cls
     * @return
     * @throws HttpException
     */
    public <T> T jsonToBean(String json, Class<T> cls) throws HttpException {
        return JsonMananger.jsonToBean(json, cls);
    }

    /**
     * JSON转JAVA数组方法
     * @param json
     * @param
     * @return
     * @throws HttpException
     */
    public <T> List<T> jsonToList(String json, Class<T> cls) throws HttpException {
        return JsonMananger.jsonToList(json, cls);
    }

    /**
     * JAVA对象转JSON方法
     * @param obj
     * @return
     * @throws HttpException
     */
    public String BeanTojson(Object obj) throws HttpException {
        return JsonMananger.beanToJson(obj);
    }


    /**
     * 获取完整URL方法
     * @param url
     * @return
     */
    protected String getURL(String url) {
        return getURL(url, new String[]{});
    }
    /**
     * 获取完整URL方法
     * @param url
     * @param params
     * @return
     */
    protected String getURL(String url, String... params) {
        StringBuilder urlBilder = new StringBuilder(DOMAIN).append(url);
        if (params != null) {
            for (String param : params) {
                if (!urlBilder.toString().endsWith("/")) {
                    urlBilder.append("/");
                }
                urlBilder.append(param);
            }
        }
        return urlBilder.toString();
    }
}
