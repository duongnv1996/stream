package com.skynet.stream.network.socket;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

import com.blankj.utilcode.util.LogUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.io.InputStream;
public class ClientSendAndListen implements Runnable {
    int portVal = 5555;
    String ipVal = "192.168.0.130";
    AudioTrack mAudioTrack ;
    public ClientSendAndListen(AudioTrack mAudio,String host,int port){
        this.mAudioTrack= mAudio;
        this.portVal = port;
        this.ipVal = host;
    }
    @Override
    public void run() {
        boolean run = true;
        try {

            DatagramSocket udpSocket = new DatagramSocket(portVal);
            InetAddress serverAddr = InetAddress.getByName(ipVal);
            byte[] buf = ("FILES").getBytes();
            DatagramPacket packetSend = new DatagramPacket(buf, buf.length,serverAddr, portVal);
            udpSocket.send(packetSend);
            while (run) {
                try {
                    byte[] message = new byte[800];
                    DatagramPacket packet = new DatagramPacket(message,message.length);
                    LogUtils.i("UDP client: ", "about to wait to receive");
                    udpSocket.setSoTimeout(10000);
                    udpSocket.receive(packet);
                //    String text = new String(message, 0, packet.getLength());
                    LogUtils.d("Received byte : ", message.length);


//
                    byte audioData[] = message;
                    // Get an input stream on the byte array
                    // containing the data
                    mAudioTrack.write(audioData, 0, audioData.length);

                    mAudioTrack.play();

                } catch (IOException e) {
                   LogUtils.e(" UDP client has IOException", "error: ", e);
                    run = false;
                    udpSocket.close();
                }
            }
        } catch (SocketException e) {
            LogUtils.e("Socket Open:", "Error:", e);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}