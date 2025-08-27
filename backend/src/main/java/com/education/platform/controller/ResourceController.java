package com.education.platform.controller;

import com.education.platform.entity.Resource;
import com.education.platform.service.IResourceService;
import com.education.platform.util.R;
import com.education.platform.util.VideoUtils;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;


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

            String datePath = LocalDateTime.now().format(DateTimeFormatter.ofPattern("MM/dd"));
            String uploadDir = System.getProperty("user.dir") + "/upload/" + datePath;
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String originalName = file.getOriginalFilename();
            String ext = originalName.substring(originalName.lastIndexOf(".") + 1).toLowerCase();

            File dest = new File(dir, originalName);
            file.transferTo(dest);

            String fileUrl = "/files/" + datePath + "/" + originalName;

            Resource resource = new Resource();
            resource.setChapterId(chapterId);
            resource.setTitle(originalName);
            resource.setUrl(fileUrl);
            resource.setType(getTypeByExtension(ext));
            resource.setCreateTime(LocalDateTime.now());

            if ("video".equals(resource.getType())) {
                try {
                    long info = VideoUtils.getVideoDuration(dest);
                    resource.setDuration((int) (info / 1000));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            resourceService.save(resource);
            return R.ok(resource);

        } catch (Exception e) {
            return R.fail("文件上传失败: " + e.getMessage());
        }
    }

    private String getTypeByExtension(String ext) {
        switch (ext) {
            case "mp4":
            case "avi":
            case "mov":
                return "video";
            case "pdf":
                return "pdf";
            case "ppt":
            case "pptx":
                return "ppt";
            default:
                return "other";
        }
    }
}

