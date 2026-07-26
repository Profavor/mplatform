package com.classification.domain_system.service;

import com.classification.domain_system.entity.PermissionGroup;
import com.classification.domain_system.entity.PermissionItem;
import com.classification.domain_system.repository.PermissionGroupRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class PermissionMasterInitializer implements CommandLineRunner {

    private final PermissionGroupRepository groupRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("Checking and initializing default Permission Master groups...");

        // 1. Domain Group
        if (!groupRepository.existsById("domain")) {
            PermissionGroup domain = new PermissionGroup();
            domain.setId("domain");
            domain.setCode("domain");
            domain.setTitleKo("도메인 권한");
            domain.setTitleEn("Domain Permissions");
            domain.setIcon("🌐");
            domain.setColor("#3b82f6");
            domain.setChipClass("");
            domain.setSortOrder(1);

            addPermissionItem(domain, "도메인 전체 (*)", "Domain All (*)", "domain:*", 1);
            addPermissionItem(domain, "조회 (read)", "Read (read)", "domain:read", 2);
            addPermissionItem(domain, "작성 (write)", "Write (write)", "domain:write", 3);
            groupRepository.save(domain);
        }

        // 2. Node Group
        if (!groupRepository.existsById("node")) {
            PermissionGroup node = new PermissionGroup();
            node.setId("node");
            node.setCode("node");
            node.setTitleKo("분류 노드 권한");
            node.setTitleEn("Category Node Permissions");
            node.setIcon("📁");
            node.setColor("#10b981");
            node.setChipClass("green");
            node.setSortOrder(2);

            addPermissionItem(node, "노드 전체 (*)", "Node All (*)", "node:*", 1);
            addPermissionItem(node, "조회 (read)", "Read (read)", "node:read", 2);
            addPermissionItem(node, "작성 (write)", "Write (write)", "node:write", 3);
            groupRepository.save(node);
        }

        // 3. Field Group
        if (!groupRepository.existsById("field")) {
            PermissionGroup field = new PermissionGroup();
            field.setId("field");
            field.setCode("field");
            field.setTitleKo("속성 필드 권한");
            field.setTitleEn("Field Permissions");
            field.setIcon("📌");
            field.setColor("#f59e0b");
            field.setChipClass("amber");
            field.setSortOrder(3);

            addPermissionItem(field, "필드 전체 (*)", "Field All (*)", "field:*", 1);
            addPermissionItem(field, "조회 (read)", "Read (read)", "field:read", 2);
            addPermissionItem(field, "작성 (write)", "Write (write)", "field:write", 3);
            groupRepository.save(field);
        }

        // 4. DQ Group
        if (!groupRepository.existsById("dq")) {
            PermissionGroup dq = new PermissionGroup();
            dq.setId("dq");
            dq.setCode("dq");
            dq.setTitleKo("데이터 품질 권한 (DQ)");
            dq.setTitleEn("DQ Permissions");
            dq.setIcon("⚡");
            dq.setColor("#8b5cf6");
            dq.setChipClass("purple");
            dq.setSortOrder(4);

            addPermissionItem(dq, "품질 전체 (*)", "DQ All (*)", "dq:*", 1);
            addPermissionItem(dq, "조회 (read)", "Read (read)", "dq:read", 2);
            addPermissionItem(dq, "작성 (write)", "Write (write)", "dq:write", 3);
            groupRepository.save(dq);
        }

        // 5. Org Group
        if (!groupRepository.existsById("org")) {
            PermissionGroup org = new PermissionGroup();
            org.setId("org");
            org.setCode("org");
            org.setTitleKo("조직 권한 (ORG)");
            org.setTitleEn("Organization Permissions");
            org.setIcon("⚙️");
            org.setColor("#06b6d4");
            org.setChipClass("cyan");
            org.setSortOrder(5);

            addPermissionItem(org, "조직 권한 전체 (*)", "Org All (*)", "org:*", 1);
            addPermissionItem(org, "조회 (read)", "Read (read)", "org:read", 2);
            addPermissionItem(org, "작성 (write)", "Write (write)", "org:write", 3);
            groupRepository.save(org);
        }

        log.info("Default Permission Master groups checking/seeding completed.");
    }

    private void addPermissionItem(PermissionGroup group, String labelKo, String labelEn, String permValue, int sortOrder) {
        PermissionItem item = new PermissionItem();
        item.setLabelKo(labelKo);
        item.setLabelEn(labelEn);
        item.setPermValue(permValue);
        item.setSortOrder(sortOrder);
        group.addItem(item);
    }
}
