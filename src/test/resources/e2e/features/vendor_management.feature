Feature: Admin vendor management

  Scenario: An admin creates and updates a vendor with a PostGIS location
    Given I am connected with the scopes 'admin'
    When I create a vendor with the following data
      | name             | phone         | latitude | longitude | addressText       | isStation | deliveryFeeType | deliveryFeeAmount | isActive |
      | E2E Vendor North | +237699002001 | 4.0483   | 9.7048    | E2E address, Akwa | false     | flat            | 1250              | true     |
    Then the vendor create request succeeds
    And the created user is linked to the vendor and has a stored session hash
    And the vendor is persisted with the following data
      | name             | phone         | latitude | longitude | addressText       | isStation | deliveryFeeType | deliveryFeeAmount | isActive |
      | E2E Vendor North | +237699002001 | 4.0483   | 9.7048    | E2E address, Akwa | false     | flat            | 1250              | true     |
    When I update that vendor with the following data
      | name           | phone         | latitude | longitude | addressText        | isStation | deliveryFeeType | deliveryFeeAmount |
      | E2E Vendor Hub | +237699002002 | 4.0601   | 9.7112    | E2E address, Deido | true      | flat            | 1500              |
    Then the vendor update request succeeds
    And the user and vendor remain linked after update
    And the vendor is persisted with the following data
      | name           | phone         | latitude | longitude | addressText        | isStation | deliveryFeeType | deliveryFeeAmount | isActive |
      | E2E Vendor Hub | +237699002002 | 4.0601   | 9.7112    | E2E address, Deido | true      | flat            | 1500              | true     |
    When I set that vendor's active status to 'false'
    Then the vendor status request succeeds with status 204
    And the vendor active status is false
    When I set that vendor's active status to 'true'
    Then the vendor status request succeeds with status 204
    And the vendor active status is true

  Scenario: Invalid vendor data is rejected
    Given I am connected with the scopes 'admin'
    When I create a vendor with the following data
      | name | phone | latitude | longitude | addressText | isStation | deliveryFeeType | deliveryFeeAmount | isActive |
      |      |       | 95       | 190       |             | false     |                 |                   | true     |
    Then the vendor request is rejected with status 400

  Scenario: An admin cannot update a missing vendor
    Given I am connected with the scopes 'admin'
    When I update a missing vendor with the following data
      | name       | phone         | latitude | longitude | addressText | isStation | deliveryFeeType | deliveryFeeAmount | isActive |
      | Missing ID | +237699002003 | 4.0500   | 9.7000    | Address     | false     | flat            | 1000              | true     |
    Then the vendor request is rejected with status 404

  Scenario: An admin cannot change the status of a missing vendor
    Given I am connected with the scopes 'admin'
    When I set a missing vendor's active status to 'true'
    Then the vendor request is rejected with status 404

  Scenario: A non-admin cannot change vendor status
    Given I am connected with the scopes 'demo:create'
    When I set a missing vendor's active status to 'true'
    Then the vendor request is rejected with status 403

  Scenario: A non-admin cannot create a vendor
    Given I am connected with the scopes 'demo:create'
    When I create a vendor with the following data
      | name        | phone         | latitude | longitude | addressText | isStation | deliveryFeeType | deliveryFeeAmount | isActive |
      | Denied user | +237699002004 | 4.0500   | 9.7000    | Address     | false     | flat            | 1000              | true     |
    Then the vendor request is rejected with status 403

  Scenario: Each created vendor receives a distinct one-time session token
    Given I am connected with the scopes 'admin'
    When I create a vendor with the following data
      | name    | phone         | latitude | longitude | addressText | isStation | isActive |
      | Token A | +237699002021 | 4.0500   | 9.7000    | Akwa        | false     | true     |
    Then the vendor create request succeeds
    When I create a vendor with the following data
      | name    | phone         | latitude | longitude | addressText | isStation | isActive |
      | Token B | +237699002022 | 4.0510   | 9.7010    | Deido       | false     | true     |
    Then the vendor create request succeeds
    And the generated vendor session tokens are distinct
