package com.classification.domain_system.controller;

import com.classification.domain_system.dto.CodeDetailRequest;
import com.classification.domain_system.dto.CodeDetailResponse;
import com.classification.domain_system.dto.CodeExportDto;
import com.classification.domain_system.dto.CodeGroupRequest;
import com.classification.domain_system.dto.CodeGroupResponse;
import com.classification.domain_system.entity.CodeDetail;
import com.classification.domain_system.entity.CodeGroup;
import com.classification.domain_system.service.CodeManagementService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CodeManagementControllerTest {

    @Mock
    private CodeManagementService codeManagementService;

    @InjectMocks
    private CodeManagementController codeManagementController;

    private CodeGroup createMockGroup(String groupCode) {
        CodeGroup group = new CodeGroup();
        group.setId(UUID.randomUUID());
        group.setGroupCode(groupCode);
        group.setName(Map.of("ko", "테스트그룹", "en", "TestGroup"));
        group.setIsActive(true);
        group.setCreatedAt(LocalDateTime.now());
        return group;
    }

    private CodeDetail createMockDetail(CodeGroup group, String detailCode) {
        CodeDetail detail = new CodeDetail();
        detail.setId(UUID.randomUUID());
        detail.setCodeGroup(group);
        detail.setDetailCode(detailCode);
        detail.setName(Map.of("ko", "테스트상세", "en", "TestDetail"));
        detail.setSortOrder(1);
        detail.setIsActive(true);
        detail.setCreatedAt(LocalDateTime.now());
        return detail;
    }

    @Test
    @DisplayName("코드 그룹 생성 성공")
    void testCreateGroup_Success() {
        CodeGroupRequest request = new CodeGroupRequest();
        request.setGroupCode("GRP_01");
        request.setName(Map.of("ko", "그룹1"));

        CodeGroup created = createMockGroup("GRP_01");
        when(codeManagementService.createGroup(any(CodeGroupRequest.class))).thenReturn(created);

        ResponseEntity<CodeGroupResponse> response = codeManagementController.createGroup(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("GRP_01", response.getBody().getGroupCode());
    }

    @Test
    @DisplayName("전체 코드 그룹 목록 조회 성공")
    void testGetGroups_Success() {
        CodeGroup group = createMockGroup("GRP_01");
        when(codeManagementService.getGroups()).thenReturn(List.of(group));

        ResponseEntity<List<CodeGroupResponse>> response = codeManagementController.getGroups();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("GRP_01", response.getBody().get(0).getGroupCode());
    }

    @Test
    @DisplayName("코드 그룹 페이징 조회 성공")
    void testGetGroupsPaged_Success() {
        CodeGroup group = createMockGroup("GRP_01");
        Page<CodeGroup> page = new PageImpl<>(List.of(group), PageRequest.of(0, 10), 1);
        when(codeManagementService.getGroupsPaged(eq("GRP"), any(Pageable.class))).thenReturn(page);

        ResponseEntity<Page<CodeGroupResponse>> response = codeManagementController.getGroupsPaged("GRP", 0, 10, "groupCode,asc");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getTotalElements());
    }

    @Test
    @DisplayName("그룹 코드로 단건 조회 성공")
    void testGetGroupByCode_Success() {
        CodeGroup group = createMockGroup("GRP_01");
        when(codeManagementService.getGroupByCode("GRP_01")).thenReturn(group);

        ResponseEntity<CodeGroupResponse> response = codeManagementController.getGroupByCode("GRP_01");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("GRP_01", response.getBody().getGroupCode());
    }

    @Test
    @DisplayName("코드 그룹 수정 성공")
    void testUpdateGroup_Success() {
        UUID groupId = UUID.randomUUID();
        CodeGroupRequest request = new CodeGroupRequest();
        request.setName(Map.of("ko", "수정된 그룹"));

        CodeGroup updated = createMockGroup("GRP_01");
        when(codeManagementService.updateGroup(eq(groupId), any(CodeGroupRequest.class))).thenReturn(updated);

        ResponseEntity<CodeGroupResponse> response = codeManagementController.updateGroup(groupId, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("코드 그룹 삭제 성공")
    void testDeleteGroup_Success() {
        UUID groupId = UUID.randomUUID();

        ResponseEntity<Void> response = codeManagementController.deleteGroup(groupId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(codeManagementService).deleteGroup(groupId);
    }

    @Test
    @DisplayName("상세 코드 생성 성공")
    void testCreateDetail_Success() {
        UUID groupId = UUID.randomUUID();
        CodeGroup group = createMockGroup("GRP_01");
        group.setId(groupId);

        CodeDetailRequest request = new CodeDetailRequest();
        request.setDetailCode("DTL_01");
        request.setName(Map.of("ko", "상세1"));

        CodeDetail created = createMockDetail(group, "DTL_01");
        when(codeManagementService.createDetail(eq(groupId), any(CodeDetailRequest.class))).thenReturn(created);

        ResponseEntity<CodeDetailResponse> response = codeManagementController.createDetail(groupId, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("DTL_01", response.getBody().getDetailCode());
    }

    @Test
    @DisplayName("그룹별 상세 코드 목록 조회 성공")
    void testGetDetailsByGroup_Success() {
        UUID groupId = UUID.randomUUID();
        CodeGroup group = createMockGroup("GRP_01");
        group.setId(groupId);
        CodeDetail detail = createMockDetail(group, "DTL_01");

        when(codeManagementService.getDetailsByGroup(groupId)).thenReturn(List.of(detail));

        ResponseEntity<List<CodeDetailResponse>> response = codeManagementController.getDetailsByGroup(groupId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("DTL_01", response.getBody().get(0).getDetailCode());
    }

    @Test
    @DisplayName("상세 코드 수정 성공")
    void testUpdateDetail_Success() {
        UUID detailId = UUID.randomUUID();
        CodeGroup group = createMockGroup("GRP_01");
        CodeDetailRequest request = new CodeDetailRequest();
        request.setName(Map.of("ko", "수정된 상세"));

        CodeDetail updated = createMockDetail(group, "DTL_01");
        when(codeManagementService.updateDetail(eq(detailId), any(CodeDetailRequest.class))).thenReturn(updated);

        ResponseEntity<CodeDetailResponse> response = codeManagementController.updateDetail(detailId, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("상세 코드 삭제 성공")
    void testDeleteDetail_Success() {
        UUID detailId = UUID.randomUUID();

        ResponseEntity<Void> response = codeManagementController.deleteDetail(detailId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(codeManagementService).deleteDetail(detailId);
    }

    @Test
    @DisplayName("그룹 코드로 활성 상세 코드 조회 성공")
    void testGetActiveDetailsByGroupCode_Success() {
        CodeGroup group = createMockGroup("GRP_01");
        CodeDetail detail = createMockDetail(group, "DTL_01");

        when(codeManagementService.getActiveDetailsByGroupCode("GRP_01")).thenReturn(List.of(detail));

        ResponseEntity<List<CodeDetailResponse>> response = codeManagementController.getActiveDetailsByGroupCode("GRP_01");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
    }

    @Test
    @DisplayName("코드 Export 및 Import 성공")
    void testExportAndImportCodes_Success() {
        CodeExportDto exportDto = new CodeExportDto();
        exportDto.setGroupCode("GRP_01");
        when(codeManagementService.exportCodes()).thenReturn(List.of(exportDto));

        ResponseEntity<List<CodeExportDto>> exportResponse = codeManagementController.exportCodes();
        assertEquals(HttpStatus.OK, exportResponse.getStatusCode());
        assertEquals(1, exportResponse.getBody().size());

        ResponseEntity<Void> importResponse = codeManagementController.importCodes(List.of(exportDto));
        assertEquals(HttpStatus.OK, importResponse.getStatusCode());
        verify(codeManagementService).importCodes(anyList());
    }

    @Test
    @DisplayName("시드 파일 덤프 및 동기화 성공")
    void testDumpAndSyncSeedFiles_Success() {
        ResponseEntity<Void> dumpResponse = codeManagementController.dumpSeedFiles();
        assertEquals(HttpStatus.OK, dumpResponse.getStatusCode());
        verify(codeManagementService).dumpCodeStateToSeedFiles();

        ResponseEntity<Void> syncResponse = codeManagementController.syncSeedFiles();
        assertEquals(HttpStatus.OK, syncResponse.getStatusCode());
        verify(codeManagementService).syncCodes();
    }
}
