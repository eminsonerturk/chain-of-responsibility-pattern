package com.example.ChainOfResponsibilityPattern.context;

import com.example.ChainOfResponsibilityPattern.dto.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashMap;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class BankingOperationContext {
    private List<Long> customerNoList;
    private HashMap<Long,AccountDTO> mapOfCustomerNoToAccountDto;
    private HashMap<Long,TransferDTO> mapOfCustomerNoToTransferDto;
    private HashMap<Long,LimitDTO> mapOfCustomerNoToLimitDto;
    private HashMap<Long,FraudCheckDTO> mapOfCustomerNoToFraudCheckDto;

    public List<Long> getCustomerNoList() {
        return customerNoList;
    }

    public void setCustomerNoList(List<Long> customerNoList) {
        this.customerNoList = customerNoList;
    }

    public HashMap<Long, AccountDTO> getMapOfCustomerNoToAccountDto() {
        return mapOfCustomerNoToAccountDto;
    }

    public void setMapOfCustomerNoToAccountDto(HashMap<Long, AccountDTO> mapOfCustomerNoToAccountDto) {
        this.mapOfCustomerNoToAccountDto = mapOfCustomerNoToAccountDto;
    }

    public HashMap<Long, TransferDTO> getMapOfCustomerNoToTransferDto() {
        return mapOfCustomerNoToTransferDto;
    }

    public void setMapOfCustomerNoToTransferDto(HashMap<Long, TransferDTO> mapOfCustomerNoToTransferDto) {
        this.mapOfCustomerNoToTransferDto = mapOfCustomerNoToTransferDto;
    }

    public HashMap<Long, LimitDTO> getMapOfCustomerNoToLimitDto() {
        return mapOfCustomerNoToLimitDto;
    }

    public void setMapOfCustomerNoToLimitDto(HashMap<Long, LimitDTO> mapOfCustomerNoToLimitDto) {
        this.mapOfCustomerNoToLimitDto = mapOfCustomerNoToLimitDto;
    }

    public HashMap<Long, FraudCheckDTO> getMapOfCustomerNoToFraudCheckDto() {
        return mapOfCustomerNoToFraudCheckDto;
    }

    public void setMapOfCustomerNoToFraudCheckDto(HashMap<Long, FraudCheckDTO> mapOfCustomerNoToFraudCheckDto) {
        this.mapOfCustomerNoToFraudCheckDto = mapOfCustomerNoToFraudCheckDto;
    }
}
