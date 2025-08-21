package com.education.platform.controller;

import com.education.platform.entity.Question;
import com.education.platform.service.IQuestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 考试题目表 前端控制器，提供创建、更新、删除以及获取考试题目的功能
 *
 * @author tan
 * @since 2025-08-03
 */
@RestController
@RequestMapping("/question")
@Tag(name = "考试题目相关操作", description = "管理考试题目相关操作")
public class QuestionController {

    @Autowired
    private IQuestionService questionService;

    /**
     * 教师创建题目
     *
     * @param question 题目信息
     * @return 返回题目创建成功的消息
     */
    @PostMapping("/create")
    @Operation(
            summary = "创建题目",
            description = "教师通过此接口创建考试题目",
            responses = {
                    @ApiResponse(responseCode = "200", description = "题目创建成功"),
                    @ApiResponse(responseCode = "400", description = "输入数据无效")
            }
    )
    public String createQuestion(@RequestBody @Parameter(description = "题目信息") Question question) {
        questionService.createQuestion(question);
        return "题目创建成功";
    }

    /**
     * 教师更新题目
     *
     * @param id       题目ID
     * @param question 更新后的题目信息
     * @return 返回题目更新成功的消息
     */
    @PutMapping("/update/{id}")
    @Operation(
            summary = "更新题目",
            description = "教师通过此接口更新已存在的考试题目",
            responses = {
                    @ApiResponse(responseCode = "200", description = "题目更新成功"),
                    @ApiResponse(responseCode = "404", description = "题目未找到")
            }
    )
    public String updateQuestion(
            @PathVariable @Parameter(description = "题目ID") Long id,
            @RequestBody @Parameter(description = "更新后的题目信息") Question question
    ) {
        question.setId(id);
        questionService.updateQuestion(question);
        return "题目更新成功";
    }

    /**
     * 教师删除题目
     *
     * @param id 题目ID
     * @return 返回题目删除成功的消息
     */
    @DeleteMapping("/delete/{id}")
    @Operation(
            summary = "删除题目",
            description = "教师通过此接口删除指定的考试题目",
            responses = {
                    @ApiResponse(responseCode = "200", description = "题目删除成功"),
                    @ApiResponse(responseCode = "404", description = "题目未找到")
            }
    )
    public String deleteQuestion(@PathVariable @Parameter(description = "题目ID") Long id) {
        questionService.deleteQuestion(id);
        return "题目删除成功";
    }

    /**
     * 教师获取题目列表
     *
     * @param examId 考试ID
     * @return 返回指定考试的题目列表
     */
    @GetMapping("/list/{examId}")
    @Operation(
            summary = "获取题目列表",
            description = "教师通过此接口获取指定考试的题目列表",
            responses = {
                    @ApiResponse(responseCode = "200", description = "成功获取题目列表"),
                    @ApiResponse(responseCode = "404", description = "没有找到题目")
            }
    )
    public List<Question> getQuestions(@PathVariable @Parameter(description = "考试ID") Long examId) {
        return questionService.getQuestionsByExamId(examId);
    }

    /**
     * 学生获取考试题目（不带答案）
     *
     * @param examId 考试ID
     * @return 返回学生可以回答的考试题目列表
     */
    @GetMapping("/exam/{examId}")
    @Operation(
            summary = "获取学生考试题目",
            description = "学生通过此接口获取指定考试的题目（不带答案）",
            responses = {
                    @ApiResponse(responseCode = "200", description = "成功获取题目"),
                    @ApiResponse(responseCode = "404", description = "没有找到该考试题目")
            }
    )
    public List<Question> getQuestionsForStudent(@PathVariable @Parameter(description = "考试ID") Long examId) {
        return questionService.getQuestionsForStudent(examId);
    }
}
