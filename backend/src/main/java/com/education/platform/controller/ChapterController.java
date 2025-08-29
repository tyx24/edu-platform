package com.education.platform.controller;

import com.education.platform.entity.Chapter;
import com.education.platform.service.IChapterService;
import com.education.platform.util.R;
import io.swagger.v3.oas.annotations.Operation;
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
    public R<Chapter> create(@RequestBody Chapter chapter) {
        try {
            saveChapterRecursive(chapter, null);
            return R.ok(chapter);
        } catch (Exception e) {
            return R.fail("创建章节失败: " + e.getMessage());
        }
    }

    private void saveChapterRecursive(Chapter chapter, Long parentId) {
        if (chapter.getParentId() == null || chapter.getParentId() == 0) {
            chapter.setParentId(parentId == null ? 0L : parentId);
        }
        chapterService.save(chapter);

        if (chapter.getChildren() != null && !chapter.getChildren().isEmpty()) {
            for (Chapter child : chapter.getChildren()) {
                child.setCourseId(chapter.getCourseId());
                saveChapterRecursive(child, chapter.getId());
            }
        }
    }

    /**
     * 更新章节
     */
    @PutMapping("/update/{id}")
    public R<Boolean> update(@PathVariable Long id, @RequestBody Chapter chapter) {
        chapter.setId(id);
        boolean success = chapterService.updateById(chapter);
        return success ? R.ok(true) : R.fail("章节更新失败");
    }

    /**
     * 删除章节
     */
    @DeleteMapping("/delete/{id}")
    public R<Boolean> delete(@PathVariable Long id) {
        boolean success = chapterService.removeById(id);
        return success ? R.ok(true) : R.fail("章节删除失败");
    }

    /**
     * 获取章节树
     */
    @GetMapping("/tree/{courseId}")
    public R<List<Chapter>> getTree(@PathVariable Long courseId) {
        List<Chapter> chapters = chapterService.getChapterTree(courseId);
        return R.ok(chapters);
    }

    /**
     * 获取章节详情
     */
    @GetMapping("/detail/{id}")
    public R<Chapter> getDetail(@PathVariable Long id) {
        Chapter chapter = chapterService.getById(id);
        if (chapter == null) {
            return R.fail("未找到该章节");
        }
        return R.ok(chapter);
    }
}


