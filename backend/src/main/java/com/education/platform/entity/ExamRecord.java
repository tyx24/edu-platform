package com.education.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * <p>
 * 考试记录表
 * </p>
 *
 * @author tan
 * @since 2025-08-03
 */
@Data
@TableName("exam_record")
public class ExamRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 记录ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 考试ID
     */
    private Long examId;

    /**
     * 学生ID
     */
    private Long studentId;

    /**
     * 答案 JSON
     */
    private String answers;

    /**
     * 总分
     */
    private Integer totalScore;

    /**
     * 切屏警告次数
     */
    private Integer warningCount;

    /**
     * 提交时间
     */
    private LocalDateTime submitTime;
}
