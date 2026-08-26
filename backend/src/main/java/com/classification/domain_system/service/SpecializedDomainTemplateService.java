package com.classification.domain_system.service;

import com.classification.domain_system.dto.DomainResponse;
import com.classification.domain_system.dto.SpecializedDomainProvisionRequest;
import com.classification.domain_system.dto.SpecializedDomainTemplateDto;
import com.classification.domain_system.dto.SpecializedDomainTemplateDto.DqRuleTemplateDto;
import com.classification.domain_system.dto.SpecializedDomainTemplateDto.FieldTemplateDto;
import com.classification.domain_system.entity.*;
import com.classification.domain_system.exception.ResourceNotFoundException;
import com.classification.domain_system.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class SpecializedDomainTemplateService {

    private final DomainRepository domainRepository;
    private final ClassificationAxisRepository axisRepository;
    private final ClassificationNodeRepository nodeRepository;
    private final FieldDefinitionRepository fieldDefinitionRepository;
    private final DqRuleRepository dqRuleRepository;

    private static final Map<String, SpecializedDomainTemplateDto> TEMPLATES = new LinkedHashMap<>();

    static {
        // 1. CUSTOMER (고객)
        TEMPLATES.put("CUSTOMER", SpecializedDomainTemplateDto.builder()
                .category("CUSTOMER")
                .name(Map.of("ko", "고객 마스터", "en", "Customer Master"))
                .description(Map.of("ko", "B2B/B2C 고객 정보, 식별번호, 연락처 및 세그먼트 관리", "en", "B2B/B2C Customer Master Data"))
                .icon("person_pin")
                .numberingPattern("CUST-{YYYY}-{SEQ:6}")
                .axisName(Map.of("ko", "고객 구분", "en", "Customer Type"))
                .axisCode("CUSTOMER_TYPE")
                .rootNodeName(Map.of("ko", "전체 고객", "en", "All Customers"))
                .identifierFieldKey("customer_no")
                .displayNameFieldKey("customer_name")
                .fields(List.of(
                        FieldTemplateDto.builder().key("customer_no").name(Map.of("ko", "고객번호", "en", "Customer No")).type("TEXT").required(true).isSearchable(true).isGridVisible(true).gridWidth(160).order(1).build(),
                        FieldTemplateDto.builder().key("customer_name").name(Map.of("ko", "고객명", "en", "Customer Name")).type("TEXT").required(true).isSearchable(true).isGridVisible(true).gridWidth(200).order(2).build(),
                        FieldTemplateDto.builder().key("customer_type").name(Map.of("ko", "고객유형", "en", "Customer Type")).type("SELECT").required(false).isFilterable(true).isGridVisible(true).gridWidth(120).order(3)
                                .options("[{\"value\":\"INDIVIDUAL\",\"label\":{\"ko\":\"개인\",\"en\":\"Individual\"}},{\"value\":\"CORPORATE\",\"label\":{\"ko\":\"법인\",\"en\":\"Corporate\"}}]").build(),
                        FieldTemplateDto.builder().key("contact_email").name(Map.of("ko", "연락처 이메일", "en", "Email")).type("EMAIL").required(false).isSearchable(true).isGridVisible(true).gridWidth(220).order(4).build(),
                        FieldTemplateDto.builder().key("contact_phone").name(Map.of("ko", "연락처 전화번호", "en", "Phone")).type("TEXT").required(false).isGridVisible(true).gridWidth(150).order(5).build(),
                        FieldTemplateDto.builder().key("registration_date").name(Map.of("ko", "가입/등록일자", "en", "Registration Date")).type("DATE").required(false).isGridVisible(true).gridWidth(130).order(6).build(),
                        FieldTemplateDto.builder().key("status").name(Map.of("ko", "고객 상태", "en", "Status")).type("SELECT").required(false).isFilterable(true).isGridVisible(true).gridWidth(110).order(7)
                                .options("[{\"value\":\"ACTIVE\",\"label\":{\"ko\":\"정상\",\"en\":\"Active\"}},{\"value\":\"DORMANT\",\"label\":{\"ko\":\"휴면\",\"en\":\"Dormant\"}},{\"value\":\"INACTIVE\",\"label\":{\"ko\":\"해지\",\"en\":\"Inactive\"}}]").build()
                ))
                .dqRules(List.of(
                        DqRuleTemplateDto.builder().fieldKey("customer_name").ruleType("NOT_NULL").severity("ERROR").message(Map.of("ko", "고객명은 필수 입력 항목입니다.", "en", "Customer name is required.")).build(),
                        DqRuleTemplateDto.builder().fieldKey("contact_email").ruleType("REGEX").severity("WARNING").params("{\"regex\":\"^[\\\\w-\\\\.]+@([\\\\w-]+\\\\.)+[\\\\w-]{2,4}$\"}").message(Map.of("ko", "유효한 이메일 형식이 아닙니다.", "en", "Invalid email format.")).build()
                ))
                .build());

        // 2. VENDOR (거래처)
        TEMPLATES.put("VENDOR", SpecializedDomainTemplateDto.builder()
                .category("VENDOR")
                .name(Map.of("ko", "거래처 마스터", "en", "Vendor Master"))
                .description(Map.of("ko", "공급사, 외주처, 협력사 및 사업자 정보 관리", "en", "Vendor & Partner Master Data"))
                .icon("corporate_fare")
                .numberingPattern("VEND-{YYYY}-{SEQ:6}")
                .axisName(Map.of("ko", "거래처 유형", "en", "Vendor Type"))
                .axisCode("VENDOR_TYPE")
                .rootNodeName(Map.of("ko", "전체 거래처", "en", "All Vendors"))
                .identifierFieldKey("vendor_code")
                .displayNameFieldKey("vendor_name")
                .fields(List.of(
                        FieldTemplateDto.builder().key("vendor_code").name(Map.of("ko", "거래처코드", "en", "Vendor Code")).type("TEXT").required(true).isSearchable(true).isGridVisible(true).gridWidth(160).order(1).build(),
                        FieldTemplateDto.builder().key("vendor_name").name(Map.of("ko", "거래처명", "en", "Vendor Name")).type("TEXT").required(true).isSearchable(true).isGridVisible(true).gridWidth(200).order(2).build(),
                        FieldTemplateDto.builder().key("biz_reg_no").name(Map.of("ko", "사업자등록번호", "en", "Business Reg No")).type("TEXT").required(true).isSearchable(true).isGridVisible(true).gridWidth(150).order(3).build(),
                        FieldTemplateDto.builder().key("ceo_name").name(Map.of("ko", "대표자명", "en", "CEO Name")).type("TEXT").required(false).isGridVisible(true).gridWidth(120).order(4).build(),
                        FieldTemplateDto.builder().key("biz_type").name(Map.of("ko", "업태", "en", "Business Type")).type("TEXT").required(false).isGridVisible(false).order(5).build(),
                        FieldTemplateDto.builder().key("biz_item").name(Map.of("ko", "종목", "en", "Business Item")).type("TEXT").required(false).isGridVisible(false).order(6).build(),
                        FieldTemplateDto.builder().key("credit_limit").name(Map.of("ko", "여신한도", "en", "Credit Limit")).type("NUMBER").unit("KRW").required(false).isGridVisible(true).gridWidth(140).order(7).build(),
                        FieldTemplateDto.builder().key("payment_terms").name(Map.of("ko", "결제조건", "en", "Payment Terms")).type("TEXT").required(false).isGridVisible(true).gridWidth(130).order(8).build()
                ))
                .dqRules(List.of(
                        DqRuleTemplateDto.builder().fieldKey("vendor_name").ruleType("NOT_NULL").severity("ERROR").message(Map.of("ko", "거래처명은 필수 입력 항목입니다.", "en", "Vendor name is required.")).build(),
                        DqRuleTemplateDto.builder().fieldKey("biz_reg_no").ruleType("BUSINESS_NO_CHECKSUM").severity("WARNING").message(Map.of("ko", "사업자등록번호 체크섬 검증에 실패했습니다.", "en", "Invalid business registration number checksum.")).build()
                ))
                .build());

        // 3. PRODUCT (상품)
        TEMPLATES.put("PRODUCT", SpecializedDomainTemplateDto.builder()
                .category("PRODUCT")
                .name(Map.of("ko", "상품 마스터", "en", "Product Master"))
                .description(Map.of("ko", "완제품, 패키지 상품, SKU 및 가격 정보 관리", "en", "Product & SKU Master Data"))
                .icon("shopping_bag")
                .numberingPattern("PROD-{YYYY}-{SEQ:6}")
                .axisName(Map.of("ko", "상품 대분류", "en", "Product Category"))
                .axisCode("PRODUCT_CAT")
                .rootNodeName(Map.of("ko", "전체 상품", "en", "All Products"))
                .identifierFieldKey("sku_code")
                .displayNameFieldKey("product_name")
                .fields(List.of(
                        FieldTemplateDto.builder().key("sku_code").name(Map.of("ko", "SKU 코드", "en", "SKU Code")).type("TEXT").required(true).isSearchable(true).isGridVisible(true).gridWidth(160).order(1).build(),
                        FieldTemplateDto.builder().key("product_name").name(Map.of("ko", "상품명", "en", "Product Name")).type("TEXT").required(true).isSearchable(true).isGridVisible(true).gridWidth(200).order(2).build(),
                        FieldTemplateDto.builder().key("barcode").name(Map.of("ko", "바코드(EAN)", "en", "Barcode")).type("TEXT").required(false).isSearchable(true).isGridVisible(true).gridWidth(150).order(3).build(),
                        FieldTemplateDto.builder().key("brand").name(Map.of("ko", "브랜드", "en", "Brand")).type("TEXT").required(false).isFilterable(true).isGridVisible(true).gridWidth(130).order(4).build(),
                        FieldTemplateDto.builder().key("retail_price").name(Map.of("ko", "소비자가격", "en", "Retail Price")).type("NUMBER").unit("KRW").required(false).isGridVisible(true).gridWidth(130).order(5).build(),
                        FieldTemplateDto.builder().key("cost_price").name(Map.of("ko", "원가", "en", "Cost Price")).type("NUMBER").unit("KRW").required(false).isGridVisible(true).gridWidth(130).order(6).build(),
                        FieldTemplateDto.builder().key("release_date").name(Map.of("ko", "출시일자", "en", "Release Date")).type("DATE").required(false).isGridVisible(true).gridWidth(130).order(7).build(),
                        FieldTemplateDto.builder().key("is_active").name(Map.of("ko", "판매 상태", "en", "Is Active")).type("BOOLEAN").required(false).isFilterable(true).isGridVisible(true).gridWidth(100).order(8).build()
                ))
                .dqRules(List.of(
                        DqRuleTemplateDto.builder().fieldKey("product_name").ruleType("NOT_NULL").severity("ERROR").message(Map.of("ko", "상품명은 필수 입력 항목입니다.", "en", "Product name is required.")).build(),
                        DqRuleTemplateDto.builder().fieldKey("retail_price").ruleType("RANGE").severity("ERROR").params("{\"min\":0}").message(Map.of("ko", "소비자가격은 0 이상이어야 합니다.", "en", "Retail price must be greater than or equal to 0.")).build()
                ))
                .build());

        // 4. MATERIAL (자재)
        TEMPLATES.put("MATERIAL", SpecializedDomainTemplateDto.builder()
                .category("MATERIAL")
                .name(Map.of("ko", "자재 마스터", "en", "Material Master"))
                .description(Map.of("ko", "원자재, 부자재, 반제품 및 조달/재고 기준 관리", "en", "Material & Inventory Master Data"))
                .icon("inventory")
                .numberingPattern("MAT-{SEQ:8}")
                .axisName(Map.of("ko", "자재 유형", "en", "Material Type"))
                .axisCode("MATERIAL_TYPE")
                .rootNodeName(Map.of("ko", "전체 자재", "en", "All Materials"))
                .identifierFieldKey("material_code")
                .displayNameFieldKey("material_name")
                .fields(List.of(
                        FieldTemplateDto.builder().key("material_code").name(Map.of("ko", "자재코드", "en", "Material Code")).type("TEXT").required(true).isSearchable(true).isGridVisible(true).gridWidth(160).order(1).build(),
                        FieldTemplateDto.builder().key("material_name").name(Map.of("ko", "자재명", "en", "Material Name")).type("TEXT").required(true).isSearchable(true).isGridVisible(true).gridWidth(200).order(2).build(),
                        FieldTemplateDto.builder().key("base_uom").name(Map.of("ko", "기본단위(UOM)", "en", "Base UOM")).type("SELECT").required(true).isFilterable(true).isGridVisible(true).gridWidth(120).order(3)
                                .options("[{\"value\":\"EA\",\"label\":{\"ko\":\"개(EA)\",\"en\":\"EA\"}},{\"value\":\"KG\",\"label\":{\"ko\":\"킬로그램(KG)\",\"en\":\"KG\"}},{\"value\":\"M\",\"label\":{\"ko\":\"미터(M)\",\"en\":\"M\"}},{\"value\":\"BOX\",\"label\":{\"ko\":\"박스(BOX)\",\"en\":\"BOX\"}}]").build(),
                        FieldTemplateDto.builder().key("specification").name(Map.of("ko", "규격/사양", "en", "Specification")).type("TEXT").required(false).isGridVisible(true).gridWidth(180).order(4).build(),
                        FieldTemplateDto.builder().key("safety_stock").name(Map.of("ko", "안전재고량", "en", "Safety Stock")).type("NUMBER").required(false).isGridVisible(true).gridWidth(120).order(5).build(),
                        FieldTemplateDto.builder().key("procurement_type").name(Map.of("ko", "조달구분", "en", "Procurement Type")).type("SELECT").required(false).isFilterable(true).isGridVisible(true).gridWidth(130).order(6)
                                .options("[{\"value\":\"PURCHASE\",\"label\":{\"ko\":\"구매\",\"en\":\"Purchase\"}},{\"value\":\"IN_HOUSE\",\"label\":{\"ko\":\"자체생산\",\"en\":\"In-house Production\"}}]").build(),
                        FieldTemplateDto.builder().key("storage_location").name(Map.of("ko", "기본보관위치", "en", "Storage Location")).type("TEXT").required(false).isGridVisible(true).gridWidth(140).order(7).build()
                ))
                .dqRules(List.of(
                        DqRuleTemplateDto.builder().fieldKey("material_name").ruleType("NOT_NULL").severity("ERROR").message(Map.of("ko", "자재명은 필수 입력 항목입니다.", "en", "Material name is required.")).build(),
                        DqRuleTemplateDto.builder().fieldKey("safety_stock").ruleType("RANGE").severity("WARNING").params("{\"min\":0}").message(Map.of("ko", "안전재고량은 0 이상이어야 합니다.", "en", "Safety stock must be non-negative.")).build()
                ))
                .build());

        // 5. EMPLOYEE (임직원)
        TEMPLATES.put("EMPLOYEE", SpecializedDomainTemplateDto.builder()
                .category("EMPLOYEE")
                .name(Map.of("ko", "임직원 마스터", "en", "Employee Master"))
                .description(Map.of("ko", "사번, 소속 부서, 직급/직책 및 재직 정보 관리", "en", "Employee & HR Master Data"))
                .icon("badge")
                .numberingPattern("EMP-{SEQ:6}")
                .axisName(Map.of("ko", "조직 본부", "en", "Division"))
                .axisCode("DIVISION")
                .rootNodeName(Map.of("ko", "전체 임직원", "en", "All Employees"))
                .identifierFieldKey("employee_no")
                .displayNameFieldKey("employee_name")
                .fields(List.of(
                        FieldTemplateDto.builder().key("employee_no").name(Map.of("ko", "사번", "en", "Employee No")).type("TEXT").required(true).isSearchable(true).isGridVisible(true).gridWidth(150).order(1).build(),
                        FieldTemplateDto.builder().key("employee_name").name(Map.of("ko", "성명", "en", "Employee Name")).type("TEXT").required(true).isSearchable(true).isGridVisible(true).gridWidth(160).order(2).build(),
                        FieldTemplateDto.builder().key("department").name(Map.of("ko", "소속부서", "en", "Department")).type("TEXT").required(false).isFilterable(true).isGridVisible(true).gridWidth(160).order(3).build(),
                        FieldTemplateDto.builder().key("position").name(Map.of("ko", "직급/직책", "en", "Position")).type("TEXT").required(false).isFilterable(true).isGridVisible(true).gridWidth(130).order(4).build(),
                        FieldTemplateDto.builder().key("employment_type").name(Map.of("ko", "고용형태", "en", "Employment Type")).type("SELECT").required(false).isFilterable(true).isGridVisible(true).gridWidth(120).order(5)
                                .options("[{\"value\":\"REGULAR\",\"label\":{\"ko\":\"정규직\",\"en\":\"Regular\"}},{\"value\":\"CONTRACT\",\"label\":{\"ko\":\"계약직\",\"en\":\"Contract\"}},{\"value\":\"EXECUTIVE\",\"label\":{\"ko\":\"임원\",\"en\":\"Executive\"}}]").build(),
                        FieldTemplateDto.builder().key("hire_date").name(Map.of("ko", "입사일자", "en", "Hire Date")).type("DATE").required(false).isGridVisible(true).gridWidth(130).order(6).build(),
                        FieldTemplateDto.builder().key("work_email").name(Map.of("ko", "사내 이메일", "en", "Work Email")).type("EMAIL").required(false).isSearchable(true).isGridVisible(true).gridWidth(220).order(7).build(),
                        FieldTemplateDto.builder().key("mobile_phone").name(Map.of("ko", "휴대전화번호", "en", "Mobile Phone")).type("TEXT").required(false).isGridVisible(true).gridWidth(150).order(8).build()
                ))
                .dqRules(List.of(
                        DqRuleTemplateDto.builder().fieldKey("employee_name").ruleType("NOT_NULL").severity("ERROR").message(Map.of("ko", "임직원 성명은 필수 입력 항목입니다.", "en", "Employee name is required.")).build(),
                        DqRuleTemplateDto.builder().fieldKey("work_email").ruleType("REGEX").severity("WARNING").params("{\"regex\":\"^[\\\\w-\\\\.]+@([\\\\w-]+\\\\.)+[\\\\w-]{2,4}$\"}").message(Map.of("ko", "유효한 이메일 형식이 아닙니다.", "en", "Invalid email format.")).build()
                ))
                .build());

        // 6. STOCK (주식)
        TEMPLATES.put("STOCK", SpecializedDomainTemplateDto.builder()
                .category("STOCK")
                .name(Map.of("ko", "주식 종목 마스터", "en", "Stock Master"))
                .description(Map.of("ko", "상장 종목코드, ISIN, 시장구분, 액면가 및 상장주식수 관리", "en", "Stock & Equity Master Data"))
                .icon("candlestick_chart")
                .numberingPattern("STK-{SEQ:6}")
                .axisName(Map.of("ko", "상장 시장", "en", "Market"))
                .axisCode("MARKET")
                .rootNodeName(Map.of("ko", "전체 종목", "en", "All Stocks"))
                .identifierFieldKey("ticker_code")
                .displayNameFieldKey("stock_name")
                .fields(List.of(
                        FieldTemplateDto.builder().key("ticker_code").name(Map.of("ko", "종목코드(티커)", "en", "Ticker Code")).type("TEXT").required(true).isSearchable(true).isGridVisible(true).gridWidth(140).order(1).build(),
                        FieldTemplateDto.builder().key("isin_code").name(Map.of("ko", "ISIN 코드", "en", "ISIN Code")).type("TEXT").required(false).isSearchable(true).isGridVisible(true).gridWidth(160).order(2).build(),
                        FieldTemplateDto.builder().key("stock_name").name(Map.of("ko", "종목명", "en", "Stock Name")).type("TEXT").required(true).isSearchable(true).isGridVisible(true).gridWidth(200).order(3).build(),
                        FieldTemplateDto.builder().key("market_type").name(Map.of("ko", "상장시장", "en", "Market Type")).type("SELECT").required(false).isFilterable(true).isGridVisible(true).gridWidth(130).order(4)
                                .options("[{\"value\":\"KOSPI\",\"label\":{\"ko\":\"코스피(KOSPI)\",\"en\":\"KOSPI\"}},{\"value\":\"KOSDAQ\",\"label\":{\"ko\":\"코스닥(KOSDAQ)\",\"en\":\"KOSDAQ\"}},{\"value\":\"KONEX\",\"label\":{\"ko\":\"코넥스(KONEX)\",\"en\":\"KONEX\"}},{\"value\":\"OVERSEAS\",\"label\":{\"ko\":\"해외시장\",\"en\":\"Overseas\"}}]").build(),
                        FieldTemplateDto.builder().key("industry_sector").name(Map.of("ko", "업종/섹터", "en", "Industry Sector")).type("TEXT").required(false).isFilterable(true).isGridVisible(true).gridWidth(150).order(5).build(),
                        FieldTemplateDto.builder().key("par_value").name(Map.of("ko", "액면가", "en", "Par Value")).type("NUMBER").unit("KRW").required(false).isGridVisible(true).gridWidth(120).order(6).build(),
                        FieldTemplateDto.builder().key("listed_shares").name(Map.of("ko", "상장주식수", "en", "Listed Shares")).type("NUMBER").required(false).isGridVisible(true).gridWidth(150).order(7).build(),
                        FieldTemplateDto.builder().key("currency").name(Map.of("ko", "거래통화", "en", "Currency")).type("TEXT").required(false).isGridVisible(true).gridWidth(100).order(8).build()
                ))
                .dqRules(List.of(
                        DqRuleTemplateDto.builder().fieldKey("stock_name").ruleType("NOT_NULL").severity("ERROR").message(Map.of("ko", "종목명은 필수 입력 항목입니다.", "en", "Stock name is required.")).build(),
                        DqRuleTemplateDto.builder().fieldKey("par_value").ruleType("RANGE").severity("WARNING").params("{\"min\":0}").message(Map.of("ko", "액면가는 0 이상이어야 합니다.", "en", "Par value must be greater than or equal to 0.")).build()
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

    @Transactional
    public DomainResponse provisionDomain(SpecializedDomainProvisionRequest request) {
        String category = request.getCategory() != null ? request.getCategory().toUpperCase() : "";
        SpecializedDomainTemplateDto template = getTemplate(category);

        // 1. Create Domain
        Domain domain = new Domain();
        domain.setDomainType("SPECIALIZED");
        domain.setSpecializedCategory(category);
        domain.setName(request.getName() != null && !request.getName().isEmpty() ? request.getName() : template.getName());
        domain.setDescription(request.getDescription() != null ? request.getDescription() : template.getDescription());
        domain.setIcon(request.getIcon() != null && !request.getIcon().isBlank() ? request.getIcon() : template.getIcon());
        domain.setNumberingPattern(request.getNumberingPattern() != null && !request.getNumberingPattern().isBlank() ? request.getNumberingPattern() : template.getNumberingPattern());
        domain.setAutoDqScanEnabled(true);
        domain.setSortOrder(0);

        Domain savedDomain = domainRepository.save(domain);

        // 2. Create Classification Axis
        ClassificationAxis axis = new ClassificationAxis();
        axis.setDomain(savedDomain);
        axis.setAxisCode(template.getAxisCode());
        axis.setName(template.getAxisName());
        axis.setIsDefault(true);
        axis.setSortOrder(0);
        ClassificationAxis savedAxis = axisRepository.save(axis);

        // 3. Create Root Node
        ClassificationNode rootNode = new ClassificationNode();
        rootNode.setDomain(savedDomain);
        rootNode.setAxis(savedAxis);
        rootNode.setName(template.getRootNodeName());
        rootNode.setPath("/" + template.getAxisCode().toLowerCase());
        rootNode.setDepth(0);
        rootNode.setOrder(0);
        rootNode.setIsDeleted(false);
        ClassificationNode savedRootNode = nodeRepository.save(rootNode);

        // 4. Create Standard FieldDefinitions
        Map<String, FieldDefinition> createdFields = new HashMap<>();
        if (template.getFields() != null) {
            for (FieldTemplateDto ft : template.getFields()) {
                FieldDefinition fd = new FieldDefinition();
                fd.setDomain(savedDomain);
                fd.setDefinedAtNode(savedRootNode);
                fd.setKey(ft.getKey());
                fd.setName(ft.getName());
                fd.setHint(ft.getHint());
                fd.setType(ft.getType());
                fd.setUnit(ft.getUnit());
                fd.setRequired(Boolean.TRUE.equals(ft.getRequired()));
                fd.setIsSearchable(Boolean.TRUE.equals(ft.getIsSearchable()));
                fd.setGridWidth(ft.getGridWidth() != null ? ft.getGridWidth() : 150);
                fd.setOrder(ft.getOrder() != null ? ft.getOrder() : 0);
                fd.setOptions(ft.getOptions());

                FieldDefinition savedFd = fieldDefinitionRepository.save(fd);
                createdFields.put(ft.getKey(), savedFd);
            }
        }

        // 5. Bind Identifier and Display Name fields to Domain
        if (createdFields.containsKey(template.getIdentifierFieldKey())) {
            savedDomain.setIdentifierFieldId(createdFields.get(template.getIdentifierFieldKey()).getId());
        }
        if (createdFields.containsKey(template.getDisplayNameFieldKey())) {
            savedDomain.setDisplayNameFieldId(createdFields.get(template.getDisplayNameFieldKey()).getId());
        }
        savedDomain = domainRepository.save(savedDomain);

        // 6. Create Standard DqRules
        if (template.getDqRules() != null) {
            int sortOrder = 0;
            for (DqRuleTemplateDto rt : template.getDqRules()) {
                FieldDefinition targetField = createdFields.get(rt.getFieldKey());
                if (targetField != null) {
                    DqRule rule = new DqRule();
                    rule.setDomainId(savedDomain.getId());
                    rule.setNodeId(savedRootNode.getId());
                    rule.setFieldDefinition(targetField);
                    rule.setRuleType(DqRuleType.valueOf(rt.getRuleType()));
                    rule.setSeverity(rt.getSeverity() != null && "WARNING".equalsIgnoreCase(rt.getSeverity()) ? DqSeverity.WARNING : DqSeverity.ERROR);
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
}
