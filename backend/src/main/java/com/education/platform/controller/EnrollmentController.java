package com.education.platform.controller;

import com.education.platform.entity.Course;
import com.education.platform.entity.Enrollment;
import com.education.platform.service.IEnrollmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 选课管理接口
 * </p>
 * 提供学生选课、进度更新、教师查询选课情况、管理员导出选课名单等功能
 *
 * @author tan
 * @since 2025-08-03
 */
@Tag(name = "选课管理", description = "学生选课、进度管理、教师查询、管理员导出功能接口")
@RestController
@RequestMapping("/enrollment")
public class EnrollmentController {

    @Autowired
    private IEnrollmentService enrollmentService;

    @Operation(
            summary = "学生选课",
            description = "学生根据课程ID进行选课操作",
            responses = {
                    @ApiResponse(responseCode = "200", description = "选课成功"),
                    @ApiResponse(responseCode = "403", description = "无权限，仅学生可选课")
            }
    )
    @PostMapping("/{courseId}")
    @PreAuthorize("hasRole('student')")
    public String enroll(
            @Parameter(description = "课程ID", example = "1") @PathVariable Long courseId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long studentId = Long.parseLong(auth.getName());
        enrollmentService.enroll(courseId, studentId);
        return "选课成功";
    }

    @Operation(
            summary = "学生退课",
            description = "学生可退掉自己已选的课程",
            responses = {
                    @ApiResponse(responseCode = "200", description = "退课成功"),
                    @ApiResponse(responseCode = "403", description = "只有学生可以退课")
            }
    )
    @DeleteMapping("/{courseId}")
    @PreAuthorize("hasRole('student')")
    public String dropCourse(
            @Parameter(description = "课程ID", example = "1") @PathVariable Long courseId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long studentId = Long.parseLong(auth.getName());
        enrollmentService.dropCourse(courseId, studentId);
        return "退课成功";
    }


    @Operation(
            summary = "学生查看已选课程",
            description = "学生可查看自己已选的课程列表",
            responses = {
                    @ApiResponse(responseCode = "200", description = "查询成功", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Course.class))),
                    @ApiResponse(responseCode = "403", description = "只有学生可以查看")
            }
    )
    @GetMapping("/my-courses")
    @PreAuthorize("hasRole('student')")
    public List<Course> getMyCourses() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long studentId = Long.parseLong(auth.getName());

        return enrollmentService.getCoursesBystudentId(studentId);
    }

    @Operation(
            summary = "获取选课记录",
            description = "可查看自己的选课记录",
            responses = {
                    @ApiResponse(responseCode = "200", description = "查询成功", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Course.class))),
                    @ApiResponse(responseCode = "403", description = "只有学生可以查看")
            }
    )
    @GetMapping("/my-enrollments")
    public List<Enrollment> getEnrollments(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long studentId = Long.parseLong(auth.getName());

        return enrollmentService.getEnrollments(studentId);
    }

    //    @Operation(
//            summary = "学生更新学习进度",
//            description = "学生可更新自己在某门课程的学习进度",
//            responses = {
//                    @ApiResponse(responseCode = "200", description = "更新成功"),
//                    @ApiResponse(responseCode = "403", description = "只有学生可以更新进度")
//            }
//    )
//    @PutMapping("/progress")
//    @PreAuthorize("hasRole('student')")
//    public void updateProgress(
//            @Parameter(description = "课程ID", example = "1") @RequestParam Long courseId,
//            @Parameter(description = "学习进度（0-100）", example = "50") @RequestParam Byte progress) {
//        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//        Long studentId = Long.parseLong(auth.getName());
//
//        enrollmentService.updateProgress(studentId, courseId, progress);
//    }
    @Operation(
            summary = "更新学习进度",
            description = "更新自己在某门课程的学习进度",
            responses = {
                    @ApiResponse(responseCode = "200", description = "更新成功"),
                    @ApiResponse(responseCode = "403", description = "更新失败")
            }
    )
    @PutMapping("/progress")
    @PreAuthorize("hasAnyRole('student', 'teacher','admin')")
    public void updateProgress(@Parameter(description = "课程ID", example = "1") @RequestParam Long courseId,
                               @Parameter(description = "学生ID", example = "1") @RequestParam Long studentId) {
        enrollmentService.updateCourseProgress(studentId, courseId);
    }

    @Operation(
            summary = "管理员导出选课名单（Excel）",
            description = "管理员可导出指定课程的选课学生名单（包含进度），以 Excel 文件形式返回",
            responses = {
                    @ApiResponse(responseCode = "200", description = "导出成功，返回 Excel 文件", content = @Content(mediaType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")),
                    @ApiResponse(responseCode = "403", description = "只有管理员可以导出")
            }
    )
    @GetMapping("/export-enrollments")
    @PreAuthorize("hasRole('admin')")
    public void exportEnrollments(
            @Parameter(description = "课程ID", example = "1") @RequestParam Long courseId,
            HttpServletResponse response) throws IOException {

        List<Map<String, Object>> list = enrollmentService.getEnrollmentsForExport(courseId);

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("选课名单");

        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("学生ID");
        header.createCell(1).setCellValue("学生用户名");
        header.createCell(2).setCellValue("邮箱");
        header.createCell(3).setCellValue("进度");

        for (int i = 0; i < list.size(); i++) {
            Map<String, Object> item = list.get(i);
            Row row = sheet.createRow(i + 1);
            row.createCell(0).setCellValue(item.get("id").toString());
            row.createCell(1).setCellValue((String) item.get("username"));
            row.createCell(2).setCellValue((String) item.get("email"));
            row.createCell(3).setCellValue(item.get("progress").toString());
        }

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=enrollments.xlsx");
        workbook.write(response.getOutputStream());
        workbook.close();
    }

    @Operation(
            summary = "教师查看自己课程及学生名单",
            description = "教师可查看自己所开课程及学生选课名单和学习进度",
            responses = {
                    @ApiResponse(responseCode = "200", description = "查询成功"),
                    @ApiResponse(responseCode = "403", description = "只有教师可以查看")
            }
    )
    @GetMapping("/my-courses-with-students")
    @PreAuthorize("hasRole('teacher')")
    public List<Map<String, Object>> getMyCoursesWithstudents() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long teacherId = Long.parseLong(auth.getName());

        return enrollmentService.getMyCoursesWithstudents(teacherId);
    }
}
