package com.example.ChainOfResponsibilityPattern.result;

public record BankingOperationResult(boolean valid, boolean chainBreaker, String message) {
    public static BankingOperationResult ok() {
        return new BankingOperationResult(true, false, null);
    }

    public static BankingOperationResult error(String msg) {
        return new BankingOperationResult(false, true, msg);
    }

    public static BankingOperationResult info(String msg) {
        return new BankingOperationResult(false, false, msg);
    }
}