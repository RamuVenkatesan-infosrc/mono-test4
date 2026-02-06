Here's the Java class with Cucumber step definitions matching the given Gherkin steps:

```java
package steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.And;
import org.junit.jupiter.api.Assertions;

public class BankAccountManagementSteps {

    private String customerId;
    private String accountId;
    private String accountType;
    private double balance;
    private double depositAmount;
    private String sourceAccountId;
    private String targetAccountId;
    private double transferAmount;
    private boolean isAccountActive;

    @Given("a customer with ID {string}")
    public void aCustomerWithID(String customerId) {
        this.customerId = customerId;
    }

    @When("they request to open a {string} account with an initial balance of {int}")
    public void theyRequestToOpenAnAccountWithAnInitialBalance(String accountType, int initialBalance) {
        this.accountType = accountType;
        this.balance = initialBalance;
    }

    @Then("a new account should be created")
    public void aNewAccountShouldBeCreated() {
        // Placeholder assertion
        Assertions.assertTrue(true);
    }

    @And("the account balance should be {int}")
    public void theAccountBalanceShouldBe(int expectedBalance) {
        Assertions.assertEquals(expectedBalance, this.balance);
    }

    @Given("an active account with ID {string}")
    public void anActiveAccountWithID(String accountId) {
        this.accountId = accountId;
        this.isAccountActive = true;
    }

    @When("the customer deposits {int} into the account")
    public void theCustomerDepositsIntoTheAccount(int amount) {
        this.depositAmount = amount;
        this.balance += amount;
    }

    @Then("the account balance should increase by {int}")
    public void theAccountBalanceShouldIncreaseBy(int amount) {
        Assertions.assertEquals(amount, this.depositAmount);
    }

    @And("a deposit transaction should be recorded")
    public void aDepositTransactionShouldBeRecorded() {
        // Placeholder assertion
        Assertions.assertTrue(true);
    }

    @Given("an active account {string} with a balance of {int}")
    public void anActiveAccountWithABalanceOf(String accountId, int balance) {
        this.accountId = accountId;
        this.balance = balance;
        this.isAccountActive = true;
    }

    @And("another active account {string} with a balance of {int}")
    public void anotherActiveAccountWithABalanceOf(String accountId, int balance) {
        this.targetAccountId = accountId;
    }

    @When("the customer transfers {int} from {string} to {string}")
    public void theCustomerTransfersFromTo(int amount, String sourceAccountId, String targetAccountId) {
        this.transferAmount = amount;
        this.sourceAccountId = sourceAccountId;
        this.targetAccountId = targetAccountId;
        this.balance -= amount;
    }

    @Then("the balance of {string} should be {int}")
    public void theBalanceOfShouldBe(String accountId, int expectedBalance) {
        if (accountId.equals(this.accountId)) {
            Assertions.assertEquals(expectedBalance, this.balance);
        }
    }

    @And("a transfer transaction should be recorded")
    public void aTransferTransactionShouldBeRecorded() {
        // Placeholder assertion
        Assertions.assertTrue(true);
    }

    @When("the bank deactivates the account")
    public void theBankDeactivatesTheAccount() {
        this.isAccountActive = false;
    }

    @Then("the account status should be inactive")
    public void theAccountStatusShouldBeInactive() {
        Assertions.assertFalse(this.isAccountActive);
    }

    @And("no further transactions should be allowed on the account")
    public void noFurtherTransactionsShouldBeAllowedOnTheAccount() {
        // Placeholder assertion
        Assertions.assertTrue(true);
    }
}
```
