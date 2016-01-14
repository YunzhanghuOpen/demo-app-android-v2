package io.rong.app.server;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import org.apache.http.entity.StringEntity;

import java.io.UnsupportedEncodingException;

import io.rong.app.server.network.http.HttpException;
import io.rong.app.server.request.VerifyCodeRequest;
import io.rong.app.server.response.VerifyCodeResponse;
import io.rong.app.server.utils.json.JsonMananger;

/**
 * Created by AMing on 16/1/14.
 * Company RongCloud
 */
public class SealAction extends BaseAction {
    private final String CONTENTTYPE = "application/json";
    private final String ENCODING = "utf-8";
    /**
     * 构造方法
     *
     * @param context
     */
    public SealAction(Context context) {
        super(context);
    }

        /*
    * 200: 验证成功
    1000: 验证码错误
    2000: 验证码过期
    异常返回，返回的 HTTP Status Code 如下：

    400: 错误的请求
    500: 应用服务器内部错误
    * */

    /**
     * 验证验证码是否正确(必选先用手机号码调sendcode)
     *
     * @param region
     * @param phone
     * @return
     * @throws HttpException
     */
    public VerifyCodeResponse verifyCode(String region, String phone, String code) throws HttpException {
        String url = getURL("user/verify_code");
        String josn = JsonMananger.beanToJson(new VerifyCodeRequest(region, phone, code));
        VerifyCodeResponse response = null;
        StringEntity entity = null;
        try {
            entity = new StringEntity(josn, ENCODING);
            entity.setContentType(CONTENTTYPE);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String result = httpManager.post(mContext, url, entity, CONTENTTYPE);
        if (!TextUtils.isEmpty(result)) {
            Log.e("VerifyCodeResponse", result);
            response = jsonToBean(result, VerifyCodeResponse.class);
        }
        return response;
    }
}
