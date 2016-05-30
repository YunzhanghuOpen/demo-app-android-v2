package io.rong.app.model;

import java.io.Serializable;

/**
 * Created by yunyu on 16/5/16.
 */
public class GroupInfo implements Serializable{
    private int code;
    private GroupResult result;

    @Override
    public String toString() {
        return "GroupInfo{" +
                "code=" + code +
                ", result=" + result +
                '}';
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public GroupResult getResult() {
        return result;
    }

    public void setResult(GroupResult result) {
        this.result = result;
    }
}
