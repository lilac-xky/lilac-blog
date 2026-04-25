package com.lilac.domain.dto.file;

import lombok.Data;

/**
 * 上传图片结果
 */
@Data
public class UploadPictureResult {
    /**
     * 图片url
     */
    private String url;

    /**
     * 缩略图 url
     */
    private String thumbnailUrl;

}
