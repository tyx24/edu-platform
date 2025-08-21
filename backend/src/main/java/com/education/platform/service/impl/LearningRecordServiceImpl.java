package com.education.platform.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.education.platform.entity.Chapter;
import com.education.platform.entity.LearningRecord;
import com.education.platform.entity.Resource;
import com.education.platform.mapper.ChapterMapper;
import com.education.platform.mapper.LearningRecordMapper;
import com.education.platform.mapper.ResourceMapper;
import com.education.platform.service.ILearningRecordService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 学习进度记录表 服务实现类
 * </p>
 *
 * @author tan
 * @since 2025-08-10
 */
@Service
public class LearningRecordServiceImpl extends ServiceImpl<LearningRecordMapper, LearningRecord> implements ILearningRecordService {

    @Autowired
    private LearningRecordMapper learningRecordMapper;

    @Autowired
    private ChapterMapper chapterMapper;

    @Autowired
    private ResourceMapper resourceMapper;

    @Override
    @Transactional
    public void reportProgress(Long studentId, Long resourceId, Integer currentTime, Integer duration, Boolean isCompleted) {
        LearningRecord record = lambdaQuery()
                .eq(LearningRecord::getStudentId, studentId)
                .eq(LearningRecord::getResourceId, resourceId)
                .one();

        if (record == null) {
            // 新增
            record = new LearningRecord();
            record.setStudentId(studentId);
            record.setResourceId(resourceId);
            record.setCurrentTime(currentTime);
            record.setDuration(duration);
            record.setCompleted(isCompleted);
            record.setUpdateTime(LocalDateTime.now());
            System.out.println(LocalDateTime.now());
            System.out.println(record);
            save(record);
        } else {
            // 更新进度（只往前推进，不回退）
            if (currentTime > record.getCurrentTime()) {
                record.setCurrentTime(currentTime);
            }
            record.setDuration(duration);
            if (isCompleted) {
                record.setCompleted(true);
            }
            record.setUpdateTime(LocalDateTime.now());
            updateById(record);
        }
    }

    @Override
    public double getCourseProgress(Long studentId, Long courseId) {
        // 1. 获取该课程所有章节 ID
        List<Long> chapterIds = chapterMapper.selectList(
                Wrappers.<Chapter>lambdaQuery().eq(Chapter::getCourseId, courseId)
        ).stream().map(Chapter::getId).collect(Collectors.toList());

        if (chapterIds.isEmpty()) {
            return 0.0;
        }

        // 2. 获取这些章节下的所有资源 ID
        List<Long> resourceIds = resourceMapper.selectList(
                Wrappers.<Resource>lambdaQuery().in(Resource::getChapterId, chapterIds)
        ).stream().map(Resource::getId).collect(Collectors.toList());

        if (resourceIds.isEmpty()) {
            return 0.0;
        }

        // 3. 查询学习记录
        List<LearningRecord> records = learningRecordMapper.selectList(
                Wrappers.<LearningRecord>lambdaQuery()
                        .eq(LearningRecord::getStudentId, studentId)
                        .in(LearningRecord::getResourceId, resourceIds)
        );

        if (records.isEmpty()) {
            return 0.0;
        }

        // 4. 计算已完成数量
        long completed = records.stream().filter(LearningRecord::getCompleted).count();
        return (completed * 100.0) / resourceIds.size(); // 用资源总数做分母
    }
}
