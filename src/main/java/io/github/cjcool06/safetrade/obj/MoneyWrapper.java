package io.github.cjcool06.safetrade.obj;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import io.github.cjcool06.safetrade.SafeTrade;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContext;
import org.spongepowered.api.service.economy.Currency;
import org.spongepowered.api.service.economy.account.Account;
import org.spongepowered.api.service.economy.transaction.TransactionResult;

import java.math.BigDecimal;

/**
 * A MoneyWrapper encapsulates the functionality of depositing
 * currency-specific money to {@link Account}s.
 */
public class MoneyWrapper {

    private final Currency currency;
    private final long balance;

    public MoneyWrapper(Currency currency, long balance) {
        this.currency = currency;
        this.balance = balance;
    }

    /**
     * Gets the {@link Currency}.
     *
     * @return The currency
     */
    public Currency getCurrency() {
        return currency;
    }

    /**
     * Gets the balance of money.
     *
     * @return The balance
     */
    public long getBalance() {
        return balance;
    }

    /**
     * Transfers the currency-specific balance to an {@link Account}.
     *
     * @param account The account
     * @return The result
     */
    public TransactionResult transferBalance(Account account) {
        return account.deposit(currency, BigDecimal.valueOf(balance), Cause.of(EventContext.empty(), SafeTrade.getPlugin()));
    }

    public void toContainer(JsonObject jsonObject) {
        jsonObject.add("Currency", new JsonPrimitive(currency.getId()));
        jsonObject.add("Balance", new JsonPrimitive(balance));
    }

    public static MoneyWrapper fromContainer(JsonObject jsonObject) {
        try {
            String currencyID = jsonObject.get("Currency").getAsString();
            long balance = jsonObject.get("Balance").getAsLong();

            for (Currency currency : SafeTrade.getEcoService().getCurrencies()) {
                if (currency.getId().equals(currencyID)) {
                    return new MoneyWrapper(currency, balance);
                }
            }

            throw new Exception("No currency was found for id:" + currencyID);
        } catch (Exception e) {
            SafeTrade.getLogger().warn("There was a problem deserialising a MoneyWrapper from a container." +
                    "\n" + e.getMessage() + "\n");
            e.printStackTrace();
            return null;
        }
    }
}
