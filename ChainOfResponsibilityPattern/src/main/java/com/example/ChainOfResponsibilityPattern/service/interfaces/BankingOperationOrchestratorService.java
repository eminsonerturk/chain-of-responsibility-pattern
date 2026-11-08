package com.example.ChainOfResponsibilityPattern.service.interfaces;

import com.example.ChainOfResponsibilityPattern.result.BankingOperationResult;

public interface BankingOperationOrchestratorService {
    BankingOperationResult runLimitValidationFailureExample();

    BankingOperationResult runSuccessfulExample();
}
