package com.skynet.stream;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.NetworkUtils;
import com.skynet.stream.network.api.ApiResponse;
import com.skynet.stream.network.api.ApiUtil;
import com.skynet.stream.network.api.CallBackBase;
import com.skynet.stream.network.socket.ClientSendAndListen;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends BaseActivity {
    AudioTrack mTracker;
    @BindView(R.id.edtHost)
    EditText edtHost;
    @BindView(R.id.edtPort)
    EditText edtPort;
    Thread udpConnect;

    @Override
    protected int initLayout() {
        return R.layout.activity_main;
    }

    @Override
    protected void initVariables() {
        edtHost.setText(NetworkUtils.getIPAddress(true));
        edtPort.setText("5555");
    }

    @Override
    protected void initViews() {
        ButterKnife.bind(this);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        if (!canReadWriteExternal())
//            requestPermission();
//        else
//            getUpdate();
        int bufferSize = AudioTrack.getMinBufferSize(8000, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
        mTracker = new AudioTrack(AudioManager.STREAM_MUSIC, 8000
                , AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize, AudioTrack.MODE_STREAM);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mTracker.flush();
        mTracker.release();
    }

    public void getUpdate() {
        Toast.makeText(MainActivity.this, "Check update", Toast.LENGTH_SHORT).show();
        ApiUtil.createNotTokenApi().getUpdate().enqueue(new CallBackBase<ApiResponse<String>>() {
            @Override
            public void onRequestSuccess(Call<ApiResponse<String>> call, Response<ApiResponse<String>> response) {
                if (response.isSuccessful()) {
                    if (response.body().getData() != null && !response.body().getData().isEmpty()) {
                        Toast.makeText(MainActivity.this, "Cần update app", Toast.LENGTH_SHORT).show();
                        downloadFile(response.body().getData());
                    } else {
                        Toast.makeText(MainActivity.this, "Không cần update app", Toast.LENGTH_SHORT).show();
                    }
                }
            }


            @Override
            public void onRequestFailure(Call<ApiResponse<String>> call, Throwable t) {

            }
        });
    }

    private void downloadFile(String url) {
        Toast.makeText(MainActivity.this, "Bắt đầu download", Toast.LENGTH_SHORT).show();

        Call<ResponseBody> call = ApiUtil.createNotTokenApi().downloadFileWithDynamicUrlAsync(url);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, final Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Log.d("", "server contacted and has file");

                    new AsyncTask<Void, Void, Void>() {
                        @Override
                        protected Void doInBackground(Void... voids) {
                            boolean writtenToDisk = writeResponseBodyToDisk(response.body());

                            Log.d("", "file download was a success? " + writtenToDisk);
                            return null;
                        }

                        @Override
                        protected void onPostExecute(Void aVoid) {
                            super.onPostExecute(aVoid);
                            Toast.makeText(MainActivity.this, "Download xong. Update app", Toast.LENGTH_SHORT).show();

                            File file = new File(getExternalFilesDir(null) + File.separator + "app.apk");
                            if (file.exists()) {
                                try {
                                    final String command = "pm install -r " + file.getAbsolutePath();
                                    Process proc = Runtime.getRuntime().exec(new String[]{"su", "-c", command});
                                    proc.waitFor();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
//                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                                Uri apkUri = FileProvider.getUriForFile(MainActivity.this, BuildConfig.APPLICATION_ID + ".provider", toInstall);
//                                Intent intent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
//                                intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
//                                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//                                startActivity(intent);
//                            } else {
//
//                                Uri apkUri = Uri.fromFile(toInstall);
//                                Intent intent = new Intent(Intent.ACTION_VIEW);
//                                intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
//                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                                startActivity(intent);
//                            }

                        }
                    }.execute();
                } else {
                    LogUtils.e("server contact failed");
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("", "error");
            }
        });
    }

    public static void InstallAPK(File file) {
//        File file = new File(filename);
        if (file.exists()) {
            try {
                String command;
                command = "adb install -r " + file.getAbsolutePath();
                Process proc = Runtime.getRuntime().exec(new String[]{"su", "-c", command});
                proc.waitFor();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private boolean writeResponseBodyToDisk(ResponseBody body) {
        try {
            // todo change the file location/name according to your needs
            File futureStudioIconFile = new File(getExternalFilesDir(null) + File.separator + "app.apk");
            LogUtils.e(futureStudioIconFile.getPath());
            InputStream inputStream = null;
            OutputStream outputStream = null;

            try {
                byte[] fileReader = new byte[4096];

                long fileSize = body.contentLength();
                long fileSizeDownloaded = 0;

                inputStream = body.byteStream();
                outputStream = new FileOutputStream(futureStudioIconFile);

                while (true) {
                    int read = inputStream.read(fileReader);

                    if (read == -1) {
                        break;
                    }

                    outputStream.write(fileReader, 0, read);

                    fileSizeDownloaded += read;

                    LogUtils.e("file download: " + fileSizeDownloaded + " of " + fileSize);
                }

                outputStream.flush();

                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }

                if (outputStream != null) {
                    outputStream.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static final int REQUEST_WRITE_PERMISSION = 786;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_WRITE_PERMISSION && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Permission granted", Toast.LENGTH_LONG).show();
            getUpdate();
        }
    }

    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_PERMISSION);
    }

    private boolean canReadWriteExternal() {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M ||
                ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED;
    }

    @OnClick(R.id.button)
    public void onViewClicked() {
        if (!edtHost.getText().toString().isEmpty() && !edtPort.getText().toString().isEmpty()) {
            udpConnect = new Thread(new ClientSendAndListen(mTracker,
                    edtHost.getText().toString(),
                    Integer.parseInt(edtPort.getText().toString())));
            udpConnect.start();
            findViewById(R.id.button).setEnabled(false);
            findViewById(R.id.disconnect).setEnabled(true);

        }
    }

    @OnClick(R.id.disconnect)
    public void onViewDisconnectClicked() {
        if (udpConnect != null) {
            udpConnect.interrupt();
            udpConnect = null;
            mTracker.flush();
            mTracker.release();
        }
        findViewById(R.id.disconnect).setEnabled(false);
        findViewById(R.id.button).setEnabled(true);
    }
}
