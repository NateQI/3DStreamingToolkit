package microsoft.a3dtoolkitandroid;

import android.content.Intent;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.android.volley.Request;

import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.webrtc.MediaConstraints;
import org.webrtc.PeerConnection;

import java.util.HashMap;

import microsoft.a3dtoolkitandroid.util.CustomStringRequest;
import microsoft.a3dtoolkitandroid.util.HttpRequestQueue;

import static microsoft.a3dtoolkitandroid.ConnectActivity.DTLS;
import static microsoft.a3dtoolkitandroid.ConnectActivity.OfferToReceiveAudio;
import static microsoft.a3dtoolkitandroid.ConnectActivity.OfferToReceiveVideo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static util.SharedTestStrings.mockSdpAnswer;
import static util.SharedTestStrings.mockSdpJSON;
import static util.SharedTestStrings.mockServer;
import static util.SharedTestStrings.mockServerListString;

@RunWith(AndroidJUnit4.class)
public class ConnectActivityTest extends ActivityTestRule<ConnectActivity> {
    private ConnectActivity activity;
    public ConnectActivityTest() {
        super(ConnectActivity.class);
    }

    @Before
    public void setUp() {
        HttpRequestQueue mockRequestQueue = mock(HttpRequestQueue.class);
        doNothing().when(mockRequestQueue).addToQueue(any(Request.class), anyString());
        HttpRequestQueue.setInstance(mockRequestQueue);
        Intent intent = new Intent();
        intent.putExtra(SignInActivity.SERVER_LIST, mockServerListString);
        intent.putExtra(SignInActivity.SERVER_SERVER, mockServer);
        launchActivity(intent);
        activity = getActivity();
        activity.peer_id = 99;
        activity.iceServer = new PeerConnection.IceServer("stun1.l.google.com:19302");
        activity.joinPeer();
    }

    @After
    public void tearDown(){
        activity.disconnect();
        activity = null;
    }

    @Test
    public void testStartUpActivity() throws Exception{
        assertNotNull(activity.peerConnectionObserver);
        assertTrue(activity.activityRunning);
        assertEquals(mockServer, activity.server);
        assertEquals(mockServerListString, activity.stringList);
    }

    @Test
    public void testJoinPeer() throws Exception{
        assertTrue(activity.isInitiator);
        assertNotNull(activity.inputChannel);
        assertEquals(mockServer, activity.server);
    }

    @Test
    public void testCreateMediaConstraints() throws Exception{
        assertNotNull(activity.remoteVideoRenderer);

        assertEquals(1, activity.peerConnectionConstraints.optional.size());
        assertEquals(new MediaConstraints.KeyValuePair("DtlsSrtpKeyAgreement", DTLS), activity.peerConnectionConstraints.optional.get(0));

        assertEquals(2, activity.sdpMediaConstraints.mandatory.size());
        assertEquals(new MediaConstraints.KeyValuePair("OfferToReceiveAudio", OfferToReceiveAudio), activity.sdpMediaConstraints.mandatory.get(0));
        assertEquals(new MediaConstraints.KeyValuePair("OfferToReceiveVideo", OfferToReceiveVideo), activity.sdpMediaConstraints.mandatory.get(1));

        assertNotNull(activity.remoteVideoRenderer);
    }

    @Test
    public void testHandleAnswerFromHangingGet() throws Exception{
        CustomStringRequest.ResponseM mockResult = new CustomStringRequest.ResponseM();
        mockResult.headers = new HashMap<>();
        mockResult.headers.put("Pragma", "99");
        mockResult.response = mockSdpJSON(mockSdpAnswer);

        activity.onHangingGetSuccess(mockResult);
        assertEquals(99, activity.peer_id);
        assertTrue(activity.isInitiator);
    }

    @Test
    public void testHandleOfferFromHangingGet() throws Exception{
//        CustomStringRequest.ResponseM mockResult = new CustomStringRequest.ResponseM();
//        mockResult.headers = new HashMap<>();
//        mockResult.headers.put("Pragma", "99");
//        mockResult.response = mockSdpJSON(mockSdpOffer);
//
//        activity.onHangingGetSuccess(mockResult);
//        assertEquals(99, activity.peer_id);
//        assertFalse(activity.isInitiator);
    }

    @Test
    public void testHangingGetHandleServerNotification() throws Exception{
        CustomStringRequest.ResponseM mockResult = new CustomStringRequest.ResponseM();
        mockResult.headers = new HashMap<>();
        mockResult.headers.put("Pragma", String.valueOf(activity.my_id));
        JSONObject server = new JSONObject();
        server.put("Server", "test,5,1");
        mockResult.response = server;
        int before = activity.serverListFragment.servers.size();
        activity.onHangingGetSuccess(mockResult);
        assertEquals(before +1, activity.serverListFragment.servers.size());
    }

    @Test
    public void testHangingGetFailure() throws Exception{
    }

    @Test
    public void testHeartBeat() throws Exception{
        assertTrue(activity.heartbeatRunning);
    }


    @Test
    public void testServerListFragment() throws Exception{
        assertNotNull(activity.serverListFragment);
        assertEquals(1, activity.my_id);
        assertEquals(3, activity.serverListFragment.servers.size());
    }

}