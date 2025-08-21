package com.education.platform.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.education.platform.entity.Course;
import com.education.platform.entity.Homework;
import com.education.platform.entity.Submission;
import com.education.platform.mapper.CourseMapper;
import com.education.platform.mapper.HomeworkMapper;
import com.education.platform.mapper.SubmissionMapper;
import com.education.platform.service.ISubmissionService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * <p>
 * 作业提交表 服务实现类
 * </p>
 *
 * @author tan
 * @since 2025-08-03
 */
@Service
public class SubmissionServiceImpl extends ServiceImpl<SubmissionMapper, Submission> implements ISubmissionService {

    @Autowired
    private SubmissionMapper submissionMapper;
    @Autowired
    private HomeworkMapper homeworkMapper;
    @Autowired
    private CourseMapper courseMapper;

    @Override
    public void submitHomework(Submission submission) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long Id = Long.parseLong(auth.getName());
        submission.setStudentId(Id);
        submission.setSubmitTime(LocalDateTime.now());
        submissionMapper.insert(submission);
    }

    @Override
    public void gradeSubmission(Long submissionId, Integer score, String comment) {
        Submission sub = submissionMapper.selectById(submissionId);
        if (sub == null) throw new RuntimeException("提交记录不存在");
        Homework hw = homeworkMapper.selectById(sub.getHomeworkId());
        if (hw == null) throw new RuntimeException("作业不存在");
        Course course = courseMapper.selectById(hw.getCourseId());
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long Id = Long.parseLong(auth.getName());
        if (!course.getTeacherId().equals(Id)) {
            throw new AccessDeniedException("无权批改该作业");
        }
        sub.setScore(score);
        sub.setComment(comment);
        submissionMapper.updateById(sub);
    }

    @Override
    public List<Submission> listByHomework(Long homeworkId) {
        Homework hw = homeworkMapper.selectById(homeworkId);
        Course course = courseMapper.selectById(hw.getCourseId());
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long Id = Long.parseLong(auth.getName());
        if (!course.getTeacherId().equals(Id)) {
            throw new AccessDeniedException("无权查看提交记录");
        }
        return submissionMapper.selectList(Wrappers.<Submission>lambdaQuery()
                .eq(Submission::getHomeworkId, homeworkId));
    }

    @Override
    public List<Submission> listMySubmissions(Long studentId) {
        return submissionMapper.selectList(Wrappers.<Submission>lambdaQuery()
                .eq(Submission::getStudentId, studentId));
    }
}
