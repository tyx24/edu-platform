package com.education.platform.controller;

import com.aliyun.oss.OSS;
import com.education.platform.entity.Resource;
import com.education.platform.service.IResourceService;
import com.education.platform.util.R;
import com.education.platform.util.VideoUtils;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.UUID;


/**
 * 资源管理控制器
 * 提供课程章节资源（如视频、文档、图片等）的增删查接口
 */
@RestController
@RequestMapping("/resource")
@Tag(name = "资源管理", description = "课程章节的资源文件管理接口")
public class ResourceController {

    @Autowired
    private IResourceService resourceService;

    @Autowired
    private OSS ossClient; // 注入OSS客户端

    @Value("${aliyun.oss.bucket-name}")
    private String bucketName; // 注入Bucket名称

    /**
     * 删除资源
     */
    @DeleteMapping("/delete/{id}")
    public R<Boolean> delete(@PathVariable Long id) {
        boolean success = resourceService.removeById(id);
        return success ? R.ok(true) : R.fail("删除资源失败");
    }

    /**
     * 获取章节下的资源列表
     */
    @GetMapping("/list/{chapterId}")
    public R<List<Resource>> list(@PathVariable Long chapterId) {
        List<Resource> resources = resourceService.getResourcesByChapter(chapterId);
        return R.ok(resources);
    }

    /**
     * 上传文件并保存资源记录
     */
    @PreAuthorize("hasAnyRole('teacher','admin')")
    @PostMapping("/upload")
    public R<Resource> uploadFileAndSave(
            @RequestParam("file") MultipartFile file,
            @RequestParam("chapterId") Long chapterId,
            @RequestParam(value = "title", required = false) String title
    ) {
        try {
            if (file.isEmpty()) {
                return R.fail("文件不能为空");
            }

            // 生成存储路径
            String datePath = LocalDateTime.now().format(DateTimeFormatter.ofPattern("MM/dd"));
            // 本地目录
            /*String uploadDir = System.getProperty("user.dir") + "/upload/" + datePath;
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }*/

            // 获取文件扩展名
            String originalName = file.getOriginalFilename();  // 获取原文件名
            String ext = originalName.substring(originalName.lastIndexOf(".") + 1).toLowerCase();

            // 文件名
            String objectName = datePath + "/" + UUID.randomUUID() + "_" + originalName;

            // 保存文件到指定目录
            /*File dest = new File(dir, objectName);
            file.transferTo(dest);*/

            // 上传文件到OSS
            try (InputStream inputStream = file.getInputStream()) {
                ossClient.putObject(bucketName, objectName, inputStream);
            }
            // 文件URL
            /*String fileUrl = "/files/" + datePath + "/" + objectName;*/ // 本地URL

            // 生成文件访问URL（1年后过期）
            Date expiration = new Date(System.currentTimeMillis() + 3600 * 1000 * 24 * 365 * 1);
            String fileUrl = ossClient.generatePresignedUrl(bucketName, objectName, expiration).toString();
            System.out.println("文件访问URL: " + fileUrl);

            // 如果是公开读的Bucket，可以直接构造URL
            // String fileUrl = "https://" + bucketName + "." + endpoint + "/" + objectName;

            // 创建资源记录
            Resource resource = new Resource();
            resource.setChapterId(chapterId);
            resource.setTitle(title != null ? title : originalName);
            resource.setUrl(fileUrl);
            resource.setType(getTypeByExtension(ext));
            resource.setCreateTime(LocalDateTime.now());

            // 如果是视频文件，获取视频时长
            // 本地视频
           /* if ("video".equals(resource.getType())) {
                try {
                    long info = VideoUtils.getVideoDuration(dest);
                    resource.setDuration((int) (info / 1000));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
*/
            // 视频时长处理 - 需要先下载到临时文件
            if ("video".equals(resource.getType())) {
                File tempFile = null;
                try {
                    // 创建临时文件
                    tempFile = File.createTempFile("video", "." + ext);
                    file.transferTo(tempFile);

                    // 获取视频时长
                    long duration = VideoUtils.getVideoDuration(tempFile);
                    resource.setDuration((int) (duration / 1000));

                } catch (Exception e) {
                    e.printStackTrace();
                    return R.fail("视频时长获取失败: " + e.getMessage());
                } finally {
                    // 删除临时文件
                    if (tempFile != null && tempFile.exists()) {
                        tempFile.delete();
                    }
                }
            }

            resourceService.save(resource);
            return R.ok(resource);

        } catch (Exception e) {
            e.printStackTrace();
            return R.fail("文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 根据文件扩展名获取资源类型
     */
    private String getTypeByExtension(String ext) {
        switch (ext) {
            case "mp4":
            case "avi":
            case "mov":
            case "mkv":
            case "wmv":
                return "video";
            case "pdf":
                return "pdf";
            case "ppt":
            case "pptx":
                return "ppt";
            case "doc":
            case "docx":
                return "document";
            case "jpg":
            case "jpeg":
            case "png":
            case "gif":
                return "image";
            default:
                return "other";
        }
    }
}

