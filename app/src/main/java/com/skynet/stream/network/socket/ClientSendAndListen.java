package com.skynet.stream.network.socket;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

import com.blankj.utilcode.util.LogUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public class ClientSendAndListen implements Runnable {
    int portVal = 5555;
    String ipVal = "192.168.0.130";
    AudioTrack mAudioTrack;
    ByteBuffer _intShifter;

    public ClientSendAndListen(AudioTrack mAudio, String host, int port) {
        this.mAudioTrack = mAudio;
        this.portVal = port;
        this.ipVal = host;
        _intShifter = ByteBuffer.allocate(Integer.SIZE / Byte.SIZE)
                .order(ByteOrder.LITTLE_ENDIAN);
    }

    @Override
    public void run() {
        boolean run = true;
        try {

            DatagramSocket udpSocket = new DatagramSocket(portVal);
            InetAddress serverAddr = InetAddress.getByName(ipVal);
            byte[] buf = ("FILES").getBytes();
            DatagramPacket packetSend = new DatagramPacket(buf, buf.length, serverAddr, portVal);
            udpSocket.send(packetSend);
            while (run) {
                try {
                    byte[] message = new byte[50000];
                    DatagramPacket packet = new DatagramPacket(message, message.length);

                    LogUtils.i("UDP client: ", message.toString());
                    udpSocket.setSoTimeout(10000);
                    udpSocket.receive(packet);
                    //    String text = new String(message, 0, packet.getLength());
//                    LogUtils.d("Received byte : ", message.length);
                    byte isMp3 = message[0];
                    LogUtils.e("Mp3 or record : " + isMp3);
                    byte[] lengthData = new byte[]{message[1], message[2], message[3], message[4]};

                    int length = byteToInt(lengthData);
                    LogUtils.e("length = " + length + " - " + lengthData[0] + " - " + lengthData[1] + " - " + lengthData[2] + " - " + lengthData[3]);
                    //   byte audioData[] = Arrays.copyOfRange(message, 4, length + 4);
                    // Get an input stream on the byte array
                    // containing the data
                    mAudioTrack.write(message, 5,length);
                    mAudioTrack.play();
//                    playSound(message,length);
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

    public int byteToInt(byte[] data) {
        _intShifter.clear();
        _intShifter.put(data, 0, Integer.SIZE / Byte.SIZE);
        _intShifter.flip();
        return _intShifter.getInt();
    }
    public void playSound(short[] buffer,int length)
    {
        final String funcName = "playSound";



        mAudioTrack = new AudioTrack(
                AudioManager.STREAM_MUSIC,
                48000,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                buffer.length*2,    //buffer length in bytes
                AudioTrack.MODE_STATIC);
        mAudioTrack.write(buffer, 5,length);
        mAudioTrack.setNotificationMarkerPosition(buffer.length);
//        mAudioTrack.setPlaybackPositionUpdateListener(this);
        mAudioTrack.play();
    }
}