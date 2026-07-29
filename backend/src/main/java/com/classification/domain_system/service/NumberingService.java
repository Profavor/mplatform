package com.classification.domain_system.service;

import com.classification.domain_system.entity.Domain;
import com.classification.domain_system.repository.DomainRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class NumberingService {

    private final DomainRepository domainRepository;

    /**
     * Generate code based on domain pattern and current sequence without saving.
     */
    public String generateCode(Domain domain) {
        String pattern = domain.getNumberingPattern();
        if (pattern == null || pattern.isBlank()) {
            return "";
        }

        LocalDate now = LocalDate.now();
        String result = pattern;

        // Replace Date tokens
        result = result.replace("{YYYY}", now.format(DateTimeFormatter.ofPattern("yyyy")))
                       .replace("{MM}", now.format(DateTimeFormatter.ofPattern("MM")))
                       .replace("{DD}", now.format(DateTimeFormatter.ofPattern("dd")));

        // Replace Sequence token (e.g. {SEQ:5} -> 00001)
        Pattern seqPattern = Pattern.compile("\\{SEQ:(\\d+)}");
        Matcher matcher = seqPattern.matcher(result);
        if (matcher.find()) {
            int digits = Integer.parseInt(matcher.group(1));
            long seq = domain.getCurrentSequence() != null ? domain.getCurrentSequence() : 0L;
            String seqStr = String.format("%0" + digits + "d", seq);
            result = matcher.replaceFirst(seqStr);
        }

        return result;
    }

    /**
     * Safely increments sequence by 1, saves to DB, and returns the newly generated code.
     */
    @Transactional
    public String issueNextCode(UUID domainId) {
        Domain domain = domainRepository.findWithLockById(domainId)
                .orElseThrow(() -> new RuntimeException("Domain not found: " + domainId));

        if (domain.getNumberingPattern() == null || domain.getNumberingPattern().isBlank()) {
            return "";
        }

        long nextSeq = (domain.getCurrentSequence() != null ? domain.getCurrentSequence() : 0L) + 1;
        domain.setCurrentSequence(nextSeq);
        domainRepository.save(domain);

        return generateCode(domain);
    }
}
