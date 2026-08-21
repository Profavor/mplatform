package com.classification.domain_system.exception;

import lombok.Getter;

/**
 * 암호화 처리(AES-GCM, Vault Transit encrypt, HMAC blind index 등) 실패 시 발생하는 예외.
 * 암호화 실패를 조용히 무시하지 않고 반드시 상위로 전파하기 위해 사용한다.
 */
@Getter
public class EncryptionException extends BusinessException {

    public EncryptionException(String message) {
        super(ErrorCode.ENCRYPTION_FAILED, message);
    }

    public EncryptionException(String message, Throwable cause) {
        super(ErrorCode.ENCRYPTION_FAILED, message);
        initCause(cause);
    }
}
