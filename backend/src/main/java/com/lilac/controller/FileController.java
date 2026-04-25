package com.lilac.controller;

import com.lilac.domain.dto.file.UploadPictureResult;
import com.lilac.domain.result.Result;
import com.lilac.enums.HttpsCodeEnum;
import com.lilac.exception.BusinessException;
import com.lilac.manager.upload.FileStorageService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/file")
public class FileController {

    @Resource
    private FileStorageService fileStorageService;

    private static final String UPLOAD_PATH_PREFIX = "avtar";

    /**
     * 文件上传
     *
     * @param file 文件
     * @return 上传结果（原图 URL）
     */
    @PostMapping("/upload")
    public Result<UploadPictureResult> uploadFile(@RequestPart("file") MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessException(HttpsCodeEnum.PARAMS_ERROR, "上传文件不能为空");
        }
        UploadPictureResult result = fileStorageService.uploadFile(file, UPLOAD_PATH_PREFIX);
        if (result == null) {
            return Result.error(HttpsCodeEnum.SYSTEM_ERROR, "上传文件失败");
        }
        return Result.success(result);
    }

    /**
     * 通过外链 URL 上传
     *
     * @param url 外链图片 URL
     * @return 上传结果
     */
    @PostMapping("/uploadByUrl")
    public Result<UploadPictureResult> uploadByUrl(@RequestParam("url") String url) {
        UploadPictureResult result = fileStorageService.uploadByUrl(url, UPLOAD_PATH_PREFIX);
        if (result == null) {
            return Result.error(HttpsCodeEnum.SYSTEM_ERROR, "上传文件失败");
        }
        return Result.success(result);
    }
}
