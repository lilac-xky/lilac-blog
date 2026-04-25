package com.lilac.manager.upload;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import com.lilac.config.OSSClientConfig;
import com.lilac.domain.dto.file.UploadPictureResult;
import com.lilac.enums.HttpsCodeEnum;
import com.lilac.exception.BusinessException;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.Date;

import static com.lilac.constant.FileConstant.PROJECT_NAME;
import static com.lilac.constant.FileConstant.THUMBNAIL_STYLE;

/**
 * 图片上传模板
 * 子类只需实现差异化的三个钩子：校验来源、推断原始文件名、把数据写入临时文件。
 */
@Slf4j
public abstract class PictureUploadTemplate<S> {

    @Resource
    protected OssManager ossManager;

    @Resource
    protected OSSClientConfig ossClientConfig;

    /**
     * 上传图片
     *
     * @param source           数据源
     * @param uploadPathPrefix OSS 路径前缀
     * @return 原图与缩略图 URL
     */
    public final UploadPictureResult uploadPicture(S source, String uploadPathPrefix) {
        // 校验图片
        validPicture(source);
        // 获取原始文件名
        String originalFilename = getOriginalFilename(source);
        String suffix = FileUtil.getSuffix(originalFilename);
        String uuid = RandomUtil.randomString(16);
        String uploadFileName = String.format("%s%s.%s", DateUtil.formatDate(new Date()), uuid, suffix);
        String objectName = String.format(PROJECT_NAME + "/%s/%s/%s", uploadPathPrefix,
                DateUtil.format(new Date(), "yyyy/MM/dd"), uploadFileName);

        // 上传图片
        File tempFile = null;
        try {
            tempFile = File.createTempFile("oss_upload_", "." + suffix);
            processFile(source, tempFile);
            ossManager.putObject(objectName, tempFile);
            // 返回图片 URL
            String url = buildUrl(objectName);
            UploadPictureResult result = new UploadPictureResult();
            result.setUrl(url);
            result.setThumbnailUrl(url + "?" + THUMBNAIL_STYLE);
            return result;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("OSS 上传失败, objectName={}", objectName, e);
            throw new BusinessException(HttpsCodeEnum.SYSTEM_ERROR, "上传文件失败: " + e.getMessage());
        } finally {
            if (tempFile != null && tempFile.exists()) {
                FileUtil.del(tempFile);
            }
        }
    }

    /**
     *  校验图片
     */
    protected abstract void validPicture(S source);

    /**
     * 获取原始文件名
     */
    protected abstract String getOriginalFilename(S source);

    /**
     * 把数据源内容写入临时文件
     */
    protected abstract void processFile(S source, File tempFile) throws Exception;

    /**
     * 构建图片 URL
     */
    private String buildUrl(String objectName) {
        String endpoint = ossClientConfig.getEndpoint();
        if (endpoint.startsWith("http://") || endpoint.startsWith("https://")) {
            int idx = endpoint.indexOf("://") + 3;
            endpoint = endpoint.substring(idx);
        }
        return "https://" + ossClientConfig.getBucketName() + "." + endpoint + "/" + objectName;
    }
}
