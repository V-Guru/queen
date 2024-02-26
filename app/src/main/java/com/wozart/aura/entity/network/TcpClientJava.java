package com.wozart.aura.entity.network;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

public class TcpClientJava {

    public static final int SERVER_PORT = 2345;
    private String mServerMessage;
    private OnMessageReceived mMessageListener = null;
    private boolean mRun = false;
    private PrintWriter mBufferOut;
    private BufferedReader mBufferIn;
    public static long COUNTER = 0;


    public TcpClientJava(OnMessageReceived listener) {
        mMessageListener = listener;
    }

    /**
     * Sends the message entered by client to the server
     */
    public void sendMessage(String message) {
        if (mBufferOut != null && !mBufferOut.checkError()) {
            mBufferOut.println(message);
            mBufferOut.flush();
        }
    }

    /**
     * Close the connection and release the members
     */
    public void stopClient() {
        mRun = false;
        if (mBufferOut != null) {
            mBufferOut.flush();
            mBufferOut.close();
        }
        mMessageListener = null;
        mBufferIn = null;
        mBufferOut = null;
        mServerMessage = null;
    }

    public void run(final String message, final String ip, final String name) {
        mRun = true;
        try {
            InetAddress serverAddr = InetAddress.getByName(ip);
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(serverAddr, SERVER_PORT), 2000);
            try {
                mBufferOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                mBufferIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                sendMessage(message);
                int count = 0;
                boolean messageFlag = false;
                while (mRun && count < 1000) {
                    mServerMessage = mBufferIn.readLine();
                    if (mServerMessage != null && mMessageListener != null) {
                        mMessageListener.messageReceived(mServerMessage);
                        break;
                    }
                    count++;
                }
            } catch (Exception e) {
                socket.close();
                stopClient();
            } finally {
                socket.close();
                stopClient();
            }

        } catch (Exception e) {
            mMessageListener.messageReceived("ERROR:" + name);
            stopClient();
        }

    }

    public interface OnMessageReceived {
        void messageReceived(String message);
    }
}
