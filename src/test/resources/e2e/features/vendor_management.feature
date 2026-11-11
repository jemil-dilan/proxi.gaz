Feature: Admin vendor management

  Scenario: An admin creates and updates a vendor with a PostGIS location
    Given I am connected with the scopes 'admin'
    When I create a vendor with the following data
      | name             | phone         | latitude | longitude | addressText       | isStation | deliveryFeeType | deliveryFeeAmount |
      | E2E Vendor North | +237699002001 | 4.0483   | 9.7048    | E2E address, Akwa | false     | FIXED           | 1250              |
    And the created user is linked to the vendor and has a stored session hash
    And the vendor is persisted with the following data
      | phone         | name             | latitude | longitude | addressText       | isStation | deliveryFeeType | deliveryFeeAmount | isActive |
      | +237699002001 | E2E Vendor North | 4.0483   | 9.7048    | E2E address, Akwa | false     | FIXED           | 1250              | true     |
    When I update that vendor with the following data
      | name           | phone         | latitude | longitude | addressText        | isStation | deliveryFeeType | deliveryFeeAmount |
      | E2E Vendor Hub | +237699002002 | 4.0601   | 9.7112    | E2E address, Deido | true      | FIXED           | 1500              |
    And the user and vendor remain linked after update
    And the vendor is persisted with the following data
      | name           | phone         | latitude | longitude | addressText        | isStation | deliveryFeeType | deliveryFeeAmount | isActive |
      | E2E Vendor Hub | +237699002002 | 4.0601   | 9.7112    | E2E address, Deido | true      | FIXED           | 1500              | true     |
    When I set that vendor's active status to 'false'
    And the vendor active status is false
    When I set that vendor's active status to 'true'
    And the vendor active status is true

  Scenario: Invalid vendor data is rejected
    Given I am connected with the scopes 'admin'
    When I try to create a vendor with the following data
      | name | phone | latitude | longitude | addressText | isStation | deliveryFeeType | deliveryFeeAmount |
      |      |       | 95       | 190       |             | false     |                 |                   |
    Then The last request failed with the http status 'BAD_REQUEST' and error code 'PROXIGAZ-400-002'

  Scenario: Missing required vendor flags are rejected at the request boundary
    Given I am connected with the scopes 'admin'
    When I try to create a vendor with the following data
      | name                | phone         | latitude | longitude | addressText | isStation | deliveryFeeType | deliveryFeeAmount |
      | Missing addressText | +237699002024 | 4.05     | 9.70      |             | false     |                 |                   |
    Then The last request failed with the http status 'BAD_REQUEST' and error code 'PROXIGAZ-400-002'

  Scenario: An admin cannot update a missing vendor
    Given I am connected with the scopes 'admin'
    When I try to update a vendor with the following data
      | name       | phone         | latitude | longitude | addressText | isStation | deliveryFeeType | deliveryFeeAmount |
      | Missing ID | +237699002003 | 4.0500   | 9.7000    | Address     | false     | FIXED           | 1000              |
    Then The last request failed with the http status 'NOT_FOUND' and error code 'PROXIGAZ-404-002'

  Scenario: An admin cannot change the status of a missing vendor
    Given I am connected with the scopes 'admin'
    When I try to modify vendor's active status to 'true'
    Then The last request failed with the http status 'NOT_FOUND' and error code 'PROXIGAZ-404-002'

  Scenario: A non-admin cannot change vendor status
    Given I am connected with the scopes 'demo:create'
    When I try to modify vendor's active status to 'true'
    Then The last request failed with the http status 'FORBIDDEN'

  Scenario: A non-admin cannot create a vendor
    Given I am connected with the scopes 'demo:create'
    When I try to create a vendor with the following data
      | name        | phone         | latitude | longitude | addressText | isStation | deliveryFeeType | deliveryFeeAmount |
      | Denied user | +237699002004 | 4.0500   | 9.7000    | Address     | false     | FIXED           | 1000              |
    Then The last request failed with the http status 'FORBIDDEN'

  Scenario: Each created vendor receives a distinct one-time session token
    Given I am connected with the scopes 'admin'
    When I create a vendor with the following data
      | name    | phone         | latitude | longitude | addressText | isStation |
      | Token A | +237699002021 | 4.0500   | 9.7000    | Akwa        | false     |
    When I create a vendor with the following data
      | name    | phone         | latitude | longitude | addressText | isStation |
      | Token B | +237699002022 | 4.0510   | 9.7010    | Deido       | false     |
    And the generated vendor session tokens are distinct

  Scenario: Vendor can authenticate, and admin rotation and revocation invalidate old tokens
    Given I am connected with the scopes 'admin'
    When I create a vendor with the following data
      | name        | phone         | latitude | longitude | addressText | isStation |
      | Session E2E | +237699002023 | 4.0520   | 9.7020    | Akwa        | false     |
    When I access the vendor session using the issued token
    And I rotate the vendor session as admin
    When I try to access the vendor session using the previous token
    Then The last request failed with the http status 'UNAUTHORIZED' and error code 'PROXIGAZ-401-001'
    When I access the vendor session using the issued token
    When I revoke the vendor session as admin
    When I try to access the vendor session using the issued token
    Then The last request failed with the http status 'UNAUTHORIZED' and error code 'PROXIGAZ-401-001'

  Scenario: GET vendor by ID returns session metadata
    Given I am connected with the scopes 'admin'
    When I create a vendor with the following data
      | name         | phone         | latitude | longitude | addressText | isStation |
      | Session Meta | +237699002025 | 4.0530   | 9.7030    | Akwa        | false     |
    When I get vendor by id using the created vendor id
    Then the vendor detail response contains active session metadata

  Scenario: GET vendor by ID for vendor with no active session
    Given I am connected with the scopes 'admin'
    When I create a vendor with the following data
      | name       | phone         | latitude | longitude | addressText | isStation |
      | No Session | +237699002026 | 4.0540   | 9.7040    | Akwa        | false     |
    When I revoke the vendor session as admin
    When I get vendor by id using the created vendor id
    Then the vendor detail response contains revoked session metadata

  Scenario: GET vendor by ID for inactive vendor returns 403 when vendor tries to access
    Given I am connected with the scopes 'admin'
    When I create a vendor with the following data
      | name     | phone         | latitude | longitude | addressText | isStation |
      | Inactive | +237699002027 | 4.0550   | 9.7050    | Akwa        | false     |
    When I set that vendor's active status to 'false'
    When I try to access my vendor session
    Then The last request failed with the http status 'FORBIDDEN' and error code 'PROXIGAZ-403-002'

  Scenario: Admin can get inactive vendor detail
    Given I am connected with the scopes 'admin'
    When I create a vendor with the following data
      | name            | phone         | latitude | longitude | addressText | isStation |
      | Inactive Detail | +237699002030 | 4.0555   | 9.7055    | Akwa        | false     |
    When I set that vendor's active status to 'false'
    When I get vendor by id using the created vendor id
    Then the vendor detail isActive is false
    And the session has active session

  Scenario: GET /vendor/session returns name and isActive
    Given I am connected with the scopes 'admin'
    When I create a vendor with the following data
      | name      | phone         | latitude | longitude | addressText | isStation |
      | Name Test | +237699002028 | 4.0560   | 9.7060    | Akwa        | false     |
    When I access the vendor session using the issued token
    And the vendor session contains name 'Name Test'
    And the vendor session contains isActive true

  Scenario: Vendor session access updates sessionLastUsedAt
    Given I am connected with the scopes 'admin'
    When I create a vendor with the following data
      | name      | phone         | latitude | longitude | addressText | isStation |
      | Last Used | +237699002031 | 4.0565   | 9.7065    | Akwa        | false     |
    When I access the vendor session using the issued token
    When I get vendor by id using the created vendor id
    Then the vendor detail response contains used session metadata

  Scenario: Vendor list supports sort by name and createdAt
    Given I am connected with the scopes 'admin'
    When I create a vendor with the following data
      | name      | phone         | latitude | longitude | addressText | isStation |
      | Sort Zeta | +237699002032 | 4.0570   | 9.7070    | Akwa        | false     |
    When I create a vendor with the following data
      | name       | phone         | latitude | longitude | addressText | isStation |
      | Sort Alpha | +237699002033 | 4.0571   | 9.7071    | Akwa        | false     |
    When I list vendors at page 0 with size 20, active 'any', name 'Sort', and sort 'name,asc'
    Then the vendor list is sorted by name ascending
    When I list vendors at page 0 with size 20, active 'any', name 'Sort', and sort 'createdAt,asc'
    Then the vendor list is sorted by createdAt ascending

  Scenario: Vendor create response includes Cache-Control no-store header
    Given I am connected with the scopes 'admin'
    When I create a vendor with the following data
      | name      | phone         | latitude | longitude | addressText | isStation |
      | CacheTest | +237699002029 | 4.0570   | 9.7070    | Akwa        | false     |
    And the vendor create response has Cache-Control no-store header
