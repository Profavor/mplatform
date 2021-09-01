package com.favorsoft.mplatform.cdn.service.impl;

import com.favorsoft.mplatform.cdn.domain.MasterCode;
import com.favorsoft.mplatform.cdn.domain.keys.MasterCodeKey;
import com.favorsoft.mplatform.cdn.repository.jpa.MasterCodeRepository;
import com.favorsoft.mplatform.cdn.service.MasterCodeService;
import com.favorsoft.mplatform.exception.MplatformErrorCode;
import com.favorsoft.mplatform.exception.MplatformRuntimeException;

import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;

@Transactional(rollbackOn = Exception.class)
@Service
public class MasterCodeServiceImpl implements MasterCodeService {

    private final MasterCodeRepository masterCodeRepository;

	public MasterCodeServiceImpl(MasterCodeRepository masterCodeRepository){
		this.masterCodeRepository = masterCodeRepository;
	}
	
	@Override
	public String createMasterId(String domainId, String classId) {
		MasterCode masterCode = updateMasterCodeCnt(domainId, classId);
		return generateMasterId(masterCode);
	}

	private MasterCode updateMasterCodeCnt(String domainId, String classId) {
		MasterCodeKey masterCodeKey = new MasterCodeKey();
		masterCodeKey.setDomainId(domainId);
		masterCodeKey.setClassId(classId);

		Optional<MasterCode> optional = masterCodeRepository.findById(masterCodeKey);
		if(!optional.isPresent()) {
			masterCodeKey.setClassId("root");
			optional = masterCodeRepository.findById(masterCodeKey);
		}
		
		MasterCode masterCode = optional.get();
		masterCode.setCnt(masterCode.getCnt() + 1); //TODO: 이부분 오류!! Db 시퀀스로 변경 필요
		MasterCode newMasterCode = masterCodeRepository.save(masterCode);
		if (newMasterCode == null) {
			throw new MplatformRuntimeException(MplatformErrorCode.INVALID_REQUEST_PARAM, "Cannot update MasterCode cnt");
		}
		return newMasterCode;
	}
	
	private String generateMasterId(MasterCode masterCode) {
		String pre = masterCode.getPrefix();
		String suf = masterCode.getSuffix();
		int cnt = masterCode.getCnt();
		int len = masterCode.getLength();
		if (ObjectUtils.isEmpty(suf)) {
			return String.format("%s_%s", pre, fillToNumber(len, cnt));
		}
		return String.format("%s_%s_%s", pre, fillToNumber(len, cnt), suf);
	}
	
	private String fillToNumber(int len, int num) {
		String numStr = String.format("%d", num);
		if (numStr.length() > len) {
			return numStr.substring(0, len);
		}
		StringBuffer ret = new StringBuffer();
		for (int i=0; i< len-numStr.length(); i++) {
			ret.append("0");
		}
		ret.append(num);
		return ret.toString();
	}

	@Override
	public List<MasterCode> getList() {
		return masterCodeRepository.findAll();
	}

	@Override
	public MasterCode save(MasterCode entity) {
		return masterCodeRepository.saveAndFlush(entity);
	}

	@Override
	public MasterCode getObject(Object key) {
		MasterCodeKey masterCodeKey = (MasterCodeKey) key;
		return masterCodeRepository.findById(masterCodeKey).orElse(new MasterCode());
	}

	@Override
	public void delete(Object key) {
		MasterCodeKey masterCodeKey = (MasterCodeKey) key;
		MasterCode masterCode = getObject(masterCodeKey);
		masterCodeRepository.delete(masterCode);
	}
}
