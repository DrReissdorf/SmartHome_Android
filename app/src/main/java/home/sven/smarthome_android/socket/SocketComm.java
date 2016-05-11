package home.sven.smarthome_android.socket;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class SocketComm {
    private Socket socket;
    private PrintWriter pw;
    private BufferedReader br;
    private String ip;
    private int port;
    private final int SOCKET_CONNECT_TIMEOUT = 1000;
    private boolean printDebugLog = false;

    public SocketComm(String ip, int port) throws IOException {
        if(printDebugLog) Log.v("SocketComm", "constructor()");
        this.ip = ip;
        this.port = port;
        init();
    }

    public void send(String text) throws IOException {
        if(printDebugLog) Log.v("SocketComm", "send()");
        pw.println(text);
        pw.flush();
    }

    public String receive() throws IOException {
        if(printDebugLog) Log.v("SocketComm", "receive()");
        String temp;
        temp = br.readLine();
        return temp;
    }

    private void init() throws IOException {
        if(printDebugLog) Log.v("SocketComm", "init()");
        socket = new Socket();
        socket.connect(new InetSocketAddress(ip,port),SOCKET_CONNECT_TIMEOUT);
        pw = new PrintWriter(socket.getOutputStream());
        br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    public void close() throws IOException {
        if(printDebugLog) Log.v("SocketComm", "close()");
        socket.close();

        br = null;
        pw = null;
        socket = null;
    }

    public String getIp() {
        return socket.getInetAddress().getHostAddress();
    }
}
