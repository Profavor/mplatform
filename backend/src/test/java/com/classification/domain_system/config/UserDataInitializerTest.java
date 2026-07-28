package com.classification.domain_system.config;

import com.classification.domain_system.entity.User;
import com.classification.domain_system.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.ApplicationArguments;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserDataInitializerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ApplicationArguments applicationArguments;

    @InjectMocks
    private UserDataInitializer userDataInitializer;

    @Test
    @DisplayName("admin 계정이 없을 때 admin 계정을 ROLE_ADMIN 권한과 암호화된 비밀번호로 생성한다")
    void run_createsAdminUserWhenNotExists() {
        // given
        given(userRepository.findByUsername("admin")).willReturn(Optional.empty());
        given(passwordEncoder.encode("Knight12!")).willReturn("encodedPassword123");

        // when
        userDataInitializer.run(applicationArguments);

        // then
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(userCaptor.capture());
        
        User savedUser = userCaptor.getValue();
        assertEquals("admin", savedUser.getUsername());
        assertEquals("encodedPassword123", savedUser.getPassword());
        assertEquals("ROLE_ADMIN", savedUser.getRole());
        assertTrue(savedUser.getIsActive());
    }

    @Test
    @DisplayName("admin 계정이 이미 존재할 때 비밀번호와 ROLE_ADMIN 권한을 업데이트한다")
    void run_updatesAdminUserWhenAlreadyExists() {
        // given
        User existingAdmin = new User();
        existingAdmin.setUsername("admin");
        existingAdmin.setPassword("oldPassword");
        existingAdmin.setRole("ROLE_USER");

        given(userRepository.findByUsername("admin")).willReturn(Optional.of(existingAdmin));
        given(passwordEncoder.encode("Knight12!")).willReturn("newEncodedPassword123");

        // when
        userDataInitializer.run(applicationArguments);

        // then
        verify(userRepository, times(1)).save(existingAdmin);
        assertEquals("newEncodedPassword123", existingAdmin.getPassword());
        assertEquals("ROLE_ADMIN", existingAdmin.getRole());
    }
}
