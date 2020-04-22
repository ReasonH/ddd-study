package aggregate;
import java.math.BigDecimal;

public class Money {
    private final BigDecimal amount;

    public Money(BigDecimal amount) {
        this.amount = amount;
    }

    public Money(long amount) {
        this(new BigDecimal(amount));
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof Money)) {
            return false;
        }
        return amount.equals(((Money)object).amount);
    }

    public int hashCode() {
        return amount.hashCode();
    }

    public Money add(Money added) {
        return new Money(this.amount.add(added.amount));
    }

    public Money multiply(int quntity) {
        return new Money(this.amount.multiply(new BigDecimal(quntity)));
    }

    public boolean isGreaterThan(Money limitPrice){
        return this.amount.compareTo(limitPrice.amount) == 1;
    }

    public String toString() {
        return amount.toString();
    }
}