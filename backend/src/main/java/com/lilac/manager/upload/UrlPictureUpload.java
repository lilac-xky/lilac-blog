package com.lilac.manager.upload;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.lilac.enums.HttpsCodeEnum;
import com.lilac.exception.BusinessException;
import com.lilac.utils.ThrowUtils;
import org.springframework.stereotype.Component;

import java.io.File;
import java.net.URI;
import java.util.List;

import static com.lilac.constant.FileConstant.*;

/**
 * 通过 URL 触发的图片上传：服务端拉取后转存到 OSS。
 */
@Component
public class UrlPictureUpload extends PictureUploadTemplate<String> {

    /**
     * 校验 URL 是否可访问，并下载图片。
     */
    @Override
    protected void validPicture(String url) {
        ThrowUtils.throwIf(StrUtil.isBlank(url), HttpsCodeEnum.PARAMS_ERROR, "图片 URL 不能为空");
        URI uri;
        try {
            uri = new URI(url);
        } catch (Exception e) {
            throw new BusinessException(HttpsCodeEnum.PARAMS_ERROR, "图片 URL 格式错误");
        }
        // 校验
        String scheme = uri.getScheme();
        ThrowUtils.throwIf(!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme),
                HttpsCodeEnum.PARAMS_ERROR, "仅支持 http/https 协议");
        String host = uri.getHost();
        ThrowUtils.throwIf(StrUtil.isBlank(host), HttpsCodeEnum.PARAMS_ERROR, "URL 主机名无效");
        ThrowUtils.throwIf(isPrivateOrLoopback(host), HttpsCodeEnum.PARAMS_ERROR, "禁止访问内网 / 回环地址");

        // 探测图片大小
        try (HttpResponse head = HttpRequest.head(url).timeout(TIMEOUT_MS).execute()) {
            ThrowUtils.throwIf(!head.isOk(), HttpsCodeEnum.PARAMS_ERROR, "URL 不可访问: HTTP " + head.getStatus());
            String contentType = head.header("Content-Type");
            ThrowUtils.throwIf(contentType == null || !contentType.toLowerCase().startsWith("image/"),
                    HttpsCodeEnum.PARAMS_ERROR, "URL 指向的不是图片");
            String contentLength = head.header("Content-Length");
            if (StrUtil.isNotBlank(contentLength)) {
                try {
                    long length = Long.parseLong(contentLength);
                    ThrowUtils.throwIf(length > MAX_SIZE, HttpsCodeEnum.PARAMS_ERROR, "图片大小不能超过5M");
                } catch (NumberFormatException ignored) {
                }
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(HttpsCodeEnum.PARAMS_ERROR, "URL 探测失败: " + e.getMessage());
        }
    }

    /**
     * 获取原始文件名。
     */
    @Override
    protected String getOriginalFilename(String url) {
        String path;
        try {
            path = new URI(url).getPath();
        } catch (Exception e) {
            return "image.jpg";
        }
        if (StrUtil.isBlank(path)) {
            return "image.jpg";
        }
        String name = path.substring(path.lastIndexOf('/') + 1);
        String suffix = FileUtil.getSuffix(name);
        if (!ALLOW_FORMAT_LIST.contains(suffix.toLowerCase())) {
            throw new BusinessException(HttpsCodeEnum.PARAMS_ERROR, "URL 文件后缀不在允许范围内");
        }
        return name;
    }

    /**
     * 下载图片并保存到临时文件。
     */
    @Override
    protected void processFile(String url, File tempFile) throws Exception {
        HttpUtil.downloadFile(url, tempFile, TIMEOUT_MS);
        ThrowUtils.throwIf(tempFile.length() > MAX_SIZE, HttpsCodeEnum.PARAMS_ERROR, "图片大小不能超过5M");
        ThrowUtils.throwIf(tempFile.length() == 0, HttpsCodeEnum.PARAMS_ERROR, "图片下载失败：文件为空");
    }

    /**
     * 判断 IP 地址是否为内网地址。
     */
    private boolean isPrivateOrLoopback(String host) {
        if (host.equalsIgnoreCase("localhost") || host.equals("0.0.0.0")) return true;
        if (host.startsWith("127.") || host.startsWith("10.") || host.startsWith("192.168.") || host.startsWith("169.254.")) return true;
        if (host.startsWith("172.")) {
            String[] parts = host.split("\\.");
            if (parts.length >= 2) {
                try {
                    int second = Integer.parseInt(parts[1]);
                    return second >= 16 && second <= 31;
                } catch (NumberFormatException ignored) {
                    return false;
                }
            }
        }
        return false;
    }
}
