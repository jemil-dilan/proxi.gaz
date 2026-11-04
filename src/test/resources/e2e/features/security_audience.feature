Feature: Security - Audience Separation

  Scenario: Admin JWT token is rejected on vendor endpoint
    Given I am connected with an admin token with subject "proxigaz-e2e-admin"
    When I try to access my vendor session
    Then The last request failed with the http status 'UNAUTHORIZED' or 'FORBIDDEN'

  Scenario: Vendor opaque token is rejected on admin endpoint
    Given I am connected with an admin token with subject "proxigaz-e2e-admin"
    And I create a vendor with the following data
      | name    | phone            | latitude | longitude | addressText | isStation | deliveryFeeType | deliveryFeeAmount | isActive |
      | Vendor1 | +237699002020 | 4.05     | 9.7       | Address1    | false     | FIXED           | 500               | true    |
    And I rotate the vendor session as admin
    When I try to list vendors with the vendor session token
    Then The last request failed with the http status 'UNAUTHORIZED' or 'FORBIDDEN'

  Scenario: Revoked vendor token is rejected
    Given I am connected with an admin token with subject "proxigaz-e2e-admin"
    And I create a vendor with the following data
      | name    | phone            | latitude | longitude | addressText | isStation | deliveryFeeType | deliveryFeeAmount | isActive |
      | Vendor2 | +237699002021 | 4.05     | 9.7       | Address2    | false     | FIXED           | 500               | true    |
    And I rotate the vendor session as admin
    When I revoke the vendor session as admin
    And I try to access vendor session with the revoked token
    Then The last request failed with the http status 'UNAUTHORIZED'
