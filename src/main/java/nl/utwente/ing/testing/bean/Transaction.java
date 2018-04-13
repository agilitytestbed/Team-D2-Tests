package nl.utwente.ing.testing.bean;

/**
 * The Transaction class.
 * Used to store information about a Transaction.
 *
 * @author Daan Kooij
 */
public class Transaction {

    private String date;
    private float amount;
    private String externalIBAN;
    private String type;

    /**
     * An empty constructor of Transaction.
     * Used by the Spring framework.
     */
    public Transaction() {

    }

    /**
     * A constructor of Transaction.
     *
     * @param date         The date of the to be created Transaction.
     * @param amount       The amount of the to be created Transaction.
     * @param externalIBAN The externalIBAN of the to be created Transaction.
     * @param type         The type of the to be created Transaction.
     */
    public Transaction(String date, float amount, String externalIBAN, String type) {
        this.date = date;
        this.amount = amount;
        this.externalIBAN = externalIBAN;
        this.type = type;
    }

    /**
     * Method used to retrieve the date of Transaction.
     *
     * @return The date of Transaction.
     */
    public String getDate() {
        return date;
    }

    /**
     * Method used to update the date of Transaction.
     *
     * @param date The new date of Transaction.
     */
    public void setDate(String date) {
        this.date = date;
    }

    /**
     * Method used to retrieve the amount of Transaction.
     *
     * @return The amount of Transaction.
     */
    public float getAmount() {
        return amount;
    }

    /**
     * Method used to update the amount of Transaction.
     *
     * @param amount The new amount of Transaction.
     */
    public void setAmount(float amount) {
        this.amount = amount;
    }

    /**
     * Method used to retrieve the externalIBAN of Transaction.
     *
     * @return The externalIBAN of Transaction.
     */
    public String getExternalIBAN() {
        return externalIBAN;
    }

    /**
     * Method used to update the externalIBAN of Transaction.
     *
     * @param externalIBAN The new externalIBAN of Transaction.
     */
    public void setExternalIBAN(String externalIBAN) {
        this.externalIBAN = externalIBAN;
    }

    /**
     * Method used to retrieve the type of Transaction.
     *
     * @return The type of Transaction.
     */
    public String getType() {
        return type;
    }

    /**
     * Method used to update the type of Transaction.
     *
     * @param type The new type of Transaction.
     */
    public void setType(String type) {
        this.type = type;
    }

}
