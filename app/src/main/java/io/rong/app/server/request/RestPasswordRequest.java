package io.rong.app.server.request;


/**
 * Created by AMing on 15/12/24.
 * Company RongCloud
 */
public class RestPasswordRequest{

    private String password;

    private String activation_token;

    public RestPasswordRequest(String password, String activation_token) {
        this.password = password;
        this.activation_token = activation_token;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getActivation_token() {
        return activation_token;
    }

    public void setActivation_token(String activation_token) {
        this.activation_token = activation_token;
    }
}
