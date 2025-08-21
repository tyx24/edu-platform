package com.education.platform.service;

import com.education.platform.entity.LearningRecord;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 学习进度记录表 服务类
 * </p>
 *
 * @author tan
 * @since 2025-08-10
 */
public interface ILearningRecordService extends IService<LearningRecord> {

    // 提交学习进度
    void reportProgress(Long studentId, Long resourceId, Integer currentTime, Integer duration, Boolean isCompleted);


    // 获取课程学习进度
    double getCourseProgress(Long studentId, Long courseId);

}
