package com.lilac.enums;

import com.lilac.exception.BusinessException;
import lombok.Getter;

import static com.lilac.constant.FileConstant.THUMBNAIL_STYLE_AVATAR;
import static com.lilac.constant.FileConstant.THUMBNAIL_STYLE_COVER;

/**
 * 上传场景枚举，决定 OSS 路径前缀和缩略图处理样式。
 */
@Getter
public enum UploadTypeEnum {

    AVATAR("avatar", "avatar", THUMBNAIL_STYLE_AVATAR),
    COVER("cover", "cover", THUMBNAIL_STYLE_COVER),
    CONTENT("content", "content", "");

    private final String type;
    private final String pathPrefix;
    private final String thumbnailStyle;

    UploadTypeEnum(String type, String pathPrefix, String thumbnailStyle) {
        this.type = type;
        this.pathPrefix = pathPrefix;
        this.thumbnailStyle = thumbnailStyle;
    }

    /**
     * 根据上传场景获取枚举
     */
    public static UploadTypeEnum fromType(String type) {
        for (UploadTypeEnum e : values()) {
            if (e.type.equals(type)) {
                return e;
            }
        }
        throw new BusinessException(HttpsCodeEnum.PARAMS_ERROR, "不支持的上传类型: " + type);
    }
}
