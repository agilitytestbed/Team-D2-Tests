package nl.utwente.ing.testing.bean;

public class SavingGoal {

    private String name;
    private float goal;
    private float savePerMonth;
    private float minBalanceRequired;
    private float balance;

    public SavingGoal(String name, float goal, float savePerMonth, float minBalanceRequired, float balance) {
        this.name = name;
        this.goal = goal;
        this.savePerMonth = savePerMonth;
        this.minBalanceRequired = minBalanceRequired;
        this.balance = balance;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public float getGoal() {
        return goal;
    }

    public void setGoal(float goal) {
        this.goal = goal;
    }

    public float getSavePerMonth() {
        return savePerMonth;
    }

    public void setSavePerMonth(float savePerMonth) {
        this.savePerMonth = savePerMonth;
    }

    public float getMinBalanceRequired() {
        return minBalanceRequired;
    }

    public void setMinBalanceRequired(float minBalanceRequired) {
        this.minBalanceRequired = minBalanceRequired;
    }

    public float getBalance() {
        return balance;
    }

    public void setBalance(float balance) {
        this.balance = balance;
    }
}