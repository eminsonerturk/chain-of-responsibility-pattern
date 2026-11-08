package com.example.ChainOfResponsibilityPattern.service;

import com.example.ChainOfResponsibilityPattern.context.BankingOperationContext;
import com.example.ChainOfResponsibilityPattern.dto.*;
import com.example.ChainOfResponsibilityPattern.result.BankingOperationResult;
import com.example.ChainOfResponsibilityPattern.service.interfaces.BankingOperationService;
import org.springframework.stereotype.Service;

@Service
public class BankingOperationServiceImpl implements BankingOperationService {

    @Override
    public BankingOperationResult accountOperation(BankingOperationContext context) {
        for (Long customerNo : context.getCustomerNoList()) {
            AccountDTO accountDTO = context.getMapOfCustomerNoToAccountDto().get(customerNo);

            if (accountDTO.getBalance().signum() < 0) {
                return BankingOperationResult.error(String.format(
                        "%d numaralı müşterinin hesap bakiyesi negatif olamaz.",
                        customerNo
                ));
            }

            if (!"TRY".equals(accountDTO.getCurrency())) {
                return BankingOperationResult.error(String.format(
                        "%d numaralı müşterinin hesap para birimi desteklenmiyor: %s.",
                        customerNo,
                        accountDTO.getCurrency()
                ));
            }
        }

        return BankingOperationResult.ok();
    }

    @Override
    public BankingOperationResult transferOperation(BankingOperationContext context) {
        for (Long customerNo : context.getCustomerNoList()) {
            TransferDTO transferDTO = context.getMapOfCustomerNoToTransferDto().get(customerNo);

            if (transferDTO.getAmount().signum() <= 0) {
                return BankingOperationResult.error(String.format(
                        "%d numaralı müşteri için transfer tutarı sıfırdan büyük olmalıdır.",
                        customerNo
                ));
            }

            if (transferDTO.getFromIban().equals(transferDTO.getToIban())) {
                return BankingOperationResult.error(String.format(
                        "%d numaralı müşteri aynı hesaba transfer yapamaz.",
                        customerNo
                ));
            }
        }

        return BankingOperationResult.ok();
    }

    @Override
    public BankingOperationResult limitOperation(BankingOperationContext context) {
        for (Long customerNo : context.getCustomerNoList()) {
            LimitDTO limitDTO = context.getMapOfCustomerNoToLimitDto().get(customerNo);

            if (limitDTO.getUsedToday().compareTo(limitDTO.getDailyLimit()) > 0) {
                return BankingOperationResult.error(String.format(
                        "%d numaralı müşteri günlük transfer limitini aşmıştır. Kullanılan: %s, günlük limit: %s.",
                        customerNo,
                        limitDTO.getUsedToday(),
                        limitDTO.getDailyLimit()
                ));
            }
        }

        return BankingOperationResult.ok();
    }

    @Override
    public BankingOperationResult fraudOperation(BankingOperationContext context) {
        for (Long customerNo : context.getCustomerNoList()) {
            FraudCheckDTO fraudCheckDTO =
                    context.getMapOfCustomerNoToFraudCheckDto().get(customerNo);

            if (fraudCheckDTO.isSuspicious()) {
                return BankingOperationResult.error(String.format(
                        "%d numaralı müşteri için şüpheli işlem tespit edildi; transfer durduruldu.",
                        customerNo
                ));
            }

            if ("UNKNOWN".equalsIgnoreCase(fraudCheckDTO.getChannel())) {
                return BankingOperationResult.error(String.format(
                        "%d numaralı müşterinin işlem kanalı doğrulanamadı: %s.",
                        customerNo,
                        fraudCheckDTO.getChannel()
                ));
            }
        }

        return BankingOperationResult.ok();
    }
}
