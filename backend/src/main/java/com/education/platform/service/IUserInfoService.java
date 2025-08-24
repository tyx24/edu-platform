package com.education.platform.service;

import com.education.platform.dto.BasicInfoDTO;
import com.education.platform.dto.PageRequest;
import com.education.platform.dto.PageResult;
import com.education.platform.entity.UserInfo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.education.platform.util.R;

/**
 * <p>
 * 用户信息表 服务类
 * </p>
 *
 * @author tan
 * @since 2025-08-22
 */
public interface IUserInfoService extends IService<UserInfo> {

    PageResult<UserInfo> getUserList(PageRequest request);

    UserInfo getByUserId(Long userId);

    R<Object> updateBasicInfo(BasicInfoDTO request); // 更新基本信息


    R<Object> updateStudentInfo(UserInfo request);

    R<Object> updateTeacherInfo(UserInfo request);

    R<Object> updateBio(UserInfo request);

    R<Object> updateContactInfo(UserInfo request);
}
