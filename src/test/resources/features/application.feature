Feature: Bank Account Management

  Scenario: Create a new account
    Given a customer with ID "12345"
    When they request to open a "SAVINGS" account with an initial balance of 1000
    Then a new account should be created
    And the account balance should be 1000

  Scenario: Deposit money into an account
    Given an active account with ID "ACC001"
    When the customer deposits 500 into the account
    Then the account balance should increase by 500
    And a deposit transaction should be recorded

  Scenario: Transfer money between accounts
    Given an active account "ACC001" with a balance of 1000
    And another active account "ACC002" with a balance of 500
    When the customer transfers 300 from "ACC001" to "ACC002"
    Then the balance of "ACC001" should be 700
    And the balance of "ACC002" should be 800
    And a transfer transaction should be recorded

  Scenario: Deactivate an account
    Given an active account with ID "ACC003"
    When the bank deactivates the account
    Then the account status should be inactive
    And no further transactions should be allowed on the account
