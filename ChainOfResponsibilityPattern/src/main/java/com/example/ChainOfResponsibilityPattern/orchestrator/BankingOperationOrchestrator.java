package com.example.ChainOfResponsibilityPattern.orchestrator;

import com.example.ChainOfResponsibilityPattern.context.BankingOperationContext;
import com.example.ChainOfResponsibilityPattern.service.interfaces.BankingOperationService;
import com.example.ChainOfResponsibilityPattern.service.validator.BankingOperationValidator;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BankingOperationOrchestrator {
    private final BankingOperationService bankingOperationService;

    public BankingOperationOrchestrator(BankingOperationService bankingOperationService) {
        this.bankingOperationService = bankingOperationService;
    }

    public List<BankingOperationValidator<BankingOperationContext>> prepareBankingOperationChainForCustomers(){
        return List.of(
                bankingOperationService::accountOperation,
                bankingOperationService::transferOperation,
                bankingOperationService::limitOperation,
                bankingOperationService::fraudOperation
        );
    }
}
