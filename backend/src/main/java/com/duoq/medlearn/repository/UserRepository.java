package com.duoq.medlearn.repository;

import com.duoq.medlearn.domain.entity.User;
import com.duoq.medlearn.domain.enums.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Tìm user theo username (không load roles)
    Optional<User> findByUsername(String username);

    // Tìm user theo email (không load roles)
    Optional<User> findByEmail(String email);

    // Kiểm tra username đã tồn tại chưa
    boolean existsByUsername(String username);

    // Kiểm tra email đã tồn tại chưa
    boolean existsByEmail(String email);

    // Tìm user theo username, chỉ lấy chưa xóa
    Optional<User> findByUsernameAndIsDeletedFalse(String username);

    // Tìm user theo id, chỉ lấy chưa xóa
    Optional<User> findByIdAndIsDeletedFalse(Long id);

    // Tìm user theo id, kèm theo roles (dùng cho phân quyền)
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.roles WHERE u.id = :id AND u.isDeleted = false")
    Optional<User> findByIdWithRoles(@Param("id") Long id);

    // Tìm user theo username, kèm theo roles (dùng cho phân quyền)
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.roles WHERE u.username = :username AND u.isDeleted = false")
    Optional<User> findByUsernameWithRoles(@Param("username") String username);
}