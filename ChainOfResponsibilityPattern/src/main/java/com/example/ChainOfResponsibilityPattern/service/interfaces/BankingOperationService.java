package com.example.ChainOfResponsibilityPattern.service.interfaces;

import com.example.ChainOfResponsibilityPattern.context.BankingOperationContext;
import com.example.ChainOfResponsibilityPattern.result.BankingOperationResult;

public interface BankingOperationService {
    BankingOperationResult accountOperation(BankingOperationContext context);

    BankingOperationResult transferOperation(BankingOperationContext context);

    BankingOperationResult limitOperation(BankingOperationContext context);

    BankingOperationResult fraudOperation(BankingOperationContext context);
}
