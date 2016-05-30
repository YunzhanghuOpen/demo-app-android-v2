package com.easemob.redpacketui.message;

import android.os.Parcel;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import io.rong.common.ParcelUtils;
import io.rong.imlib.MessageTag;
import io.rong.imlib.model.MessageContent;
import io.rong.imlib.model.UserInfo;

/**
 * 自定义红包消息类
 *
 * @author desert
 * @date 2016-05-19
 *
 * MessageTag 中 flag 中参数的含义：
 * 1.NONE，空值，不表示任何意义.在会话列表不会显示出来。
 * 2.ISPERSISTED，消息需要被存储到消息历史记录。
 * 3.ISCOUNTED，消息需要被记入未读消息数。
 *
 * value：消息对象名称。
 * 请不要以 "RC:" 开头， "RC:" 为官方保留前缀。
 */

@MessageTag(value = "YZH:RedPacketMsg", flag = MessageTag.ISPERSISTED|MessageTag.ISCOUNTED)
public class RongRedPacketMessage extends MessageContent {
    private String sendUserId;//红包发送者ID
    private String sendUserName;//红包发送者名字
    private String message;//红包祝福语
    private String moneyID;//红包ID
    public RongRedPacketMessage() {

    }

    public static RongRedPacketMessage obtain(String sendUserId, String sendUserName,String message,String moneyID) {
        RongRedPacketMessage rongRedPacketMessage = new RongRedPacketMessage();
        rongRedPacketMessage.sendUserId = sendUserId;
        rongRedPacketMessage.sendUserName = sendUserName;
        rongRedPacketMessage.message = message;
        rongRedPacketMessage.moneyID = moneyID;
        return rongRedPacketMessage;
    }

    // 给消息赋值。
    public RongRedPacketMessage(byte[] data) {

        try {
            String jsonStr = new String(data, "UTF-8");
            JSONObject jsonObj = new JSONObject(jsonStr);
            setSendUserId(jsonObj.getString("sendUserId"));
            setSendUserName(jsonObj.getString("sendUserName"));
            setMessage(jsonObj.getString("message"));
            setMoneyID(jsonObj.getString("moneyID"));
            if (jsonObj.has("user")) {
                setUserInfo(parseJsonToUserInfo(jsonObj.getJSONObject("user")));
            }
        } catch (JSONException e) {
            Log.e("JSONException", e.getMessage());
        } catch (UnsupportedEncodingException e1) {

        }
    }

    /**
     * 构造函数。
     *
     * @param in 初始化传入的 Parcel。
     */
    public RongRedPacketMessage(Parcel in) {
        setSendUserId(ParcelUtils.readFromParcel(in));
        setSendUserName(ParcelUtils.readFromParcel(in));
        setMessage(ParcelUtils.readFromParcel(in));
        setMoneyID(ParcelUtils.readFromParcel(in));
        setUserInfo(ParcelUtils.readFromParcel(in, UserInfo.class));
    }

    /**
     * 读取接口，目的是要从Parcel中构造一个实现了Parcelable的类的实例处理。
     */
    public static final Creator<RongRedPacketMessage> CREATOR = new Creator<RongRedPacketMessage>() {

        @Override
        public RongRedPacketMessage createFromParcel(Parcel source) {
            return new RongRedPacketMessage(source);
        }

        @Override
        public RongRedPacketMessage[] newArray(int size) {
            return new RongRedPacketMessage[size];
        }
    };

    /**
     * 描述了包含在 Parcelable 对象排列信息中的特殊对象的类型。
     *
     * @return 一个标志位，表明Parcelable对象特殊对象类型集合的排列。
     */
    @Override
    public int describeContents() {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     * 将类的数据写入外部提供的 Parcel 中。
     *
     * @param dest  对象被写入的 Parcel。
     * @param flags 对象如何被写入的附加标志。
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        // 这里可继续增加你消息的属性
        ParcelUtils.writeToParcel(dest, sendUserId);
        ParcelUtils.writeToParcel(dest, sendUserName);
        ParcelUtils.writeToParcel(dest, message);
        ParcelUtils.writeToParcel(dest, moneyID);
        ParcelUtils.writeToParcel(dest, getUserInfo());

    }

    /**
     * 将消息属性封装成 json 串，再将 json 串转成 byte 数组，该方法会在发消息时调用
     */
    @Override
    public byte[] encode() {
        JSONObject jsonObj = new JSONObject();
        try {

            jsonObj.put("sendUserId", sendUserId);
            jsonObj.put("sendUserName", sendUserName);
            jsonObj.put("message", message);
            jsonObj.put("moneyID", moneyID);

            if (getJSONUserInfo() != null)
                jsonObj.putOpt("user", getJSONUserInfo());

        } catch (JSONException e) {
            Log.e("JSONException", e.getMessage());
        }

        try {
            return jsonObj.toString().getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    public String getSendUserId() {
        return sendUserId;
    }

    public void setSendUserId(String sendUserId) {
        this.sendUserId = sendUserId;
    }

    public String getSendUserName() {
        return sendUserName;
    }

    public void setSendUserName(String sendUserName) {
        this.sendUserName = sendUserName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMoneyID() {
        return moneyID;
    }

    public void setMoneyID(String moneyID) {
        this.moneyID = moneyID;
    }
}
