package com.favorsoft.mplatform.cdn.domain.keys;

import java.io.Serializable;

import jakarta.persistence.*;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MasterCodeKey implements Serializable {

	@Column(length = 100)
	private String domainId;

	@Column(length = 100)
	private String classId;

	public MasterCodeKey(String domainId, String classId) {
		this.domainId = domainId;
		this.classId = classId;
	}
	
}

