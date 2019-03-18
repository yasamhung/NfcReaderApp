package com.example.nfcreaderapp.conn;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Message;

import com.example.nfcreaderapp.MainActivity;


import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static java.net.Proxy.Type.HTTP;

public class UbicastHttpConnPost extends Service {
    String strTxt=null;
    String postUrl=null;
    String strResult=null;
    String strMethod = "POST";

    public void Post(String StrTxt, final String PostUrl){
        this.strTxt = StrTxt;
        this.postUrl = PostUrl;

        new Thread(new Runnable() {

            @Override
            public void run() {
                //建立一個ArrayList且需是NameValuePair，此ArrayList是用來傳送給Http server端的訊息
                //List params = new ArrayList();
                //params.add(new BasicNameValuePair("data", strTxt));

                try{
                    // HttpURLConnection
                    String trueQuestion = "";
                    String query = "{\"question\":\""+ trueQuestion + "\"} ";
                    URL endpoint = new URL(postUrl);
                    HttpURLConnection httpConnection = (HttpURLConnection) endpoint.openConnection();
                    httpConnection.setRequestMethod(strMethod);
                    httpConnection.setDoInput(true);
                    httpConnection.setDoOutput(true);
                    httpConnection.setRequestProperty("Content-Type", "application/json");

                    DataOutputStream outputStream = new DataOutputStream(httpConnection.getOutputStream());
                    outputStream.write(query.toString().getBytes("UTF-8"));
                    outputStream.flush();
                    outputStream.close();

                    InputStreamReader isr = new InputStreamReader(httpConnection.getInputStream());
                    BufferedReader br = new BufferedReader(isr);
                    String line = "";
                    while( (line = br.readLine()) != null ) {
                        System.out.println(line);
                    }
                }catch(Exception e){
                    e.printStackTrace();
                }
            }}).start();

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
