import java.util.*;

/**
 * TESTS OBJECTIF 5: Pattern Adapter
 *
 * Ce fichier teste:
 * 1. Les trois APIs legacy (Orange, MTN, Twilio)
 * 2. Les trois adaptateurs
 * 3. La conversion des formats de numeros
 * 4. La conversion des resultats
 * 5. La selection automatique du provider
 *
 * Compilation: javac Objectif5Test.java
 * Execution: java Objectif5Test
 */
public class Objectif5Test {

    private static int testsReussis = 0;
    private static int testsTotal = 0;

    public static void main(String[] args) {
        System.out.println("================================================================");
        System.out.println("        TESTS OBJECTIF 5 - PATTERN ADAPTER                     ");
        System.out.println("        Integration Services SMS Externes                      ");
        System.out.println("================================================================\n");

        // Tests Orange Adapter
        System.out.println("================================================================");
        System.out.println("  TESTS: OrangeSMSAdapter");
        System.out.println("================================================================\n");

        testOrangeAdapterBasic();
        testOrangeNumberConversion();
        testOrangeStatusConversion();

        // Tests MTN Adapter
        System.out.println("\n================================================================");
        System.out.println("  TESTS: MTNSMSAdapter");
        System.out.println("================================================================\n");

        testMTNAdapterBasic();
        testMTNNumberConversion();
        testMTNBulkSMS();

        // Tests Twilio Adapter
        System.out.println("\n================================================================");
        System.out.println("  TESTS: TwilioSMSAdapter");
        System.out.println("================================================================\n");

        testTwilioAdapterBasic();
        testTwilioE164Conversion();
        testTwilioExceptionHandling();

        // Test Selection Automatique
        System.out.println("\n================================================================");
        System.out.println("  TESTS: Selection Automatique du Provider");
        System.out.println("================================================================\n");

        testProviderSelection();

        // Test Interface Unifiee
        System.out.println("\n================================================================");
        System.out.println("  TESTS: Interface Unifiee (Tous Adapters)");
        System.out.println("================================================================\n");

        testUnifiedInterface();

        // Resume
        System.out.println("\n================================================================");
        System.out.println("                    RESUME DES TESTS                           ");
        System.out.println("================================================================");
        System.out.printf("  Tests reussis: %d/%d%n", testsReussis, testsTotal);
        if (testsReussis == testsTotal) {
            System.out.println("  Statut: [OK] TOUS LES TESTS PASSES");
        } else {
            System.out.println("  Statut: [ECHEC] CERTAINS TESTS ONT ECHOUE");
        }
        System.out.println("================================================================");
    }

    // ==================== TESTS ORANGE ADAPTER ====================

    private static void testOrangeAdapterBasic() {
        System.out.println("TEST 1: Orange Adapter - Envoi SMS basique");
        testsTotal++;

        SMSService adapter = new OrangeSMSAdapter("api-key", "merchant-001", "BANQUE");
        SMSResult result = adapter.sendSMS("+237699123456", "Test Orange");

        boolean success = result.isSuccess()
            && result.getMessageId() != null
            && result.getMessageId().startsWith("ORA-");

        if (success) {
            System.out.println("  [OK] SMS envoye via Orange");
            System.out.println("  [OK] Message ID: " + result.getMessageId());
            System.out.println("  [OK] Provider: " + adapter.getProviderName());
            testsReussis++;
        } else {
            System.out.println("  [ECHEC] " + result.getErrorMessage());
        }
        System.out.println();
    }

    private static void testOrangeNumberConversion() {
        System.out.println("TEST 2: Orange Adapter - Conversion format numero");
        testsTotal++;

        OrangeSMSAdapter adapter = new OrangeSMSAdapter("api-key", "merchant-001", "BANQUE");

        // Test differents formats
        SMSResult result1 = adapter.sendSMS("+237699123456", "Test 1"); // Format international
        SMSResult result2 = adapter.sendSMS("237699123456", "Test 2");  // Sans +
        SMSResult result3 = adapter.sendSMS("699123456", "Test 3");     // Local (9 chiffres)

        boolean success = result1.isSuccess() && result2.isSuccess() && result3.isSuccess();

        if (success) {
            System.out.println("  [OK] +237699123456 -> converti correctement");
            System.out.println("  [OK] 237699123456 -> converti correctement");
            System.out.println("  [OK] 699123456 -> converti correctement");
            testsReussis++;
        } else {
            System.out.println("  [ECHEC] Erreur de conversion de numero");
        }
        System.out.println();
    }

    private static void testOrangeStatusConversion() {
        System.out.println("TEST 3: Orange Adapter - Conversion statuts");
        testsTotal++;

        SMSService adapter = new OrangeSMSAdapter("api-key", "merchant-001", "BANQUE");

        // Envoyer un SMS pour obtenir un ID
        SMSResult result = adapter.sendSMS("+237699123456", "Test status");
        SMSStatus status = adapter.checkStatus(result.getMessageId());

        boolean success = status != null && status != SMSStatus.UNKNOWN;

        if (success) {
            System.out.println("  [OK] Statut recupere: " + status.name());
            System.out.println("  [OK] Description: " + status.getDescription());
            System.out.println("  [OK] Conversion Orange -> SMSStatus reussie");
            testsReussis++;
        } else {
            System.out.println("  [ECHEC] Statut: " + status);
        }
        System.out.println();
    }

    // ==================== TESTS MTN ADAPTER ====================

    private static void testMTNAdapterBasic() {
        System.out.println("TEST 4: MTN Adapter - Envoi SMS basique");
        testsTotal++;

        SMSService adapter = new MTNSMSAdapter("client-id", "client-secret", "BANQUE");
        SMSResult result = adapter.sendSMS("+237670123456", "Test MTN");

        boolean success = result.isSuccess()
            && result.getMessageId() != null
            && result.getMessageId().startsWith("MTN-");

        if (success) {
            System.out.println("  [OK] SMS envoye via MTN");
            System.out.println("  [OK] Transaction ID: " + result.getMessageId());
            System.out.println("  [OK] Provider: " + adapter.getProviderName());
            testsReussis++;
        } else {
            System.out.println("  [ECHEC] " + result.getErrorMessage());
        }
        System.out.println();
    }

    private static void testMTNNumberConversion() {
        System.out.println("TEST 5: MTN Adapter - Conversion format numero");
        testsTotal++;

        MTNSMSAdapter adapter = new MTNSMSAdapter("client-id", "client-secret", "BANQUE");

        // Test differents formats
        SMSResult result1 = adapter.sendSMS("+237670123456", "Test 1"); // Deja bon format
        SMSResult result2 = adapter.sendSMS("237670123456", "Test 2");  // Sans +
        SMSResult result3 = adapter.sendSMS("670123456", "Test 3");     // Local

        boolean success = result1.isSuccess() && result2.isSuccess() && result3.isSuccess();

        if (success) {
            System.out.println("  [OK] +237670123456 -> garde tel quel");
            System.out.println("  [OK] 237670123456 -> +237670123456");
            System.out.println("  [OK] 670123456 -> +237670123456");
            testsReussis++;
        } else {
            System.out.println("  [ECHEC] Erreur de conversion");
        }
        System.out.println();
    }

    private static void testMTNBulkSMS() {
        System.out.println("TEST 6: MTN Adapter - Envoi en masse");
        testsTotal++;

        SMSService adapter = new MTNSMSAdapter("client-id", "client-secret", "BANQUE");

        List<String> numbers = Arrays.asList(
            "+237670000001",
            "+237670000002",
            "+237670000003"
        );

        BulkSMSResult result = adapter.sendBulkSMS(numbers, "Message en masse");

        boolean success = result.getTotalSent() == 3 && result.getTotalFailed() == 0;

        if (success) {
            System.out.println("  [OK] " + result.getTotalSent() + "/" + numbers.size() + " SMS envoyes");
            System.out.println("  [OK] Taux de succes: " + String.format("%.1f%%", result.getSuccessRate()));
            testsReussis++;
        } else {
            System.out.println("  [ECHEC] " + result.getTotalSent() + " envoyes, " + result.getTotalFailed() + " echecs");
        }
        System.out.println();
    }

    // ==================== TESTS TWILIO ADAPTER ====================

    private static void testTwilioAdapterBasic() {
        System.out.println("TEST 7: Twilio Adapter - Envoi SMS basique");
        testsTotal++;

        SMSService adapter = new TwilioSMSAdapter("AC-account-sid", "auth-token", "+15551234567");
        SMSResult result = adapter.sendSMS("+237699123456", "Test Twilio");

        boolean success = result.isSuccess()
            && result.getMessageId() != null
            && result.getMessageId().startsWith("SM");

        if (success) {
            System.out.println("  [OK] SMS envoye via Twilio");
            System.out.println("  [OK] Message SID: " + result.getMessageId());
            System.out.println("  [OK] Provider: " + adapter.getProviderName());
            testsReussis++;
        } else {
            System.out.println("  [ECHEC] " + result.getErrorMessage());
        }
        System.out.println();
    }

    private static void testTwilioE164Conversion() {
        System.out.println("TEST 8: Twilio Adapter - Conversion format E.164");
        testsTotal++;

        TwilioSMSAdapter adapter = new TwilioSMSAdapter("AC-sid", "token", "+15551234567");

        // Test differents formats
        SMSResult result1 = adapter.sendSMS("+237699123456", "Test 1");  // E.164
        SMSResult result2 = adapter.sendSMS("237699123456", "Test 2");   // Sans +
        SMSResult result3 = adapter.sendSMS("+33612345678", "Test 3");   // France

        boolean success = result1.isSuccess() && result2.isSuccess() && result3.isSuccess();

        if (success) {
            System.out.println("  [OK] +237699123456 -> valide E.164");
            System.out.println("  [OK] 237699123456 -> +237699123456");
            System.out.println("  [OK] +33612345678 -> valide E.164 (France)");
            testsReussis++;
        } else {
            System.out.println("  [ECHEC] Erreur de conversion E.164");
        }
        System.out.println();
    }

    private static void testTwilioExceptionHandling() {
        System.out.println("TEST 9: Twilio Adapter - Gestion des erreurs");
        testsTotal++;

        TwilioSMSAdapter adapter = new TwilioSMSAdapter("AC-sid", "token", "+15551234567");

        // Numero invalide (devrait echouer)
        SMSResult resultInvalid = adapter.sendSMS("invalid", "Test");

        // Message vide (devrait echouer)
        SMSResult resultEmpty = adapter.sendSMS("+237699123456", "");

        boolean success = !resultInvalid.isSuccess() && !resultEmpty.isSuccess()
            && resultInvalid.getErrorMessage().contains("Twilio")
            && resultEmpty.getErrorMessage().contains("Twilio");

        if (success) {
            System.out.println("  [OK] Numero invalide detecte: " + resultInvalid.getErrorMessage());
            System.out.println("  [OK] Message vide detecte: " + resultEmpty.getErrorMessage());
            System.out.println("  [OK] Exceptions Twilio converties en SMSResult.failure()");
            testsReussis++;
        } else {
            System.out.println("  [ECHEC] Gestion des erreurs incorrecte");
        }
        System.out.println();
    }

    // ==================== TEST SELECTION PROVIDER ====================

    private static void testProviderSelection() {
        System.out.println("TEST 10: Selection automatique du provider");
        testsTotal++;

        // Orange: 69X, 655-659
        String orange1 = selectProvider("+237699123456");
        String orange2 = selectProvider("+237655123456");

        // MTN: 67X, 68X, 650-654
        String mtn1 = selectProvider("+237670123456");
        String mtn2 = selectProvider("+237680123456");

        // Twilio: international
        String twilio1 = selectProvider("+33612345678");
        String twilio2 = selectProvider("+14155551234");

        boolean success = orange1.equals("ORANGE") && orange2.equals("ORANGE")
            && mtn1.equals("MTN") && mtn2.equals("MTN")
            && twilio1.equals("TWILIO") && twilio2.equals("TWILIO");

        if (success) {
            System.out.println("  [OK] +237699... -> Orange Cameroun");
            System.out.println("  [OK] +237670... -> MTN Cameroun");
            System.out.println("  [OK] +33612... -> Twilio (France)");
            System.out.println("  [OK] +1415... -> Twilio (USA)");
            testsReussis++;
        } else {
            System.out.println("  [ECHEC] Selection incorrecte");
            System.out.println("    Orange test: " + orange1 + ", " + orange2);
            System.out.println("    MTN test: " + mtn1 + ", " + mtn2);
            System.out.println("    Twilio test: " + twilio1 + ", " + twilio2);
        }
        System.out.println();
    }

    // ==================== TEST INTERFACE UNIFIEE ====================

    private static void testUnifiedInterface() {
        System.out.println("TEST 11: Interface unifiee - Polymorphisme");
        testsTotal++;

        // Creer les 3 adaptateurs
        SMSService[] adapters = {
            new OrangeSMSAdapter("key", "merchant", "BANQUE"),
            new MTNSMSAdapter("client", "secret", "BANQUE"),
            new TwilioSMSAdapter("sid", "token", "+15551234567")
        };

        boolean allSuccess = true;
        StringBuilder results = new StringBuilder();

        // Utiliser chaque adaptateur de maniere uniforme
        for (SMSService adapter : adapters) {
            String provider = adapter.getProviderName();
            int balance = adapter.getBalance();
            boolean available = adapter.isAvailable();
            SMSResult result = adapter.sendSMS("+237699123456", "Test unifie");

            results.append(String.format("  [OK] %s: balance=%d, available=%s, sent=%s%n",
                provider, balance, available, result.isSuccess()));

            if (!result.isSuccess()) allSuccess = false;
        }

        if (allSuccess) {
            System.out.print(results);
            System.out.println("  [OK] Tous les adapters implementent SMSService correctement");
            testsReussis++;
        } else {
            System.out.println("  [ECHEC] Un ou plusieurs adapters ont echoue");
        }
        System.out.println();
    }

    // ==================== HELPER ====================

    private static String selectProvider(String phoneNumber) {
        if (phoneNumber == null) return "TWILIO";

        String cleaned = phoneNumber.replace("+", "");
        if (cleaned.startsWith("237")) {
            String local = cleaned.substring(3);
            if (local.startsWith("69") || (local.startsWith("65") && local.charAt(2) >= '5')) {
                return "ORANGE";
            }
            if (local.startsWith("67") || local.startsWith("68") ||
                (local.startsWith("65") && local.charAt(2) < '5')) {
                return "MTN";
            }
        }
        return "TWILIO";
    }
}

// ============================================================================
// CLASSES SIMPLIFIEES POUR TEST STANDALONE
// ============================================================================

interface SMSService {
    SMSResult sendSMS(String phoneNumber, String message);
    SMSResult sendSMS(String phoneNumber, String message, String senderId);
    BulkSMSResult sendBulkSMS(List<String> phoneNumbers, String message);
    SMSStatus checkStatus(String messageId);
    int getBalance();
    String getProviderName();
    boolean isAvailable();
}

class SMSResult {
    private final boolean success;
    private final String messageId;
    private final String errorMessage;
    private final int creditsUsed;
    private final long responseTimeMs;

    public SMSResult(boolean success, String messageId, String errorMessage, int credits, long time) {
        this.success = success;
        this.messageId = messageId;
        this.errorMessage = errorMessage;
        this.creditsUsed = credits;
        this.responseTimeMs = time;
    }

    public static SMSResult success(String messageId, int credits, long time) {
        return new SMSResult(true, messageId, null, credits, time);
    }

    public static SMSResult failure(String error) {
        return new SMSResult(false, null, error, 0, 0);
    }

    public boolean isSuccess() { return success; }
    public String getMessageId() { return messageId; }
    public String getErrorMessage() { return errorMessage; }
    public int getCreditsUsed() { return creditsUsed; }
    public long getResponseTimeMs() { return responseTimeMs; }
}

class BulkSMSResult {
    private final int totalSent;
    private final int totalFailed;
    private final List<SMSResult> results;
    private final int totalCreditsUsed;

    public BulkSMSResult(int sent, int failed, List<SMSResult> results, int credits) {
        this.totalSent = sent;
        this.totalFailed = failed;
        this.results = results;
        this.totalCreditsUsed = credits;
    }

    public int getTotalSent() { return totalSent; }
    public int getTotalFailed() { return totalFailed; }
    public double getSuccessRate() {
        int total = totalSent + totalFailed;
        return total > 0 ? (double) totalSent / total * 100 : 0;
    }
}

enum SMSStatus {
    PENDING("En attente"), SENT("Envoye"), DELIVERED("Livre"),
    FAILED("Echec"), EXPIRED("Expire"), UNKNOWN("Inconnu");

    private final String description;
    SMSStatus(String desc) { this.description = desc; }
    public String getDescription() { return description; }
}

// ==================== ORANGE ADAPTER ====================

class OrangeSMSAdapter implements SMSService {
    private final OrangeAPI orangeApi;
    private final String defaultSenderId;

    public OrangeSMSAdapter(String apiKey, String merchantId, String senderId) {
        this.orangeApi = new OrangeAPI(apiKey, merchantId);
        this.defaultSenderId = senderId;
    }

    @Override
    public SMSResult sendSMS(String phoneNumber, String message) {
        return sendSMS(phoneNumber, message, defaultSenderId);
    }

    @Override
    public SMSResult sendSMS(String phoneNumber, String message, String senderId) {
        String orangeFormat = convertToOrangeFormat(phoneNumber);
        Map<String, Object> result = orangeApi.envoyerMessage(orangeFormat, message, senderId);
        return convertResult(result);
    }

    @Override
    public BulkSMSResult sendBulkSMS(List<String> phoneNumbers, String message) {
        List<SMSResult> results = new ArrayList<>();
        int sent = 0, failed = 0, credits = 0;
        for (String phone : phoneNumbers) {
            SMSResult r = sendSMS(phone, message);
            results.add(r);
            if (r.isSuccess()) { sent++; credits += r.getCreditsUsed(); } else { failed++; }
        }
        return new BulkSMSResult(sent, failed, results, credits);
    }

    @Override
    public SMSStatus checkStatus(String messageId) {
        Map<String, Object> status = orangeApi.verifierLivraison(messageId);
        return convertStatus((String) status.get("statut"));
    }

    @Override
    public int getBalance() {
        return (int) orangeApi.consulterSolde().get("creditsDisponibles");
    }

    @Override
    public String getProviderName() { return "Orange Cameroun"; }

    @Override
    public boolean isAvailable() {
        return (boolean) orangeApi.verifierEtatService().get("serviceActif");
    }

    private String convertToOrangeFormat(String phone) {
        if (phone == null) return null;
        String cleaned = phone.trim();
        if (cleaned.startsWith("+")) cleaned = cleaned.substring(1);
        if (!cleaned.startsWith("237") && cleaned.length() == 9) cleaned = "237" + cleaned;
        return cleaned;
    }

    private SMSResult convertResult(Map<String, Object> result) {
        if ((boolean) result.getOrDefault("succes", false)) {
            return SMSResult.success(
                (String) result.get("identifiantMessage"),
                (int) result.getOrDefault("creditsUtilises", 1),
                (long) result.getOrDefault("tempsTraitement", 0L)
            );
        }
        return SMSResult.failure("[Orange] " + result.getOrDefault("messageErreur", "Erreur"));
    }

    private SMSStatus convertStatus(String status) {
        if (status == null) return SMSStatus.UNKNOWN;
        switch (status) {
            case "LIVRE": return SMSStatus.DELIVERED;
            case "EN_ATTENTE": return SMSStatus.PENDING;
            case "ECHEC": return SMSStatus.FAILED;
            default: return SMSStatus.UNKNOWN;
        }
    }
}

class OrangeAPI {
    private final String apiKey, merchantId;
    private int credits = 1000;
    private boolean actif = true;

    public OrangeAPI(String apiKey, String merchantId) {
        this.apiKey = apiKey;
        this.merchantId = merchantId;
    }

    public Map<String, Object> envoyerMessage(String numero, String contenu, String expediteur) {
        Map<String, Object> result = new HashMap<>();
        try { Thread.sleep(30); } catch (InterruptedException e) {}

        if (!numero.matches("237[0-9]{9}")) {
            result.put("succes", false);
            result.put("messageErreur", "Format numero invalide");
            return result;
        }

        credits--;
        result.put("succes", true);
        result.put("identifiantMessage", "ORA-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        result.put("creditsUtilises", 1);
        result.put("tempsTraitement", 30L);
        return result;
    }

    public Map<String, Object> verifierLivraison(String id) {
        Map<String, Object> result = new HashMap<>();
        result.put("statut", id.hashCode() % 3 == 0 ? "LIVRE" : "EN_ATTENTE");
        return result;
    }

    public Map<String, Object> consulterSolde() {
        return Map.of("creditsDisponibles", credits);
    }

    public Map<String, Object> verifierEtatService() {
        return Map.of("serviceActif", actif);
    }
}

// ==================== MTN ADAPTER ====================

class MTNSMSAdapter implements SMSService {
    private final MTNAPI mtnApi;
    private final String defaultSender;

    public MTNSMSAdapter(String clientId, String clientSecret, String sender) {
        this.mtnApi = new MTNAPI(clientId, clientSecret);
        this.defaultSender = sender;
    }

    @Override
    public SMSResult sendSMS(String phoneNumber, String message) {
        return sendSMS(phoneNumber, message, defaultSender);
    }

    @Override
    public SMSResult sendSMS(String phoneNumber, String message, String senderId) {
        String mtnFormat = convertToMTNFormat(phoneNumber);
        MTNAPI.MTNResponse response = mtnApi.dispatchSMS(mtnFormat, message, senderId);
        return convertResponse(response);
    }

    @Override
    public BulkSMSResult sendBulkSMS(List<String> phoneNumbers, String message) {
        List<SMSResult> results = new ArrayList<>();
        int sent = 0, failed = 0, credits = 0;
        for (String phone : phoneNumbers) {
            SMSResult r = sendSMS(phone, message);
            results.add(r);
            if (r.isSuccess()) { sent++; credits++; } else { failed++; }
        }
        return new BulkSMSResult(sent, failed, results, credits);
    }

    @Override
    public SMSStatus checkStatus(String messageId) {
        String status = mtnApi.getDeliveryStatus(messageId);
        switch (status) {
            case "DELIVERED": return SMSStatus.DELIVERED;
            case "PENDING": return SMSStatus.PENDING;
            case "FAILED": return SMSStatus.FAILED;
            default: return SMSStatus.UNKNOWN;
        }
    }

    @Override
    public int getBalance() { return mtnApi.getAvailableUnits(); }

    @Override
    public String getProviderName() { return "MTN Cameroun"; }

    @Override
    public boolean isAvailable() { return mtnApi.isOnline(); }

    private String convertToMTNFormat(String phone) {
        if (phone == null) return null;
        String cleaned = phone.trim();
        if (cleaned.startsWith("+237")) return cleaned;
        if (cleaned.startsWith("237")) return "+" + cleaned;
        if (cleaned.length() == 9) return "+237" + cleaned;
        if (cleaned.startsWith("+")) return cleaned;
        return "+237" + cleaned;
    }

    private SMSResult convertResponse(MTNAPI.MTNResponse response) {
        if (response.successful) {
            return SMSResult.success(response.transactionId, 1, response.processingTime);
        }
        return SMSResult.failure("[MTN " + response.errorCode + "] " + response.errorMessage);
    }
}

class MTNAPI {
    private int units = 500;
    private boolean online = true;

    public MTNAPI(String clientId, String clientSecret) {}

    public MTNResponse dispatchSMS(String recipient, String content, String sender) {
        try { Thread.sleep(40); } catch (InterruptedException e) {}

        if (!recipient.matches("\\+237[0-9]{9}")) {
            return new MTNResponse(false, null, "INVALID_RECIPIENT", "Format invalide", 0);
        }

        units--;
        return new MTNResponse(true, "MTN-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase(),
                               null, null, 40);
    }

    public String getDeliveryStatus(String id) {
        return id.hashCode() % 3 == 0 ? "DELIVERED" : "PENDING";
    }

    public int getAvailableUnits() { return units; }
    public boolean isOnline() { return online; }

    static class MTNResponse {
        boolean successful;
        String transactionId, errorCode, errorMessage;
        long processingTime;

        MTNResponse(boolean success, String txId, String errCode, String errMsg, long time) {
            this.successful = success;
            this.transactionId = txId;
            this.errorCode = errCode;
            this.errorMessage = errMsg;
            this.processingTime = time;
        }
    }
}

// ==================== TWILIO ADAPTER ====================

class TwilioSMSAdapter implements SMSService {
    private final TwilioAPI twilioApi;
    private final String defaultFrom;

    public TwilioSMSAdapter(String accountSid, String authToken, String defaultFrom) {
        this.twilioApi = new TwilioAPI(accountSid, authToken);
        this.defaultFrom = defaultFrom;
    }

    @Override
    public SMSResult sendSMS(String phoneNumber, String message) {
        return sendSMS(phoneNumber, message, defaultFrom);
    }

    @Override
    public SMSResult sendSMS(String phoneNumber, String message, String senderId) {
        String e164 = convertToE164(phoneNumber);
        try {
            TwilioAPI.MessageInstance msg = twilioApi.createMessage(e164, senderId, message);
            return SMSResult.success(msg.sid, 1, msg.responseTime);
        } catch (TwilioAPI.TwilioException e) {
            return SMSResult.failure("[Twilio " + e.code + "] " + e.getMessage());
        }
    }

    @Override
    public BulkSMSResult sendBulkSMS(List<String> phoneNumbers, String message) {
        List<SMSResult> results = new ArrayList<>();
        int sent = 0, failed = 0, credits = 0;
        for (String phone : phoneNumbers) {
            SMSResult r = sendSMS(phone, message);
            results.add(r);
            if (r.isSuccess()) { sent++; credits++; } else { failed++; }
        }
        return new BulkSMSResult(sent, failed, results, credits);
    }

    @Override
    public SMSStatus checkStatus(String messageId) {
        String status = twilioApi.fetchStatus(messageId);
        switch (status) {
            case "DELIVERED": return SMSStatus.DELIVERED;
            case "SENT": return SMSStatus.SENT;
            case "QUEUED": return SMSStatus.PENDING;
            case "FAILED": return SMSStatus.FAILED;
            default: return SMSStatus.UNKNOWN;
        }
    }

    @Override
    public int getBalance() { return (int) (twilioApi.getBalance() / 0.0075); }

    @Override
    public String getProviderName() { return "Twilio International"; }

    @Override
    public boolean isAvailable() { return twilioApi.isActive(); }

    private String convertToE164(String phone) {
        if (phone == null) return null;
        String cleaned = phone.trim();
        if (cleaned.matches("\\+[1-9][0-9]{1,14}")) return cleaned;
        if (cleaned.startsWith("237") && cleaned.length() == 12) return "+" + cleaned;
        if (cleaned.length() == 9) return "+237" + cleaned;
        if (cleaned.matches("[0-9]{10,15}")) return "+" + cleaned;
        return cleaned;
    }
}

class TwilioAPI {
    private double balance = 50.0;
    private boolean active = true;

    public TwilioAPI(String sid, String token) {}

    public MessageInstance createMessage(String to, String from, String body) {
        try { Thread.sleep(50); } catch (InterruptedException e) {}

        if (!to.matches("\\+[1-9][0-9]{1,14}")) {
            throw new TwilioException(21211, "Invalid To number: " + to);
        }
        if (body == null || body.isEmpty()) {
            throw new TwilioException(21602, "Message body required");
        }

        balance -= 0.0075;
        return new MessageInstance("SM" + UUID.randomUUID().toString().replace("-", "").substring(0, 32), 50);
    }

    public String fetchStatus(String sid) {
        return sid.hashCode() % 3 == 0 ? "DELIVERED" : "SENT";
    }

    public double getBalance() { return balance; }
    public boolean isActive() { return active; }

    static class MessageInstance {
        String sid;
        long responseTime;
        MessageInstance(String sid, long time) { this.sid = sid; this.responseTime = time; }
    }

    static class TwilioException extends RuntimeException {
        int code;
        TwilioException(int code, String msg) { super(msg); this.code = code; }
    }
}
