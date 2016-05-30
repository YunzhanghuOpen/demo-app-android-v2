package io.rong.app.model;

import java.io.Serializable;

/**
 * Created by yunyu on 16/5/16.
 */
public class UserInfo implements Serializable{

   // "id": "21787",
    //        "username": "吕朋",
     //       "portrait": "http://www.gravatar.com/avatar/8fc6abd6f700c65b369e40bb4db89b5a?s=82&d=wavatar"
    private String id;
    private String username;
    private String portrait;

    @Override
    public String toString() {
        return "UserInfo{" +
                "id='" + id + '\'' +
                ", username='" + username + '\'' +
                ", portrait='" + portrait + '\'' +
                '}';
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPortrait() {
        return portrait;
    }

    public void setPortrait(String portrait) {
        this.portrait = portrait;
    }
}
