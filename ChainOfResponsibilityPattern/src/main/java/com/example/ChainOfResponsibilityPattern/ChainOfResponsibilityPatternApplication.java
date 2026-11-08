package com.example.ChainOfResponsibilityPattern;

import com.example.ChainOfResponsibilityPattern.service.interfaces.BankingOperationOrchestratorService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ChainOfResponsibilityPatternApplication {

	public static void main(String[] args) {
		var applicationContext = SpringApplication.run(ChainOfResponsibilityPatternApplication.class, args);
		BankingOperationOrchestratorService bankingOperationService =
				applicationContext.getBean(BankingOperationOrchestratorService.class);

		bankingOperationService.runLimitValidationFailureExample();
		bankingOperationService.runSuccessfulExample();
	}
}
