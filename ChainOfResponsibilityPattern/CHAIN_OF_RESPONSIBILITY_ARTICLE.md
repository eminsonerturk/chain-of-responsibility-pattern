# Bankacılık İşlemlerinde Chain of Responsibility Tasarım Deseni

## Giriş

Bir banka transferi, tek bir kontrolden ibaret değildir. Hesabın kullanılabilir ve desteklenen para biriminde olması, transfer tutarının geçerli olması, günlük limitin aşılmaması ve işlemin şüpheli görünmemesi gerekir. Bu kuralların tümünü tek bir uzun metotta toplamak, yeni bir kontrol eklendikçe metodu büyütür; kuralların sırasını ve hangi durumda işlemin duracağını takip etmeyi de zorlaştırır.

Örneğin para birimi kontrolü başarısız olduğunda transfer işlemini sürdürmek doğru değildir. Buna karşılık kesici olmayan bir bilgilendirme veya uyarı, sonraki kontrollerin de çalışmasına izin verebilir. Chain of Responsibility (Sorumluluk Zinciri) deseni, bu kontrolleri sıralı ve birbirinden ayrılabilir adımlar hâline getirerek her adımın sonucu üzerinden devam veya durma kararı verilmesini sağlar.

Bu projede zincirin adımları hesap kontrolü, transfer kontrolü, limit kontrolü ve fraud kontrolüdür. Örnek uygulama gerçek bir bankacılık servisine bağlanmaz; doğrulama akışını göstermek için örnek DTO verileri üretir.

## Gelişme

### 1. İşleme ait ortak context

Kontroller aynı müşteri listesine ve o müşterilere ait hesap, transfer, limit ve fraud bilgilerine ihtiyaç duyar. Bu veriler `BankingOperationContext` içinde taşınır:

```java
public class BankingOperationContext {
    private List<Long> customerNoList;
    private HashMap<Long, AccountDTO> mapOfCustomerNoToAccountDto;
    private HashMap<Long, TransferDTO> mapOfCustomerNoToTransferDto;
    private HashMap<Long, LimitDTO> mapOfCustomerNoToLimitDto;
    private HashMap<Long, FraudCheckDTO> mapOfCustomerNoToFraudCheckDto;
}
```

Her doğrulayıcıya aynı context verilir. Böylece adımların birbirine DTO veya servis sonucu aktarmasına gerek kalmadan işlem verilerine erişmesi mümkün olur.

### 2. Her adımın ortak sözleşmesi

Projede doğrulayıcılar, tek bir `validate` metoduna sahip generic bir fonksiyonel arayüzle tanımlanır:

```java
@FunctionalInterface
public interface BankingOperationValidator<T> {
    BankingOperationResult validate(T context);
}
```

`BankingOperationServiceImpl` bu sözleşmenin `BankingOperationContext` için uygulamalarını sağlar. Örneğin hesap adımı, bakiyenin negatif olmamasını ve para biriminin `TRY` olmasını denetler:

```java
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

return BankingOperationResult.ok();
```

Diğer adımlar benzer biçimde transfer tutarının pozitifliğini ve kaynak/hedef IBAN farklılığını, günlük limit kullanımını ve fraud durumunu kontrol eder. Herhangi bir kontrolün başarısız olması `error(...)` sonucu üretir.

### 3. Sonucun devam veya kesme anlamı

`BankingOperationResult` üç bilgiyi taşır: doğrulama başarılı mı (`valid`), zincir kesilmeli mi (`chainBreaker`) ve açıklama mesajı (`message`). Sonuç üretmek için projede şu yardımcı metotlar bulunur:

```java
public static BankingOperationResult ok() {
    return new BankingOperationResult(true, false, null);
}

public static BankingOperationResult error(String msg) {
    return new BankingOperationResult(false, true, msg);
}

public static BankingOperationResult info(String msg) {
    return new BankingOperationResult(false, false, msg);
}
```

`ok()` zincirin devam etmesine izin veren başarılı sonucu, `error(...)` ise hata mesajıyla birlikte zinciri kesen sonucu üretir. `info(...)` başarısız bir sonuçtur ama `chainBreaker` değeri `false` olduğu için zinciri kesmez. Mevcut chain sınıfı bu tür bir sonucun mesajını saklamaz; yalnızca sonraki doğrulayıcıya geçer. Bilgilendirme mesajlarının kullanıcıya gösterilmesi istenirse sonuç toplama davranışının ayrıca eklenmesi gerekir.

### 4. Zincirin kurulması

`BankingOperationOrchestrator`, doğrulama sırasını belirler. Metot referansları sayesinde her servis metodu, ortak `BankingOperationValidator` sözleşmesini karşılayan bir zincir adımı olur:

```java
public List<BankingOperationValidator<BankingOperationContext>>
        prepareBankingOperationChainForCustomers() {
    return List.of(
            bankingOperationService::accountOperation,
            bankingOperationService::transferOperation,
            bankingOperationService::limitOperation,
            bankingOperationService::fraudOperation
    );
}
```

Sıra önemlidir: hesap kontrolü başarısızsa transfer, limit ve fraud kontrollerine geçilmez. Hesap geçerliyse sıradaki adıma ilerlenir.

### 5. Zincir kesici davranış

`BankingOperationChain`, doğrulayıcıları sırayla çalıştırır. Sonuç geçersiz ve kesici ise hata mesajını sonuç DTO’suna koyup döner:

```java
for (BankingOperationValidator<BankingOperationContext> validator : bankingOperationValidatorList) {
    BankingOperationResult result = validator.validate(context);

    if (!result.valid() && result.chainBreaker()) {
        resultDTO.setErrorMessage(result.message());
        return Optional.of(resultDTO);
    }
}
```

Bu koşul zincirin temel karar noktasıdır:

- `valid == true`: Sonraki adıma devam edilir.
- `valid == false` ve `chainBreaker == true`: Hata kaydedilir ve zincir hemen durur.
- `valid == false` ve `chainBreaker == false`: Zincir devam eder; mevcut uygulamada bu sonucun mesajı kaydedilmez.

Hiçbir kesici hata oluşmazsa zincir tamamlanır ve hata mesajı boş olan sonuç döner.

### 6. Hata örneği: üçüncü adımda durma

`runLimitValidationFailureExample()` örnek context’i `1001` müşteri numarasıyla kurar. Hesap ve transfer verileri geçerlidir; günlük limit `1000`, kullanılan tutar ise `1200` olarak ayarlanmıştır:

```java
BankingOperationContext context =
        createExampleContext(List.of(1001L), new BigDecimal("1200"));
BankingOperationResult result = executeBankingOperations(context);

if (!result.valid()) {
    System.out.println("Limit doğrulama hatası: " + result.message());
}
```

İlk adım olan hesap kontrolü ve ikinci adım olan transfer kontrolü başarılı olur. Üçüncü adım limit kontrolü `1200 > 1000` durumunu bulur ve `error(...)` döndürür. Chain bu sonucu kesici olarak algılar, hata mesajını sonuç DTO’suna yazar ve fraud kontrolünü çalıştırmadan durur. Konsolda limit aşımını açıklayan mesaj görülür.

### 7. Başarı örneği: bütün adımları geçme

Başarılı örnekte context aynı hesap ve transfer verilerini kullanır; yalnızca günlük kullanım `100` olarak belirlenir. Bu değer `1000` limitinin altında olduğundan limit kontrolü de geçer:

```java
BankingOperationContext context =
        createExampleContext(List.of(1001L), new BigDecimal("100"));
BankingOperationResult result = executeBankingOperations(context);

System.out.println(result.valid()
        ? "Tüm doğrulamalar başarıyla geçti."
        : "Doğrulama hatası: " + result.message());
```

Hesap, transfer ve limit kontrolleri geçtikten sonra fraud kontrolü çalışır. Örnek veride işlem şüpheli işaretlenmemiş ve kanal `MOBILE` olduğu için bu adım da başarılıdır. Zincirin tamamı çalışır ve başarı mesajı yazdırılır.

### 8. Örneklerin uygulama başlangıcından çağrılması

Spring Boot başladıktan sonra servis bean’i uygulama context’inden alınabilir ve iki senaryo da interface üzerinden çalıştırılabilir:

```java
public static void main(String[] args) {
    var applicationContext =
            SpringApplication.run(ChainOfResponsibilityPatternApplication.class, args);

    BankingOperationOrchestratorService service =
            applicationContext.getBean(BankingOperationOrchestratorService.class);

    service.runLimitValidationFailureExample();
    service.runSuccessfulExample();
}
```

Bu örnekte Spring, `BankingOperationOrchestratorService` implementasyonunu ve onun bağımlılıklarını kendisi oluşturur. Hata senaryosunun ardından başarı senaryosu çalıştırılır.

## Sonuç

Chain of Responsibility, bankacılık işlemindeki kontrolleri sıralı ve ayrı sorumluluklara bölerek yeni kuralların eklenmesini ve mevcut adımların anlaşılmasını kolaylaştırır. Zincir kesici sonuç, güvenlik veya iş kuralı ihlali görüldüğünde işlemin sonraki adımlara ulaşmasını engeller. Kesici olmayan sonuç ise akışın sürmesini sağlar. Bu projedeki örnek; doğrulama sırasını orchestrator’da, iş kurallarını servis adımlarında ve devam/durma kararını chain sınıfında toplar.

Örnek uygulama desenin işleyişini göstermek içindir. Gerçek bankacılık sistemlerinde ayrıca kalıcı veri erişimi, yetkilendirme, işlem tutarlılığı, denetim kayıtları ve kapsamlı hata yönetimi gerekir.
