package com.example.ChainOfResponsibilityPattern.service;

import com.example.ChainOfResponsibilityPattern.context.BankingOperationContext;
import com.example.ChainOfResponsibilityPattern.chain.BankingOperationChain;
import com.example.ChainOfResponsibilityPattern.dto.AccountDTO;
import com.example.ChainOfResponsibilityPattern.dto.BankingOperationResultDTO;
import com.example.ChainOfResponsibilityPattern.dto.FraudCheckDTO;
import com.example.ChainOfResponsibilityPattern.dto.LimitDTO;
import com.example.ChainOfResponsibilityPattern.dto.TransferDTO;
import com.example.ChainOfResponsibilityPattern.orchestrator.BankingOperationOrchestrator;
import com.example.ChainOfResponsibilityPattern.result.BankingOperationResult;
import com.example.ChainOfResponsibilityPattern.service.interfaces.BankingOperationOrchestratorService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;

@Service
public class BankingOperationOrchestratorServiceImpl implements BankingOperationOrchestratorService {
    private final BankingOperationOrchestrator bankingOperationOrchestrator;
    private final BankingOperationChain bankingOperationChain;

    public BankingOperationOrchestratorServiceImpl(
            BankingOperationOrchestrator bankingOperationOrchestrator,
            BankingOperationChain bankingOperationChain
    ) {
        this.bankingOperationOrchestrator = bankingOperationOrchestrator;
        this.bankingOperationChain = bankingOperationChain;
    }

    @Override
    public BankingOperationResult runLimitValidationFailureExample() {
        BankingOperationContext context = createExampleContext(List.of(1001L), new BigDecimal("1200"));
        BankingOperationResult result = executeBankingOperations(context);
        if (!result.valid()) {
            System.out.println("Limit doğrulama hatası: " + result.message());
        }
        return result;
    }

    @Override
    public BankingOperationResult runSuccessfulExample() {
        BankingOperationContext context = createExampleContext(List.of(1001L), new BigDecimal("100"));
        BankingOperationResult result = executeBankingOperations(context);
        System.out.println(result.valid()
                ? "Tüm doğrulamalar başarıyla geçti."
                : "Doğrulama hatası: " + result.message());
        return result;
    }

    private BankingOperationContext createExampleContext(List<Long> customerNoList, BigDecimal usedToday) {
        BankingOperationContext bankingOperationContext = new BankingOperationContext();
        bankingOperationContext.setCustomerNoList(customerNoList);

        HashMap<Long, AccountDTO> accounts = new HashMap<>();
        HashMap<Long, TransferDTO> transfers = new HashMap<>();
        HashMap<Long, LimitDTO> limits = new HashMap<>();
        HashMap<Long, FraudCheckDTO> fraudChecks = new HashMap<>();

        for (Long customerNo : customerNoList) {
            AccountDTO account = new AccountDTO();
            account.setBalance(new BigDecimal("10000"));
            account.setCurrency("TRY");
            accounts.put(customerNo, account);

            TransferDTO transfer = new TransferDTO();
            transfer.setFromIban("TR000000000000000000000001");
            transfer.setToIban("TR000000000000000000000002");
            transfer.setAmount(new BigDecimal("100"));
            transfers.put(customerNo, transfer);

            LimitDTO limit = new LimitDTO();
            limit.setDailyLimit(new BigDecimal("1000"));
            limit.setUsedToday(usedToday);
            limits.put(customerNo, limit);

            FraudCheckDTO fraudCheck = new FraudCheckDTO();
            fraudCheck.setChannel("MOBILE");
            fraudCheck.setSuspicious(false);
            fraudChecks.put(customerNo, fraudCheck);
        }

        bankingOperationContext.setMapOfCustomerNoToAccountDto(accounts);
        bankingOperationContext.setMapOfCustomerNoToTransferDto(transfers);
        bankingOperationContext.setMapOfCustomerNoToLimitDto(limits);
        bankingOperationContext.setMapOfCustomerNoToFraudCheckDto(fraudChecks);

        return bankingOperationContext;
    }

    private BankingOperationResult executeBankingOperations(BankingOperationContext context) {
        BankingOperationResultDTO resultDTO = bankingOperationChain.executeBankingOperationsWithChainBreaker(
                context,
                bankingOperationOrchestrator.prepareBankingOperationChainForCustomers()
        );

        if (resultDTO.getErrorMessage() != null) {
            return BankingOperationResult.error(resultDTO.getErrorMessage());
        }
        return BankingOperationResult.ok();
    }
}
