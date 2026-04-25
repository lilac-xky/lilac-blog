package com.lilac.manager.upload;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.GetObjectRequest;
import com.aliyun.oss.model.OSSObject;
import com.aliyun.oss.model.PutObjectRequest;
import com.aliyun.oss.model.PutObjectResult;
import com.lilac.config.OSSClientConfig;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * 阿里云OSS存储服务
 */
@Component
public class OssManager {

    @Resource
    private OSSClientConfig ossClientConfig;
    @Resource
    private OSS ossClient;

    /**
     * 上传文件
     *
     * @param objectName 文件名
     * @param file 文件
     * @return 上传结果
     */
    public PutObjectResult putObject(String objectName, File file){
        PutObjectRequest putObjectRequest = new PutObjectRequest(ossClientConfig.getBucketName(), objectName, file);
        return ossClient.putObject(putObjectRequest);
    }

    /**
     * 获取文件
     *
     * @param objectName 文件名
     * @return 文件
     */
    public OSSObject getObject(String objectName){
        GetObjectRequest getObjectRequest = new GetObjectRequest(ossClientConfig.getBucketName(), objectName);
        return ossClient.getObject(getObjectRequest);
    }

    /**
     * 删除文件
     *
     * @param objectName 文件名
     */
    public void deleteObject(String objectName){
        ossClient.deleteObject(ossClientConfig.getBucketName(), objectName);
    }
}
