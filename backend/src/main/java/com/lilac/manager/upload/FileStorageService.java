package com.lilac.manager.upload;

import com.lilac.domain.dto.file.UploadPictureResult;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件存储服务
 */
public interface FileStorageService {

    /**
     * 通过本地文件上传
     *
     * @param file             文件
     * @param uploadPathPrefix 路径前缀
     * @param thumbnailStyle   OSS 缩略图处理样式，空字符串表示不处理
     * @return 原图与缩略图 URL
     */
    UploadPictureResult uploadFile(MultipartFile file, String uploadPathPrefix, String thumbnailStyle);

    /**
     * 通过外链 URL 上传：服务端拉取后转存
     *
     * @param url              外链图片 URL
     * @param uploadPathPrefix 路径前缀
     * @param thumbnailStyle   OSS 缩略图处理样式，空字符串表示不处理
     * @return 原图与缩略图 URL
     */
    UploadPictureResult uploadByUrl(String url, String uploadPathPrefix, String thumbnailStyle);
}
