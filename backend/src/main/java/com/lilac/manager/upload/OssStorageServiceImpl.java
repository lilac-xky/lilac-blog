package com.lilac.manager.upload;

import com.lilac.domain.dto.file.UploadPictureResult;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * 阿里云 OSS 存储服务实现。
 * <p>
 * 仅存原图，缩略图通过 OSS 图片处理参数（x-oss-process）按需生成，避免重复存储。
 * 文件 / URL 两种上传来源由 {@link PictureUploadTemplate} 子类承担差异化逻辑，本类只做派发。
 */
@Service("ossStorageService")
public class OssStorageServiceImpl implements FileStorageService {

    @Resource
    private FilePictureUpload filePictureUpload;

    @Resource
    private UrlPictureUpload urlPictureUpload;

    /**
     * 文件上传
     *
     * @param file 文件
     * @return 上传结果（原图 URL）
     */
    @Override
    public UploadPictureResult uploadFile(MultipartFile file, String uploadPathPrefix) {
        return filePictureUpload.uploadPicture(file, uploadPathPrefix);
    }

    /**
     * 通过外链 URL 上传：服务端拉取后转存
     *
     * @param url 外链图片 URL
     * @return 上传结果（原图 + 缩略图 URL）
     */
    @Override
    public UploadPictureResult uploadByUrl(String url, String uploadPathPrefix) {
        return urlPictureUpload.uploadPicture(url, uploadPathPrefix);
    }
}
