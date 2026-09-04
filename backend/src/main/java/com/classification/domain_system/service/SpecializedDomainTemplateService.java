package com.classification.domain_system.service;

import com.classification.domain_system.dto.DomainResponse;
import com.classification.domain_system.dto.SpecializedDomainProvisionRequest;
import com.classification.domain_system.dto.SpecializedDomainTemplateDto;
import com.classification.domain_system.dto.SpecializedDomainTemplateDto.ClassificationNodeTemplateDto;
import com.classification.domain_system.dto.SpecializedDomainTemplateDto.DqRuleTemplateDto;
import com.classification.domain_system.dto.SpecializedDomainTemplateDto.FieldGroupTemplateDto;
import com.classification.domain_system.dto.SpecializedDomainTemplateDto.FieldTemplateDto;
import com.classification.domain_system.dto.SpecializedDomainTemplateDto.SectorTemplateDto;
import com.classification.domain_system.entity.*;
import com.classification.domain_system.exception.ResourceNotFoundException;
import com.classification.domain_system.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SpecializedDomainTemplateService {

    private final DomainRepository domainRepository;
    private final ClassificationAxisRepository axisRepository;
    private final ClassificationNodeRepository nodeRepository;
    private final FieldDefinitionRepository fieldDefinitionRepository;
    private final DqRuleRepository dqRuleRepository;
    private final SectorRepository sectorRepository;
    private final FieldGroupRepository fieldGroupRepository;
    private final CodeDetailRepository codeDetailRepository;

    private static final Map<String, SpecializedDomainTemplateDto> TEMPLATES = new LinkedHashMap<>();

    static {
        // =========================================================================
        // 1. CUSTOMER (고객 마스터)
        // =========================================================================
        TEMPLATES.put("CUSTOMER", SpecializedDomainTemplateDto.builder()
                .category("CUSTOMER")
                .name(Map.of("ko", "고객 마스터", "en", "Customer Master"))
                .description(Map.of("ko", "B2B/B2C 고객 정보, 식별번호, 연락처, 세그먼트 및 여신 거래 관리", "en", "B2B/B2C Customer Master Data"))
                .icon("person_pin")
                .numberingPattern("CUST-{YYYY}-{SEQ:6}")
                .axisName(Map.of("ko", "고객 구분", "en", "Customer Type"))
                .axisCode("CUSTOMER_TYPE")
                .rootNodeName(Map.of("ko", "전체 고객", "en", "All Customers"))
                .identifierFieldKey("customer_no")
                .displayNameFieldKey("customer_name")
                .nodes(List.of(
                        ClassificationNodeTemplateDto.builder().code("INDIVIDUAL").name(Map.of("ko", "개인 고객", "en", "Individual Customers")).icon("person").order(1).build(),
                        ClassificationNodeTemplateDto.builder().code("INDIVIDUAL_GENERAL").parentCode("INDIVIDUAL").name(Map.of("ko", "일반 개인", "en", "General")).icon("person_outline").order(1).build(),
                        ClassificationNodeTemplateDto.builder().code("INDIVIDUAL_VIP").parentCode("INDIVIDUAL").name(Map.of("ko", "VIP 고객", "en", "VIP")).icon("stars").order(2).build(),
                        ClassificationNodeTemplateDto.builder().code("CORPORATE").name(Map.of("ko", "법인/기업 고객", "en", "Corporate Customers")).icon("business").order(2).build(),
                        ClassificationNodeTemplateDto.builder().code("CORP_ENTERPRISE").parentCode("CORPORATE").name(Map.of("ko", "대기업", "en", "Enterprise")).icon("domain").order(1).build(),
                        ClassificationNodeTemplateDto.builder().code("CORP_SMB").parentCode("CORPORATE").name(Map.of("ko", "중소기업/스타트업", "en", "SMB / Startup")).icon("storefront").order(2).build(),
                        ClassificationNodeTemplateDto.builder().code("CORP_PUBLIC").parentCode("CORPORATE").name(Map.of("ko", "공공/공기업", "en", "Public Sector")).icon("account_balance").order(3).build(),
                        ClassificationNodeTemplateDto.builder().code("PROSPECT").name(Map.of("ko", "잠재 고객/리드", "en", "Prospects / Leads")).icon("contact_page").order(3).build()
                ))
                .sectors(List.of(
                        SectorTemplateDto.builder().code("BASIC_INFO").name(Map.of("ko", "기본 정보", "en", "Basic Information")).order(1)
                                .groups(List.of(
                                        FieldGroupTemplateDto.builder().code("IDENTIFIER_GROUP").name(Map.of("ko", "식별 및 법적 정보", "en", "Identification & Legal")).order(1).isDefaultOpen(true).build(),
                                        FieldGroupTemplateDto.builder().code("SEGMENT_GROUP").name(Map.of("ko", "고객 등급 및 상태", "en", "Tier & Status")).order(2).isDefaultOpen(true).build()
                                )).build(),
                        SectorTemplateDto.builder().code("CONTACT_LOCATION").name(Map.of("ko", "연락처 및 사업장", "en", "Contact & Location")).order(2)
                                .groups(List.of(
                                        FieldGroupTemplateDto.builder().code("CONTACT_GROUP").name(Map.of("ko", "담당자 통신 정보", "en", "Contact Persons")).order(1).isDefaultOpen(true).build(),
                                        FieldGroupTemplateDto.builder().code("ADDRESS_GROUP").name(Map.of("ko", "사업장 주소", "en", "Address & Location")).order(2).isDefaultOpen(true).build()
                                )).build(),
                        SectorTemplateDto.builder().code("SALES_CREDIT").name(Map.of("ko", "영업 및 여신 관리", "en", "Sales & Credit Terms")).order(3)
                                .groups(List.of(
                                        FieldGroupTemplateDto.builder().code("SALES_MGMT_GROUP").name(Map.of("ko", "영업 조직 및 계약", "en", "Sales Organization")).order(1).isDefaultOpen(true).build(),
                                        FieldGroupTemplateDto.builder().code("CREDIT_FINANCE_GROUP").name(Map.of("ko", "여신 및 결제 조건", "en", "Credit & Payment")).order(2).isDefaultOpen(true).build()
                                )).build(),
                        SectorTemplateDto.builder().code("HISTORY_NOTES").name(Map.of("ko", "고객 히스토리 및 특이사항", "en", "History & Notes")).order(4)
                                .groups(List.of(
                                        FieldGroupTemplateDto.builder().code("REMARKS_GROUP").name(Map.of("ko", "종합 비고", "en", "General Remarks")).order(1).isDefaultOpen(true).build()
                                )).build()
                ))
                .fields(List.of(
                        FieldTemplateDto.builder().key("customer_no").groupCode("IDENTIFIER_GROUP").name(Map.of("ko", "고객번호", "en", "Customer No")).type("TEXT").required(true).isSearchable(true).isGridVisible(true).gridWidth(3).tableColumnWidth(140).order(1).build(),
                        FieldTemplateDto.builder().key("customer_name").groupCode("IDENTIFIER_GROUP").name(Map.of("ko", "고객명/상호명", "en", "Customer Name")).type("TEXT").required(true).isSearchable(true).isGridVisible(true).gridWidth(4).tableColumnWidth(200).order(2).build(),
                        FieldTemplateDto.builder().key("customer_type").groupCode("IDENTIFIER_GROUP").name(Map.of("ko", "고객구분", "en", "Customer Type")).type("SELECT").required(true).isFilterable(true).isGridVisible(true).gridWidth(2).tableColumnWidth(120).order(3)
                                .options("[{\"key\":\"INDIVIDUAL\",\"value\":\"INDIVIDUAL\",\"label\":{\"ko\":\"개인\",\"en\":\"Individual\"}},{\"key\":\"CORPORATE\",\"value\":\"CORPORATE\",\"label\":{\"ko\":\"법인\",\"en\":\"Corporate\"}},{\"key\":\"PUBLIC\",\"value\":\"PUBLIC\",\"label\":{\"ko\":\"공공\",\"en\":\"Public\"}},{\"key\":\"FOREIGN\",\"value\":\"FOREIGN\",\"label\":{\"ko\":\"외국법인\",\"en\":\"Foreign\"}}]").build(),
                        FieldTemplateDto.builder().key("registration_no").groupCode("IDENTIFIER_GROUP").name(Map.of("ko", "사업자/식별번호", "en", "Reg No")).type("TEXT").required(false).isSearchable(true).isGridVisible(true).gridWidth(3).tableColumnWidth(150).order(4).build(),
                        FieldTemplateDto.builder().key("representative_name").groupCode("IDENTIFIER_GROUP").name(Map.of("ko", "대표자 성명", "en", "Representative")).type("TEXT").required(false).isGridVisible(true).gridWidth(3).tableColumnWidth(140).order(5).build(),

                        FieldTemplateDto.builder().key("customer_tier").groupCode("SEGMENT_GROUP").name(Map.of("ko", "고객 등급", "en", "Customer Tier")).type("SELECT").required(false).isFilterable(true).isGridVisible(true).gridWidth(2).tableColumnWidth(120).order(6)
                                .options("[{\"key\":\"VIP\",\"value\":\"VIP\",\"label\":{\"ko\":\"VIP\",\"en\":\"VIP\"}},{\"key\":\"GOLD\",\"value\":\"GOLD\",\"label\":{\"ko\":\"골드\",\"en\":\"Gold\"}},{\"key\":\"SILVER\",\"value\":\"SILVER\",\"label\":{\"ko\":\"실버\",\"en\":\"Silver\"}},{\"key\":\"BRONZE\",\"value\":\"BRONZE\",\"label\":{\"ko\":\"브론즈\",\"en\":\"Bronze\"}}]").build(),
                        FieldTemplateDto.builder().key("customer_status").groupCode("SEGMENT_GROUP").name(Map.of("ko", "거래 상태", "en", "Status")).type("SELECT").required(false).isFilterable(true).isGridVisible(true).gridWidth(2).tableColumnWidth(120).order(7)
                                .options("[{\"key\":\"ACTIVE\",\"value\":\"ACTIVE\",\"label\":{\"ko\":\"정상\",\"en\":\"Active\"}},{\"key\":\"DORMANT\",\"value\":\"DORMANT\",\"label\":{\"ko\":\"휴면\",\"en\":\"Dormant\"}},{\"key\":\"BLOCKED\",\"value\":\"BLOCKED\",\"label\":{\"ko\":\"거래정지\",\"en\":\"Blocked\"}},{\"key\":\"TERMINATED\",\"value\":\"TERMINATED\",\"label\":{\"ko\":\"해지\",\"en\":\"Terminated\"}}]").build(),
                        FieldTemplateDto.builder().key("registration_date").groupCode("SEGMENT_GROUP").name(Map.of("ko", "가입/등록일자", "en", "Registration Date")).type("DATE").required(false).isGridVisible(true).gridWidth(3).tableColumnWidth(130).order(8).build(),

                        FieldTemplateDto.builder().key("contact_person").groupCode("CONTACT_GROUP").name(Map.of("ko", "주담당자명", "en", "Contact Person")).type("TEXT").required(false).isGridVisible(true).gridWidth(3).tableColumnWidth(140).order(9).build(),
                        FieldTemplateDto.builder().key("contact_email").groupCode("CONTACT_GROUP").name(Map.of("ko", "대표/담당 이메일", "en", "Email")).type("EMAIL").required(false).isSearchable(true).isGridVisible(true).gridWidth(4).tableColumnWidth(200).order(10).build(),
                        FieldTemplateDto.builder().key("contact_phone").groupCode("CONTACT_GROUP").name(Map.of("ko", "대표 전화번호", "en", "Phone")).type("TEXT").required(false).isGridVisible(true).gridWidth(3).tableColumnWidth(150).order(11).build(),
                        FieldTemplateDto.builder().key("mobile_phone").groupCode("CONTACT_GROUP").name(Map.of("ko", "휴대전화번호", "en", "Mobile Phone")).type("TEXT").required(false).isGridVisible(true).gridWidth(3).tableColumnWidth(150).order(12).build(),
                        FieldTemplateDto.builder().key("fax_no").groupCode("CONTACT_GROUP").name(Map.of("ko", "팩스번호", "en", "Fax No")).type("TEXT").required(false).isGridVisible(false).gridWidth(3).tableColumnWidth(140).order(13).build(),

                        FieldTemplateDto.builder().key("postal_code").groupCode("ADDRESS_GROUP").name(Map.of("ko", "우편번호", "en", "Postal Code")).type("TEXT").required(false).isGridVisible(false).gridWidth(2).tableColumnWidth(110).order(14).build(),
                        FieldTemplateDto.builder().key("address_primary").groupCode("ADDRESS_GROUP").name(Map.of("ko", "기본 주소", "en", "Primary Address")).type("TEXT").required(false).isGridVisible(true).gridWidth(6).tableColumnWidth(250).order(15).build(),
                        FieldTemplateDto.builder().key("address_detail").groupCode("ADDRESS_GROUP").name(Map.of("ko", "상세 주소", "en", "Detail Address")).type("TEXT").required(false).isGridVisible(false).gridWidth(4).tableColumnWidth(200).order(16).build(),

                        FieldTemplateDto.builder().key("account_manager").groupCode("SALES_MGMT_GROUP").name(Map.of("ko", "담당 영업대표", "en", "Account Manager")).type("TEXT").required(false).isFilterable(true).isGridVisible(true).gridWidth(3).tableColumnWidth(140).order(17).build(),
                        FieldTemplateDto.builder().key("sales_region").groupCode("SALES_MGMT_GROUP").name(Map.of("ko", "영업 권역", "en", "Sales Region")).type("SELECT").required(false).isFilterable(true).isGridVisible(true).gridWidth(3).tableColumnWidth(130).order(18)
                                .options("[{\"key\":\"SEOUL\",\"value\":\"SEOUL\",\"label\":{\"ko\":\"수도권\",\"en\":\"Seoul Capital\"}},{\"key\":\"YEONGNAM\",\"value\":\"YEONGNAM\",\"label\":{\"ko\":\"영남권\",\"en\":\"Yeongnam\"}},{\"key\":\"HONAM\",\"value\":\"HONAM\",\"label\":{\"ko\":\"호남권\",\"en\":\"Honam\"}},{\"key\":\"CHUNGCHEONG\",\"value\":\"CHUNGCHEONG\",\"label\":{\"ko\":\"충청권\",\"en\":\"Chungcheong\"}},{\"key\":\"GLOBAL\",\"value\":\"GLOBAL\",\"label\":{\"ko\":\"해외\",\"en\":\"Global\"}}]").build(),
                        FieldTemplateDto.builder().key("contract_date").groupCode("SALES_MGMT_GROUP").name(Map.of("ko", "최초 계약일자", "en", "Contract Date")).type("DATE").required(false).isGridVisible(true).gridWidth(3).tableColumnWidth(130).order(19).build(),

                        FieldTemplateDto.builder().key("payment_terms").groupCode("CREDIT_FINANCE_GROUP").name(Map.of("ko", "결제 조건", "en", "Payment Terms")).type("SELECT").required(false).isFilterable(true).isGridVisible(true).gridWidth(3).tableColumnWidth(140).order(20)
                                .options("[{\"key\":\"CASH\",\"value\":\"CASH\",\"label\":{\"ko\":\"현금\",\"en\":\"Cash\"}},{\"key\":\"NET30\",\"value\":\"NET30\",\"label\":{\"ko\":\"30일 여신\",\"en\":\"Net 30\"}},{\"key\":\"NET60\",\"value\":\"NET60\",\"label\":{\"ko\":\"60일 여신\",\"en\":\"Net 60\"}},{\"key\":\"EOM\",\"value\":\"EOM\",\"label\":{\"ko\":\"익월말 결제\",\"en\":\"End of Month\"}}]").build(),
                        FieldTemplateDto.builder().key("credit_limit").groupCode("CREDIT_FINANCE_GROUP").name(Map.of("ko", "여신한도액", "en", "Credit Limit")).type("NUMBER").unit("KRW").required(false).isGridVisible(true).gridWidth(3).tableColumnWidth(140).order(21).build(),
                        FieldTemplateDto.builder().key("tax_invoice_email").groupCode("CREDIT_FINANCE_GROUP").name(Map.of("ko", "전자세금계산서 이메일", "en", "Tax Invoice Email")).type("EMAIL").required(false).isGridVisible(true).gridWidth(4).tableColumnWidth(200).order(22).build(),

                        FieldTemplateDto.builder().key("memo").groupCode("REMARKS_GROUP").name(Map.of("ko", "고객 특이사항 및 히스토리", "en", "Remarks & History")).type("HTML_TEXT").required(false).isGridVisible(false).gridWidth(8).tableColumnWidth(250).order(23).build()
                ))
                .dqRules(List.of(
                        DqRuleTemplateDto.builder().fieldKey("customer_name").ruleType("NOT_NULL").severity("ERROR").message(Map.of("ko", "고객명은 필수 입력 항목입니다.", "en", "Customer name is required.")).build(),
                        DqRuleTemplateDto.builder().fieldKey("contact_email").ruleType("REGEX").severity("WARNING").params("{\"pattern\":\"^[\\\\w-\\\\.]+@([\\\\w-]+\\\\.)+[\\\\w-]{2,4}$\"}").message(Map.of("ko", "유효한 이메일 형식이 아닙니다.", "en", "Invalid email format.")).build(),
                        DqRuleTemplateDto.builder().fieldKey("credit_limit").ruleType("RANGE").severity("WARNING").params("{\"min\":0}").message(Map.of("ko", "여신한도액은 0 이상이어야 합니다.", "en", "Credit limit must be greater than or equal to 0.")).build()
                ))
                .build());

        // =========================================================================
        // 2. VENDOR (거래처/협력사 마스터)
        // =========================================================================
        TEMPLATES.put("VENDOR", SpecializedDomainTemplateDto.builder()
                .category("VENDOR")
                .name(Map.of("ko", "거래처 마스터", "en", "Vendor Master"))
                .description(Map.of("ko", "공급사, 외주처, 협력사, 사업자 식별, 계좌 및 구매 조달 기준 관리", "en", "Vendor & Partner Master Data"))
                .icon("corporate_fare")
                .numberingPattern("VEND-{YYYY}-{SEQ:6}")
                .axisName(Map.of("ko", "거래처 유형", "en", "Vendor Type"))
                .axisCode("VENDOR_TYPE")
                .rootNodeName(Map.of("ko", "전체 거래처", "en", "All Vendors"))
                .identifierFieldKey("vendor_code")
                .displayNameFieldKey("vendor_name")
                .nodes(List.of(
                        ClassificationNodeTemplateDto.builder().code("RAW_SUPPLIER").name(Map.of("ko", "원자재 공급사", "en", "Raw Material Suppliers")).icon("precision_manufacturing").order(1).build(),
                        ClassificationNodeTemplateDto.builder().code("PARTS_SUPPLIER").name(Map.of("ko", "부품/반제품 협력사", "en", "Parts & Components")).icon("settings_suggest").order(2).build(),
                        ClassificationNodeTemplateDto.builder().code("LOGISTICS").name(Map.of("ko", "물류/운송 파트너", "en", "Logistics Partners")).icon("local_shipping").order(3).build(),
                        ClassificationNodeTemplateDto.builder().code("IT_SERVICE").name(Map.of("ko", "IT/외주 서비스", "en", "IT & Outsourcing")).icon("computer").order(4).build(),
                        ClassificationNodeTemplateDto.builder().code("MRO_SUPPLIES").name(Map.of("ko", "일반소모품/MRO", "en", "MRO & Consumables")).icon("inventory_2").order(5).build()
                ))
                .sectors(List.of(
                        SectorTemplateDto.builder().code("CORP_BASIC").name(Map.of("ko", "기업 기본 정보", "en", "Corporate Basic")).order(1)
                                .groups(List.of(
                                        FieldGroupTemplateDto.builder().code("BIZ_IDENTIFIER_GROUP").name(Map.of("ko", "법인 및 사업자 식별", "en", "Business Identity")).order(1).isDefaultOpen(true).build(),
                                        FieldGroupTemplateDto.builder().code("BIZ_CATEGORY_GROUP").name(Map.of("ko", "업종 및 상태", "en", "Business Sector & Status")).order(2).isDefaultOpen(true).build()
                                )).build(),
                        SectorTemplateDto.builder().code("LOCATION_BANKING").name(Map.of("ko", "사업장 및 금융 계좌", "en", "Location & Banking")).order(2)
                                .groups(List.of(
                                        FieldGroupTemplateDto.builder().code("BIZ_LOCATION_GROUP").name(Map.of("ko", "사업장 주소", "en", "Business Address")).order(1).isDefaultOpen(true).build(),
                                        FieldGroupTemplateDto.builder().code("BANKING_GROUP").name(Map.of("ko", "결제 계좌 정보", "en", "Banking & Account")).order(2).isDefaultOpen(true).build()
                                )).build(),
                        SectorTemplateDto.builder().code("PROCUREMENT_TERMS").name(Map.of("ko", "조달 및 거래 조건", "en", "Procurement Terms")).order(3)
                                .groups(List.of(
                                        FieldGroupTemplateDto.builder().code("COMMERCIAL_TERMS_GROUP").name(Map.of("ko", "상업 거래 기준", "en", "Commercial Terms")).order(1).isDefaultOpen(true).build()
                                )).build(),
                        SectorTemplateDto.builder().code("QUALITY_NOTES").name(Map.of("ko", "품질/ESG 및 비고", "en", "Quality, ESG & Notes")).order(4)
                                .groups(List.of(
                                        FieldGroupTemplateDto.builder().code("QUALITY_EVAL_GROUP").name(Map.of("ko", "품질 및 종합 비고", "en", "Evaluation & Remarks")).order(1).isDefaultOpen(true).build()
                                )).build()
                ))
                .fields(List.of(
                        FieldTemplateDto.builder().key("vendor_code").groupCode("BIZ_IDENTIFIER_GROUP").name(Map.of("ko", "거래처코드", "en", "Vendor Code")).type("TEXT").required(true).isSearchable(true).isGridVisible(true).gridWidth(3).tableColumnWidth(140).order(1).build(),
                        FieldTemplateDto.builder().key("vendor_name").groupCode("BIZ_IDENTIFIER_GROUP").name(Map.of("ko", "거래처명/법인명", "en", "Vendor Name")).type("TEXT").required(true).isSearchable(true).isGridVisible(true).gridWidth(4).tableColumnWidth(200).order(2).build(),
                        FieldTemplateDto.builder().key("biz_reg_no").groupCode("BIZ_IDENTIFIER_GROUP").name(Map.of("ko", "사업자등록번호", "en", "Biz Reg No")).type("TEXT").required(true).isSearchable(true).isGridVisible(true).gridWidth(3).tableColumnWidth(150).order(3).build(),
                        FieldTemplateDto.builder().key("corp_reg_no").groupCode("BIZ_IDENTIFIER_GROUP").name(Map.of("ko", "법인등록번호", "en", "Corp Reg No")).type("TEXT").required(false).isGridVisible(true).gridWidth(3).tableColumnWidth(150).order(4).build(),
                        FieldTemplateDto.builder().key("ceo_name").groupCode("BIZ_IDENTIFIER_GROUP").name(Map.of("ko", "대표자 성명", "en", "CEO Name")).type("TEXT").required(false).isGridVisible(true).gridWidth(3).tableColumnWidth(140).order(5).build(),

                        FieldTemplateDto.builder().key("biz_type").groupCode("BIZ_CATEGORY_GROUP").name(Map.of("ko", "업태", "en", "Business Type")).type("TEXT").required(false).isGridVisible(false).gridWidth(3).tableColumnWidth(150).order(6).build(),
                        FieldTemplateDto.builder().key("biz_item").groupCode("BIZ_CATEGORY_GROUP").name(Map.of("ko", "종목", "en", "Business Item")).type("TEXT").required(false).isGridVisible(false).gridWidth(3).tableColumnWidth(150).order(7).build(),
                        FieldTemplateDto.builder().key("establishment_date").groupCode("BIZ_CATEGORY_GROUP").name(Map.of("ko", "설립일자", "en", "Est Date")).type("DATE").required(false).isGridVisible(true).gridWidth(3).tableColumnWidth(130).order(8).build(),
                        FieldTemplateDto.builder().key("vendor_status").groupCode("BIZ_CATEGORY_GROUP").name(Map.of("ko", "거래처 상태", "en", "Vendor Status")).type("SELECT").required(false).isFilterable(true).isGridVisible(true).gridWidth(2).tableColumnWidth(130).order(9)
                                .options("[{\"key\":\"NORMAL\",\"value\":\"NORMAL\",\"label\":{\"ko\":\"정규 거래처\",\"en\":\"Normal\"}},{\"key\":\"TEMPORARY\",\"value\":\"TEMPORARY\",\"label\":{\"ko\":\"임시 거래처\",\"en\":\"Temporary\"}},{\"key\":\"SUSPENDED\",\"value\":\"SUSPENDED\",\"label\":{\"ko\":\"거래정지\",\"en\":\"Suspended\"}}]").build(),

                        FieldTemplateDto.builder().key("postal_code").groupCode("BIZ_LOCATION_GROUP").name(Map.of("ko", "사업장 우편번호", "en", "Postal Code")).type("TEXT").required(false).isGridVisible(false).gridWidth(2).tableColumnWidth(110).order(10).build(),
                        FieldTemplateDto.builder().key("address_primary").groupCode("BIZ_LOCATION_GROUP").name(Map.of("ko", "기본 사업장 주소", "en", "Primary Address")).type("TEXT").required(false).isGridVisible(true).gridWidth(6).tableColumnWidth(250).order(11).build(),
                        FieldTemplateDto.builder().key("address_detail").groupCode("BIZ_LOCATION_GROUP").name(Map.of("ko", "상세 사업장 주소", "en", "Detail Address")).type("TEXT").required(false).isGridVisible(false).gridWidth(4).tableColumnWidth(200).order(12).build(),

                        FieldTemplateDto.builder().key("bank_name").groupCode("BANKING_GROUP").name(Map.of("ko", "결제 은행명", "en", "Bank Name")).type("SELECT").required(false).isFilterable(true).isGridVisible(true).gridWidth(3).tableColumnWidth(140).order(13)
                                .options("[{\"key\":\"KB\",\"value\":\"KB\",\"label\":{\"ko\":\"국민은행\",\"en\":\"KB Bank\"}},{\"key\":\"SHINHAN\",\"value\":\"SHINHAN\",\"label\":{\"ko\":\"신한은행\",\"en\":\"Shinhan Bank\"}},{\"key\":\"WOORI\",\"value\":\"WOORI\",\"label\":{\"ko\":\"우리은행\",\"en\":\"Woori Bank\"}},{\"key\":\"HANA\",\"value\":\"HANA\",\"label\":{\"ko\":\"하나은행\",\"en\":\"Hana Bank\"}},{\"key\":\"IBK\",\"value\":\"IBK\",\"label\":{\"ko\":\"기업은행\",\"en\":\"IBK\"}}]").build(),
                        FieldTemplateDto.builder().key("bank_account_no").groupCode("BANKING_GROUP").name(Map.of("ko", "계좌번호", "en", "Bank Account No")).type("TEXT").required(false).isGridVisible(true).gridWidth(4).tableColumnWidth(180).order(14).build(),
                        FieldTemplateDto.builder().key("account_holder").groupCode("BANKING_GROUP").name(Map.of("ko", "예금주명", "en", "Account Holder")).type("TEXT").required(false).isGridVisible(true).gridWidth(3).tableColumnWidth(140).order(15).build(),
                        FieldTemplateDto.builder().key("tax_invoice_email").groupCode("BANKING_GROUP").name(Map.of("ko", "세금계산서 수신 이메일", "en", "Tax Invoice Email")).type("EMAIL").required(true).isGridVisible(true).gridWidth(4).tableColumnWidth(200).order(16).build(),

                        FieldTemplateDto.builder().key("procurement_category").groupCode("COMMERCIAL_TERMS_GROUP").name(Map.of("ko", "조달 분류", "en", "Procurement Category")).type("SELECT").required(false).isFilterable(true).isGridVisible(true).gridWidth(3).tableColumnWidth(140).order(17)
                                .options("[{\"key\":\"RAW_MATERIAL\",\"value\":\"RAW_MATERIAL\",\"label\":{\"ko\":\"원자재\",\"en\":\"Raw Material\"}},{\"key\":\"PARTS\",\"value\":\"PARTS\",\"label\":{\"ko\":\"부품외주\",\"en\":\"Parts\"}},{\"key\":\"MRO\",\"value\":\"MRO\",\"label\":{\"ko\":\"소모품/MRO\",\"en\":\"MRO\"}},{\"key\":\"IT_SERVICE\",\"value\":\"IT_SERVICE\",\"label\":{\"ko\":\"IT용역\",\"en\":\"IT Service\"}}]").build(),
                        FieldTemplateDto.builder().key("payment_terms").groupCode("COMMERCIAL_TERMS_GROUP").name(Map.of("ko", "대금지급조건", "en", "Payment Terms")).type("SELECT").required(false).isFilterable(true).isGridVisible(true).gridWidth(3).tableColumnWidth(140).order(18)
                                .options("[{\"key\":\"CASH\",\"value\":\"CASH\",\"label\":{\"ko\":\"현금\",\"en\":\"Cash\"}},{\"key\":\"NET30\",\"value\":\"NET30\",\"label\":{\"ko\":\"마감후 30일\",\"en\":\"Net 30\"}},{\"key\":\"NET60\",\"value\":\"NET60\",\"label\":{\"ko\":\"마감후 60일\",\"en\":\"Net 60\"}},{\"key\":\"PURCHASE_CARD\",\"value\":\"PURCHASE_CARD\",\"label\":{\"ko\":\"구매카드\",\"en\":\"Purchasing Card\"}}]").build(),
                        FieldTemplateDto.builder().key("currency").groupCode("COMMERCIAL_TERMS_GROUP").name(Map.of("ko", "기준 거래통화", "en", "Currency")).type("SELECT").required(false).isGridVisible(true).gridWidth(2).tableColumnWidth(110).order(19)
                                .options("[{\"key\":\"KRW\",\"value\":\"KRW\",\"label\":{\"ko\":\"KRW\",\"en\":\"KRW\"}},{\"key\":\"USD\",\"value\":\"USD\",\"label\":{\"ko\":\"USD\",\"en\":\"USD\"}},{\"key\":\"EUR\",\"value\":\"EUR\",\"label\":{\"ko\":\"EUR\",\"en\":\"EUR\"}},{\"key\":\"JPY\",\"value\":\"JPY\",\"label\":{\"ko\":\"JPY\",\"en\":\"JPY\"}}]").build(),
                        FieldTemplateDto.builder().key("credit_limit").groupCode("COMMERCIAL_TERMS_GROUP").name(Map.of("ko", "거래 보증한도", "en", "Credit Limit")).type("NUMBER").unit("KRW").required(false).isGridVisible(true).gridWidth(3).tableColumnWidth(140).order(20).build(),
                        FieldTemplateDto.builder().key("buyer_in_charge").groupCode("COMMERCIAL_TERMS_GROUP").name(Map.of("ko", "구매 담당자", "en", "Buyer In Charge")).type("TEXT").required(false).isGridVisible(true).gridWidth(3).tableColumnWidth(140).order(21).build(),

                        FieldTemplateDto.builder().key("iso_certified").groupCode("QUALITY_EVAL_GROUP").name(Map.of("ko", "ISO 품질인증 보유", "en", "ISO Certified")).type("BOOLEAN").required(false).isGridVisible(true).gridWidth(2).tableColumnWidth(120).order(22).build(),
                        FieldTemplateDto.builder().key("esg_grade").groupCode("QUALITY_EVAL_GROUP").name(Map.of("ko", "ESG 종합등급", "en", "ESG Grade")).type("SELECT").required(false).isFilterable(true).isGridVisible(true).gridWidth(2).tableColumnWidth(120).order(23)
                                .options("[{\"key\":\"A_PLUS\",\"value\":\"A_PLUS\",\"label\":{\"ko\":\"A+\",\"en\":\"A+\"}},{\"key\":\"A\",\"value\":\"A\",\"label\":{\"ko\":\"A\",\"en\":\"A\"}},{\"key\":\"B_PLUS\",\"value\":\"B_PLUS\",\"label\":{\"ko\":\"B+\",\"en\":\"B+\"}},{\"key\":\"B\",\"value\":\"B\",\"label\":{\"ko\":\"B\",\"en\":\"B\"}},{\"key\":\"C\",\"value\":\"C\",\"label\":{\"ko\":\"C\",\"en\":\"C\"}}]").build(),
                        FieldTemplateDto.builder().key("notes").groupCode("QUALITY_EVAL_GROUP").name(Map.of("ko", "거래처 평가 및 협의사항", "en", "Evaluation Remarks")).type("HTML_TEXT").required(false).isGridVisible(false).gridWidth(8).tableColumnWidth(250).order(24).build()
                ))
                .dqRules(List.of(
                        DqRuleTemplateDto.builder().fieldKey("vendor_name").ruleType("NOT_NULL").severity("ERROR").message(Map.of("ko", "거래처명은 필수 입력 항목입니다.", "en", "Vendor name is required.")).build(),
                        DqRuleTemplateDto.builder().fieldKey("biz_reg_no").ruleType("BUSINESS_NO_CHECKSUM").severity("WARNING").message(Map.of("ko", "사업자등록번호 체크섬 검증에 실패했습니다.", "en", "Invalid business registration number checksum.")).build(),
                        DqRuleTemplateDto.builder().fieldKey("tax_invoice_email").ruleType("REGEX").severity("ERROR").params("{\"pattern\":\"^[\\\\w-\\\\.]+@([\\\\w-]+\\\\.)+[\\\\w-]{2,4}$\"}").message(Map.of("ko", "세금계산서 수신 이메일 형식이 유효하지 않습니다.", "en", "Invalid tax invoice email format.")).build()
                ))
                .build());

        // =========================================================================
        // 3. PRODUCT (상품 마스터)
        // =========================================================================
        TEMPLATES.put("PRODUCT", SpecializedDomainTemplateDto.builder()
                .category("PRODUCT")
                .name(Map.of("ko", "상품 마스터", "en", "Product Master"))
                .description(Map.of("ko", "완제품, 패키지, SKU, 가격, 바코드 및 물류 규격 기준 관리", "en", "Product & SKU Master Data"))
                .icon("shopping_bag")
                .numberingPattern("PROD-{YYYY}-{SEQ:6}")
                .axisName(Map.of("ko", "상품 대분류", "en", "Product Category"))
                .axisCode("PRODUCT_CAT")
                .rootNodeName(Map.of("ko", "전체 상품", "en", "All Products"))
                .identifierFieldKey("sku_code")
                .displayNameFieldKey("product_name")
                .nodes(List.of(
                        ClassificationNodeTemplateDto.builder().code("FINISHED_GOODS").name(Map.of("ko", "완제품", "en", "Finished Goods")).icon("inventory").order(1).build(),
                        ClassificationNodeTemplateDto.builder().code("STANDARD_PRODUCT").parentCode("FINISHED_GOODS").name(Map.of("ko", "표준 규격 상품", "en", "Standard Products")).icon("check_box").order(1).build(),
                        ClassificationNodeTemplateDto.builder().code("CUSTOM_PRODUCT").parentCode("FINISHED_GOODS").name(Map.of("ko", "주문 제작형 상품", "en", "Custom Products")).icon("tune").order(2).build(),
                        ClassificationNodeTemplateDto.builder().code("SEMI_FINISHED").name(Map.of("ko", "반제품", "en", "Semi-Finished Goods")).icon("build").order(2).build(),
                        ClassificationNodeTemplateDto.builder().code("DIGITAL_GOODS").name(Map.of("ko", "디지털/소프트웨어", "en", "Digital & Software")).icon("cloud_download").order(3).build(),
                        ClassificationNodeTemplateDto.builder().code("SUBSCRIPTION").name(Map.of("ko", "정기구독/서비스", "en", "Subscription & Services")).icon("autorenew").order(4).build()
                ))
                .sectors(List.of(
                        SectorTemplateDto.builder().code("PROD_IDENTIFIER").name(Map.of("ko", "상품 기본 식별", "en", "Product Identification")).order(1)
                                .groups(List.of(
                                        FieldGroupTemplateDto.builder().code("SKU_BASIC_GROUP").name(Map.of("ko", "SKU 및 상품명", "en", "SKU & Titles")).order(1).isDefaultOpen(true).build(),
                                        FieldGroupTemplateDto.builder().code("BRAND_ORIGIN_GROUP").name(Map.of("ko", "브랜드 및 원산지", "en", "Brand & Origin")).order(2).isDefaultOpen(true).build()
                                )).build(),
                        SectorTemplateDto.builder().code("PRICING_COST").name(Map.of("ko", "가격 및 원가 정보", "en", "Pricing & Cost")).order(2)
                                .groups(List.of(
                                        FieldGroupTemplateDto.builder().code("PRICE_GROUP").name(Map.of("ko", "소비자가 및 공급단가", "en", "Price & Margin")).order(1).isDefaultOpen(true).build()
                                )).build(),
                        SectorTemplateDto.builder().code("LOGISTICS_SPEC").name(Map.of("ko", "물류 및 사양 규격", "en", "Logistics & Specifications")).order(3)
                                .groups(List.of(
                                        FieldGroupTemplateDto.builder().code("DIMENSION_WEIGHT_GROUP").name(Map.of("ko", "크기 및 포장 중량", "en", "Dimensions & Weight")).order(1).isDefaultOpen(true).build()
                                )).build(),
                        SectorTemplateDto.builder().code("COMMERCE_DESC").name(Map.of("ko", "유통 및 상세설명", "en", "Commerce & Description")).order(4)
                                .groups(List.of(
                                        FieldGroupTemplateDto.builder().code("COMMERCE_STATUS_GROUP").name(Map.of("ko", "판매 및 상품 설명", "en", "Sales Status & Details")).order(1).isDefaultOpen(true).build()
                                )).build()
                ))
                .fields(List.of(
                        FieldTemplateDto.builder().key("sku_code").groupCode("SKU_BASIC_GROUP").name(Map.of("ko", "SKU 코드", "en", "SKU Code")).type("TEXT").required(true).isSearchable(true).isGridVisible(true).gridWidth(3).tableColumnWidth(140).order(1).build(),
                        FieldTemplateDto.builder().key("product_name").groupCode("SKU_BASIC_GROUP").name(Map.of("ko", "상품명", "en", "Product Name")).type("TEXT").required(true).isSearchable(true).isGridVisible(true).gridWidth(4).tableColumnWidth(220).order(2).build(),
                        FieldTemplateDto.builder().key("product_name_en").groupCode("SKU_BASIC_GROUP").name(Map.of("ko", "영문 상품명", "en", "Product Name (EN)")).type("TEXT").required(false).isGridVisible(true).gridWidth(4).tableColumnWidth(200).order(3).build(),
                        FieldTemplateDto.builder().key("barcode").groupCode("SKU_BASIC_GROUP").name(Map.of("ko", "바코드 (EAN-13)", "en", "Barcode")).type("TEXT").required(false).isSearchable(true).isGridVisible(true).gridWidth(3).tableColumnWidth(160).order(4).build(),

                        FieldTemplateDto.builder().key("brand").groupCode("BRAND_ORIGIN_GROUP").name(Map.of("ko", "브랜드명", "en", "Brand")).type("TEXT").required(false).isFilterable(true).isGridVisible(true).gridWidth(3).tableColumnWidth(150).order(5).build(),
                        FieldTemplateDto.builder().key("model_no").groupCode("BRAND_ORIGIN_GROUP").name(Map.of("ko", "제조사 모델번호", "en", "Model No")).type("TEXT").required(false).isGridVisible(true).gridWidth(3).tableColumnWidth(150).order(6).build(),
                        FieldTemplateDto.builder().key("origin_country").groupCode("BRAND_ORIGIN_GROUP").name(Map.of("ko", "원산지 국가", "en", "Origin Country")).type("SELECT").required(false).isFilterable(true).isGridVisible(true).gridWidth(2).tableColumnWidth(130).order(7)
                                .options("[{\"key\":\"KR\",\"value\":\"KR\",\"label\":{\"ko\":\"대한민국\",\"en\":\"Korea\"}},{\"key\":\"US\",\"value\":\"US\",\"label\":{\"ko\":\"미국\",\"en\":\"USA\"}},{\"key\":\"JP\",\"value\":\"JP\",\"label\":{\"ko\":\"일본\",\"en\":\"Japan\"}},{\"key\":\"DE\",\"value\":\"DE\",\"label\":{\"ko\":\"독일\",\"en\":\"Germany\"}},{\"key\":\"CN\",\"value\":\"CN\",\"label\":{\"ko\":\"중국\",\"en\":\"China\"}},{\"key\":\"VN\",\"value\":\"VN\",\"label\":{\"ko\":\"베트남\",\"en\":\"Vietnam\"}}]").build(),
                        FieldTemplateDto.builder().key("release_date").groupCode("BRAND_ORIGIN_GROUP").name(Map.of("ko", "출시일자", "en", "Release Date")).type("DATE").required(false).isGridVisible(true).gridWidth(3).tableColumnWidth(130).order(8).build(),

                        FieldTemplateDto.builder().key("retail_price").groupCode("PRICE_GROUP").name(Map.of("ko", "소비자가격", "en", "Retail Price")).type("NUMBER").unit("KRW").required(true).isGridVisible(true).gridWidth(3).tableColumnWidth(140).order(9).build(),
                        FieldTemplateDto.builder().key("wholesale_price").groupCode("PRICE_GROUP").name(Map.of("ko", "도매 공급가", "en", "Wholesale Price")).type("NUMBER").unit("KRW").required(false).isGridVisible(true).gridWidth(3).tableColumnWidth(140).order(10).build(),
                        FieldTemplateDto.builder().key("cost_price").groupCode("PRICE_GROUP").name(Map.of("ko", "제조/매입원가", "en", "Cost Price")).type("NUMBER").unit("KRW").required(false).isGridVisible(true).gridWidth(3).tableColumnWidth(140).order(11).build(),
                        FieldTemplateDto.builder().key("tax_type").groupCode("PRICE_GROUP").name(Map.of("ko", "과세구분", "en", "Tax Type")).type("SELECT").required(false).isFilterable(true).isGridVisible(true).gridWidth(2).tableColumnWidth(120).order(12)
                                .options("[{\"key\":\"TAXABLE\",\"value\":\"TAXABLE\",\"label\":{\"ko\":\"과세(10%)\",\"en\":\"Taxable\"}},{\"key\":\"TAX_FREE\",\"value\":\"TAX_FREE\",\"label\":{\"ko\":\"면세\",\"en\":\"Tax Free\"}},{\"key\":\"ZERO_TAX\",\"value\":\"ZERO_TAX\",\"label\":{\"ko\":\"영세율\",\"en\":\"Zero Tax\"}}]").build(),
                        FieldTemplateDto.builder().key("margin_rate").groupCode("PRICE_GROUP").name(Map.of("ko", "마진율 (%)", "en", "Margin Rate (%)")).type("NUMBER").unit("%").required(false).isGridVisible(true).gridWidth(2).tableColumnWidth(120).order(13).build(),

                        FieldTemplateDto.builder().key("width").groupCode("DIMENSION_WEIGHT_GROUP").name(Map.of("ko", "가로 규격", "en", "Width")).type("NUMBER").unit("mm").required(false).isGridVisible(true).gridWidth(2).tableColumnWidth(110).order(14).build(),
                        FieldTemplateDto.builder().key("depth").groupCode("DIMENSION_WEIGHT_GROUP").name(Map.of("ko", "세로 규격", "en", "Depth")).type("NUMBER").unit("mm").required(false).isGridVisible(true).gridWidth(2).tableColumnWidth(110).order(15).build(),
                        FieldTemplateDto.builder().key("height").groupCode("DIMENSION_WEIGHT_GROUP").name(Map.of("ko", "높이 규격", "en", "Height")).type("NUMBER").unit("mm").required(false).isGridVisible(true).gridWidth(2).tableColumnWidth(110).order(16).build(),
                        FieldTemplateDto.builder().key("gross_weight").groupCode("DIMENSION_WEIGHT_GROUP").name(Map.of("ko", "총중량", "en", "Gross Weight")).type("NUMBER").unit("g").required(false).isGridVisible(true).gridWidth(2).tableColumnWidth(120).order(17).build(),
                        FieldTemplateDto.builder().key("packaging_unit").groupCode("DIMENSION_WEIGHT_GROUP").name(Map.of("ko", "포장 단위", "en", "Package Unit")).type("SELECT").required(false).isGridVisible(true).gridWidth(2).tableColumnWidth(120).order(18)
                                .options("[{\"key\":\"EA\",\"value\":\"EA\",\"label\":{\"ko\":\"개(EA)\",\"en\":\"EA\"}},{\"key\":\"BOX\",\"value\":\"BOX\",\"label\":{\"ko\":\"박스(BOX)\",\"en\":\"BOX\"}},{\"key\":\"SET\",\"value\":\"SET\",\"label\":{\"ko\":\"세트(SET)\",\"en\":\"SET\"}}]").build(),

                        FieldTemplateDto.builder().key("is_active").groupCode("COMMERCE_STATUS_GROUP").name(Map.of("ko", "판매 상태", "en", "Is Active")).type("BOOLEAN").required(false).isFilterable(true).isGridVisible(true).gridWidth(2).tableColumnWidth(110).order(19).build(),
                        FieldTemplateDto.builder().key("safety_cert_no").groupCode("COMMERCE_STATUS_GROUP").name(Map.of("ko", "KC/CE 인증번호", "en", "Safety Cert No")).type("TEXT").required(false).isGridVisible(true).gridWidth(3).tableColumnWidth(160).order(20).build(),
                        FieldTemplateDto.builder().key("short_description").groupCode("COMMERCE_STATUS_GROUP").name(Map.of("ko", "상품 요약 설명", "en", "Short Description")).type("TEXT").required(false).isGridVisible(false).gridWidth(4).tableColumnWidth(220).order(21).build(),
                        FieldTemplateDto.builder().key("detail_description").groupCode("COMMERCE_STATUS_GROUP").name(Map.of("ko", "상품 상세 기술서", "en", "Detail Description")).type("HTML_TEXT").required(false).isGridVisible(false).gridWidth(8).tableColumnWidth(250).order(22).build()
                ))
                .dqRules(List.of(
                        DqRuleTemplateDto.builder().fieldKey("product_name").ruleType("NOT_NULL").severity("ERROR").message(Map.of("ko", "상품명은 필수 입력 항목입니다.", "en", "Product name is required.")).build(),
                        DqRuleTemplateDto.builder().fieldKey("retail_price").ruleType("RANGE").severity("ERROR").params("{\"min\":0}").message(Map.of("ko", "소비자가격은 0 이상이어야 합니다.", "en", "Retail price must be greater than or equal to 0.")).build(),
                        DqRuleTemplateDto.builder().fieldKey("cost_price").ruleType("RANGE").severity("WARNING").params("{\"min\":0}").message(Map.of("ko", "원가는 0 이상이어야 합니다.", "en", "Cost price must be non-negative.")).build()
                ))
                .build());

        // =========================================================================
        // 4. MATERIAL (자재 마스터)
        // =========================================================================
        TEMPLATES.put("MATERIAL", SpecializedDomainTemplateDto.builder()
                .category("MATERIAL")
                .name(Map.of("ko", "자재 마스터", "en", "Material Master"))
                .description(Map.of("ko", "원자재, 부자재, 반제품, 규격/사양, 조달 계획 및 안전재고 관리", "en", "Material & Inventory Master Data"))
                .icon("inventory")
                .numberingPattern("MAT-{SEQ:8}")
                .axisName(Map.of("ko", "자재 유형", "en", "Material Type"))
                .axisCode("MATERIAL_TYPE")
                .rootNodeName(Map.of("ko", "전체 자재", "en", "All Materials"))
                .identifierFieldKey("material_code")
                .displayNameFieldKey("material_name")
                .nodes(List.of(
                        ClassificationNodeTemplateDto.builder().code("PRIMARY_RAW").name(Map.of("ko", "주원자재", "en", "Primary Raw Materials")).icon("category").order(1).build(),
                        ClassificationNodeTemplateDto.builder().code("SECONDARY_RAW").name(Map.of("ko", "부자재", "en", "Secondary Materials")).icon("dashboard_customize").order(2).build(),
                        ClassificationNodeTemplateDto.builder().code("PACKAGING").name(Map.of("ko", "포장재", "en", "Packaging Materials")).icon("shopping_bag").order(3).build(),
                        ClassificationNodeTemplateDto.builder().code("CONSUMABLES").name(Map.of("ko", "공구/소모품", "en", "Tools & Consumables")).icon("construction").order(4).build()
                ))
                .sectors(List.of(
                        SectorTemplateDto.builder().code("MAT_IDENTIFIER").name(Map.of("ko", "자재 기본 식별", "en", "Material Identification")).order(1)
                                .groups(List.of(
                                        FieldGroupTemplateDto.builder().code("MAT_CODE_GROUP").name(Map.of("ko", "자재 코드 및 분류", "en", "Code & Classification")).order(1).isDefaultOpen(true).build(),
                                        FieldGroupTemplateDto.builder().code("TECH_SPEC_GROUP").name(Map.of("ko", "도면 및 기술 규격", "en", "Drawings & Specifications")).order(2).isDefaultOpen(true).build()
                                )).build(),
                        SectorTemplateDto.builder().code("SUPPLY_CHAIN").name(Map.of("ko", "조달 및 공급망", "en", "Procurement & Supply Chain")).order(2)
                                .groups(List.of(
                                        FieldGroupTemplateDto.builder().code("PROCUREMENT_GROUP").name(Map.of("ko", "조달 계획 및 협력사", "en", "Procurement Planning")).order(1).isDefaultOpen(true).build()
                                )).build(),
                        SectorTemplateDto.builder().code("INVENTORY_STORAGE").name(Map.of("ko", "재고 및 보관 환경", "en", "Inventory & Storage Environment")).order(3)
                                .groups(List.of(
                                        FieldGroupTemplateDto.builder().code("STOCK_LEVEL_GROUP").name(Map.of("ko", "안전재고 및 보관처", "en", "Stock Level & Location")).order(1).isDefaultOpen(true).build(),
                                        FieldGroupTemplateDto.builder().code("STORAGE_ENV_GROUP").name(Map.of("ko", "보관 조건 및 물질안전", "en", "Storage Conditions")).order(2).isDefaultOpen(true).build()
                                )).build(),
                        SectorTemplateDto.builder().code("COSTING_NOTES").name(Map.of("ko", "원가 및 특기사항", "en", "Costing & Technical Notes")).order(4)
                                .groups(List.of(
                                        FieldGroupTemplateDto.builder().code("COSTING_GROUP").name(Map.of("ko", "원가 및 기술 특기사항", "en", "Costing & Notes")).order(1).isDefaultOpen(true).build()
                                )).build()
                ))
                .fields(List.of(
                        FieldTemplateDto.builder().key("material_code").groupCode("MAT_CODE_GROUP").name(Map.of("ko", "자재코드", "en", "Material Code")).type("TEXT").required(true).isSearchable(true).isGridVisible(true).gridWidth(3).tableColumnWidth(140).order(1).build(),
                        FieldTemplateDto.builder().key("material_name").groupCode("MAT_CODE_GROUP").name(Map.of("ko", "자재명", "en", "Material Name")).type("TEXT").required(true).isSearchable(true).isGridVisible(true).gridWidth(4).tableColumnWidth(220).order(2).build(),
                        FieldTemplateDto.builder().key("material_type").groupCode("MAT_CODE_GROUP").name(Map.of("ko", "자재 유형", "en", "Material Type")).type("SELECT").required(true).isFilterable(true).isGridVisible(true).gridWidth(3).tableColumnWidth(140).order(3)
                                .options("[{\"key\":\"RAW\",\"value\":\"RAW\",\"label\":{\"ko\":\"원자재(ROH)\",\"en\":\"Raw (ROH)\"}},{\"key\":\"SEMI\",\"value\":\"SEMI\",\"label\":{\"ko\":\"반제품(HALB)\",\"en\":\"Semi (HALB)\"}},{\"key\":\"FINISHED\",\"value\":\"FINISHED\",\"label\":{\"ko\":\"완제품(FERT)\",\"en\":\"Finished (FERT)\"}},{\"key\":\"MRO\",\"value\":\"MRO\",\"label\":{\"ko\":\"소모품(HIBE)\",\"en\":\"MRO (HIBE)\"}}]").build(),
                        FieldTemplateDto.builder().key("base_uom").groupCode("MAT_CODE_GROUP").name(Map.of("ko", "기본 관리단위", "en", "Base UOM")).type("SELECT").required(true).isFilterable(true).isGridVisible(true).gridWidth(2).tableColumnWidth(120).order(4)
                                .options("[{\"key\":\"EA\",\"value\":\"EA\",\"label\":{\"ko\":\"개(EA)\",\"en\":\"EA\"}},{\"key\":\"KG\",\"value\":\"KG\",\"label\":{\"ko\":\"킬로그램(KG)\",\"en\":\"KG\"}},{\"key\":\"M\",\"value\":\"M\",\"label\":{\"ko\":\"미터(M)\",\"en\":\"M\"}},{\"key\":\"L\",\"value\":\"L\",\"label\":{\"ko\":\"리터(L)\",\"en\":\"L\"}},{\"key\":\"TON\",\"value\":\"TON\",\"label\":{\"ko\":\"톤(TON)\",\"en\":\"TON\"}}]").build(),

                        FieldTemplateDto.builder().key("drawing_no").groupCode("TECH_SPEC_GROUP").name(Map.of("ko", "도면 번호", "en", "Drawing No")).type("TEXT").required(false).isSearchable(true).isGridVisible(true).gridWidth(3).tableColumnWidth(150).order(5).build(),
                        FieldTemplateDto.builder().key("specification").groupCode("TECH_SPEC_GROUP").name(Map.of("ko", "규격/기술사양", "en", "Specification")).type("TEXT").required(false).isGridVisible(true).gridWidth(4).tableColumnWidth(200).order(6).build(),

                        FieldTemplateDto.builder().key("procurement_type").groupCode("PROCUREMENT_GROUP").name(Map.of("ko", "조달 구분", "en", "Procurement Type")).type("SELECT").required(true).isFilterable(true).isGridVisible(true).gridWidth(3).tableColumnWidth(140).order(7)
                                .options("[{\"key\":\"PURCHASE\",\"value\":\"PURCHASE\",\"label\":{\"ko\":\"구매조달\",\"en\":\"Purchase\"}},{\"key\":\"IN_HOUSE\",\"value\":\"IN_HOUSE\",\"label\":{\"ko\":\"사내생산\",\"en\":\"In-house\"}},{\"key\":\"SUBCONTRACT\",\"value\":\"SUBCONTRACT\",\"label\":{\"ko\":\"외주가공\",\"en\":\"Subcontract\"}}]").build(),
                        FieldTemplateDto.builder().key("primary_vendor").groupCode("PROCUREMENT_GROUP").name(Map.of("ko", "주공급사 코드", "en", "Primary Vendor")).type("TEXT").required(false).isGridVisible(true).gridWidth(3).tableColumnWidth(150).order(8).build(),
                        FieldTemplateDto.builder().key("lead_time_days").groupCode("PROCUREMENT_GROUP").name(Map.of("ko", "조달 리드타임", "en", "Lead Time")).type("NUMBER").unit("일").required(false).isGridVisible(true).gridWidth(2).tableColumnWidth(120).order(9).build(),
                        FieldTemplateDto.builder().key("min_order_qty").groupCode("PROCUREMENT_GROUP").name(Map.of("ko", "최소발주량 (MOQ)", "en", "Min Order Qty")).type("NUMBER").required(false).isGridVisible(true).gridWidth(2).tableColumnWidth(130).order(10).build(),
                        FieldTemplateDto.builder().key("order_multiple").groupCode("PROCUREMENT_GROUP").name(Map.of("ko", "발주배수단위", "en", "Order Multiple")).type("NUMBER").required(false).isGridVisible(true).gridWidth(2).tableColumnWidth(120).order(11).build(),

                        FieldTemplateDto.builder().key("safety_stock").groupCode("STOCK_LEVEL_GROUP").name(Map.of("ko", "안전재고량", "en", "Safety Stock")).type("NUMBER").required(false).isGridVisible(true).gridWidth(2).tableColumnWidth(120).order(12).build(),
                        FieldTemplateDto.builder().key("max_stock_level").groupCode("STOCK_LEVEL_GROUP").name(Map.of("ko", "최대보관량", "en", "Max Stock")).type("NUMBER").required(false).isGridVisible(true).gridWidth(2).tableColumnWidth(120).order(13).build(),
                        FieldTemplateDto.builder().key("storage_location").groupCode("STOCK_LEVEL_GROUP").name(Map.of("ko", "기본 저장위치", "en", "Storage Location")).type("TEXT").required(false).isGridVisible(true).gridWidth(3).tableColumnWidth(140).order(14).build(),

                        FieldTemplateDto.builder().key("is_hazardous").groupCode("STORAGE_ENV_GROUP").name(Map.of("ko", "유해물질 여부", "en", "Is Hazardous")).type("BOOLEAN").required(false).isGridVisible(true).gridWidth(2).tableColumnWidth(120).order(15).build(),
                        FieldTemplateDto.builder().key("msds_code").groupCode("STORAGE_ENV_GROUP").name(Map.of("ko", "MSDS 번호", "en", "MSDS Code")).type("TEXT").required(false).isGridVisible(true).gridWidth(3).tableColumnWidth(140).order(16).build(),
                        FieldTemplateDto.builder().key("storage_temp").groupCode("STORAGE_ENV_GROUP").name(Map.of("ko", "보관온도 조건", "en", "Storage Temp")).type("SELECT").required(false).isGridVisible(true).gridWidth(3).tableColumnWidth(140).order(17)
                                .options("[{\"key\":\"ROOM_TEMP\",\"value\":\"ROOM_TEMP\",\"label\":{\"ko\":\"상온(15~25℃)\",\"en\":\"Room Temp\"}},{\"key\":\"REFRIGERATED\",\"value\":\"REFRIGERATED\",\"label\":{\"ko\":\"냉장(0~10℃)\",\"en\":\"Refrigerated\"}},{\"key\":\"FROZEN\",\"value\":\"FROZEN\",\"label\":{\"ko\":\"냉동(-18℃ 이하)\",\"en\":\"Frozen\"}},{\"key\":\"CONSTANT\",\"value\":\"CONSTANT\",\"label\":{\"ko\":\"항온항습\",\"en\":\"Constant Temp/Humidity\"}}]").build(),
                        FieldTemplateDto.builder().key("shelf_life_days").groupCode("STORAGE_ENV_GROUP").name(Map.of("ko", "유효보관기한", "en", "Shelf Life")).type("NUMBER").unit("일").required(false).isGridVisible(true).gridWidth(2).tableColumnWidth(120).order(18).build(),

                        FieldTemplateDto.builder().key("standard_cost").groupCode("COSTING_GROUP").name(Map.of("ko", "표준원가", "en", "Standard Cost")).type("NUMBER").unit("KRW").required(false).isGridVisible(true).gridWidth(3).tableColumnWidth(140).order(19).build(),
                        FieldTemplateDto.builder().key("technical_notes").groupCode("COSTING_GROUP").name(Map.of("ko", "자재 기술 특기사항", "en", "Technical Notes")).type("HTML_TEXT").required(false).isGridVisible(false).gridWidth(8).tableColumnWidth(250).order(20).build()
                ))
                .dqRules(List.of(
                        DqRuleTemplateDto.builder().fieldKey("material_name").ruleType("NOT_NULL").severity("ERROR").message(Map.of("ko", "자재명은 필수 입력 항목입니다.", "en", "Material name is required.")).build(),
                        DqRuleTemplateDto.builder().fieldKey("safety_stock").ruleType("RANGE").severity("WARNING").params("{\"min\":0}").message(Map.of("ko", "안전재고량은 0 이상이어야 합니다.", "en", "Safety stock must be non-negative.")).build(),
                        DqRuleTemplateDto.builder().fieldKey("standard_cost").ruleType("RANGE").severity("WARNING").params("{\"min\":0}").message(Map.of("ko", "표준원가는 0 이상이어야 합니다.", "en", "Standard cost must be non-negative.")).build()
                ))
                .build());

        // =========================================================================
        // 5. EMPLOYEE (임직원 마스터)
        // =========================================================================
        TEMPLATES.put("EMPLOYEE", SpecializedDomainTemplateDto.builder()
                .category("EMPLOYEE")
                .name(Map.of("ko", "임직원 마스터", "en", "Employee Master"))
                .description(Map.of("ko", "사번, 인적 사항, 소속 부서/팀, 직위/직책, 재직 정보 및 비상연락망 관리", "en", "Employee & HR Master Data"))
                .icon("badge")
                .numberingPattern("EMP-{SEQ:6}")
                .axisName(Map.of("ko", "조직 본부", "en", "Division"))
                .axisCode("DIVISION")
                .rootNodeName(Map.of("ko", "전체 임직원", "en", "All Employees"))
                .identifierFieldKey("employee_no")
                .displayNameFieldKey("employee_name")
                .nodes(List.of(
                        ClassificationNodeTemplateDto.builder().code("EXECUTIVE").name(Map.of("ko", "임원실", "en", "Executive Office")).icon("military_tech").order(1).build(),
                        ClassificationNodeTemplateDto.builder().code("RND").name(Map.of("ko", "연구개발본부", "en", "R&D Center")).icon("science").order(2).build(),
                        ClassificationNodeTemplateDto.builder().code("RND_SW").parentCode("RND").name(Map.of("ko", "소프트웨어 개발팀", "en", "Software Team")).icon("code").order(1).build(),
                        ClassificationNodeTemplateDto.builder().code("RND_HW").parentCode("RND").name(Map.of("ko", "하드웨어/인프라팀", "en", "Hardware & Infra")).icon("memory").order(2).build(),
                        ClassificationNodeTemplateDto.builder().code("SALES_MKT").name(Map.of("ko", "영업/마케팅본부", "en", "Sales & Marketing")).icon("trending_up").order(3).build(),
                        ClassificationNodeTemplateDto.builder().code("DOMESTIC_SALES").parentCode("SALES_MKT").name(Map.of("ko", "국내영업팀", "en", "Domestic Sales")).icon("store").order(1).build(),
                        ClassificationNodeTemplateDto.builder().code("GLOBAL_SALES").parentCode("SALES_MKT").name(Map.of("ko", "해외영업팀", "en", "Global Sales")).icon("public").order(2).build(),
                        ClassificationNodeTemplateDto.builder().code("MARKETING").parentCode("SALES_MKT").name(Map.of("ko", "마케팅팀", "en", "Marketing Team")).icon("campaign").order(3).build(),
                        ClassificationNodeTemplateDto.builder().code("MGMT_SUPPORT").name(Map.of("ko", "경영지원본부", "en", "Management Support")).icon("support_agent").order(4).build(),
                        ClassificationNodeTemplateDto.builder().code("HR").parentCode("MGMT_SUPPORT").name(Map.of("ko", "인사총무팀", "en", "HR & General Affairs")).icon("groups").order(1).build(),
                        ClassificationNodeTemplateDto.builder().code("FINANCE").parentCode("MGMT_SUPPORT").name(Map.of("ko", "재경/회계팀", "en", "Finance & Accounting")).icon("account_balance_wallet").order(2).build()
                ))
                .sectors(List.of(
                        SectorTemplateDto.builder().code("HR_PERSONAL").name(Map.of("ko", "인적 기본 정보", "en", "Personal Identification")).order(1)
                                .groups(List.of(
                                        FieldGroupTemplateDto.builder().code("IDENTITY_GROUP").name(Map.of("ko", "신원 및 기본 인적", "en", "Identity & Demographics")).order(1).isDefaultOpen(true).build()
                                )).build(),
                        SectorTemplateDto.builder().code("HR_ORG").name(Map.of("ko", "조직 및 인사 발령", "en", "Organization & Position")).order(2)
                                .groups(List.of(
                                        FieldGroupTemplateDto.builder().code("ORG_POSITION_GROUP").name(Map.of("ko", "소속 본부 및 직위/직책", "en", "Division & Title")).order(1).isDefaultOpen(true).build(),
                                        FieldGroupTemplateDto.builder().code("APPOINTMENT_GROUP").name(Map.of("ko", "고용형태 및 재직상태", "en", "Employment Status")).order(2).isDefaultOpen(true).build()
                                )).build(),
                        SectorTemplateDto.builder().code("HR_CONTACT").name(Map.of("ko", "연락처 및 비상연락망", "en", "Contact & Emergency")).order(3)
                                .groups(List.of(
                                        FieldGroupTemplateDto.builder().code("WORK_COMM_GROUP").name(Map.of("ko", "사내 업무 통신", "en", "Work Communications")).order(1).isDefaultOpen(true).build(),
                                        FieldGroupTemplateDto.builder().code("EMERGENCY_GROUP").name(Map.of("ko", "비상 연락처", "en", "Emergency Contact")).order(2).isDefaultOpen(true).build()
                                )).build(),
                        SectorTemplateDto.builder().code("HR_CAREER").name(Map.of("ko", "직무 및 경력 요약", "en", "Career & Remarks")).order(4)
                                .groups(List.of(
                                        FieldGroupTemplateDto.builder().code("CAREER_GROUP").name(Map.of("ko", "주요 직무 및 이력", "en", "Role & Career Summary")).order(1).isDefaultOpen(true).build()
                                )).build()
                ))
                .fields(List.of(
                        FieldTemplateDto.builder().key("employee_no").groupCode("IDENTITY_GROUP").name(Map.of("ko", "사번", "en", "Employee No")).type("TEXT").required(true).isSearchable(true).isGridVisible(true).gridWidth(3).tableColumnWidth(140).order(1).build(),
                        FieldTemplateDto.builder().key("employee_name").groupCode("IDENTITY_GROUP").name(Map.of("ko", "성명", "en", "Employee Name")).type("TEXT").required(true).isSearchable(true).isGridVisible(true).gridWidth(3).tableColumnWidth(160).order(2).build(),
                        FieldTemplateDto.builder().key("english_name").groupCode("IDENTITY_GROUP").name(Map.of("ko", "영문 성명", "en", "English Name")).type("TEXT").required(false).isGridVisible(true).gridWidth(3).tableColumnWidth(160).order(3).build(),
                        FieldTemplateDto.builder().key("birth_date").groupCode("IDENTITY_GROUP").name(Map.of("ko", "생년월일", "en", "Birth Date")).type("DATE").required(false).isGridVisible(true).gridWidth(3).tableColumnWidth(130).order(4).build(),
                        FieldTemplateDto.builder().key("gender").groupCode("IDENTITY_GROUP").name(Map.of("ko", "성별", "en", "Gender")).type("SELECT").required(false).isGridVisible(true).gridWidth(2).tableColumnWidth(100).order(5)
                                .options("[{\"key\":\"MALE\",\"value\":\"MALE\",\"label\":{\"ko\":\"남성\",\"en\":\"Male\"}},{\"key\":\"FEMALE\",\"value\":\"FEMALE\",\"label\":{\"ko\":\"여성\",\"en\":\"Female\"}},{\"key\":\"OTHER\",\"value\":\"OTHER\",\"label\":{\"ko\":\"기타\",\"en\":\"Other\"}}]").build(),
                        FieldTemplateDto.builder().key("nationality").groupCode("IDENTITY_GROUP").name(Map.of("ko", "국적", "en", "Nationality")).type("TEXT").required(false).isGridVisible(false).gridWidth(2).tableColumnWidth(120).order(6).build(),

                        FieldTemplateDto.builder().key("division").groupCode("ORG_POSITION_GROUP").name(Map.of("ko", "소속 본부", "en", "Division")).type("SELECT").required(true).isFilterable(true).isGridVisible(true).gridWidth(3).tableColumnWidth(150).order(7)
                                .options("[{\"key\":\"EXECUTIVE\",\"value\":\"EXECUTIVE\",\"label\":{\"ko\":\"임원실\",\"en\":\"Executive\"}},{\"key\":\"RND\",\"value\":\"RND\",\"label\":{\"ko\":\"연구개발본부\",\"en\":\"R&D Center\"}},{\"key\":\"SALES_MKT\",\"value\":\"SALES_MKT\",\"label\":{\"ko\":\"영업/마케팅본부\",\"en\":\"Sales & Mkt\"}},{\"key\":\"MGMT_SUPPORT\",\"value\":\"MGMT_SUPPORT\",\"label\":{\"ko\":\"경영지원본부\",\"en\":\"Mgmt Support\"}}]").build(),
                        FieldTemplateDto.builder().key("department").groupCode("ORG_POSITION_GROUP").name(Map.of("ko", "소속 부서/팀", "en", "Department")).type("TEXT").required(false).isFilterable(true).isGridVisible(true).gridWidth(3).tableColumnWidth(160).order(8).build(),
                        FieldTemplateDto.builder().key("job_title").groupCode("ORG_POSITION_GROUP").name(Map.of("ko", "직위/호칭", "en", "Job Title")).type("SELECT").required(false).isFilterable(true).isGridVisible(true).gridWidth(3).tableColumnWidth(130).order(9)
                                .options("[{\"key\":\"STAFF\",\"value\":\"STAFF\",\"label\":{\"ko\":\"사원\",\"en\":\"Staff\"}},{\"key\":\"SENIOR\",\"value\":\"SENIOR\",\"label\":{\"ko\":\"대리/선임\",\"en\":\"Senior\"}},{\"key\":\"MANAGER\",\"value\":\"MANAGER\",\"label\":{\"ko\":\"과장/책임\",\"en\":\"Manager\"}},{\"key\":\"DIRECTOR\",\"value\":\"DIRECTOR\",\"label\":{\"ko\":\"차장/부장\",\"en\":\"Director\"}},{\"key\":\"EXECUTIVE\",\"value\":\"EXECUTIVE\",\"label\":{\"ko\":\"임원\",\"en\":\"Executive\"}}]").build(),
                        FieldTemplateDto.builder().key("job_role").groupCode("ORG_POSITION_GROUP").name(Map.of("ko", "직무/직책", "en", "Job Role")).type("SELECT").required(false).isFilterable(true).isGridVisible(true).gridWidth(3).tableColumnWidth(130).order(10)
                                .options("[{\"key\":\"MEMBER\",\"value\":\"MEMBER\",\"label\":{\"ko\":\"팀원\",\"en\":\"Member\"}},{\"key\":\"TEAM_LEADER\",\"value\":\"TEAM_LEADER\",\"label\":{\"ko\":\"팀장\",\"en\":\"Team Leader\"}},{\"key\":\"DEPT_HEAD\",\"value\":\"DEPT_HEAD\",\"label\":{\"ko\":\"실장/부서장\",\"en\":\"Head\"}},{\"key\":\"DIV_CHIEF\",\"value\":\"DIV_CHIEF\",\"label\":{\"ko\":\"본부장\",\"en\":\"Division Chief\"}}]").build(),
                        FieldTemplateDto.builder().key("work_location").groupCode("ORG_POSITION_GROUP").name(Map.of("ko", "근무지", "en", "Work Location")).type("SELECT").required(false).isFilterable(true).isGridVisible(true).gridWidth(3).tableColumnWidth(140).order(11)
                                .options("[{\"key\":\"HQ_SEOUL\",\"value\":\"HQ_SEOUL\",\"label\":{\"ko\":\"서울 본사\",\"en\":\"Seoul HQ\"}},{\"key\":\"RND_PANGYO\",\"value\":\"RND_PANGYO\",\"label\":{\"ko\":\"판교 R&D\",\"en\":\"Pangyo R&D\"}},{\"key\":\"DAEJEON\",\"value\":\"DAEJEON\",\"label\":{\"ko\":\"대전 지사\",\"en\":\"Daejeon\"}},{\"key\":\"BUSAN\",\"value\":\"BUSAN\",\"label\":{\"ko\":\"부산 공장\",\"en\":\"Busan Plant\"}}]").build(),

                        FieldTemplateDto.builder().key("employment_type").groupCode("APPOINTMENT_GROUP").name(Map.of("ko", "고용형태", "en", "Employment Type")).type("SELECT").required(false).isFilterable(true).isGridVisible(true).gridWidth(2).tableColumnWidth(130).order(12)
                                .options("[{\"key\":\"REGULAR\",\"value\":\"REGULAR\",\"label\":{\"ko\":\"정규직\",\"en\":\"Regular\"}},{\"key\":\"CONTRACT\",\"value\":\"CONTRACT\",\"label\":{\"ko\":\"계약직\",\"en\":\"Contract\"}},{\"key\":\"EXECUTIVE\",\"value\":\"EXECUTIVE\",\"label\":{\"ko\":\"임원\",\"en\":\"Executive\"}},{\"key\":\"INTERN\",\"value\":\"INTERN\",\"label\":{\"ko\":\"인턴\",\"en\":\"Intern\"}}]").build(),
                        FieldTemplateDto.builder().key("employment_status").groupCode("APPOINTMENT_GROUP").name(Map.of("ko", "재직상태", "en", "Employment Status")).type("SELECT").required(false).isFilterable(true).isGridVisible(true).gridWidth(2).tableColumnWidth(120).order(13)
                                .options("[{\"key\":\"EMPLOYED\",\"value\":\"EMPLOYED\",\"label\":{\"ko\":\"재직\",\"en\":\"Employed\"}},{\"key\":\"ON_LEAVE\",\"value\":\"ON_LEAVE\",\"label\":{\"ko\":\"휴직\",\"en\":\"On Leave\"}},{\"key\":\"RESIGNED\",\"value\":\"RESIGNED\",\"label\":{\"ko\":\"퇴사\",\"en\":\"Resigned\"}}]").build(),
                        FieldTemplateDto.builder().key("hire_date").groupCode("APPOINTMENT_GROUP").name(Map.of("ko", "입사일자", "en", "Hire Date")).type("DATE").required(false).isGridVisible(true).gridWidth(3).tableColumnWidth(130).order(14).build(),
                        FieldTemplateDto.builder().key("resignation_date").groupCode("APPOINTMENT_GROUP").name(Map.of("ko", "퇴사일자", "en", "Resignation Date")).type("DATE").required(false).isGridVisible(false).gridWidth(3).tableColumnWidth(130).order(15).build(),

                        FieldTemplateDto.builder().key("work_email").groupCode("WORK_COMM_GROUP").name(Map.of("ko", "사내 이메일", "en", "Work Email")).type("EMAIL").required(true).isSearchable(true).isGridVisible(true).gridWidth(4).tableColumnWidth(200).order(16).build(),
                        FieldTemplateDto.builder().key("mobile_phone").groupCode("WORK_COMM_GROUP").name(Map.of("ko", "휴대전화번호", "en", "Mobile Phone")).type("TEXT").required(false).isGridVisible(true).gridWidth(3).tableColumnWidth(150).order(17).build(),
                        FieldTemplateDto.builder().key("internal_ext").groupCode("WORK_COMM_GROUP").name(Map.of("ko", "사내 내선번호", "en", "Internal Ext")).type("TEXT").required(false).isGridVisible(true).gridWidth(2).tableColumnWidth(120).order(18).build(),

                        FieldTemplateDto.builder().key("emergency_contact").groupCode("EMERGENCY_GROUP").name(Map.of("ko", "비상연락처 성명/관계", "en", "Emergency Contact")).type("TEXT").required(false).isGridVisible(false).gridWidth(3).tableColumnWidth(150).order(19).build(),
                        FieldTemplateDto.builder().key("emergency_phone").groupCode("EMERGENCY_GROUP").name(Map.of("ko", "비상연락처 전화번호", "en", "Emergency Phone")).type("TEXT").required(false).isGridVisible(false).gridWidth(3).tableColumnWidth(150).order(20).build(),

                        FieldTemplateDto.builder().key("duty_description").groupCode("CAREER_GROUP").name(Map.of("ko", "주요 담당 업무", "en", "Duty Description")).type("TEXT").required(false).isGridVisible(false).gridWidth(4).tableColumnWidth(220).order(21).build(),
                        FieldTemplateDto.builder().key("career_summary").groupCode("CAREER_GROUP").name(Map.of("ko", "주요 경력 및 이력", "en", "Career Summary")).type("HTML_TEXT").required(false).isGridVisible(false).gridWidth(8).tableColumnWidth(250).order(22).build()
                ))
                .dqRules(List.of(
                        DqRuleTemplateDto.builder().fieldKey("employee_name").ruleType("NOT_NULL").severity("ERROR").message(Map.of("ko", "임직원 성명은 필수 입력 항목입니다.", "en", "Employee name is required.")).build(),
                        DqRuleTemplateDto.builder().fieldKey("work_email").ruleType("REGEX").severity("WARNING").params("{\"pattern\":\"^[\\\\w-\\\\.]+@([\\\\w-]+\\\\.)+[\\\\w-]{2,4}$\"}").message(Map.of("ko", "유효한 사내 이메일 형식이 아닙니다.", "en", "Invalid work email format.")).build()
                ))
                .build());

        // =========================================================================
        // 6. STOCK (주식/증권 마스터)
        // =========================================================================
        TEMPLATES.put("STOCK", SpecializedDomainTemplateDto.builder()
                .category("STOCK")
                .name(Map.of("ko", "주식 종목 마스터", "en", "Stock Master"))
                .description(Map.of("ko", "상장 종목코드, ISIN, 시장구분, 액면가, 상장주식수 및 IR 정보 관리", "en", "Stock & Equity Master Data"))
                .icon("candlestick_chart")
                .numberingPattern("STK-{SEQ:6}")
                .axisName(Map.of("ko", "상장 시장", "en", "Market"))
                .axisCode("MARKET")
                .rootNodeName(Map.of("ko", "전체 종목", "en", "All Stocks"))
                .identifierFieldKey("ticker_code")
                .displayNameFieldKey("stock_name")
                .nodes(List.of(
                        ClassificationNodeTemplateDto.builder().code("DOMESTIC_STOCK").name(Map.of("ko", "국내 유가증권", "en", "Domestic Markets")).icon("account_balance").order(1).build(),
                        ClassificationNodeTemplateDto.builder().code("KOSPI").parentCode("DOMESTIC_STOCK").name(Map.of("ko", "유가증권시장 (KOSPI)", "en", "KOSPI")).icon("show_chart").order(1).build(),
                        ClassificationNodeTemplateDto.builder().code("KOSDAQ").parentCode("DOMESTIC_STOCK").name(Map.of("ko", "코스닥시장 (KOSDAQ)", "en", "KOSDAQ")).icon("ssid_chart").order(2).build(),
                        ClassificationNodeTemplateDto.builder().code("KONEX").parentCode("DOMESTIC_STOCK").name(Map.of("ko", "코넥스시장 (KONEX)", "en", "KONEX")).icon("stacked_line_chart").order(3).build(),
                        ClassificationNodeTemplateDto.builder().code("GLOBAL_STOCK").name(Map.of("ko", "해외 주식", "en", "Global Markets")).icon("language").order(2).build(),
                        ClassificationNodeTemplateDto.builder().code("US_MARKET").parentCode("GLOBAL_STOCK").name(Map.of("ko", "미국 주식 (NYSE/NASDAQ)", "en", "US Markets")).icon("flag").order(1).build(),
                        ClassificationNodeTemplateDto.builder().code("ASIA_MARKET").parentCode("GLOBAL_STOCK").name(Map.of("ko", "아시아 주식 (Tokyo/HK/Shanghai)", "en", "Asia Markets")).icon("travel_explore").order(2).build(),
                        ClassificationNodeTemplateDto.builder().code("EUROPE_MARKET").parentCode("GLOBAL_STOCK").name(Map.of("ko", "유럽 주식 (LSE/Euronext)", "en", "Europe Markets")).icon("euro").order(3).build(),
                        ClassificationNodeTemplateDto.builder().code("DERIVATIVES").name(Map.of("ko", "파생상품/기타", "en", "Derivatives & Others")).icon("pie_chart").order(3).build()
                ))
                .sectors(List.of(
                        SectorTemplateDto.builder().code("SEC_IDENTIFIER").name(Map.of("ko", "종목 기본 식별", "en", "Security Identification")).order(1)
                                .groups(List.of(
                                        FieldGroupTemplateDto.builder().code("TICKER_BASIC_GROUP").name(Map.of("ko", "티커 및 종목명", "en", "Ticker & Names")).order(1).isDefaultOpen(true).build(),
                                        FieldGroupTemplateDto.builder().code("MARKET_SECTOR_GROUP").name(Map.of("ko", "시장 및 업종 분류", "en", "Market & Sector")).order(2).isDefaultOpen(true).build()
                                )).build(),
                        SectorTemplateDto.builder().code("CAPITAL_SHARES").name(Map.of("ko", "발행 및 자본금", "en", "Shares & Capitalization")).order(2)
                                .groups(List.of(
                                        FieldGroupTemplateDto.builder().code("SHARES_GROUP").name(Map.of("ko", "액면가 및 상장주식수", "en", "Par Value & Shares")).order(1).isDefaultOpen(true).build(),
                                        FieldGroupTemplateDto.builder().code("PRICE_VALUATION_GROUP").name(Map.of("ko", "기준 시세 및 시가총액", "en", "Price & Valuation")).order(2).isDefaultOpen(true).build(),
                                        FieldGroupTemplateDto.builder().code("LISTING_SCHEDULE_GROUP").name(Map.of("ko", "상장 일정 및 결산", "en", "Listing Date & Fiscal")).order(3).isDefaultOpen(true).build()
                                )).build(),
                        SectorTemplateDto.builder().code("TRADING_GOVERNANCE").name(Map.of("ko", "매매 및 수급 지표", "en", "Trading & Market Metrics")).order(3)
                                .groups(List.of(
                                        FieldGroupTemplateDto.builder().code("TRADING_HALT_GROUP").name(Map.of("ko", "거래 제한 및 예탁 기관", "en", "Trading Status & Agent")).order(1).isDefaultOpen(true).build(),
                                        FieldGroupTemplateDto.builder().code("MARKET_TRADING_METRICS_GROUP").name(Map.of("ko", "신용 및 공매도 지표", "en", "Margin & Short Trading")).order(2).isDefaultOpen(true).build(),
                                        FieldGroupTemplateDto.builder().code("INVESTOR_TRADING_GROUP").name(Map.of("ko", "투자자별 매매동향 및 외인지분", "en", "Investor Trading Breakdown")).order(3).isDefaultOpen(true).build()
                                )).build(),
                        SectorTemplateDto.builder().code("CORP_IR").name(Map.of("ko", "IR 및 사업 개요", "en", "IR & Business Summary")).order(4)
                                .groups(List.of(
                                        FieldGroupTemplateDto.builder().code("IR_INFO_GROUP").name(Map.of("ko", "투자 정보 및 요약", "en", "IR Information")).order(1).isDefaultOpen(true).build()
                                )).build()
                ))
                .fields(List.of(
                        FieldTemplateDto.builder().key("ticker_code").groupCode("TICKER_BASIC_GROUP").name(Map.of("ko", "종목코드(티커)", "en", "Ticker Code")).type("TEXT").required(true).isSearchable(true).isGridVisible(true).gridWidth(3).tableColumnWidth(140).order(1).build(),
                        FieldTemplateDto.builder().key("isin_code").groupCode("TICKER_BASIC_GROUP").name(Map.of("ko", "ISIN 코드", "en", "ISIN Code")).type("TEXT").required(false).isSearchable(true).isGridVisible(true).gridWidth(3).tableColumnWidth(150).order(2).build(),
                        FieldTemplateDto.builder().key("stock_name").groupCode("TICKER_BASIC_GROUP").name(Map.of("ko", "종목명(한글)", "en", "Stock Name")).type("TEXT").required(true).isSearchable(true).isGridVisible(true).gridWidth(4).tableColumnWidth(200).order(3).build(),
                        FieldTemplateDto.builder().key("stock_name_en").groupCode("TICKER_BASIC_GROUP").name(Map.of("ko", "영문 종목명", "en", "Stock Name (EN)")).type("TEXT").required(false).isGridVisible(true).gridWidth(4).tableColumnWidth(200).order(4).build(),

                        FieldTemplateDto.builder().key("market_type").groupCode("MARKET_SECTOR_GROUP").name(Map.of("ko", "상장 시장", "en", "Market Type")).type("SELECT").required(true).isFilterable(true).isGridVisible(true).gridWidth(3).tableColumnWidth(130).order(5)
                                .options("[{\"key\":\"KOSPI\",\"value\":\"KOSPI\",\"label\":{\"ko\":\"코스피(KOSPI)\",\"en\":\"KOSPI\"}},{\"key\":\"KOSDAQ\",\"value\":\"KOSDAQ\",\"label\":{\"ko\":\"코스닥(KOSDAQ)\",\"en\":\"KOSDAQ\"}},{\"key\":\"KONEX\",\"value\":\"KONEX\",\"label\":{\"ko\":\"코넥스(KONEX)\",\"en\":\"KONEX\"}},{\"key\":\"NYSE\",\"value\":\"NYSE\",\"label\":{\"ko\":\"NYSE\",\"en\":\"NYSE\"}},{\"key\":\"NASDAQ\",\"value\":\"NASDAQ\",\"label\":{\"ko\":\"NASDAQ\",\"en\":\"NASDAQ\"}},{\"key\":\"OVERSEAS\",\"value\":\"OVERSEAS\",\"label\":{\"ko\":\"기타 해외\",\"en\":\"Overseas\"}}]").build(),
                        FieldTemplateDto.builder().key("industry_sector").groupCode("MARKET_SECTOR_GROUP").name(Map.of("ko", "업종/섹터 (WICS)", "en", "Industry Sector")).type("TEXT").required(false).isFilterable(true).isGridVisible(true).gridWidth(3).tableColumnWidth(150).order(6).build(),
                        FieldTemplateDto.builder().key("security_type").groupCode("MARKET_SECTOR_GROUP").name(Map.of("ko", "증권 종류", "en", "Security Type")).type("SELECT").required(false).isFilterable(true).isGridVisible(true).gridWidth(3).tableColumnWidth(130).order(7)
                                .options("[{\"key\":\"COMMON\",\"value\":\"COMMON\",\"label\":{\"ko\":\"보통주\",\"en\":\"Common Stock\"}},{\"key\":\"PREFERRED\",\"value\":\"PREFERRED\",\"label\":{\"ko\":\"우선주\",\"en\":\"Preferred Stock\"}},{\"key\":\"ETF\",\"value\":\"ETF\",\"label\":{\"ko\":\"ETF\",\"en\":\"ETF\"}},{\"key\":\"ETN\",\"value\":\"ETN\",\"label\":{\"ko\":\"ETN\",\"en\":\"ETN\"}},{\"key\":\"REIT\",\"value\":\"REIT\",\"label\":{\"ko\":\"리츠(REITs)\",\"en\":\"REITs\"}}]").build(),

                        FieldTemplateDto.builder().key("par_value").groupCode("SHARES_GROUP").name(Map.of("ko", "액면가", "en", "Par Value")).type("NUMBER").unit("KRW").required(false).isGridVisible(true).gridWidth(2).tableColumnWidth(130).order(8).build(),
                        FieldTemplateDto.builder().key("listed_shares").groupCode("SHARES_GROUP").name(Map.of("ko", "상장주식수", "en", "Listed Shares")).type("NUMBER").required(false).isGridVisible(true).gridWidth(3).tableColumnWidth(150).order(9).build(),
                        FieldTemplateDto.builder().key("capital_amount").groupCode("SHARES_GROUP").name(Map.of("ko", "자본금", "en", "Capital Amount")).type("NUMBER").unit("KRW").required(false).isGridVisible(true).gridWidth(3).tableColumnWidth(150).order(10).build(),
                        FieldTemplateDto.builder().key("currency").groupCode("SHARES_GROUP").name(Map.of("ko", "거래 통화", "en", "Currency")).type("SELECT").required(false).isGridVisible(true).gridWidth(2).tableColumnWidth(110).order(11)
                                .options("[{\"key\":\"KRW\",\"value\":\"KRW\",\"label\":{\"ko\":\"KRW\",\"en\":\"KRW\"}},{\"key\":\"USD\",\"value\":\"USD\",\"label\":{\"ko\":\"USD\",\"en\":\"USD\"}},{\"key\":\"EUR\",\"value\":\"EUR\",\"label\":{\"ko\":\"EUR\",\"en\":\"EUR\"}},{\"key\":\"JPY\",\"value\":\"JPY\",\"label\":{\"ko\":\"JPY\",\"en\":\"JPY\"}}]").build(),

                        FieldTemplateDto.builder().key("current_price").groupCode("PRICE_VALUATION_GROUP").name(Map.of("ko", "최근 기준가/종가", "en", "Current/Close Price")).type("NUMBER").required(false).isGridVisible(true).gridWidth(3).tableColumnWidth(140).order(12).build(),
                        FieldTemplateDto.builder().key("previous_close_price").groupCode("PRICE_VALUATION_GROUP").name(Map.of("ko", "전일 종가", "en", "Previous Close Price")).type("NUMBER").required(false).isGridVisible(false).gridWidth(3).tableColumnWidth(130).order(13).build(),
                        FieldTemplateDto.builder().key("market_cap").groupCode("PRICE_VALUATION_GROUP").name(Map.of("ko", "시가총액", "en", "Market Capitalization")).type("NUMBER").required(false).isGridVisible(true).gridWidth(3).tableColumnWidth(160).order(14).build(),
                        FieldTemplateDto.builder().key("week52_high").groupCode("PRICE_VALUATION_GROUP").name(Map.of("ko", "52주 최고가", "en", "52-Week High")).type("NUMBER").required(false).isGridVisible(false).gridWidth(3).tableColumnWidth(130).order(15).build(),
                        FieldTemplateDto.builder().key("week52_low").groupCode("PRICE_VALUATION_GROUP").name(Map.of("ko", "52주 최저가", "en", "52-Week Low")).type("NUMBER").required(false).isGridVisible(false).gridWidth(3).tableColumnWidth(130).order(16).build(),
                        FieldTemplateDto.builder().key("price_base_date").groupCode("PRICE_VALUATION_GROUP").name(Map.of("ko", "시세 기준일자", "en", "Price Base Date")).type("DATE").required(false).isGridVisible(true).gridWidth(3).tableColumnWidth(130).order(17).build(),

                        FieldTemplateDto.builder().key("listing_date").groupCode("LISTING_SCHEDULE_GROUP").name(Map.of("ko", "최초 상장일자", "en", "Listing Date")).type("DATE").required(false).isGridVisible(true).gridWidth(3).tableColumnWidth(130).order(18).build(),
                        FieldTemplateDto.builder().key("fiscal_month").groupCode("LISTING_SCHEDULE_GROUP").name(Map.of("ko", "결산월", "en", "Fiscal Month")).type("SELECT").required(false).isGridVisible(true).gridWidth(2).tableColumnWidth(110).order(19)
                                .options("[{\"key\":\"12\",\"value\":\"12\",\"label\":{\"ko\":\"12월 결산\",\"en\":\"Dec\"}},{\"key\":\"3\",\"value\":\"3\",\"label\":{\"ko\":\"3월 결산\",\"en\":\"Mar\"}},{\"key\":\"6\",\"value\":\"6\",\"label\":{\"ko\":\"6월 결산\",\"en\":\"Jun\"}},{\"key\":\"9\",\"value\":\"9\",\"label\":{\"ko\":\"9월 결산\",\"en\":\"Sep\"}}]").build(),

                        FieldTemplateDto.builder().key("is_trading_halt").groupCode("TRADING_HALT_GROUP").name(Map.of("ko", "거래정지 여부", "en", "Is Trading Halt")).type("BOOLEAN").required(false).isFilterable(true).isGridVisible(true).gridWidth(2).tableColumnWidth(120).order(20).build(),
                        FieldTemplateDto.builder().key("is_delisting_risk").groupCode("TRADING_HALT_GROUP").name(Map.of("ko", "관리/투자주의 종목", "en", "Delisting Risk")).type("BOOLEAN").required(false).isFilterable(true).isGridVisible(true).gridWidth(2).tableColumnWidth(130).order(21).build(),
                        FieldTemplateDto.builder().key("transfer_agent").groupCode("TRADING_HALT_GROUP").name(Map.of("ko", "명의개서대행기관", "en", "Transfer Agent")).type("SELECT").required(false).isGridVisible(true).gridWidth(3).tableColumnWidth(150).order(22)
                                .options("[{\"key\":\"KSD\",\"value\":\"KSD\",\"label\":{\"ko\":\"한국예탁결제원\",\"en\":\"KSD\"}},{\"key\":\"KB\",\"value\":\"KB\",\"label\":{\"ko\":\"국민은행\",\"en\":\"KB Bank\"}},{\"key\":\"HANA\",\"value\":\"HANA\",\"label\":{\"ko\":\"하나은행\",\"en\":\"Hana Bank\"}}]").build(),
                        FieldTemplateDto.builder().key("settlement_cycle").groupCode("TRADING_HALT_GROUP").name(Map.of("ko", "결제주기", "en", "Settlement Cycle")).type("SELECT").required(false).isGridVisible(true).gridWidth(2).tableColumnWidth(120).order(23)
                                .options("[{\"key\":\"T_PLUS_2\",\"value\":\"T_PLUS_2\",\"label\":{\"ko\":\"T+2일 결제\",\"en\":\"T+2\"}},{\"key\":\"T_PLUS_1\",\"value\":\"T_PLUS_1\",\"label\":{\"ko\":\"T+1일 결제\",\"en\":\"T+1\"}}]").build(),

                        FieldTemplateDto.builder().key("margin_balance_shares").groupCode("MARKET_TRADING_METRICS_GROUP").name(Map.of("ko", "누적 신용잔고 수량", "en", "Margin Balance Shares")).type("NUMBER").unit("Shares").required(false).isGridVisible(true).gridWidth(3).tableColumnWidth(150).order(24).build(),
                        FieldTemplateDto.builder().key("margin_balance_ratio").groupCode("MARKET_TRADING_METRICS_GROUP").name(Map.of("ko", "신용잔고 비율(%)", "en", "Margin Balance Ratio (%)")).type("NUMBER").unit("%").required(false).isGridVisible(true).gridWidth(2).tableColumnWidth(120).order(25).build(),
                        FieldTemplateDto.builder().key("short_selling_balance_shares").groupCode("MARKET_TRADING_METRICS_GROUP").name(Map.of("ko", "누적 공매도 잔고수량", "en", "Short Selling Balance Shares")).type("NUMBER").unit("Shares").required(false).isGridVisible(true).gridWidth(3).tableColumnWidth(160).order(26).build(),
                        FieldTemplateDto.builder().key("short_selling_ratio").groupCode("MARKET_TRADING_METRICS_GROUP").name(Map.of("ko", "공매도 잔고비율(%)", "en", "Short Selling Ratio (%)")).type("NUMBER").unit("%").required(false).isGridVisible(true).gridWidth(2).tableColumnWidth(130).order(27).build(),
                        FieldTemplateDto.builder().key("is_short_selling_overheated").groupCode("MARKET_TRADING_METRICS_GROUP").name(Map.of("ko", "공매도 과열종목 지정 여부", "en", "Short Selling Overheated")).type("BOOLEAN").required(false).isGridVisible(false).gridWidth(2).tableColumnWidth(130).order(28).build(),

                        FieldTemplateDto.builder().key("foreign_ownership_ratio").groupCode("INVESTOR_TRADING_GROUP").name(Map.of("ko", "외국인 지분율(%)", "en", "Foreign Ownership Ratio (%)")).type("NUMBER").unit("%").required(false).isGridVisible(true).gridWidth(2).tableColumnWidth(130).order(29).build(),
                        FieldTemplateDto.builder().key("foreign_holding_shares").groupCode("INVESTOR_TRADING_GROUP").name(Map.of("ko", "외국인 보유주식수", "en", "Foreign Holding Shares")).type("NUMBER").unit("Shares").required(false).isGridVisible(false).gridWidth(3).tableColumnWidth(150).order(30).build(),
                        FieldTemplateDto.builder().key("foreign_daily_net_buy").groupCode("INVESTOR_TRADING_GROUP").name(Map.of("ko", "외국인 당일 순매수", "en", "Foreign Daily Net Buy")).type("NUMBER").unit("Shares").required(false).isGridVisible(true).gridWidth(3).tableColumnWidth(140).order(31).build(),
                        FieldTemplateDto.builder().key("inst_daily_net_buy").groupCode("INVESTOR_TRADING_GROUP").name(Map.of("ko", "기관 당일 순매수", "en", "Institutional Daily Net Buy")).type("NUMBER").unit("Shares").required(false).isGridVisible(true).gridWidth(3).tableColumnWidth(140).order(32).build(),
                        FieldTemplateDto.builder().key("retail_daily_net_buy").groupCode("INVESTOR_TRADING_GROUP").name(Map.of("ko", "개인 당일 순매수", "en", "Retail Daily Net Buy")).type("NUMBER").unit("Shares").required(false).isGridVisible(true).gridWidth(3).tableColumnWidth(140).order(33).build(),
                        FieldTemplateDto.builder().key("foreign_cumulative_net_buy_20d").groupCode("INVESTOR_TRADING_GROUP").name(Map.of("ko", "외국인 20일 누적순매수", "en", "Foreign 20D Net Buy")).type("NUMBER").unit("Shares").required(false).isGridVisible(false).gridWidth(3).tableColumnWidth(150).order(34).build(),
                        FieldTemplateDto.builder().key("inst_cumulative_net_buy_20d").groupCode("INVESTOR_TRADING_GROUP").name(Map.of("ko", "기관 20일 누적순매수", "en", "Institutional 20D Net Buy")).type("NUMBER").unit("Shares").required(false).isGridVisible(false).gridWidth(3).tableColumnWidth(150).order(35).build(),
                        FieldTemplateDto.builder().key("retail_cumulative_net_buy_20d").groupCode("INVESTOR_TRADING_GROUP").name(Map.of("ko", "개인 20일 누적순매수", "en", "Retail 20D Net Buy")).type("NUMBER").unit("Shares").required(false).isGridVisible(false).gridWidth(3).tableColumnWidth(150).order(36).build(),

                        FieldTemplateDto.builder().key("investor_relations_url").groupCode("IR_INFO_GROUP").name(Map.of("ko", "IR 웹사이트 링크", "en", "IR URL")).type("TEXT").required(false).isGridVisible(false).gridWidth(4).tableColumnWidth(200).order(37).build(),
                        FieldTemplateDto.builder().key("business_summary").groupCode("IR_INFO_GROUP").name(Map.of("ko", "주요 사업 내용 및 투자설명", "en", "Business Summary")).type("HTML_TEXT").required(false).isGridVisible(false).gridWidth(8).tableColumnWidth(250).order(38).build()
                ))
                .dqRules(List.of(
                        DqRuleTemplateDto.builder().fieldKey("stock_name").ruleType("NOT_NULL").severity("ERROR").message(Map.of("ko", "종목명은 필수 입력 항목입니다.", "en", "Stock name is required.")).build(),
                        DqRuleTemplateDto.builder().fieldKey("par_value").ruleType("RANGE").severity("WARNING").params("{\"min\":0}").message(Map.of("ko", "액면가는 0 이상이어야 합니다.", "en", "Par value must be greater than or equal to 0.")).build(),
                        DqRuleTemplateDto.builder().fieldKey("listed_shares").ruleType("RANGE").severity("WARNING").params("{\"min\":1}").message(Map.of("ko", "상장주식수는 1주 이상이어야 합니다.", "en", "Listed shares must be at least 1.")).build()
                ))
                .build());
    }

    @Transactional(readOnly = true)
    public List<SpecializedDomainTemplateDto> getTemplates() {
        return new ArrayList<>(TEMPLATES.values());
    }

    @Transactional(readOnly = true)
    public SpecializedDomainTemplateDto getTemplate(String category) {
        SpecializedDomainTemplateDto template = TEMPLATES.get(category != null ? category.toUpperCase() : "");
        if (template == null) {
            throw new ResourceNotFoundException("Specialized domain template not found: " + category);
        }
        return template;
    }

    private Set<String> getValidFieldTypes() {
        try {
            if (codeDetailRepository != null) {
                List<CodeDetail> details = codeDetailRepository.findByCodeGroupGroupCodeAndIsActiveTrue("FIELD_TYPE");
                if (details != null && !details.isEmpty()) {
                    return details.stream()
                            .map(CodeDetail::getDetailCode)
                            .filter(Objects::nonNull)
                            .map(String::toUpperCase)
                            .collect(Collectors.toSet());
                }
            }
        } catch (Exception e) {
            log.warn("Failed to load FIELD_TYPE common codes, falling back to default set", e);
        }
        return Set.of(
                "TEXT", "NUMBER", "DATE", "BOOLEAN", "JSON", "SELECT",
                "DOMAIN_REFERENCE", "TIME", "HTML_TEXT", "CALCULATED",
                "MULTILINGUAL", "FILE", "IMAGE", "DATE_RANGE", "EMAIL"
        );
    }

    @Transactional
    public DomainResponse provisionDomain(SpecializedDomainProvisionRequest request) {
        String category = request.getCategory() != null ? request.getCategory().toUpperCase() : "";
        SpecializedDomainTemplateDto template = getTemplate(category);
        Set<String> validFieldTypes = getValidFieldTypes();

        // 1. Find existing domain for Merge or Create new Domain
        Domain domain = domainRepository.findBySpecializedCategory(category).orElse(null);
        if (domain == null) {
            domain = new Domain();
            domain.setDomainType("SPECIALIZED");
            domain.setSpecializedCategory(category);
            domain.setSortOrder(0);
        }

        domain.setName(request.getName() != null && !request.getName().isEmpty() ? request.getName() : template.getName());
        domain.setDescription(request.getDescription() != null ? request.getDescription() : template.getDescription());
        domain.setIcon(request.getIcon() != null && !request.getIcon().isBlank() ? request.getIcon() : template.getIcon());
        domain.setNumberingPattern(request.getNumberingPattern() != null && !request.getNumberingPattern().isBlank() ? request.getNumberingPattern() : template.getNumberingPattern());
        domain.setAutoDqScanEnabled(true);

        Domain savedDomain = domainRepository.save(domain);
        UUID domainId = savedDomain.getId();

        // 2. Merge/Create Classification Axis
        ClassificationAxis axis;
        List<ClassificationAxis> existingAxes = axisRepository.findByDomainIdOrderBySortOrderAsc(domainId);
        if (existingAxes != null && !existingAxes.isEmpty()) {
            axis = existingAxes.stream()
                    .filter(a -> template.getAxisCode().equalsIgnoreCase(a.getAxisCode()))
                    .findFirst()
                    .orElse(existingAxes.get(0));
            axis.setName(template.getAxisName());
        } else {
            axis = new ClassificationAxis();
            axis.setDomain(savedDomain);
            axis.setAxisCode(template.getAxisCode());
            axis.setName(template.getAxisName());
            axis.setIsDefault(true);
            axis.setSortOrder(0);
            axis = axisRepository.save(axis);
        }

        // 3. Merge/Create Predefined Child Classification Nodes (No dummy root node)
        createPredefinedNodes(savedDomain, axis, template);

        // 4. Merge/Create Predefined Sectors & Field Groups
        Map<String, FieldGroup> createdGroups = createSectorsAndFieldGroups(savedDomain, template);

        // 5. Merge/Create Standard FieldDefinitions
        Map<String, FieldDefinition> existingFieldMap = new HashMap<>();
        List<FieldDefinition> existingFields = fieldDefinitionRepository.findByDomain_Id(domainId);
        if (existingFields != null) {
            for (FieldDefinition fd : existingFields) {
                if (fd.getKey() != null) {
                    existingFieldMap.put(fd.getKey().toUpperCase(), fd);
                }
            }
        }

        Map<String, FieldDefinition> createdFields = new HashMap<>();
        if (template.getFields() != null) {
            for (FieldTemplateDto ft : template.getFields()) {
                String fieldKey = ft.getKey();
                String upperKey = fieldKey != null ? fieldKey.toUpperCase() : "";
                FieldDefinition fd = existingFieldMap.getOrDefault(upperKey, new FieldDefinition());

                fd.setDomain(savedDomain);
                fd.setDefinedAtNode(null); // Global domain field (no dummy root node)

                String upperGroupCode = ft.getGroupCode() != null ? ft.getGroupCode().toUpperCase() : null;
                if (upperGroupCode != null && createdGroups.containsKey(upperGroupCode)) {
                    fd.setFieldGroup(createdGroups.get(upperGroupCode));
                }

                fd.setKey(fieldKey);
                fd.setName(ft.getName());
                fd.setHint(ft.getHint());

                // Field Type validation against Common Codes
                String rawType = ft.getType() != null ? ft.getType().toUpperCase() : "TEXT";
                String finalType = validFieldTypes.contains(rawType) ? rawType : "TEXT";
                fd.setType(finalType);

                fd.setUnit(ft.getUnit());
                fd.setRequired(Boolean.TRUE.equals(ft.getRequired()));
                fd.setIsSearchable(Boolean.TRUE.equals(ft.getIsSearchable()));
                fd.setGridWidth(ft.getGridWidth() != null ? ft.getGridWidth() : 3);
                fd.setTableColumnWidth(ft.getTableColumnWidth() != null ? ft.getTableColumnWidth() : 150);
                fd.setOrder(ft.getOrder() != null ? ft.getOrder() : 0);
                fd.setOptions(ft.getOptions());

                FieldDefinition savedFd = fieldDefinitionRepository.save(fd);
                if (fieldKey != null) {
                    createdFields.put(fieldKey.toUpperCase(), savedFd);
                    createdFields.put(fieldKey.toLowerCase(), savedFd);
                }
            }
        }

        // 6. Bind Identifier and Display Name fields to Domain
        String idKey = template.getIdentifierFieldKey() != null ? template.getIdentifierFieldKey().toUpperCase() : "";
        String nameKey = template.getDisplayNameFieldKey() != null ? template.getDisplayNameFieldKey().toUpperCase() : "";
        if (createdFields.containsKey(idKey)) {
            savedDomain.setIdentifierFieldId(createdFields.get(idKey).getId());
        }
        if (createdFields.containsKey(nameKey)) {
            savedDomain.setDisplayNameFieldId(createdFields.get(nameKey).getId());
        }
        savedDomain = domainRepository.save(savedDomain);

        // 7. Merge/Create Standard DqRules
        if (template.getDqRules() != null) {
            Map<String, DqRule> existingDqRuleMap = new HashMap<>();
            List<DqRule> existingDqRules = dqRuleRepository.findByDomainId(domainId);
            if (existingDqRules != null) {
                for (DqRule r : existingDqRules) {
                    if (r.getFieldDefinition() != null && r.getRuleType() != null) {
                        String ruleKey = r.getFieldDefinition().getId() + "_" + r.getRuleType().name();
                        existingDqRuleMap.put(ruleKey, r);
                    }
                }
            }

            int sortOrder = 0;
            for (DqRuleTemplateDto rt : template.getDqRules()) {
                String fieldKey = rt.getFieldKey() != null ? rt.getFieldKey().toUpperCase() : "";
                FieldDefinition targetField = createdFields.get(fieldKey);
                if (targetField != null) {
                    DqRuleType ruleType = DqRuleType.valueOf(rt.getRuleType());
                    String ruleKey = targetField.getId() + "_" + ruleType.name();
                    DqRule rule = existingDqRuleMap.getOrDefault(ruleKey, new DqRule());

                    rule.setDomainId(savedDomain.getId());
                    rule.setNodeId(null);
                    rule.setFieldDefinition(targetField);
                    rule.setRuleType(ruleType);
                    rule.setSeverity(DqSeverity.valueOf(rt.getSeverity()));
                    rule.setParams(rt.getParams());
                    rule.setMessage(rt.getMessage());
                    rule.setSortOrder(sortOrder++);
                    rule.setIsActive(true);
                    dqRuleRepository.save(rule);
                }
            }
        }

        return DomainResponse.from(savedDomain);
    }

    public void createPredefinedNodes(Domain domain, ClassificationAxis axis, SpecializedDomainTemplateDto template) {
        if (template.getNodes() == null || template.getNodes().isEmpty()) return;

        List<ClassificationNode> existingNodes = nodeRepository.findByDomain_Id(domain.getId());
        Map<String, ClassificationNode> existingNodeMap = new HashMap<>();
        if (existingNodes != null) {
            for (ClassificationNode n : existingNodes) {
                if (n.getName() != null) {
                    String koName = n.getName().get("ko");
                    if (koName != null) existingNodeMap.put(koName.toUpperCase(), n);
                    String enName = n.getName().get("en");
                    if (enName != null) existingNodeMap.put(enName.toUpperCase(), n);
                }
            }
        }

        Map<String, ClassificationNode> processedNodes = new HashMap<>();
        String axisPath = "/" + (axis.getAxisCode() != null ? axis.getAxisCode().toLowerCase() : "axis");

        for (ClassificationNodeTemplateDto nt : template.getNodes()) {
            ClassificationNode parentNode = nt.getParentCode() != null && processedNodes.containsKey(nt.getParentCode())
                    ? processedNodes.get(nt.getParentCode())
                    : null;

            String nodeNameStr = nt.getName() != null ? nt.getName().getOrDefault("ko", nt.getName().get("en")) : nt.getCode();
            int nodeDepth = parentNode != null && parentNode.getDepth() != null ? parentNode.getDepth() + 1 : 0;
            String parentPath = parentNode != null && parentNode.getPath() != null ? parentNode.getPath() : axisPath;
            String nodePath = parentPath + "/" + nodeNameStr;

            String matchKey = nodeNameStr != null ? nodeNameStr.toUpperCase() : "";
            ClassificationNode node = existingNodeMap.getOrDefault(matchKey, new ClassificationNode());

            node.setDomain(domain);
            node.setAxis(axis);
            node.setParent(parentNode);
            node.setName(nt.getName());
            node.setPath(nodePath);
            node.setDepth(nodeDepth);
            node.setOrder(nt.getOrder() != null ? nt.getOrder() : 0);
            node.setIcon(nt.getIcon());
            node.setIsDeleted(false);

            ClassificationNode savedNode = nodeRepository.save(node);
            processedNodes.put(nt.getCode(), savedNode != null ? savedNode : node);
        }
    }

    public Map<String, FieldGroup> createSectorsAndFieldGroups(Domain domain, SpecializedDomainTemplateDto template) {
        Map<String, FieldGroup> processedGroups = new HashMap<>();
        if (template.getSectors() == null || template.getSectors().isEmpty()) return processedGroups;

        List<Sector> existingSectors = sectorRepository.findByDomainIdOrderBySortOrderAsc(domain.getId());
        Map<String, Sector> existingSectorMap = new HashMap<>();
        if (existingSectors != null) {
            for (Sector s : existingSectors) {
                if (s.getName() != null) {
                    String ko = s.getName().get("ko");
                    if (ko != null) existingSectorMap.put(ko.toUpperCase(), s);
                }
            }
        }

        List<FieldGroup> existingGroups = fieldGroupRepository.findByDomainIdOrderBySortOrderAsc(domain.getId());
        Map<String, FieldGroup> existingGroupMap = new HashMap<>();
        if (existingGroups != null) {
            for (FieldGroup fg : existingGroups) {
                if (fg.getName() != null) {
                    String ko = fg.getName().get("ko");
                    if (ko != null) existingGroupMap.put(ko.toUpperCase(), fg);
                }
            }
        }

        for (SectorTemplateDto st : template.getSectors()) {
            String sectorNameKo = st.getName() != null ? st.getName().get("ko") : "";
            String matchKey = sectorNameKo != null ? sectorNameKo.toUpperCase() : "";
            Sector sector = existingSectorMap.getOrDefault(matchKey, new Sector());

            sector.setDomain(domain);
            sector.setName(st.getName());
            sector.setSortOrder(st.getOrder() != null ? st.getOrder() : 0);
            Sector savedSector = sectorRepository.save(sector);

            if (st.getGroups() != null) {
                for (FieldGroupTemplateDto gt : st.getGroups()) {
                    String groupNameKo = gt.getName() != null ? gt.getName().get("ko") : "";
                    String grpMatchKey = groupNameKo != null ? groupNameKo.toUpperCase() : "";
                    FieldGroup group = existingGroupMap.getOrDefault(grpMatchKey, new FieldGroup());

                    group.setDomain(domain);
                    group.setSector(savedSector);
                    group.setName(gt.getName());
                    group.setSortOrder(gt.getOrder() != null ? gt.getOrder() : 0);
                    group.setIsDefaultOpen(gt.getIsDefaultOpen() != null ? gt.getIsDefaultOpen() : true);
                    FieldGroup savedGroup = fieldGroupRepository.save(group);
                    if (gt.getCode() != null) {
                        processedGroups.put(gt.getCode().toUpperCase(), savedGroup);
                        processedGroups.put(gt.getCode().toLowerCase(), savedGroup);
                    }
                }
            }
        }
        return processedGroups;
    }
}
