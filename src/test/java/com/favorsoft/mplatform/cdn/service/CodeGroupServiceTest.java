package com.favorsoft.mplatform.cdn.service;

import com.favorsoft.mplatform.cdn.domain.*;
import com.favorsoft.mplatform.cdn.domain.keys.CodeGroupPropKey;
import com.favorsoft.mplatform.cdn.dto.CodeGroupDTO;
import com.favorsoft.mplatform.cdn.dto.PropValue;
import com.favorsoft.mplatform.cdn.dto.request.CodeGroupSystemReq;
import com.favorsoft.mplatform.cdn.dto.response.CodeGroupSystemRes;
import com.favorsoft.mplatform.cdn.enums.PropMode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest
@ActiveProfiles("local")
public class CodeGroupServiceTest {

    private static Logger logger = LoggerFactory.getLogger(CodeGroupServiceTest.class);

    @Autowired
    private CodeGroupService codeGroupService;

    @Autowired
    private CodeGroupPropService codeGroupPropService;

    @Autowired
    private PropService propService;

    @BeforeEach
    public void create() {
        logger.info("##### Spring security TEST_ACCOUNT 인증 #####");
        User user = new User("TEST_USER1");
        Authentication authentication = new UsernamePasswordAuthenticationToken(user, null, null);
        SecurityContext securityContext = SecurityContextHolder.getContext();
        securityContext.setAuthentication(authentication);

        logger.info("##### CodeGroup 생성하기 #####");
        CodeGroup codeGroup = new CodeGroup();
        codeGroup.setCodeGroupId("TEST_GROUP");
        codeGroup.setDescription("Test Group Description");
        codeGroup.setMessage(new Message("PROP_TYPE_COMBO"));
        codeGroup.setIsEnable("Y");
        codeGroupService.save(codeGroup);

        logger.info("##### 속석 생성 #####");
        logger.info("##### TEST_PROP1 속성 생성 #####");
        Prop prop1 = new Prop();
        prop1.setPropId("TEST_PROP1");
        prop1.setArea(new Area("*"));
        prop1.setMgroup(new Mgroup("STANDARD"));
        prop1.setDbType("VARCHAR(100)");
        prop1.setMessage(new Message("PROP_TYPE_DATE"));
        prop1.setWidth(100);
        prop1.setPropType(new PropType("STRN"));
        propService.save(prop1);

        logger.info("##### TEST_PROP2 속성 생성 #####");
        Prop prop2 = new Prop();
        prop2.setPropId("TEST_PROP2");
        prop2.setArea(new Area("*"));
        prop2.setMgroup(new Mgroup("STANDARD"));
        prop2.setDbType("INT(10)");
        prop2.setMessage(new Message("PROP_TYPE_DATE"));
        prop2.setWidth(100);
        prop2.setPropType(new PropType("NVAL"));
        propService.save(prop2);

        logger.info("##### TEST_PROP3 속성 생성 #####");
        Prop prop3 = new Prop();
        prop3.setPropId("TEST_PROP3");
        prop3.setArea(new Area("*"));
        prop3.setMgroup(new Mgroup("STANDARD"));
        prop3.setDbType("DATETIME");
        prop3.setMessage(new Message("PROP_TYPE_DATE"));
        prop3.setWidth(100);
        prop3.setPropType(new PropType("DATE"));
        propService.save(prop3);

        logger.info("##### CodeGroup [TEST_GROUP] 속성 생성하기 #####");
        CodeGroupProp codeGroupProp1 = new CodeGroupProp();
        codeGroupProp1.setCodeGroup(new CodeGroup("TEST_GROUP"));
        codeGroupProp1.setProp(new Prop("TEST_PROP1"));
        codeGroupProp1.setPropMode(PropMode.IDENTITY);
        codeGroupPropService.save(codeGroupProp1);
        CodeGroupProp codeGroupProp2 = new CodeGroupProp();
        codeGroupProp2.setCodeGroup(new CodeGroup("TEST_GROUP"));
        codeGroupProp2.setProp(new Prop("TEST_PROP2"));
        codeGroupProp2.setPropMode(PropMode.NAME);
        codeGroupPropService.save(codeGroupProp2);
        CodeGroupProp codeGroupProp3 = new CodeGroupProp();
        codeGroupProp3.setCodeGroup(new CodeGroup("TEST_GROUP"));
        codeGroupProp3.setProp(new Prop("TEST_PROP3"));
        codeGroupProp3.setPropMode(PropMode.GENERAL);
        codeGroupPropService.save(codeGroupProp3);
    }

    @Test
    public void totalTest() {
        logger.info("##### CodeGroup [TEST_GROUP] 가져오기 #####");
        CodeGroup codeGroup = codeGroupService.getObject("TEST_GROUP");
        assertEquals("TEST_GROUP", codeGroup.getCodeGroupId());
        assertEquals("Test Group Description", codeGroup.getDescription());

        logger.info("##### Code LIST [TEST_GROUP] Code insert #####");
        List<PropValue> propValue = new ArrayList<>();
        propValue.add(PropValue.builder().propId("TEST_PROP1").value("TEXT").build());
        propValue.add(PropValue.builder().propId("TEST_PROP2").value("1234").build());
        propValue.add(PropValue.builder().propId("TEST_PROP3").value("2020-10-10").build());

        CodeGroupDTO codeGroupDTO = new CodeGroupDTO();
        codeGroupDTO.setCodeGroupId(codeGroup.getCodeGroupId());
        codeGroupDTO.setCode("CODE1");
        codeGroupDTO.setData(propValue);

        codeGroupService.saveCode(codeGroupDTO);

        logger.info("##### Code LIST [TEST_GROUP] 가져오기 #####");
        List<Map<String, Object>> codeList = codeGroupService.getCodeList(codeGroup.getCodeGroupId());
        logger.info(codeList.toString());
        assertEquals("TEXT", codeList.get(0).get("TEST_PROP1"));
        assertEquals(Integer.parseInt("1234"), codeList.get(0).get("TEST_PROP2"));

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        assertEquals("2020-10-10", sdf.format(codeList.get(0).get("TEST_PROP3")));


        logger.info("##### Code [TEST_PROP2] 값 변경  #####");
        PropValue pv = propValue.stream().filter(s->s.getPropId().equals("TEST_PROP2")).findAny().get();
        pv.setValue("5678");

        codeGroupService.saveCode(codeGroupDTO);

        logger.info("##### Code [TEST_PROP2] 값 테스트  #####");
        codeList = codeGroupService.getCodeList(codeGroup.getCodeGroupId());
        assertEquals(Integer.parseInt("5678"), codeList.get(0).get("TEST_PROP2"));

        logger.info("##### Code [TEST_PROP2] 컬럼 삭제  #####");
        long id = codeGroup.getCodeGroupProps().stream().filter(s->s.getProp().getPropId().equals("TEST_PROP2")).findFirst().get().getId();
        codeGroupPropService.delete(id);
        codeList = codeGroupService.getCodeList(codeGroup.getCodeGroupId());
        assertNull(codeList.get(0).get("TEST_PROP2"));

    }

    @Test
    public void getUiForm(){
        CodeGroupSystemReq req = new CodeGroupSystemReq();
        req.setCodeGroupId("TEST_GROUP");
        req.setLang("ko");
        logger.info("##### GET UI FORM #####");
        CodeGroupSystemRes res =  codeGroupService.getFormatedFormUI(req);
        assertEquals("TEST_GROUP", res.getCodeGroup().getCodeGroupId());
        assertEquals("BASIC", res.getMsections().get(0).getSectionId());
        assertEquals("STANDARD", res.getMsections().get(0).getMgroups().get(0).getGroupId());
        assertEquals("TEST_PROP1", res.getMsections().get(0).getMgroups().get(0).getProps().stream().filter(s->s.getPropId().equals("TEST_PROP1")).findFirst().get().getPropId());
        assertEquals("TEST_PROP2", res.getMsections().get(0).getMgroups().get(0).getProps().stream().filter(s->s.getPropId().equals("TEST_PROP2")).findFirst().get().getPropId());
        assertEquals("TEST_PROP3", res.getMsections().get(0).getMgroups().get(0).getProps().stream().filter(s->s.getPropId().equals("TEST_PROP3")).findFirst().get().getPropId());
    }

    @AfterEach
    public void delete() {
        codeGroupService.delete("TEST_GROUP");
        propService.delete("TEST_PROP1");
        propService.delete("TEST_PROP2");
        propService.delete("TEST_PROP3");
    }

}
