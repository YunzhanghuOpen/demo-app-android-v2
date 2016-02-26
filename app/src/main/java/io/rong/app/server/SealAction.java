package io.rong.app.server;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import org.apache.http.entity.StringEntity;

import java.io.UnsupportedEncodingException;
import java.util.List;

import io.rong.app.server.network.http.HttpException;
import io.rong.app.server.request.AddGroupMemberRequest;
import io.rong.app.server.request.AgreeFriendsRequest;
import io.rong.app.server.request.ChangePasswordRequest;
import io.rong.app.server.request.CheckPhoneRequest;
import io.rong.app.server.request.CreateGroupRequest;
import io.rong.app.server.request.DeleteFriendRequest;
import io.rong.app.server.request.DeleteGroupMemberRequest;
import io.rong.app.server.request.DismissGroupRequest;
import io.rong.app.server.request.FriendInvitationRequest;
import io.rong.app.server.request.LoginRequest;
import io.rong.app.server.request.QuitGroupRequest;
import io.rong.app.server.request.RegisterRequest;
import io.rong.app.server.request.RestPasswordRequest;
import io.rong.app.server.request.SendCodeRequest;
import io.rong.app.server.request.SetFriendDisplayNameRequest;
import io.rong.app.server.request.SetGroupDisplayNameRequest;
import io.rong.app.server.request.SetGroupNameRequest;
import io.rong.app.server.request.SetGroupPortraitRequest;
import io.rong.app.server.request.SetNameRequest;
import io.rong.app.server.request.SetPortraitRequest;
import io.rong.app.server.request.VerifyCodeRequest;
import io.rong.app.server.response.AddGroupMemberResponse;
import io.rong.app.server.response.AgreeFriendsResponse;
import io.rong.app.server.response.ChangePasswordResponse;
import io.rong.app.server.response.CheckPhoneResponse;
import io.rong.app.server.response.CreateGroupResponse;
import io.rong.app.server.response.DeleteFriendResponse;
import io.rong.app.server.response.DeleteGroupMemberResponse;
import io.rong.app.server.response.DismissGroupResponse;
import io.rong.app.server.response.FriendInvitationResponse;
import io.rong.app.server.response.GetGroupInfoResponse;
import io.rong.app.server.response.GetGroupMemberResponse;
import io.rong.app.server.response.GetGroupResponse;
import io.rong.app.server.response.GetTokenResponse;
import io.rong.app.server.response.GetUserInfoByIdResponse;
import io.rong.app.server.response.GetUserInfoByPhoneResponse;
import io.rong.app.server.response.LoginResponse;
import io.rong.app.server.response.QuitGroupResponse;
import io.rong.app.server.response.RegisterResponse;
import io.rong.app.server.response.RestPasswordResponse;
import io.rong.app.server.response.SendCodeResponse;
import io.rong.app.server.response.SetFriendDisplayNameResponse;
import io.rong.app.server.response.SetGroupDisplayNameResponse;
import io.rong.app.server.response.SetGroupPortraitResponse;
import io.rong.app.server.response.SetNameResponse;
import io.rong.app.server.response.SetPortraitResponse;
import io.rong.app.server.response.UserRelationshipResponse;
import io.rong.app.server.response.VerifyCodeResponse;
import io.rong.app.server.response.SetGroupNameResponse;
import io.rong.app.server.utils.NLog;
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


    /**
     * 检查手机是否被注册
     *
     * @param region
     * @param phone
     * @return
     * @throws HttpException
     */
    public CheckPhoneResponse checkPhoneAvailable(String region, String phone) throws HttpException {
        String url = getURL("user/check_phone_available");
        String json = JsonMananger.beanToJson(new CheckPhoneRequest(phone, region));
        StringEntity entity = null;
        try {
            entity = new StringEntity(json, ENCODING);
            entity.setContentType(CONTENTTYPE);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        CheckPhoneResponse response = null;
        String result = httpManager.post(mContext, url, entity, CONTENTTYPE);
        if (!TextUtils.isEmpty(result)) {
            response = jsonToBean(result, CheckPhoneResponse.class);
        }
        return response;
    }


    /**
     * 发送验证码
     *
     * @param region
     * @param phone
     * @return
     * @throws HttpException
     */
    public SendCodeResponse sendCode(String region, String phone) throws HttpException {
        String url = getURL("user/send_code");
        String json = JsonMananger.beanToJson(new SendCodeRequest(region, phone));
        StringEntity entity = null;
        try {
            entity = new StringEntity(json, ENCODING);
            entity.setContentType(CONTENTTYPE);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        SendCodeResponse response = null;
        String result = httpManager.post(mContext, url, entity, CONTENTTYPE);
        if (!TextUtils.isEmpty(result)) {
            response = JsonMananger.jsonToBean(result, SendCodeResponse.class);
        }
        return response;
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

    /**
     * 注册
     *
     * @param nickname
     * @param password
     * @param
     * @return
     * @throws HttpException
     */
    public RegisterResponse register(String nickname, String password, String verification_token) throws HttpException {
        String url = getURL("user/register");
        StringEntity entity = null;
        try {
            entity = new StringEntity(JsonMananger.beanToJson(new RegisterRequest(nickname, password, verification_token)), ENCODING);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        RegisterResponse response = null;
        String result = httpManager.post(mContext, url, entity, CONTENTTYPE);
        if (!TextUtils.isEmpty(result)) {
            NLog.e("RegisterResponse", result);
            response = jsonToBean(result, RegisterResponse.class);
        }
        return response;
    }

    /**
     * 登录: 登录成功后，会设置 Cookie，后续接口调用需要登录的权限都依赖于 Cookie。
     *
     * @param region
     * @param phone
     * @param password
     * @return
     * @throws HttpException
     */
    public LoginResponse login(String region, String phone, String password) throws HttpException {
        String uri = getURL("user/login");
        String json = JsonMananger.beanToJson(new LoginRequest(region, phone, password));
        StringEntity entity = null;
        try {
            entity = new StringEntity(json, ENCODING);
            entity.setContentType(CONTENTTYPE);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String result = httpManager.post(mContext, uri, entity, CONTENTTYPE);
        LoginResponse response = null;
        if (!TextUtils.isEmpty(result)) {
            NLog.e("LoginResponse", result);
            response = JsonMananger.jsonToBean(result, LoginResponse.class);
        }
        return response;
    }


    /**
     * 获取 token 前置条件需要登录   502 坏的网关 测试环境用户已达上限
     *
     * @return
     * @throws HttpException
     */
    public GetTokenResponse getToken() throws HttpException {
        String url = getURL("user/get_token");
        String result = httpManager.get(url);
        GetTokenResponse response = null;
        if (!TextUtils.isEmpty(result)) {
            NLog.e("GetTokenResponse", result);
            response = jsonToBean(result, GetTokenResponse.class);
        }
        return response;
    }

    /**
     * 设置自己的昵称
     *
     * @param nickname
     * @return
     * @throws HttpException
     */
    public SetNameResponse setName(String nickname) throws HttpException {
        String url = getURL("user/set_nickname");
        String json = JsonMananger.beanToJson(new SetNameRequest(nickname));
        StringEntity entity = null;
        try {
            entity = new StringEntity(json, ENCODING);
            entity.setContentType(CONTENTTYPE);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        SetNameResponse response = null;
        String result = httpManager.post(mContext, url, entity, CONTENTTYPE);
        if (!TextUtils.isEmpty(result)) {
            response = jsonToBean(result, SetNameResponse.class);
        }
        return response;
    }

    /**
     * 设置用户头像
     *
     * @param portraitUri
     * @return
     * @throws HttpException
     */
    public SetPortraitResponse setPortrait(String portraitUri) throws HttpException {
        String url = getURL("user/set_portrait_uri");
        String json = JsonMananger.beanToJson(new SetPortraitRequest(portraitUri));
        StringEntity entity = null;
        try {
            entity = new StringEntity(json, ENCODING);
            entity.setContentType(CONTENTTYPE);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        SetPortraitResponse response = null;
        String result = httpManager.post(mContext, url, entity, CONTENTTYPE);
        if (!TextUtils.isEmpty(result)) {
            response = jsonToBean(result, SetPortraitResponse.class);
        }
        return response;
    }


    /**
     * 当前登录用户通过旧密码设置新密码  前置条件需要登录才能访问
     *
     * @param oldPassword
     * @param newPassword
     * @return
     * @throws HttpException
     */
    public ChangePasswordResponse changePassword(String oldPassword, String newPassword) throws HttpException {
        String url = getURL("user/change_password");
        String json = JsonMananger.beanToJson(new ChangePasswordRequest(oldPassword, newPassword));
        StringEntity entity = null;
        try {
            entity = new StringEntity(json, ENCODING);
            entity.setContentType(CONTENTTYPE);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String result = httpManager.post(mContext, url, entity, CONTENTTYPE);
        ChangePasswordResponse response = null;
        if (!TextUtils.isEmpty(result)) {
            NLog.e("ChangePasswordResponse", result);
            response = jsonToBean(result, ChangePasswordResponse.class);
        }
        return response;
    }


    /**
     * 通过手机验证码重置密码
     *
     * @param password         密码，6 到 20 个字节，不能包含空格
     * @param verification_token 调用 /user/verify_code 成功后返回的 activation_token
     * @return
     * @throws HttpException
     */
    public RestPasswordResponse restPassword(String password, String verification_token) throws HttpException {
        String uri = getURL("user/reset_password");
        String json = JsonMananger.beanToJson(new RestPasswordRequest(password, verification_token));
        StringEntity entity = null;
        try {
            entity = new StringEntity(json, ENCODING);
            entity.setContentType(CONTENTTYPE);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String result = httpManager.post(mContext, uri, entity, CONTENTTYPE);
        RestPasswordResponse response = null;
        if (!TextUtils.isEmpty(result)) {
            NLog.e("RestPasswordResponse", result);
            response = jsonToBean(result, RestPasswordResponse.class);
        }
        return response;
    }

    /**
     * 根据 id 去服务端查询用户信息 (未测试)
     *
     * @param userid
     * @return
     * @throws HttpException
     */
    public GetUserInfoByIdResponse getUserInfoById(String userid) throws HttpException {
        String url = getURL("user/" + userid);
        String result = httpManager.get(url);
        GetUserInfoByIdResponse response = null;
        if (!TextUtils.isEmpty(result)) {
            response = jsonToBean(result, GetUserInfoByIdResponse.class);
        }
        return response;
    }


    /**
     * 通过国家码和手机号查询用户信息
     *
     * @param region
     * @param phone
     * @return
     * @throws HttpException
     */
    public GetUserInfoByPhoneResponse getUserInfoFromPhone(String region, String phone) throws HttpException {
        String url = getURL("user/find/" + region + "/" + phone);
        String result = httpManager.get(url);
        GetUserInfoByPhoneResponse response = null;
        if (!TextUtils.isEmpty(result)) {
            response = jsonToBean(result, GetUserInfoByPhoneResponse.class);
        }
        return response;
    }


    /**
     * 发送好友邀请
     *
     * @param userid           好友id
     * @param addFriendMessage 添加好友的信息
     * @return
     * @throws HttpException
     */
    public FriendInvitationResponse sendFriendInvitation(String userid, String addFriendMessage) throws HttpException {
        String url = getURL("friendship/invite");
        String json = JsonMananger.beanToJson(new FriendInvitationRequest(userid, addFriendMessage));
        StringEntity entity = null;
        try {
            entity = new StringEntity(json, ENCODING);
            entity.setContentType(CONTENTTYPE);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String result = httpManager.post(mContext, url, entity, CONTENTTYPE);
        FriendInvitationResponse response = null;
        if (!TextUtils.isEmpty(result)) {
            response = jsonToBean(result, FriendInvitationResponse.class);
        }
        return response;
    }


    /**
     * 获取发生过用户关系的列表
     *
     * @return
     * @throws HttpException
     */
    public UserRelationshipResponse getAllUserRelationship() throws HttpException {
        String url = getURL("friendship/all");
        String result = httpManager.get(url);
        UserRelationshipResponse response = null;
        if (!TextUtils.isEmpty(result)) {
            response = jsonToBean(result, UserRelationshipResponse.class);
        }
        return response;
    }


    /**
     * 同意对方好友邀请
     *
     * @param friendId
     * @return
     * @throws HttpException
     */
    public AgreeFriendsResponse agreeFriends(String friendId) throws HttpException {
        String url = getURL("friendship/agree");
        String json = JsonMananger.beanToJson(new AgreeFriendsRequest(friendId));
        StringEntity entity = null;
        try {
            entity = new StringEntity(json, ENCODING);
            entity.setContentType(CONTENTTYPE);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String result = httpManager.post(mContext, url, entity, CONTENTTYPE);
        AgreeFriendsResponse response = null;
        if (!TextUtils.isEmpty(result)) {
            response = jsonToBean(result, AgreeFriendsResponse.class);
        }
        return response;
    }

    /**
     * 创建群组
     *
     * @param name      群组名
     * @param memberIds 群组成员id
     * @return
     * @throws HttpException
     */
    public CreateGroupResponse createGroup(String name, List<String> memberIds) throws HttpException {
        String url = getURL("group/create");
        String json = JsonMananger.beanToJson(new CreateGroupRequest(name, memberIds));
        StringEntity entity = null;
        try {
            entity = new StringEntity(json, ENCODING);
            entity.setContentType(CONTENTTYPE);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String result = httpManager.post(mContext, url, entity, CONTENTTYPE);
        CreateGroupResponse response = null;
        if (!TextUtils.isEmpty(result)) {
            response = jsonToBean(result, CreateGroupResponse.class);
        }
        return response;
    }

    /**
     * 创建者设置群组头像
     *
     * @param groupId
     * @param portraitUri
     * @return
     * @throws HttpException
     */
    public SetGroupPortraitResponse setGroupPortrait(String groupId, String portraitUri) throws HttpException {
        String url = getURL("group/set_portrait_uri");
        String json = JsonMananger.beanToJson(new SetGroupPortraitRequest(groupId, portraitUri));
        StringEntity entity = null;
        try {
            entity = new StringEntity(json, ENCODING);
            entity.setContentType(CONTENTTYPE);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String result = httpManager.post(mContext, url, entity, CONTENTTYPE);
        SetGroupPortraitResponse response = null;
        if (!TextUtils.isEmpty(result)) {
            response = jsonToBean(result, SetGroupPortraitResponse.class);
        }
        return response;
    }

    /**
     * 获取当前用户所属群组列表
     *
     * @return
     * @throws HttpException
     */
    public GetGroupResponse getGroups() throws HttpException {
        String url = getURL("user/groups");
        String result = httpManager.get(mContext, url);
        GetGroupResponse response = null;
        if (!TextUtils.isEmpty(result)) {
            response = jsonToBean(result, GetGroupResponse.class);
        }
        return response;
    }

    /**
     * 根据 群组id 查询该群组信息   403 群组成员才能看
     *
     * @param groupId
     * @return
     * @throws HttpException
     */
    public GetGroupInfoResponse getGroupInfo(String groupId) throws HttpException {
        String url = getURL("group/" + groupId);
        String result = httpManager.get(mContext, url);
        GetGroupInfoResponse response = null;
        if (!TextUtils.isEmpty(result)) {
            response = jsonToBean(result, GetGroupInfoResponse.class);
        }
        return response;
    }

    /**
     * 根据群id获取群组成员
     *
     * @param qunId
     * @return
     * @throws HttpException
     */
    public GetGroupMemberResponse getGroupMember(String qunId) throws HttpException {
        String url = getURL("group/" + qunId + "/members");
        String result = httpManager.get(mContext, url);
        GetGroupMemberResponse response = null;
        if (!TextUtils.isEmpty(result)) {
            response = jsonToBean(result, GetGroupMemberResponse.class);
        }
        return response;
    }

    /**
     * 当前用户添加群组成员
     *
     * @param groupId
     * @param memberIds
     * @return
     * @throws HttpException
     */
    public AddGroupMemberResponse addGroupMember(String groupId, List<String> memberIds) throws HttpException {
        String url = getURL("group/add");
        String json = JsonMananger.beanToJson(new AddGroupMemberRequest(groupId, memberIds));
        StringEntity entity = null;
        try {
            entity = new StringEntity(json, ENCODING);
            entity.setContentType(CONTENTTYPE);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String result = httpManager.post(mContext, url, entity, CONTENTTYPE);
        AddGroupMemberResponse response = null;
        if (!TextUtils.isEmpty(result)) {
            response = jsonToBean(result, AddGroupMemberResponse.class);
        }
        return response;
    }

    /**
     * 创建者将群组成员提出群组
     *
     * @param groupId
     * @param memberIds
     * @return
     * @throws HttpException
     */
    public DeleteGroupMemberResponse deleGroupMember(String groupId, List<String> memberIds) throws HttpException {
        String url = getURL("group/kick");
        String json = JsonMananger.beanToJson(new DeleteGroupMemberRequest(groupId, memberIds));
        StringEntity entity = null;
        try {
            entity = new StringEntity(json, ENCODING);
            entity.setContentType(CONTENTTYPE);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String result = httpManager.post(mContext, url, entity, CONTENTTYPE);
        DeleteGroupMemberResponse response = null;
        if (!TextUtils.isEmpty(result)) {
            response = jsonToBean(result, DeleteGroupMemberResponse.class);
        }
        return response;
    }

    /**
     * 创建者更改群组昵称
     *
     * @param groupId
     * @param name
     * @return
     * @throws HttpException
     */
    public SetGroupNameResponse setGroupName(String groupId, String name) throws HttpException {
        String url = getURL("group/rename");
        String json = JsonMananger.beanToJson(new SetGroupNameRequest(groupId, name));
        StringEntity entity = null;
        try {
            entity = new StringEntity(json, ENCODING);
            entity.setContentType(CONTENTTYPE);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String result = httpManager.post(mContext, url, entity, CONTENTTYPE);
        SetGroupNameResponse response = null;
        if (!TextUtils.isEmpty(result)) {
            response = jsonToBean(result, SetGroupNameResponse.class);
        }
        return response;
    }

    /**
     * 用户自行退出群组
     *
     * @param groupId
     * @return
     * @throws HttpException
     */
    public QuitGroupResponse quitGroup(String groupId) throws HttpException {
        String url = getURL("group/quit");
        String json = JsonMananger.beanToJson(new QuitGroupRequest(groupId));
        StringEntity entity = null;
        try {
            entity = new StringEntity(json, ENCODING);
            entity.setContentType(CONTENTTYPE);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String result = httpManager.post(mContext, url, entity, CONTENTTYPE);
        QuitGroupResponse response = null;
        if (!TextUtils.isEmpty(result)) {
            response = jsonToBean(result, QuitGroupResponse.class);
        }
        return response;
    }

    /**
     * 创建者解散群组
     *
     * @param groupId
     * @return
     * @throws HttpException
     */
    public DismissGroupResponse dissmissGroup(String groupId) throws HttpException {
        String url = getURL("group/dismiss");
        String json = JsonMananger.beanToJson(new DismissGroupRequest(groupId));
        StringEntity entity = null;
        try {
            entity = new StringEntity(json, ENCODING);
            entity.setContentType(CONTENTTYPE);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String result = httpManager.post(mContext, url, entity, CONTENTTYPE);
        DismissGroupResponse response = null;
        if (!TextUtils.isEmpty(result)) {
            response = jsonToBean(result, DismissGroupResponse.class);
        }
        return response;
    }


    /**
     * 修改自己的当前的群昵称
     *
     * @param groupId
     * @param displayName
     * @return
     * @throws HttpException
     */
    public SetGroupDisplayNameResponse setGroupDisplayName(String groupId, String displayName) throws HttpException {
        String url = getURL("group/set_display_name");
        String json = JsonMananger.beanToJson(new SetGroupDisplayNameRequest(groupId, displayName));
        StringEntity entity = null;
        try {
            entity = new StringEntity(json, ENCODING);
            entity.setContentType(CONTENTTYPE);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String result = httpManager.post(mContext, url, entity, CONTENTTYPE);
        SetGroupDisplayNameResponse response = null;
        if (!TextUtils.isEmpty(result)) {
            response = jsonToBean(result, SetGroupDisplayNameResponse.class);
        }
        return response;
    }

    /**
     * 删除好友  未测试
     * @param friendId
     * @return
     * @throws HttpException
     */
    public DeleteFriendResponse deleteFriend(String friendId) throws HttpException {
        String url = getURL("friendship/delete");
        String json = JsonMananger.beanToJson(new DeleteFriendRequest(friendId));
        StringEntity entity = null;
        try {
            entity = new StringEntity(json, ENCODING);
            entity.setContentType(CONTENTTYPE);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String result = httpManager.post(mContext, url, entity, CONTENTTYPE);
        DeleteFriendResponse response = null;
        if (!TextUtils.isEmpty(result)) {
            response = jsonToBean(result, DeleteFriendResponse.class);
        }
        return response;
    }

    /**
     * 设置好友的备注名称 未测试
     * @param friendId
     * @param displayName
     * @return
     * @throws HttpException
     */
    public SetFriendDisplayNameResponse setFriendDisplayName(String friendId, String displayName)throws HttpException{
        String url = getURL("friendship/set_display_name");
        String json = JsonMananger.beanToJson(new SetFriendDisplayNameRequest(friendId,displayName));
        StringEntity entity = null;
        try {
            entity = new StringEntity(json, ENCODING);
            entity.setContentType(CONTENTTYPE);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String result = httpManager.post(mContext, url, entity, CONTENTTYPE);
        SetFriendDisplayNameResponse response = null;
        if (!TextUtils.isEmpty(result)) {
            response = jsonToBean(result, SetFriendDisplayNameResponse.class);
        }
        return response;
    }
}
