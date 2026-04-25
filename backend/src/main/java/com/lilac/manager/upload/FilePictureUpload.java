package com.lilac.manager.upload;

import cn.hutool.core.io.FileUtil;
import com.lilac.enums.HttpsCodeEnum;
import com.lilac.utils.ThrowUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;

import static com.lilac.constant.FileConstant.ALLOW_FORMAT_LIST;
import static com.lilac.constant.FileConstant.MAX_SIZE;

/**
 * 文件上传：服务端把文件保存到临时目录，再转存到 OSS。
 */
@Component
public class FilePictureUpload extends PictureUploadTemplate<MultipartFile> {

    /**
     * 校验文件：大小、格式。
     */
    @Override
    protected void validPicture(MultipartFile file) {
        ThrowUtils.throwIf(file == null || file.isEmpty(), HttpsCodeEnum.PARAMS_ERROR, "上传文件不能为空");
        ThrowUtils.throwIf(file.getSize() > MAX_SIZE, HttpsCodeEnum.PARAMS_ERROR, "文件大小不能超过5M");
        String suffix = FileUtil.getSuffix(file.getOriginalFilename());
        ThrowUtils.throwIf(!ALLOW_FORMAT_LIST.contains(suffix), HttpsCodeEnum.PARAMS_ERROR, "文件格式错误");
    }

    /**
     * 获取原始文件名。
     */
    @Override
    protected String getOriginalFilename(MultipartFile file) {
        return file.getOriginalFilename();
    }

    /**
     * 把文件写入临时目录。
     */
    @Override
    protected void processFile(MultipartFile file, File tempFile) throws Exception {
        file.transferTo(tempFile);
    }
}
