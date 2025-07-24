package konkuk.corkCharge.domain.corkageStore.dto.request;

public record MultiCorkageRequest(
        String liquorType,
        int price
) {
    public static MultiCorkageRequest of(String liquorType, int price) {
        return new MultiCorkageRequest(liquorType, price);
    }
}