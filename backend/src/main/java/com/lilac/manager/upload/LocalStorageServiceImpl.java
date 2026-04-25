package com.lilac.manager.upload;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import com.lilac.domain.dto.file.UploadPictureResult;
import com.lilac.enums.HttpsCodeEnum;
import com.lilac.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Date;
import java.util.List;

import static com.lilac.constant.FileConstant.ALLOW_FORMAT_LIST;

/**
 * 本地存储服务实现类。
 * <p>
// * 仅作为无 OSS 环境的开源兜底方案，生产环境优先使用 {@link OssStorageServiceImpl}。
 * 本实现不支持服务端图片处理（缩略图与原图为同一 URL），且 localPath 需根据部署环境自行调整。
 */
@Deprecated
@Service("localStorageService")
public class LocalStorageServiceImpl implements FileStorageService {

    private String localPath = "xxx";

    private static final String host = "http://localhost:9090/api/uploads/";

    /**
     * 上传文件
     *
     * @param file 文件
     * @param uploadPathPrefix 文件上传路径前缀
     * @return 文件访问路径
     */
    @Override
    public UploadPictureResult uploadFile(MultipartFile file, String uploadPathPrefix) {
        if (file.isEmpty()) {
            throw new BusinessException(HttpsCodeEnum.PARAMS_ERROR, "上传文件不能为空");
        }
        String uuid = RandomUtil.randomString(16);
        String originalFilename = file.getOriginalFilename();
        String suffix = FileUtil.getSuffix(originalFilename);
        if (!ALLOW_FORMAT_LIST.contains(suffix)) {
            throw new BusinessException(HttpsCodeEnum.PARAMS_ERROR, "上传文件格式错误");
        }
        String uploadFileName = String.format("%s%s.%s", DateUtil.formatDate(new Date()), uuid, suffix);
        String relativePath = String.format("/%s/%s/%s", uploadPathPrefix, DateUtil.format(new Date(), "yyyy/MM/dd"), uploadFileName);
        String uploadPath = localPath + relativePath;
        File destFile = new File(uploadPath);
        if (!destFile.getParentFile().exists()) {
            destFile.getParentFile().mkdirs();
        }
        try {
            file.transferTo(destFile);
            String url = host + relativePath;
            // 本地存储未做缩略图处理，缩略图 URL 与原图相同
            UploadPictureResult uploadPictureResult = new UploadPictureResult();
            uploadPictureResult.setUrl(url);
            uploadPictureResult.setThumbnailUrl(url);
            return uploadPictureResult;
        } catch (Exception e) {
            throw new BusinessException(HttpsCodeEnum.SYSTEM_ERROR, "上传文件失败");
        }
    }

    /**
     * 通过 URL 上传文件
     *
     * @param url 文件 URL
     * @param uploadPathPrefix 文件上传路径前缀
     * @return 文件访问路径
     */
    @Override
    public UploadPictureResult uploadByUrl(String url, String uploadPathPrefix) {
        // 本地存储未实现 URL 拉取：开源用户如有需要可按 OSS 模板自行扩展
        throw new BusinessException(HttpsCodeEnum.SYSTEM_ERROR, "本地存储暂不支持 URL 上传，请切换到 OSS");
    }
}
