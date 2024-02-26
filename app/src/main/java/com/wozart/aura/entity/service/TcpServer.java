package com.wozart.aura.entity.service;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

/***************************************************************************
 * File Name : 
 * Author : Aarth Tandel
 * Date of Creation : 30/04/18
 * Description : 
 * Revision History :
 * ____________________________________________________________________________
 * 29/12/17  Aarth Tandel - Initial Commit
 * ____________________________________________________________________________
 * 29/12/17 Version 1.0
 * ____________________________________________________________________________
 *
 *****************************************************************************/

public class TcpServer extends Service {
    private static String LOG_TAG = "TcpServer";

    private static String value = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890.,';:[]{}!@#$%^&*()_=+\"/- ";
    private static int[] random = {82, 11, 31, 63, 52, 13, 74, 3, 42, 46,
            10, 64, 12, 9, 17, 15, 75, 16, 66, 55,
            25, 36, 57, 61, 50, 83, 72, 20, 33, 69,
            18, 77, 1, 14, 6, 0, 4, 65, 24, 37,
            39, 70, 59, 32, 51, 2, 35, 68, 67, 84,
            27, 78, 45, 48, 41, 87, 80, 60, 73, 40,
            53, 76, 81, 43, 38, 30, 23, 44, 19, 86,
            56, 58, 62, 7, 29, 47, 8, 28, 22, 5,
            71, 49, 85, 21, 34, 79, 54, 26};

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Thread socketServerThread = new Thread(new SocketServerThread());
        socketServerThread.start();
        Log.d(LOG_TAG, "Server started");
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private class SocketServerThread extends Thread {

        public void run() {

            try {
                ServerSocket soServer = new ServerSocket(2345);
                Socket socClient = null;
                while (!Thread.currentThread().isInterrupted()) {
                    socClient = soServer.accept();
                    ServerAsyncTask serverAsyncTask = new ServerAsyncTask();
                    serverAsyncTask.execute(socClient);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private class ServerAsyncTask extends AsyncTask<Socket, Void, String> {
        @Override
        protected String doInBackground(Socket... params) {
            String result = null;
            Socket mySocket = params[0];
            try {
                InputStream is = mySocket.getInputStream();
                PrintWriter out = new PrintWriter(mySocket.getOutputStream(),
                        true);

                BufferedReader br = new BufferedReader(
                        new InputStreamReader(is));

                result = br.readLine();

            } catch (IOException e) {
                e.printStackTrace();
            }
            sendMessageToActivity(result);
            return result;
        }

        @Override
        protected void onPostExecute(String s) {
        }
    }

    private void sendMessageToActivity(String data) {

        String decryptedData = "";
        if (data == null) {

        } else {
            for (int i = 0; i < data.length(); i++) {
                int index = value.indexOf(data.charAt(i));
                for (int j = 0; j < random.length; j++) {
                    if (random[j] == index) {
                        decryptedData = decryptedData + value.charAt(j);
                    }
                }
            }
        }
        Intent intent = new Intent("intentKey");
        intent.putExtra("key", decryptedData);
        LocalBroadcastManager.getInstance(TcpServer.this).sendBroadcast(intent);
    }
}
