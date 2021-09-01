package com.favorsoft.mplatform.cdn.domain;

import javax.persistence.*;

import com.favorsoft.mplatform.cdn.domain.keys.MasterCodeKey;

import com.favorsoft.mplatform.cdn.dto.MasterCodeDTO;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@IdClass(MasterCodeKey.class)
public class MasterCode extends BaseEntity {

	@Id
	private String domainId;

	@Id
	private String classId;
	
	@Column(length = 8)
	private int length;
	
	@Column(length = 30)
	private String prefix;
	
	@Column(length = 30)
	private String suffix;
	
	@Column(length = 8)
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int cnt;

	@Builder
	public MasterCode(String domainId, String classId, int length, String prefix, String suffix, int cnt){
		this.domainId = domainId;
		this.classId = classId;
		this.length = length;
		this.prefix = prefix;
		this.suffix = suffix;
		this.cnt = cnt;
	}

	public static MasterCode.MasterCodeBuilder fromMasterCodeDTO(MasterCodeDTO masterCodeDTO){
		return MasterCode.builder()
				.domainId(masterCodeDTO.getDomainId())
				.classId(masterCodeDTO.getClassId())
				.length(masterCodeDTO.getLength())
				.prefix(masterCodeDTO.getPrefix())
				.suffix(masterCodeDTO.getSuffix())
				.cnt(masterCodeDTO.getCnt());
	}
	
}
