package com.example.ChainOfResponsibilityPattern.service.validator;

import com.example.ChainOfResponsibilityPattern.result.BankingOperationResult;

@FunctionalInterface
public interface BankingOperationValidator<T> {
    BankingOperationResult validate(T context);
}