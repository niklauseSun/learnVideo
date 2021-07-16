package com.vapp.taketosee;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.agora.rtm.ErrorInfo;
import io.agora.rtm.ResultCallback;
import io.agora.rtm.RtmChannel;
import io.agora.rtm.RtmChannelAttribute;
import io.agora.rtm.RtmChannelListener;
import io.agora.rtm.RtmChannelMember;
import io.agora.rtm.RtmClient;
import io.agora.rtm.RtmClientListener;
import io.agora.rtm.RtmFileMessage;
import io.agora.rtm.RtmImageMessage;
import io.agora.rtm.RtmMediaOperationProgress;
import io.agora.rtm.RtmMessage;
import io.agora.rtm.SendMessageOptions;

public class AgoraMessage implements IRtcImpl {

    private final String LOG_TAG = "AgoraMessage_LOG";
    private final String LOG_MESSAGE = "AgoraMessage_message";


    private static AgoraMessage single = null;

    // RTM 客户端实例
    private RtmClient mRtmClient = null;
    // RTM 频道实例
    private RtmChannel mRtmChannel = null;

    private Context mContext = null;

    private RtmChannelListener mRtmChannelListener = null;

    private String currentUid = null;

    private HashMap channelMap = new HashMap<>();

    public static AgoraMessage getInstance() {
        if (single == null) {
            single = new AgoraMessage();  //在第一次调用getInstance()时才实例化，实现懒加载,所以叫懒汉式
        }
        return single;
    }


    public void initRTMMessageClient(Context context, String appId) {
        mContext = context;
        try {
            mRtmClient = RtmClient.createInstance(context, appId, new RtmClientListener() {
                @Override
                public void onConnectionStateChanged(int state, int reason) {
                    String text = "Connection state changed to " + state + "Reason: " + reason + "\n";
                    Log.i(LOG_TAG, text);
                }

                @Override
                public void onMessageReceived(RtmMessage rtmMessage, String peerId) {
                    String text = "Message received from " + peerId + " Message: " + rtmMessage.getText() + "\n";
                    Log.i(LOG_MESSAGE, text);
                }

                @Override
                public void onImageMessageReceivedFromPeer(RtmImageMessage rtmImageMessage, String s) {

                }

                @Override
                public void onFileMessageReceivedFromPeer(RtmFileMessage rtmFileMessage, String s) {

                }

                @Override
                public void onMediaUploadingProgress(RtmMediaOperationProgress rtmMediaOperationProgress, long l) {

                }

                @Override
                public void onMediaDownloadingProgress(RtmMediaOperationProgress rtmMediaOperationProgress, long l) {

                }

                @Override
                public void onTokenExpired() {

                }

                @Override
                public void onPeersOnlineStatusChanged(Map<String, Integer> map) {

                }
            });
        } catch (Exception e) {
            throw new RuntimeException("RTM initialization failed!");
        }
    }


    public void loginRTMClient(String token, String uid) {
        currentUid = uid;
        mRtmClient.login(token, uid, new ResultCallback<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Log.i(LOG_TAG, "登录成功");
            }

            @Override
            public void onFailure(ErrorInfo errorInfo) {
                CharSequence text = "User: " + uid + " failed to log in to the RTM system!" + errorInfo.toString();
                int duration = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(mContext, text, duration);
                toast.show();
            }
        });
    }

    public void loginRTMClientWithOutToken(String uid) {
        currentUid = uid;
        mRtmClient.login(null, uid, new ResultCallback<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Log.i(LOG_TAG, "登录成功");
                Log.i(LOG_TAG, "user:" + currentUid + "登录成功");
            }

            @Override
            public void onFailure(ErrorInfo errorInfo) {
                CharSequence text = "User: " + uid + " failed to log in to the RTM system!" + errorInfo.toString();
                int duration = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(mContext, text, duration);
                toast.show();
            }
        });
    }

    public void createChannerListener() {
        mRtmChannelListener = new RtmChannelListener() {
            @Override
            public void onMemberCountUpdated(int i) {

            }

            @Override
            public void onAttributesUpdated(List<RtmChannelAttribute> list) {

            }

            @Override
            public void onMessageReceived(RtmMessage rtmMessage, RtmChannelMember rtmChannelMember) {
                String text = rtmMessage.getText();
                String fromUser = rtmChannelMember.getUserId();

                String message_text = "Message received from " + fromUser + " : " + text + "\n";

                Log.i("CREATE MESSAGE LISTENER", message_text);
            }

            @Override
            public void onImageMessageReceived(RtmImageMessage rtmImageMessage, RtmChannelMember rtmChannelMember) {

            }

            @Override
            public void onFileMessageReceived(RtmFileMessage rtmFileMessage, RtmChannelMember rtmChannelMember) {

            }

            @Override
            public void onMemberJoined(RtmChannelMember rtmChannelMember) {

            }

            @Override
            public void onMemberLeft(RtmChannelMember rtmChannelMember) {

            }
        };
    }

    public void createRTMChannel(String channel_name) {
        try {
            mRtmChannel = mRtmClient.createChannel(channel_name, mRtmChannelListener);
        } catch (RuntimeException e) {
        }
    }

    public void joinChannel(String channel_name) {
        createRTMChannel(channel_name);
        // 加入 RTM 频道
        mRtmChannel.join(new ResultCallback<Void>() {
            @Override
            public void onSuccess(Void responseInfo) {
                Log.i(LOG_MESSAGE, "SUCCESS");
            }

            @Override
            public void onFailure(ErrorInfo errorInfo) {
                CharSequence text = "User: " + currentUid + " failed to join the channel!" + errorInfo.toString();
                int duration = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(mContext, text, duration);
                toast.show();

            }
        });
    }

    public void logoutRtm() {
        // 登出 RTM 系统
        mRtmClient.logout(null);
    }

    public void leaveChannel() {
        mRtmChannel.leave(null);
    }

    // 点对点发送消息
    public void sendPeerMessage(String message_content, String peer_id) {
        // 创建消息实例
        final RtmMessage message = mRtmClient.createMessage();
        message.setText(message_content);

        SendMessageOptions option = new SendMessageOptions();
        option.enableOfflineMessaging = true;

        mRtmClient.sendMessageToPeer(peer_id, message, option, new ResultCallback<Void>() {
            @Override
            public void onSuccess(Void unused) {
                String text = "Message sent from " + currentUid + " To " + peer_id + " ： " + message.getText() + "\n";
                Log.i(LOG_MESSAGE, text);
            }

            @Override
            public void onFailure(ErrorInfo errorInfo) {
                String text = "Message fails to send from " + currentUid + " To " + peer_id + " Error ： " + errorInfo + "\n";
                Log.i(LOG_MESSAGE, text);
            }
        });
    }

    // 发送channel短信
    public void sendChannelMessage(String channel_message) {
        // 创建消息实例
        RtmMessage message = mRtmClient.createMessage();
        message.setText(channel_message);

        mRtmChannel.sendMessage(message, new ResultCallback<Void>() {
            @Override
            public void onSuccess(Void unused) {
                String text = "Message sent to channel " + mRtmChannel.getId() + " : " + message.getText() + "\n";
                Log.i(LOG_MESSAGE, text);
            }

            @Override
            public void onFailure(ErrorInfo errorInfo) {
                String text = "Message fails to send to channel " + mRtmChannel.getId() + " Error: " + errorInfo + "\n";
                Log.i(LOG_MESSAGE, text);
            }
        });
    }
}