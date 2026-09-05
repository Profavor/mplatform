package com.classification.domain_system.service.mail;

import com.classification.domain_system.dto.MailAccountResponse;
import com.classification.domain_system.entity.User;
import com.classification.domain_system.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MailAccountServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private MailAccountService mailAccountService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(mailAccountService, "configPath", tempDir.toString());
        ReflectionTestUtils.setField(mailAccountService, "mailDomain", "mplatform.com");
    }

    @Test
    @DisplayName("계정 목록 조회 시 DB에 매핑되는 사용자가 있으면 해당 사용자명이 반환된다")
    void listAccounts_withMatchedUser() throws IOException {
        Path accountsFile = tempDir.resolve("postfix-accounts.cf");
        Files.write(accountsFile, List.of("superadmin@mplatform.com|{SHA512-CRYPT}hash123"));

        User user = new User();
        user.setId("user-superadmin-uuid");
        user.setUsername("superadmin");
        user.setEmail("superadmin@mplatform.com");

        when(userRepository.findByEmail("superadmin@mplatform.com")).thenReturn(Optional.of(user));

        List<MailAccountResponse> accounts = mailAccountService.listAccounts();

        assertThat(accounts).hasSize(1);
        assertThat(accounts.get(0).getEmail()).isEqualTo("superadmin@mplatform.com");
        assertThat(accounts.get(0).getUserName()).isEqualTo("superadmin");
        assertThat(accounts.get(0).getUserId()).isEqualTo("user-superadmin-uuid");
    }

    @Test
    @DisplayName("DB에 사용자가 없는 계정(admin@mplatform.com 등)은 이메일 prefix로 사용자명이 fallback된다")
    void listAccounts_withUnmatchedUser_fallbackToUsername() throws IOException {
        Path accountsFile = tempDir.resolve("postfix-accounts.cf");
        Files.write(accountsFile, List.of("admin@mplatform.com|{SHA512-CRYPT}hash456"));

        when(userRepository.findByEmail("admin@mplatform.com")).thenReturn(Optional.empty());
        when(userRepository.findByUsername("admin")).thenReturn(Optional.empty());

        List<MailAccountResponse> accounts = mailAccountService.listAccounts();

        assertThat(accounts).hasSize(1);
        assertThat(accounts.get(0).getEmail()).isEqualTo("admin@mplatform.com");
        assertThat(accounts.get(0).getUserName()).isEqualTo("admin");
        assertThat(accounts.get(0).getUserId()).isNull();
    }
}
