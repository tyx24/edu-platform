package com.education.platform.controller;

import com.education.platform.entity.Chapter;
import com.education.platform.service.IChapterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 教学章节管理控制器
 * 提供章节的增删改查接口
 *
 * @author tan
 * @since 2025-08-03
 */
@RestController
@RequestMapping("/chapter")
@Tag(name = "章节管理", description = "课程章节的增删改查及章节树获取接口")
public class ChapterController {

    @Autowired
    private IChapterService chapterService;

    /**
     * 创建章节
     */
    @Operation(summary = "创建章节", description = "教师或管理员创建新的课程章节")
    @ApiResponse(responseCode = "200", description = "章节创建成功")
    @PreAuthorize("hasAnyRole('teacher','admin')")
    @PostMapping("/create")
    public Chapter create(
            @Parameter(description = "章节信息", required = true)
            @RequestBody Chapter chapter) {
        saveChapterRecursive(chapter, null);
        return chapter;
    }

    /**
     * 递归保存章节及其子章节
     */
    private void saveChapterRecursive(Chapter chapter, Long parentId) {
        // 设置父ID
        chapter.setParentId(parentId == null ? 0L : parentId);

        // 保存父章节
        chapterService.save(chapter);

        // 保存子章节
        if (chapter.getChildren() != null && !chapter.getChildren().isEmpty()) {
            for (Chapter child : chapter.getChildren()) {
                child.setCourseId(chapter.getCourseId()); // 子章节继承课程ID
                saveChapterRecursive(child, chapter.getId()); // 递归保存
            }
        }
    }

    /**
     * 更新章节
     */
    @Operation(summary = "更新章节", description = "教师或管理员修改已有课程章节")
    @ApiResponse(responseCode = "200", description = "章节更新成功")
    @PreAuthorize("hasAnyRole('teacher','admin')")
    @PutMapping("/update/{id}")
    public boolean update(
            @Parameter(description = "章节ID", example = "1") @PathVariable Long id,
            @Parameter(description = "新的章节信息") @RequestBody Chapter chapter) {
        chapter.setId(id);
        return chapterService.updateById(chapter);
    }

    /**
     * 删除章节
     */
    @Operation(summary = "删除章节", description = "教师或管理员删除课程章节")
    @ApiResponse(responseCode = "200", description = "章节删除成功")
    @PreAuthorize("hasAnyRole('teacher','admin')")
    @DeleteMapping("/delete/{id}")
    public boolean delete(
            @Parameter(description = "章节ID", example = "1") @PathVariable Long id) {
        return chapterService.removeById(id);
    }

    /**
     * 获取章节树
     */
    @Operation(summary = "获取章节树", description = "根据课程ID获取章节树结构")
    @ApiResponse(responseCode = "200", description = "获取成功")
    @GetMapping("/tree/{courseId}")
    public List<Chapter> getTree(
            @Parameter(description = "课程ID", example = "1") @PathVariable Long courseId) {
        return chapterService.getChapterTree(courseId);
    }
}
