package com.classification.domain_system.service.mail;

import com.classification.domain_system.dto.MailingListRequest;
import com.classification.domain_system.dto.MailingListResponse;
import com.classification.domain_system.entity.MailingList;
import com.classification.domain_system.entity.MailingListMember;
import com.classification.domain_system.entity.User;
import com.classification.domain_system.repository.MailingListMemberRepository;
import com.classification.domain_system.repository.MailingListRepository;
import com.classification.domain_system.repository.UserRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MailingListServiceTest {

    @Mock MailingListRepository mailingListRepository;
    @Mock MailingListMemberRepository mailingListMemberRepository;
    @Mock UserRepository userRepository;
    @Mock ObjectMapper objectMapper;

    @InjectMocks MailingListService service;

    @Captor ArgumentCaptor<MailingList> listCaptor;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "postfixConfigPath", tempDir.toString());
    }

    @Test
    @DisplayName("1. 메일링 리스트 생성 - 저장 및 Postfix 동기화")
    void createMailingList_savesAndSyncsPostfix() throws Exception {
        // given
        MailingListRequest request = new MailingListRequest();
        request.setName("Developers");
        request.setEmail("dev@mplatform.com");
        request.setDescription(Map.of("ko", "개발팀"));
        request.setMemberUserIds(List.of("user1"));
        request.setMemberExternalEmails(List.of("ext@example.com"));

        when(mailingListRepository.existsByEmail("dev@mplatform.com")).thenReturn(false);
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"ko\":\"개발팀\"}");
        
        // when
        MailingListResponse res = service.createMailingList(request, "creatorId");

        // then
        verify(mailingListRepository).save(listCaptor.capture());
        MailingList saved = listCaptor.getValue();
        assertThat(saved.getName()).isEqualTo("Developers");
        assertThat(saved.getEmail()).isEqualTo("dev@mplatform.com");
        assertThat(saved.getMembers()).hasSize(2);

        // Check if postfix file was created
        File aliasFile = new File(tempDir.toFile(), "postfix-virtual.cf");
        assertThat(aliasFile.exists()).isTrue();
    }

    @Test
    @DisplayName("2. 메일링 리스트 삭제 - 비활성화 처리")
    void deleteMailingList_deactivates() {
        // given
        UUID listId = UUID.randomUUID();
        MailingList list = new MailingList();
        list.setId(listId);
        list.setIsActive(true);

        when(mailingListRepository.findById(listId)).thenReturn(Optional.of(list));

        // when
        service.deleteMailingList(listId);

        // then
        assertThat(list.getIsActive()).isFalse();
        verify(mailingListRepository).save(list);
    }

    @Test
    @DisplayName("3. 멤버 추가 - 신규 멤버 추가")
    void addMember_addsMember() {
        // given
        UUID listId = UUID.randomUUID();
        MailingList list = new MailingList();
        list.setId(listId);

        when(mailingListRepository.findById(listId)).thenReturn(Optional.of(list));

        // when
        service.addMember(listId, "user2");
        service.addMember(listId, "ext@test.com");

        // then
        verify(mailingListRepository, times(2)).save(list);
        assertThat(list.getMembers()).hasSize(2);
        assertThat(list.getMembers().get(0).getUserId()).isEqualTo("user2");
        assertThat(list.getMembers().get(1).getExternalEmail()).isEqualTo("ext@test.com");
    }

    @Test
    @DisplayName("4. 멤버 삭제 - 멤버 목록에서 제거")
    void removeMember_removesMember() {
        // given
        UUID listId = UUID.randomUUID();
        MailingList list = new MailingList();
        list.setId(listId);

        UUID memberId = UUID.randomUUID();
        MailingListMember member = new MailingListMember();
        member.setId(memberId);
        list.addMember(member);

        when(mailingListRepository.findById(listId)).thenReturn(Optional.of(list));

        // when
        service.removeMember(listId, memberId);

        // then
        assertThat(list.getMembers()).isEmpty();
        verify(mailingListRepository).save(list);
    }

    @Test
    @DisplayName("5. Postfix 별칭 동기화 - 파일 정상 생성")
    void syncPostfixAliases_generatesCorrectFile() throws Exception {
        // given
        MailingList list = new MailingList();
        list.setEmail("test@mplatform.com");
        list.setIsActive(true);

        MailingListMember member1 = new MailingListMember();
        member1.setExternalEmail("ext@test.com");

        MailingListMember member2 = new MailingListMember();
        member2.setUserId("user1");

        list.addMember(member1);
        list.addMember(member2);

        User user = new User();
        user.setEmail("user1@mplatform.com");

        when(mailingListRepository.findByIsActiveTrue()).thenReturn(List.of(list));
        when(userRepository.findById("user1")).thenReturn(Optional.of(user));

        // when
        service.syncPostfixAliases();

        // then
        File aliasFile = new File(tempDir.toFile(), "postfix-virtual.cf");
        assertThat(aliasFile.exists()).isTrue();
        
        String content = Files.readString(aliasFile.toPath());
        assertThat(content).contains("test@mplatform.com ext@test.com, user1@mplatform.com");
    }
}
