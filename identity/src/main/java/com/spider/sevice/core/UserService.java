package com.spider.sevice.core;

import com.spider.common.request.filter.RecordFilter;
import com.spider.common.request.identity.UserCreateRequest;
import com.spider.common.request.identity.UserUpdateRequest;
import com.spider.common.response.identity.UserDTO;
import com.spider.common.response.identity.UserSummaryDTO;
import com.spider.common.service.CommonService;
import com.spider.enity.core.User;
import org.springframework.data.domain.Page;

import java.util.List;

public interface UserService extends CommonService<User,Long> {

    Page<UserSummaryDTO> getAllUsers(RecordFilter filter);

    UserDTO buildUserDetails(Long userId, boolean b, boolean b1);

    UserDTO createUser(UserCreateRequest request);

    UserDTO updateUser(String userUuid, UserUpdateRequest request);

    void deleteUser(String userUuid);

    UserDTO getUserByEmail(String email);

    List<UserDTO> getUsersByEmails(List<String> emails);

    UserDTO updateUserEmail(Long userId, String email);

    void resetPassword(Long userId, String newPassword);

    List<UserDTO> createUsers(List<UserCreateRequest> requests);

    void deleteUsers(List<Long> userIds);

    Page<UserSummaryDTO> getOrganizationUsers(String organizationUuid, RecordFilter filter);

    UserDTO getById(Long userId);

    UserDTO getVisibleByUuid(String userUuid);

}
