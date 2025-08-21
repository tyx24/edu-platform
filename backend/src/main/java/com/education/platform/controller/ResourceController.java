package com.education.platform.controller;

import com.education.platform.entity.Resource;
import com.education.platform.service.IResourceService;
import com.education.platform.util.VideoUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ws.schild.jave.EncoderException;

import java.io.File;
import java.io.IOException;
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

//    /**
//     * 创建资源
//     */
//    @Operation(summary = "创建资源", description = "教师或管理员为章节添加资源（视频、文档、图片等）")
//    @ApiResponse(responseCode = "200", description = "资源创建成功")
//    @PreAuthorize("hasAnyRole('teacher','admin')")
//    @PostMapping("/create")
//    public Resource create(
//            @Parameter(description = "资源信息", required = true) @RequestBody Resource resource) {
//        resourceService.save(resource);
//        return resource;
//    }

    /**
     * 删除资源
     */
    @Operation(summary = "删除资源", description = "教师或管理员删除章节资源")
    @ApiResponse(responseCode = "200", description = "资源删除成功")
    @PreAuthorize("hasAnyRole('teacher','admin')")
    @DeleteMapping("/delete/{id}")
    public boolean delete(
            @Parameter(description = "资源ID", example = "1") @PathVariable Long id) {
        return resourceService.removeById(id);
    }

    /**
     * 获取章节下的资源列表
     */
    @Operation(summary = "获取章节资源列表", description = "根据章节ID获取所有资源（学生和教师均可访问）")
    @ApiResponse(responseCode = "200", description = "获取成功")
    @GetMapping("/list/{chapterId}")
    public List<Resource> list(
            @Parameter(description = "章节ID", example = "1") @PathVariable Long chapterId) {
        return resourceService.getResourcesByChapter(chapterId);
    }

    //    /**
//     * 上传文件到本地 OSS 存储
//     */
//    @Operation(summary = "上传资源文件", description = "教师或管理员上传视频/图片/文档到本地 OSS 存储，并返回可访问 URL")
//    @ApiResponse(responseCode = "200", description = "文件上传成功")
//    @PreAuthorize("hasAnyRole('teacher','admin')")
//    @PostMapping("/upload")
//    public String uploadFile(
//            @Parameter(description = "要上传的文件", required = true) @RequestParam("file") MultipartFile file
//    ) throws IOException {
//        if (file.isEmpty()) {
//            throw new RuntimeException("文件不能为空");
//        }
//
//        // 按日期生成目录
//        String datePath = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
//        String uploadDir = System.getProperty("user.dir") + "/upload/" + datePath;
//        File dir = new File(uploadDir);
//        if (!dir.exists()) {
//            dir.mkdirs();
//        }
//
//        // 生成唯一文件名
//        String originalName = file.getOriginalFilename();
//        String suffix = originalName != null && originalName.contains(".")
//                ? originalName.substring(originalName.lastIndexOf("."))
//                : "";
//        String fileName = UUID.randomUUID() + suffix;
//
//        // 保存文件
//        File dest = new File(dir, fileName);
//        file.transferTo(dest);
//
//        // 返回可访问的 URL（假设配置了静态资源映射 /files/** → /upload/）
//        String fileUrl = "/files/" + datePath + "/" + fileName;
//        return fileUrl;
//    }
    @PreAuthorize("hasAnyRole('teacher','admin')")
    @PostMapping("/upload")
    @Operation(summary = "上传文件并保存资源记录", description = "教师或管理员上传视频/图片/文档到本地 OSS 存储，自动保存到数据库并返回资源信息")
    public Resource uploadFileAndSave(
            @RequestParam("file") MultipartFile file,
            @RequestParam("chapterId") Long chapterId,
            @RequestParam(value = "title", required = false) String title
    ) throws IOException, EncoderException {
        if (file.isEmpty()) {
            throw new RuntimeException("文件不能为空");
        }

        // 1. 按日期生成目录
        String datePath = LocalDateTime.now().format(DateTimeFormatter.ofPattern("MM/dd"));
        String uploadDir = System.getProperty("user.dir") + "/upload/" + datePath;
        File dir = new File(uploadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // 2. 生成文件名
        String originalName = file.getOriginalFilename();
        String suffix = originalName != null && originalName.contains(".")
                ? originalName.substring(originalName.lastIndexOf("."))
                : "";
        String ext = originalName.substring(originalName.lastIndexOf(".") + 1).toLowerCase();

        // 3. 保存文件
        File dest = new File(dir, originalName);
        System.out.println(dest);
        file.transferTo(dest);

        // 4. 构建可访问 URL（需配合 WebMvcConfigurer 静态映射）
        String fileUrl = "/files/" + datePath + "/" + originalName;

        // 5. 打印日志
        System.out.println("文件上传成功，URL: " + fileUrl);
        System.out.println("文件保存路径: " + dest.getAbsolutePath());
        System.out.println("文件大小: " + file.getSize() + " bytes");
        System.out.println("文件类型: " + file.getContentType());
        System.out.println("文件名: " + originalName);
        System.out.println("章节ID: " + chapterId);
        System.out.println("当前时间: " + LocalDateTime.now());

        // 6. 保存资源记录到数据库
        Resource resource = new Resource();
        resource.setChapterId(chapterId);
        resource.setTitle(originalName);
        resource.setUrl(fileUrl);
        resource.setType(getTypeByExtension(ext)); // 文件类型
        resource.setCreateTime(LocalDateTime.now());


        // 7. 如果是视频文件，获取视频时长
        if ("video".equals(resource.getType())) {
            try {
                long info = VideoUtils.getVideoDuration(new File(dest.getAbsolutePath()));
                resource.setDuration((int) (info / 1000)); // 毫秒转秒
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // 8. 保存资源记录
        resourceService.save(resource);

        return resource;
    }

    /**
     * 根据扩展名判断资源类型
     */
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
