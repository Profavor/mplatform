package com.classification.domain_system.entity;

import lombok.*;

import jakarta.persistence.*;

@Entity
@Table(name = "system_features")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SystemFeature {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private int featureNo;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private String featureNameKey; // 다국어 처리를 위한 i18n key

    @Column(nullable = false)
    private String beanName;

    @Column(nullable = false)
    private boolean isGovernanceCore; // 상단 11대 핵심 기능 여부
    
    @Column(nullable = false)
    private String iconName; // 프론트엔드 아이콘 렌더링용

    @Column(nullable = false)
    private String colorTheme; // 프론트엔드 버튼 색상(theme) 렌더링용
}
