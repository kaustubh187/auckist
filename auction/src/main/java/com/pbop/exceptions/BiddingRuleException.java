package com.pbop.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BiddingRuleException extends RuntimeException {
    public BiddingRuleException(String message) {
        super(message);
    }
}
