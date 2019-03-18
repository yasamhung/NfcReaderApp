package com.example.nfcreaderapp.conn;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Message;

import com.example.nfcreaderapp.MainActivity;
import com.example.nfcreaderapp.comm.SslUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HostnameVerifier;

public class UbicastHttpConn extends Service {
    public enum Method{
        POST("POST"),
        GET("GET");

        public final String type;

        private Method(String type){
            this.type = type;
        }

        public String value(){
            return type;
        }
    }

    public enum ContentType{
        JSON("application/json"),
        TEXTXML("text/xml"),
        OTHER("application/x-www-form-urlencoded");

        public final String type;

        private ContentType(String type){
            this.type = type;
        }

        public String value(){
            return type;
        }
    }

    private final String USER_AGENT = "Mozilla/5.0";

    private MainActivity mainActivity;
    private HttpURLConnection con;
    private String targetUrl;
    HostnameVerifier hv;
    StringBuffer sb;

    /**
     * Request Http Post/Get
     * @param method
     * @param _targetUrl
     * @param requestBody
     * @param contentType
     * @param _mainActivity
     */
    public void request(String method, String _targetUrl, String requestBody, String contentType, MainActivity _mainActivity){
        if(method.equals(Method.GET.value())){
            get(
                    method,
                    _targetUrl +
                            requestBody,
                    _mainActivity);
        }else if(method.equals(Method.POST.value())){
            post(
                    method,
                    _targetUrl,
                    requestBody,
                    contentType,
                    _mainActivity);
        }
    }

    /**
     *
     * @param method
     * @param _targetUrl
     * @param requestBody
     * @param contentType
     * @param _mainActivity
     */
    private void post(final String method, final String _targetUrl, final String requestBody, final String contentType, final MainActivity _mainActivity) {
        this.mainActivity = _mainActivity;
        this.con = null;
        this.targetUrl = _targetUrl;
        this.sb = new StringBuffer("");
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{

                    //Toast.makeText(mainActivity, "Method:" + method + " Click", Toast.LENGTH_SHORT).show();
                    URL url = new URL(targetUrl);
                    if("https".equalsIgnoreCase(url.getProtocol())) {
                        SslUtils.ignoreSsl();
                    }
                    con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod(method);
                    System.out.print(method);
                    con.setDoInput(true);
                    con.setDoOutput(true);
                    con.setRequestProperty("Content-Type", contentType);
                    con.setRequestProperty("User-Agent", "Java client");
                    //DataOutputStream outputStream = new DataOutputStream(con.getOutputStream());
                    OutputStream outputStream = con.getOutputStream();
                    outputStream.write(requestBody.getBytes("UTF-8"));
                    outputStream.flush();
                    outputStream.close();

                    //InputStreamReader isr = new InputStreamReader(con.getInputStream(), "UTF-8");
                    //BufferedReader br = new BufferedReader(isr);
                    BufferedReader br = getBody(con);
                    String line = "";
                    while( (line = br.readLine()) != null ) {
                        line = new String(line.getBytes("UTF-8"), "utf-8");
                        sb.append(line);
                    }
                    Message msg = Message.obtain();
                    //設定Message的內容
                    msg.what = method==Method.POST.value()? 1: 2;
                    msg.obj=sb.toString();
                    //使用MainActivity的static handler來丟Message
                    mainActivity.handler.sendMessage(msg);
                    br.close();
                    con.disconnect();
                }catch(Exception e){
                    e.printStackTrace();
                }finally {
                    if(con != null)
                        con.disconnect();
                }
            }
        }).start();

    }

    /**
     * http get method
     * @param method
     * @param _targetUrl
     * @param _mainActivity
     * @throws Exception
     */
    private void get(final String method, final String _targetUrl, final MainActivity _mainActivity) {

        this.mainActivity = _mainActivity;
        this.con = null;
        this.targetUrl = _targetUrl;
        this.sb = new StringBuffer("");
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    URL obj = new URL(targetUrl);
                    HttpURLConnection con = (HttpURLConnection) obj.openConnection();

                    // optional default is GET
                    con.setRequestMethod("GET");

                    //add request header
                    con.setRequestProperty("User-Agent", USER_AGENT);

                    int responseCode = con.getResponseCode();
                    //System.out.println("\nSending 'GET' request to URL : " + targetUrl);
                    //System.out.println("Response Code : " + responseCode);

                    //BufferedReader br = new BufferedReader(
                    //        new InputStreamReader(con.getInputStream()));
                    BufferedReader br = getBody(con);
                    String line;

                    while ((line = br.readLine()) != null) {
                        line = new String(line.getBytes("UTF-8"), "utf-8");
                        sb.append(line);
                    }
                    Message msg = Message.obtain();
                    //設定Message的內容
                    msg.what = method==Method.POST.value()? 1: 2;
                    msg.obj=sb.toString();
                    //使用MainActivity的static handler來丟Message
                    mainActivity.handler.sendMessage(msg);
                    br.close();
                    con.disconnect();

                    //print result
                    //System.out.println(sb.toString());
                }catch(Exception e){
                    e.printStackTrace();
                }finally {
                    if(con != null)
                        con.disconnect();
                }

            }
        }).start();

    }

    private BufferedReader getBody(HttpURLConnection conn) throws Exception{
        BufferedReader br = null;
        if (200 <= conn.getResponseCode() && conn.getResponseCode() <= 299) {
            br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
        } else {
            br = new BufferedReader(new InputStreamReader(conn.getErrorStream(), "UTF-8"));
        }
        return br;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

}
