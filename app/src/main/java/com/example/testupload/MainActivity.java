package com.example.testupload;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;
import com.wx.android.common.util.SystemUtils;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;


public class MainActivity extends AppCompatActivity {

    private final static String TAG = "MainActivity";
    private final static int WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 1;

    private String requestURL = "http://app.sirui.com/uploadtest/f.do";

    String path = null;
    private ImageView iv_photo;

    private static final int TIME_OUT = 10 * 1000;   //超时时间
    private static final String CHARSET = "utf-8"; //设置编码

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestPermissions();

        iv_photo = (ImageView) findViewById(R.id.imageView);

        findViewById(R.id.btn_select).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 相册选取
                Intent intent1 = new Intent(Intent.ACTION_GET_CONTENT);
                intent1.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                startActivityForResult(intent1, 103);
            }
        });

        findViewById(R.id.btn_upload).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "path--->" + path);

                if (path != null) {
                    File file = new File(path); //这里的path就是那个地址的全局变量

                    Log.i(TAG, "------start上传------");
//                    String result = uploadFile(file, requestURL);
                    testXUtilsPost(file);
                    Log.i(TAG, "------end上传------");
//                    Log.i(TAG, "result--->" + result);
                }
            }
        });


//        testConnectUrl();

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 103:
                Bitmap bm = null;
                // 外界的程序访问ContentProvider所提供数据 可以通过ContentResolver接口
                ContentResolver resolver = getContentResolver();

                try {
                    Uri originalUri = data.getData(); // 获得图片的uri

                    bm = MediaStore.Images.Media.getBitmap(resolver, originalUri); // 显得到bitmap图片

                    // 这里开始的第二部分，获取图片的路径：
                    String[] proj = {MediaStore.Images.Media.DATA};

                    // 好像是android多媒体数据库的封装接口，具体的看Android文档
                    @SuppressWarnings("deprecation")
                    Cursor cursor = managedQuery(originalUri, proj, null, null, null);
                    // 按我个人理解 这个是获得用户选择的图片的索引值
                    int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                    // 将光标移至开头 ，这个很重要，不小心很容易引起越界
                    cursor.moveToFirst();
                    // 最后根据索引值获取图片路径
                    path = cursor.getString(column_index);
                    iv_photo.setImageURI(originalUri); // ?

                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void requestPermissions() {
        /**
         * 6.0的sdk要请求权限
         */
        Log.i(TAG, "SDK Version--->" + String.valueOf(SystemUtils.getVersionSDK()));
        if (SystemUtils.getVersionSDK() >= 23) {

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        WRITE_EXTERNAL_STORAGE_REQUEST_CODE);
            } else {
//                uploadImg();
            }
        } else {
//            uploadImg();

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == WRITE_EXTERNAL_STORAGE_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission Granted
                Log.i(TAG, "WRITE_EXTERNAL_STORAGE Permission Granted");
//                uploadImg();
            } else {
                // Permission Denied
                Log.i(TAG, "WRITE_EXTERNAL_STORAGE Permission Denied");
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    public void testXUtilsPost(File file) {

        RequestParams params = new RequestParams();
        params.addHeader("name", "value");
//        params.addQueryStringParameter("name", "value");

        // 只包含字符串参数时默认使用BodyParamsEntity，
        // 类似于UrlEncodedFormEntity（"application/x-www-form-urlencoded"）。
//        params.addBodyParameter("name", "value");

        // 加入文件参数后默认使用MultipartEntity（"multipart/form-data"），
        // 如需"multipart/related"，xUtils中提供的MultipartEntity支持设置subType为"related"。
        // 使用params.setBodyEntity(httpEntity)可设置更多类型的HttpEntity（如：
        // MultipartEntity,BodyParamsEntity,FileUploadEntity,InputStreamUploadEntity,StringEntity）。
        // 例如发送json参数：params.setBodyEntity(new StringEntity(jsonStr,charset));
        params.addBodyParameter("ff", file);

        HttpUtils http = new HttpUtils();
        http.send(HttpRequest.HttpMethod.POST, requestURL, params,
                new RequestCallBack<String>() {

                    @Override
                    public void onStart() {
//                        testTextView.setText("conn...");
                        Log.i(TAG, "HttpUtils--->onStart()");
                    }

                    @Override
                    public void onLoading(long total, long current, boolean isUploading) {
                        if (isUploading) {
                            Log.i(TAG, "HttpUtils--->" + "upload: " + current + "/" + total);
//                            testTextView.setText("upload: " + current + "/" + total);
                        } else {
//                            testTextView.setText("reply: " + current + "/" + total);
                        }
                    }

                    @Override
                    public void onSuccess(ResponseInfo<String> responseInfo) {
//                        testTextView.setText("reply: " + responseInfo.result);
                        Log.i(TAG, "HttpUtils--->onSuccess()");
                        Log.i(TAG, "responseInfo--->" + responseInfo.result.toString());
                    }

                    @Override
                    public void onFailure(HttpException error, String msg) {
//                        testTextView.setText(error.getExceptionCode() + ":" + msg);
                        Log.i(TAG, "HttpUtils--->onFailure()");
                    }
                });
    }


    /**
     * android上传文件到服务器=====================出错闪退
     *
     * @param file       需要上传的文件
     * @param RequestURL 请求的rul
     * @return 返回响应的内容
     */
    public String uploadFile(File file, String RequestURL) {
        String result = null;
        String CONTENT_TYPE = "multiple-data";   //内容类型

        String boundary = UUID.randomUUID().toString().substring(0,11);
        String contentTypeboundary = "; boundary = " +  boundary;
        String firstboundary = "--" + boundary +"\r\n";
        String lastboundary = "\r\n--"+ boundary +"--\r\n";
        try {
            URL url = new URL(RequestURL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(TIME_OUT);
            conn.setConnectTimeout(TIME_OUT);
            conn.setDoInput(true);  //允许输入流
            conn.setDoOutput(true); //允许输出流
            conn.setUseCaches(false);  //不允许使用缓存
            conn.setRequestMethod("POST");  //请求方式
            conn.setRequestProperty("Charset", CHARSET);  //设置编码
            conn.setRequestProperty("connection", "keep-alive");
            conn.setRequestProperty("Content-Type", CONTENT_TYPE + contentTypeboundary);
//            conn.setRequestProperty("Content-Type", CONTENT_TYPE);
//            conn.connect();// 窗，后视


            /**
             * 获取响应码  200=成功
             * 当响应成功，获取响应的流
             */
//            int res = conn.getResponseCode();
//            Log.i(TAG, "=====HttpResponseCode--->" + res);
//            if (res == 200) {

                if (file != null) {
                    /**
                     * 当文件不为空，把文件包装并且上传
                     */
                    OutputStream os = conn.getOutputStream();
                    DataOutputStream dos = new DataOutputStream(os);
                    StringBuffer sb = new StringBuffer();

                    sb.append(firstboundary);
                    sb.append("Content-Disposition: form-data; name=\"ff\"; filename=\""+file.getName()+"\"\r\n");
                    sb.append("Content-Type: application/octet-stream\r\n\r\n");


                    InputStream is = new FileInputStream(file);
                    byte[] bytes = new byte[1024];
                    int len = 0;
                    while ((len = is.read(bytes)) != -1) {
                        dos.write(bytes, 0, len);
                    }


                    dos.write("\r\n".getBytes());
                    dos.write(lastboundary.getBytes());


                    InputStream input = conn.getInputStream();
                    StringBuffer sb1 = new StringBuffer();
                    int ss;
                    while ((ss = input.read()) != -1) {
                        sb1.append((char) ss);
                    }
                    result = sb1.toString();
                    Log.i(TAG, "get InputStream result--->" + result);

                    is.close();
                    dos.flush();
                } else {
                    Log.i(TAG, "File == NULL!!!!!!!!!!!!!!");
                }

//            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    private void testConnectUrl() {
        Log.i(TAG, "###URL--->" + requestURL);
        HttpUtils http = new HttpUtils();
        http.configCurrentHttpCacheExpiry(1000 * 5);
        http.send(HttpRequest.HttpMethod.POST, requestURL, new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                Log.i("HttpUtils", "### HttpUtils onSuccess");
                Log.i("HttpUtils", "### responseInfo--->" + responseInfo.toString());


            }

            @Override
            public void onFailure(HttpException e, String s) {
                Log.i("HttpUtils", "### HttpUtils onFailure");
            }
        });

    }






}
