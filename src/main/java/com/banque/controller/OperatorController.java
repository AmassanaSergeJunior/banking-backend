package com.banque.controller;

import com.banque.domain.enums.OperatorType;
import com.banque.factory.operator.products.AccountValidator;
import com.banque.factory.operator.products.ExternalSystemAdapter;
import com.banque.factory.operator.products.NotificationModule;
import com.banque.service.OperatorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller REST pour les opérations liées aux opérateurs.
 *
 * OBJECTIF 2: Ce controller démontre comment le pattern Abstract Factory
 * permet de basculer facilement d'un opérateur à un autre.
 *
 * Endpoints:
 * - GET /api/operators - Liste tous les opérateurs
 * - GET /api/operators/{type} - Info d'un opérateur
 * - POST /api/operators/{type}/validate-account - Valide un compte
 * - POST /api/operators/{type}/calculate-fee - Calcule les frais
 * - POST /api/operators/{type}/send-notification - Envoie une notification
 * - POST /api/operators/{type}/transfer - Effectue un transfert externe
 */
@RestController
@RequestMapping("/api/operators")
@CrossOrigin(origins = "*")
public class OperatorController {

    private final OperatorService operatorService;

    public OperatorController(OperatorService operatorService) {
        this.operatorService = operatorService;
    }

    /**
     * Liste tous les opérateurs disponibles avec leurs caractéristiques.
     */
    @GetMapping
    public ResponseEntity<List<OperatorService.OperatorInfo>> getAllOperators() {
        return ResponseEntity.ok(operatorService.getAllOperatorsInfo());
    }

    /**
     * Retourne les informations d'un opérateur spécifique.
     */
    @GetMapping("/{type}")
    public ResponseEntity<OperatorService.OperatorInfo> getOperator(
            @PathVariable OperatorType type) {
        return ResponseEntity.ok(operatorService.getOperatorInfo(type));
    }

    /**
     * Valide la création d'un compte pour un opérateur donné.
     *
     * Démontre comment le validateur change selon l'opérateur:
     * - BANK: exige 50000 FCFA minimum, format XX0000000000
     * - MOBILE_MONEY: pas de minimum, format 6XXXXXXXX
     * - MICROFINANCE: 5000 FCFA minimum, format MF00000000
     */
    @PostMapping("/{type}/validate-account")
    public ResponseEntity<Map<String, Object>> validateAccount(
            @PathVariable OperatorType type,
            @RequestParam String accountNumber,
            @RequestParam String clientId,
            @RequestParam BigDecimal initialDeposit) {

        AccountValidator.ValidationResult result = operatorService.validateAccountCreation(
            type, accountNumber, clientId, initialDeposit
        );

        Map<String, Object> response = new HashMap<>();
        response.put("operatorType", type);
        response.put("valid", result.isValid());
        response.put("message", result.getMessage());
        response.put("validatorUsed", result.getValidatorName());

        return ResponseEntity.ok(response);
    }

    /**
     * Calcule les frais de transaction selon l'opérateur.
     *
     * Démontre comment le calculateur change selon l'opérateur:
     * - BANK: frais fixes 500 + 1%
     * - MOBILE_MONEY: frais par palier
     * - MICROFINANCE: gratuit < 10000, sinon 0.8%
     */
    @PostMapping("/{type}/calculate-fee")
    public ResponseEntity<Map<String, Object>> calculateFee(
            @PathVariable OperatorType type,
            @RequestParam BigDecimal amount,
            @RequestParam(defaultValue = "TRANSFER") String transactionType) {

        BigDecimal fee = operatorService.calculateTransactionFee(type, amount, transactionType);

        Map<String, Object> response = new HashMap<>();
        response.put("operatorType", type);
        response.put("amount", amount);
        response.put("transactionType", transactionType);
        response.put("fee", fee);
        response.put("totalWithFee", amount.add(fee));

        return ResponseEntity.ok(response);
    }

    /**
     * Envoie une notification de transaction.
     *
     * Démontre comment le format de notification change selon l'opérateur:
     * - BANK: formel, avec numéro de service client
     * - MOBILE_MONEY: court, avec codes USSD
     * - MICROFINANCE: chaleureux, avec nom du conseiller
     */
    @PostMapping("/{type}/send-notification")
    public ResponseEntity<Map<String, Object>> sendNotification(
            @PathVariable OperatorType type,
            @RequestParam String phoneNumber,
            @RequestParam String transactionType,
            @RequestParam BigDecimal amount,
            @RequestParam BigDecimal newBalance) {

        NotificationModule.NotificationResult result = operatorService.sendTransactionNotification(
            type, phoneNumber, transactionType, amount, newBalance
        );

        Map<String, Object> response = new HashMap<>();
        response.put("operatorType", type);
        response.put("sent", result.isSent());
        response.put("channel", result.getChannel());
        response.put("message", result.getMessage());
        response.put("operatorName", result.getOperatorName());

        return ResponseEntity.ok(response);
    }

    /**
     * Effectue un transfert vers un système externe.
     *
     * Démontre comment l'adaptateur externe change selon l'opérateur:
     * - BANK: SWIFT/BEAC
     * - MOBILE_MONEY: API Telecom REST
     * - MICROFINANCE: Réseau caisses partenaires
     */
    @PostMapping("/{type}/transfer")
    public ResponseEntity<Map<String, Object>> executeTransfer(
            @PathVariable OperatorType type,
            @RequestParam String destinationAccount,
            @RequestParam BigDecimal amount,
            @RequestParam(defaultValue = "") String reference) {

        if (reference.isEmpty()) {
            reference = "REF" + System.currentTimeMillis();
        }

        ExternalSystemAdapter.TransferResult result = operatorService.executeExternalTransfer(
            type, destinationAccount, amount, reference
        );

        Map<String, Object> response = new HashMap<>();
        response.put("operatorType", type);
        response.put("success", result.isSuccess());
        response.put("externalReference", result.getExternalReference());
        response.put("message", result.getMessage());
        response.put("externalSystem", result.getSystemName());

        return ResponseEntity.ok(response);
    }

    /**
     * Compare les frais entre tous les opérateurs pour un même montant.
     * Utile pour montrer la différence de comportement des familles d'objets.
     */
    @GetMapping("/compare-fees")
    public ResponseEntity<List<Map<String, Object>>> compareFees(
            @RequestParam BigDecimal amount,
            @RequestParam(defaultValue = "TRANSFER") String transactionType) {

        List<Map<String, Object>> comparison = operatorService.getAllOperatorsInfo().stream()
            .map(info -> {
                BigDecimal fee = operatorService.calculateTransactionFee(
                    info.type(), amount, transactionType
                );

                Map<String, Object> operatorFee = new HashMap<>();
                operatorFee.put("operator", info.name());
                operatorFee.put("type", info.type());
                operatorFee.put("amount", amount);
                operatorFee.put("fee", fee);
                operatorFee.put("total", amount.add(fee));
                operatorFee.put("baseRate", info.baseRate());

                return operatorFee;
            })
            .toList();

        return ResponseEntity.ok(comparison);
    }
}
