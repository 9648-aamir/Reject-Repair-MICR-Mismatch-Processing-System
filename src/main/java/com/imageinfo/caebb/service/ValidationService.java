package com.imageinfo.caebb.service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

public class ValidationService {
    private final int chqDateValidationMonths;

    public ValidationService(int chqDateValidationMonths){
        this.chqDateValidationMonths = chqDateValidationMonths;
    }

    public enum ChequeDateStatus { VALID, STALE, POST_DATED }

    public ChequeDateStatus validateChequeDate(Date chequeDate) {
        if (chequeDate == null) return ChequeDateStatus.STALE;
        LocalDate today = LocalDate.now(ZoneId.systemDefault());
        LocalDate chq = chequeDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        
        LocalDate threshold = today.minusMonths(chqDateValidationMonths);
        if (chq.isBefore(threshold) || chq.isEqual(threshold)) {
            return ChequeDateStatus.STALE;
        }

        if (chq.isAfter(today)) {
          
            LocalDate allowedNext = today.plusDays(1);
            if (chq.isEqual(allowedNext)) {
                return ChequeDateStatus.VALID;
            }
            return ChequeDateStatus.POST_DATED;
        }

        return ChequeDateStatus.VALID;
    }
}
