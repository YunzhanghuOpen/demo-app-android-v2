package io.rong.app.server.response;


/**
 * Created by AMing on 15/12/24.
 * Company RongCloud
 */
public class LoginResponse {


    /**
     * code : 200
     * result : {"id":"zHsiKgOx5"}
     */

    private int code;
    /**
     * id : zHsiKgOx5
     */

    private ResultEntity result;

    public void setCode(int code) {
        this.code = code;
    }

    public void setResult(ResultEntity result) {
        this.result = result;
    }

    public int getCode() {
        return code;
    }

    public ResultEntity getResult() {
        return result;
    }

    public static class ResultEntity {
        private String id;

        public void setId(String id) {
            this.id = id;
        }

        public String getId() {
            return id;
        }
    }
}
