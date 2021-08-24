package com.quick.jsbridge.takeToSee;

import android.app.Instrumentation;
import android.content.Context;
import android.util.Log;

import com.alibaba.sdk.android.oss.ClientConfiguration;
import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.OSS;
import com.alibaba.sdk.android.oss.OSSClient;
import com.alibaba.sdk.android.oss.ServiceException;
import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback;
import com.alibaba.sdk.android.oss.common.OSSLog;
import com.alibaba.sdk.android.oss.common.auth.OSSAuthCredentialsProvider;
import com.alibaba.sdk.android.oss.common.auth.OSSCredentialProvider;
import com.alibaba.sdk.android.oss.internal.OSSAsyncTask;
import com.alibaba.sdk.android.oss.model.CannedAccessControlList;
import com.alibaba.sdk.android.oss.model.CreateBucketRequest;
import com.alibaba.sdk.android.oss.model.CreateBucketResult;
import com.alibaba.sdk.android.oss.model.PutObjectRequest;
import com.alibaba.sdk.android.oss.model.PutObjectResult;

public class UploadInstance implements IRtcImpl {

    private static UploadInstance upload = null;

    protected static String mBucketName;
    protected static String mPublicBucketName;
    protected static OSS oss;

    public static UploadInstance getInstance() {
        if (upload == null) {
            upload = new UploadInstance();  //在第一次调用getInstance()时才实例化，实现懒加载,所以叫懒汉式
        }
        return upload;
    }

    public static void initOSS(Context context, String endPoint, String stsServer) {
        ClientConfiguration conf = new ClientConfiguration();
        //ClientConfiguration 链接和socket 已经改为60s了
        conf.setConnectionTimeout(60 * 1000); // 连接超时，默认15秒
        conf.setSocketTimeout(60 * 1000); // socket超时，默认15秒
        conf.setMaxConcurrentRequest(5); // 最大并发请求书，默认5个
        conf.setMaxErrorRetry(2); // 失败后最大重试次数，默认2次
        conf.setHttpDnsEnable(false);
        OSSLog.enableLog();

        OSSCredentialProvider credentialProvider = new OSSAuthCredentialsProvider(stsServer);

        oss = new OSSClient(context, endPoint,credentialProvider, conf);
    }

    public static void setBucketName(String bucketName) {
        mBucketName = bucketName;
    }

    public static void setPublicBucketName(String publicBucketName) {
        mPublicBucketName = publicBucketName;
    }

    public static void createBucketName(String locationConstrait) {
        CreateBucketRequest createBucketRequest = new CreateBucketRequest(mBucketName);
        // 设置存储空间的访问权限为公共读，默认为私有读写。
        createBucketRequest.setBucketACL(CannedAccessControlList.PublicRead);
// 指定存储空间所在的地域。
        createBucketRequest.setLocationConstraint("oss-cn-hangzhou");

        OSSAsyncTask createTask = oss.asyncCreateBucket(createBucketRequest, new OSSCompletedCallback<CreateBucketRequest, CreateBucketResult>() {
            @Override
            public void onSuccess(CreateBucketRequest request, CreateBucketResult result) {
                Log.d("locationConstraint", request.getLocationConstraint());
            }

            @Override
            public void onFailure(CreateBucketRequest request, ClientException clientException, ServiceException serviceException) {
                // 请求异常。
                if (clientException != null) {
                    // 本地异常，如网络异常等。
                    clientException.printStackTrace();
                }
                if (serviceException != null) {
                    // 服务异常。
                    Log.e("ErrorCode", serviceException.getErrorCode());
                    Log.e("RequestId", serviceException.getRequestId());
                    Log.e("HostId", serviceException.getHostId());
                    Log.e("RawMessage", serviceException.getRawMessage());
                }
            }
        });

        createTask.waitUntilFinished();
    }

    public static void uploadImage(String bucketName, String objName, String filePath) {
        upload.pUploadImage(bucketName, objName, filePath);
    }

    public static void uploadImage(String objName, String filePath) {
        upload.pUploadImage(mBucketName, objName, filePath);
    }

    private void pUploadImage(String bucketName, String objName, String filePath) {
        // 构造上传请求。
        PutObjectRequest put = new PutObjectRequest(bucketName, objName, filePath);

        OSSAsyncTask task = oss.asyncPutObject(put, new OSSCompletedCallback<PutObjectRequest, PutObjectResult>() {
            @Override
            public void onSuccess(PutObjectRequest request, PutObjectResult result) {
                Log.d("PutObject", "UploadSuccess");
                Log.d("ETag", result.getETag());
                Log.d("RequestId", result.getRequestId());
            }

            @Override
            public void onFailure(PutObjectRequest request, ClientException clientException, ServiceException serviceException) {
                if (clientException != null) {
                    // 本地异常，如网络异常等。
                    clientException.printStackTrace();
                }
                if (serviceException != null) {
                    // 服务异常。
                    Log.e("ErrorCode", serviceException.getErrorCode());
                    Log.e("RequestId", serviceException.getRequestId());
                    Log.e("HostId", serviceException.getHostId());
                    Log.e("RawMessage", serviceException.getRawMessage());
                }
            }
        });

        task.waitUntilFinished(); // 等待上传完成。

    }

}
