package com.quick.jsbridge.api;

import android.net.rtp.AudioGroup;
import android.webkit.WebView;

import com.quick.jsbridge.bridge.Callback;
import com.quick.jsbridge.bridge.IBridgeImpl;
import com.quick.jsbridge.view.IQuickFragment;
import com.vapp.taketosee.AgoraMessage;

import org.json.JSONObject;

public class TakeToSeeApi implements IBridgeImpl {
    /**
     * 注册API的别名
     */
    public static String RegisterName = "taketosee";

    public static void loginWithUserName(IQuickFragment webLoader, WebView wv, JSONObject param, Callback callback) {
        String appId = param.optString("appId");
        String userId = param.optString("userId");
        String token = param.optString("token");
        AgoraMessage.getInstance().initRTMMessageClient(webLoader.getPageControl().getContext(), appId);
        AgoraMessage.getInstance().loginRTMClient(token, userId);
    }

    public static void loginWithOutToken(IQuickFragment webLoader, WebView wv, JSONObject param, Callback callback) {
        String appId = param.optString("appId");
        String userId = param.optString("userId");
        AgoraMessage.getInstance().initRTMMessageClient(webLoader.getPageControl().getContext(), appId);
        AgoraMessage.getInstance().loginRTMClientWithOutToken(userId);
    }


    public static void logout(IQuickFragment webLoader, WebView wv, JSONObject param, Callback callback) {
        AgoraMessage.getInstance().logoutRtm();
    }

    public static void createChannelListener(IQuickFragment webLoader, WebView wv, JSONObject param, Callback callback) {
        AgoraMessage.getInstance().createChannerListener();
    }

    public static void joinChannel(IQuickFragment webLoader, WebView wv, JSONObject param, Callback callback){
        String channelName = param.optString("channelName");
        AgoraMessage.getInstance().joinChannel(channelName);
    }

    public static void leaveChannel(IQuickFragment webLoader, WebView wv, JSONObject param, Callback callback) {
        AgoraMessage.getInstance().leaveChannel();
    }

    public static void sendGroupMessage(IQuickFragment webLoader, WebView wv, JSONObject param, Callback callback) {
        String channelText = param.optString("channelText");
        AgoraMessage.getInstance().sendChannelMessage(channelText);
    }

    public static void sendPeerMessage(IQuickFragment webLoader, WebView wv, JSONObject param, Callback callback) {
        String userId = param.optString("userId");
        String peerMessage = param.optString("peerMessage");
        AgoraMessage.getInstance().sendPeerMessage(peerMessage, userId);
    }
}
