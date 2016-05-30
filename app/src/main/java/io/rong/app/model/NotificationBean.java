package io.rong.app.model;

import java.io.Serializable;

import io.rong.imlib.model.Conversation;

/**
 * Created by yunyu on 16/5/16.
 */
public class NotificationBean implements Serializable{
    private String sendUserID;
    private String sendUserName;
    private String receiveUserID;
    private String receiveUserName;
    private String targetID;
    private Conversation.ConversationType chatType;

    @Override
    public String toString() {
        return "NotificationBean{" +
                "sendUserID='" + sendUserID + '\'' +
                ", sendUserName='" + sendUserName + '\'' +
                ", receiveUserID='" + receiveUserID + '\'' +
                ", receiveUserName='" + receiveUserName + '\'' +
                ", targetID='" + targetID + '\'' +
                ", chatType=" + chatType +
                '}';
    }

    public String getSendUserID() {
        return sendUserID;
    }

    public void setSendUserID(String sendUserID) {
        this.sendUserID = sendUserID;
    }

    public String getReceiveUserName() {
        return receiveUserName;
    }

    public void setReceiveUserName(String receiveUserName) {
        this.receiveUserName = receiveUserName;
    }

    public String getTargetID() {
        return targetID;
    }

    public void setTargetID(String targetID) {
        this.targetID = targetID;
    }

    public Conversation.ConversationType getChatType() {
        return chatType;
    }

    public void setChatType(Conversation.ConversationType chatType) {
        this.chatType = chatType;
    }

    public String getReceiveUserID() {
        return receiveUserID;
    }

    public void setReceiveUserID(String receiveUserID) {
        this.receiveUserID = receiveUserID;
    }

    public String getSendUserName() {
        return sendUserName;
    }

    public void setSendUserName(String sendUserName) {
        this.sendUserName = sendUserName;
    }
}
