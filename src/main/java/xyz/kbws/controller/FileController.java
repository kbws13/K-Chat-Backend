package xyz.kbws.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import xyz.kbws.common.BaseResponse;
import xyz.kbws.common.ErrorCode;
import xyz.kbws.common.ResultUtils;
import xyz.kbws.config.AppConfig;
import xyz.kbws.exception.BusinessException;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;

/**
 * @author kbws
 * @date 2024/5/4
 * @description:
 */
@Api(tags = "文件接口")
@Slf4j
@RequestMapping("/file")
@RestController
public class FileController {

    @Resource
    private AppConfig appConfig;

    @ApiOperation(value = "上传文件")
    @PostMapping("/upload")
    public BaseResponse<String> uploadAudio(@RequestPart("file") MultipartFile multipartFile) {
        if (multipartFile == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "上传文件不能为空");
        }
        File file = new File(appConfig.getProjectFolder());
        if (!file.exists()) {
            file.mkdirs();
        }
        String fileName = multipartFile.getOriginalFilename();
        File readFile = new File(new File(appConfig.getProjectFolder()).getAbsoluteFile() + fileName);
        // 保存图片到本地
        try {
            multipartFile.transferTo(readFile);
            return ResultUtils.success(appConfig.getBaseUrl() + fileName);
        } catch (IOException e) {
            log.error("上传文件失败");
            throw new BusinessException(ErrorCode.OPERATION_ERROR, e.getMessage());
        }
    }
}
