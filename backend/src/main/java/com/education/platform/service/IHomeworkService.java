package com.education.platform.service;

import com.education.platform.entity.Homework;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 作业表 服务类
 * </p>
 *
 * @author tan
 * @since 2025-08-03
 */
public interface IHomeworkService extends IService<Homework> {
    void createHomework(Homework homework); // 创建作业
    void updateHomework(Homework homework); // 更新作业
    void deleteHomework(Long id); // 删除作业
    List<Homework> listByCourse(Long courseId); // 根据课程ID获取作业列表
}
