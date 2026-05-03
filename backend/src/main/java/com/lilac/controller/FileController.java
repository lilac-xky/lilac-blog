package com.lilac.controller;

import com.lilac.domain.dto.file.UploadPictureResult;
import com.lilac.domain.result.Result;
import com.lilac.enums.HttpsCodeEnum;
import com.lilac.enums.UploadTypeEnum;
import com.lilac.exception.BusinessException;
import com.lilac.manager.upload.FileStorageService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传
 */
@RestController
@RequestMapping("/file")
public class FileController {

    @Resource
    private FileStorageService fileStorageService;

    /**
     * 文件上传
     *
     * @param file 文件
     * @param type 上传场景：avatar（头像）/ cover（封面）/ content（正文）
     * @return 上传结果（原图 URL + 缩略图 URL）
     */
    @PostMapping("/upload")
    public Result<UploadPictureResult> uploadFile(@RequestPart("file") MultipartFile file,
                                                  @RequestParam(defaultValue = "avatar") String type) {
        if (file.isEmpty()) {
            throw new BusinessException(HttpsCodeEnum.PARAMS_ERROR, "上传文件不能为空");
        }
        UploadTypeEnum uploadType = UploadTypeEnum.fromType(type);
        UploadPictureResult result = fileStorageService.uploadFile(file, uploadType.getPathPrefix(), uploadType.getThumbnailStyle());
        if (result == null) {
            return Result.error(HttpsCodeEnum.SYSTEM_ERROR, "上传文件失败");
        }
        return Result.success(result);
    }

    /**
     * 通过外链 URL 上传
     *
     * @param url  外链图片 URL
     * @param type 上传场景：avatar（头像）/ cover（封面）/ content（正文）
     * @return 上传结果
     */
    @PostMapping("/uploadByUrl")
    public Result<UploadPictureResult> uploadByUrl(@RequestParam("url") String url,
                                                   @RequestParam(defaultValue = "avatar") String type) {
        UploadTypeEnum uploadType = UploadTypeEnum.fromType(type);
        UploadPictureResult result = fileStorageService.uploadByUrl(url, uploadType.getPathPrefix(), uploadType.getThumbnailStyle());
        if (result == null) {
            return Result.error(HttpsCodeEnum.SYSTEM_ERROR, "上传文件失败");
        }
        return Result.success(result);
    }
}
