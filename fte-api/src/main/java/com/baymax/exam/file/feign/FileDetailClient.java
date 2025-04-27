package com.baymax.exam.file.feign;

import com.baymax.exam.common.core.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author ：MouseBaby678
 * @date ：Created in 2025/4/27 16:51
 * @description：
 * @modified By：
 * @version:
 */
@FeignClient(value = "exam-file",contextId = "FileDetailClient",path = "/files")
public interface FileDetailClient {
    @RequestMapping(value = "/upload-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE,method = {RequestMethod.POST},produces = {MediaType.APPLICATION_JSON_UTF8_VALUE},headers = "content-type=multipart/form-data")
    public Result<String> uploadImage(@RequestPart("file") MultipartFile file,
                              @RequestParam(required = false) String path,
                              @RequestParam(required = false) String id,
                              @RequestParam(required = false) String type
    );
}
