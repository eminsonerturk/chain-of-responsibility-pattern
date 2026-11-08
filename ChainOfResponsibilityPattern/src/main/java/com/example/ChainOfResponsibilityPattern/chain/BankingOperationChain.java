package com.example.ChainOfResponsibilityPattern.chain;

import com.example.ChainOfResponsibilityPattern.context.BankingOperationContext;
import com.example.ChainOfResponsibilityPattern.dto.BankingOperationResultDTO;
import com.example.ChainOfResponsibilityPattern.dto.CustomerDTO;
import com.example.ChainOfResponsibilityPattern.result.BankingOperationResult;
import com.example.ChainOfResponsibilityPattern.service.validator.BankingOperationValidator;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class BankingOperationChain {

    public BankingOperationResultDTO executeBankingOperationsWithChainBreaker(BankingOperationContext context, List<BankingOperationValidator<BankingOperationContext>> bankingOperationValidatorList){
        BankingOperationResultDTO resultDTO = new BankingOperationResultDTO();
        Optional<BankingOperationResultDTO> optResult = runBankingOperationsWithChainBreaker(context, bankingOperationValidatorList, resultDTO);
        return optResult.orElse(resultDTO);
    }

    private Optional<BankingOperationResultDTO> runBankingOperationsWithChainBreaker(BankingOperationContext context, List<BankingOperationValidator<BankingOperationContext>> bankingOperationValidatorList, BankingOperationResultDTO resultDTO) {
        for(BankingOperationValidator<BankingOperationContext> validator : bankingOperationValidatorList){
            BankingOperationResult result = validator.validate(context);
            if(!result.valid() && result.chainBreaker()){
                resultDTO.setErrorMessage(result.message());
                return Optional.of(resultDTO);
            }
        }
        return Optional.empty();
    }
}
