package com.favorsoft.mplatform.cdn.domain;

import lombok.*;

import javax.persistence.*;

@Entity
@Getter @Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ErrorHistory extends BaseEntity{

	private static final long serialVersionUID = -1491377423615785604L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	@Column(length = 100)
	private String errorCode;
	
	@Column(length = 500)
	private String errorMsg;
	
	@Column(columnDefinition = "TEXT")
	private String stackTrace;
	
	public ErrorHistory(String errorCode, String errorMsg, String stackTrace, String creator) {
		this.errorCode = errorCode;
		this.errorMsg = errorMsg;
		this.stackTrace = stackTrace;
		super.setCreator(creator);
	}
}
