package io.rong.app.model;

import java.io.Serializable;
import java.util.List;

/**
 * Created by yunyu on 16/5/16.
 */
public class GroupResult implements Serializable{
//    "id": "22",
//            "name": "iOS I 群",
//            "portrait": null,
//            "introduce": "iOS I 群加人了",
//            "number": "733",
//            "max_number": "1000",
//            "create_user_id": "22775",
//            "creat_datetime": "2015-04-20 21:12:17",
    private String id;
    private String name;
    private String portrait;
    private String introduce;
    private String number;
    private String max_number;
    private String create_user_id;
    private String creat_datetime;
    private List<UserInfo> users;

    @Override
    public String toString() {
        return "GroupResult{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", portrait='" + portrait + '\'' +
                ", introduce='" + introduce + '\'' +
                ", number='" + number + '\'' +
                ", max_number='" + max_number + '\'' +
                ", create_user_id='" + create_user_id + '\'' +
                ", creat_datetime='" + creat_datetime + '\'' +
                ", users=" + users +
                '}';
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIntroduce() {
        return introduce;
    }

    public void setIntroduce(String introduce) {
        this.introduce = introduce;
    }

    public String getPortrait() {
        return portrait;
    }

    public void setPortrait(String portrait) {
        this.portrait = portrait;
    }

    public String getMax_number() {
        return max_number;
    }

    public void setMax_number(String max_number) {
        this.max_number = max_number;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getCreate_user_id() {
        return create_user_id;
    }

    public void setCreate_user_id(String create_user_id) {
        this.create_user_id = create_user_id;
    }

    public String getCreat_datetime() {
        return creat_datetime;
    }

    public void setCreat_datetime(String creat_datetime) {
        this.creat_datetime = creat_datetime;
    }

    public List<UserInfo> getUsers() {
        return users;
    }

    public void setUsers(List<UserInfo> users) {
        this.users = users;
    }
}
