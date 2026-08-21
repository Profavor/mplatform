package com.classification.domain_system.exception;

import lombok.Getter;

/**
 * 복호화 처리(AES-GCM decrypt, Vault Transit decrypt 등) 실패 시 발생하는 예외.
 * 복호화 실패를 조용히 무시하고 암호문을 그대로 반환하는 것을 방지하기 위해 사용한다.
 */
@Getter
public class DecryptionException extends BusinessException {

    public DecryptionException(String message) {
        super(ErrorCode.DECRYPTION_FAILED, message);
    }

    public DecryptionException(String message, Throwable cause) {
        super(ErrorCode.DECRYPTION_FAILED, message);
        initCause(cause);
    }
}
