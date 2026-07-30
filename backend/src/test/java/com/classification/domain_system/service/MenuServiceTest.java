package com.classification.domain_system.service;

import com.classification.domain_system.entity.Menu;
import com.classification.domain_system.entity.MenuAccessLog;
import com.classification.domain_system.repository.MenuAccessLogRepository;
import com.classification.domain_system.repository.MenuRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MenuServiceTest {

    @Mock
    private MenuRepository menuRepository;

    @Mock
    private MenuAccessLogRepository menuAccessLogRepository;

    @InjectMocks
    private MenuService menuService;

    private Menu parentMenu;
    private Menu childMenu;

    @BeforeEach
    void setUp() {
        parentMenu = Menu.builder()
                .id(1L)
                .name("Admin")
                .path("/admin")
                .icon("admin_panel_settings")
                .sortOrder(1)
                .isActive(true)
                .build();

        childMenu = Menu.builder()
                .id(2L)
                .name("User Management")
                .path("/admin/users")
                .icon("manage_accounts")
                .parentId(1L)
                .sortOrder(1)
                .isActive(true)
                .build();
    }

    @Test
    void testGetMenuTree() {
        when(menuRepository.findAllByIsActiveTrueOrderBySortOrderAsc())
                .thenReturn(Arrays.asList(parentMenu, childMenu));

        List<Map<String, Object>> tree = menuService.getMenuTree();

        assertThat(tree).hasSize(1);
        assertThat(tree.get(0).get("name")).isEqualTo("Admin");
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> children = (List<Map<String, Object>>) tree.get(0).get("children");
        assertThat(children).hasSize(1);
        assertThat(children.get(0).get("name")).isEqualTo("User Management");
    }

    @Test
    void testChildRolesUnionInParentNode() {
        Menu parent = Menu.builder().id(10L).name("Management").path("/mgt").sortOrder(1).isActive(true).build();
        Menu child1 = Menu.builder().id(11L).name("Sub 1").parentId(10L).requiredRoles(new java.util.HashSet<>(java.util.Arrays.asList("USER", "DQ_MANAGER"))).sortOrder(1).isActive(true).build();
        Menu child2 = Menu.builder().id(12L).name("Sub 2").parentId(10L).requiredRoles(new java.util.HashSet<>(java.util.Arrays.asList("DATA_STEWARD", "USER"))).sortOrder(2).isActive(true).build();

        when(menuRepository.findAllByIsActiveTrueOrderBySortOrderAsc())
                .thenReturn(Arrays.asList(parent, child1, child2));

        List<Map<String, Object>> tree = menuService.getMenuTree();

        assertThat(tree).hasSize(1);
        Map<String, Object> parentNode = tree.get(0);
        
        @SuppressWarnings("unchecked")
        List<String> unionRoles = (List<String>) parentNode.get("requiredRoles");

        assertThat(unionRoles).contains("USER");
        assertThat(unionRoles).contains("DQ_MANAGER");
        assertThat(unionRoles).contains("DATA_STEWARD");
    }

    @Test
    void testChildRolesSetUnion1NF() {
        Menu parent = Menu.builder().id(20L).name("System").path("/sys").sortOrder(1).isActive(true).build();
        Menu child1 = Menu.builder().id(21L).name("Sub 21").parentId(20L).requiredRoles(new HashSet<>(Arrays.asList("ROLE_ADMIN", "ORG_ADMIN"))).sortOrder(1).isActive(true).build();
        Menu child2 = Menu.builder().id(22L).name("Sub 22").parentId(20L).requiredRoles(new HashSet<>(Arrays.asList("DATA_STEWARD", "ORG_ADMIN"))).sortOrder(2).isActive(true).build();

        when(menuRepository.findAllByIsActiveTrueOrderBySortOrderAsc())
                .thenReturn(Arrays.asList(parent, child1, child2));

        List<Map<String, Object>> tree = menuService.getMenuTree();

        assertThat(tree).hasSize(1);
        Map<String, Object> parentNode = tree.get(0);
        @SuppressWarnings("unchecked")
        List<String> unionList = (List<String>) parentNode.get("requiredRoles");

        assertThat(unionList).contains("ROLE_ADMIN", "ORG_ADMIN", "DATA_STEWARD");
    }

    @Test
    void testCreateMenu() {
        Menu newMenu = Menu.builder().name("Test").path("/test").sortOrder(5).build();
        when(menuRepository.save(any(Menu.class))).thenReturn(newMenu);

        Menu created = menuService.createMenu(newMenu);
        assertThat(created.getName()).isEqualTo("Test");
        verify(menuRepository, times(1)).save(newMenu);
    }

    @Test
    void testLogAccess() {
        MenuAccessLog log = MenuAccessLog.builder().menuId(1L).menuPath("/admin").userId("profavor").build();
        when(menuAccessLogRepository.save(any(MenuAccessLog.class))).thenReturn(log);

        menuService.logAccess(1L, "/admin", "profavor");

        verify(menuAccessLogRepository, times(1)).save(any(MenuAccessLog.class));
    }

    @Test
    void testGetMenuAccessLogs() {
        MenuAccessLog log = MenuAccessLog.builder().menuId(1L).menuPath("/admin").userId("profavor").build();
        Pageable pageable = PageRequest.of(0, 10);
        when(menuAccessLogRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class), eq(pageable)))
                .thenReturn(new PageImpl<>(Arrays.asList(log)));

        Page<MenuAccessLog> logs = menuService.getMenuAccessLogs(null, null, null, null, pageable);

        assertThat(logs.getContent()).hasSize(1);
        assertThat(logs.getContent().get(0).getUserId()).isEqualTo("profavor");
    }
}
