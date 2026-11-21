Feature: Admin audit log

  Scenario: Every admin vendor action is recorded with its author, newest first
    When I log in as admin with email 'pxg010-admin@example.test' and password 'PXG010-test-password' from bootstrapped class
    And I create a vendor with the following data
      | name            | phone         | latitude | longitude | addressText | isStation | deliveryFeeType | deliveryFeeAmount |
      | Audit Vendor A1 | +237699006101 | 4.0483   | 9.7048    | Audit, Akwa | false     | FIXED           | 1000              |
    And I update that vendor with the following data
      | name            | phone         | latitude | longitude | addressText | isStation | deliveryFeeType | deliveryFeeAmount |
      | Audit Vendor A2 | +237699006102 | 4.0483   | 9.7048    | Audit, Akwa | false     | FIXED           | 1000              |
    And I set that vendor's active status to 'false'
    And I rotate the vendor session as admin
    And I revoke the vendor session as admin
    And I list the audit log for that vendor
    Then the audit log lists the following actions
      | VENDOR_SESSION_REVOKED |
      | VENDOR_SESSION_ROTATED |
      | VENDOR_DEACTIVATED     |
      | VENDOR_UPDATED         |
      | VENDOR_CREATED         |
    And every audit entry targets that vendor and was made by the admin 'pxg010-admin@example.test'
    And the audit entry 'VENDOR_CREATED' has the metadata '{"isActive":true}'
    And the audit entry 'VENDOR_UPDATED' has the metadata '{"changedFields":["name","phone"]}'
    And the audit entry 'VENDOR_DEACTIVATED' has the metadata '{"previousActive":true}'
    And the audit entry 'VENDOR_SESSION_ROTATED' has the metadata '{}'
    And the audit entry 'VENDOR_SESSION_REVOKED' has the metadata '{}'

  Scenario: A status change that changes nothing is still recorded
    When I log in as admin with email 'pxg010-admin@example.test' and password 'PXG010-test-password' from bootstrapped class
    And I create a vendor with the following data
      | name        | phone         | latitude | longitude | addressText | isStation |
      | Audit No-op | +237699006111 | 4.0500   | 9.7000    | Akwa        | false     |
    And I set that vendor's active status to 'true'
    And I list the audit log for that vendor
    Then the audit log lists the following actions
      | VENDOR_ACTIVATED |
      | VENDOR_CREATED   |
    And the audit entry 'VENDOR_ACTIVATED' has the metadata '{"previousActive":true}'

  Scenario: The audit log never exposes a vendor session secret
    When I log in as admin with email 'pxg010-admin@example.test' and password 'PXG010-test-password' from bootstrapped class
    And I create a vendor with the following data
      | name         | phone         | latitude | longitude | addressText | isStation |
      | Audit Secret | +237699006121 | 4.0510   | 9.7010    | Akwa        | false     |
    And I rotate the vendor session as admin
    And I list the audit log for that vendor
    Then the audit log contains no issued vendor session token or token hash
    And the stored audit metadata for that vendor contains no issued vendor session token or token hash

  Scenario: A rejected admin action leaves no audit entry
    When I log in as admin with email 'pxg010-admin@example.test' and password 'PXG010-test-password' from bootstrapped class
    And I try to update a vendor with the following data
      | name       | phone         | latitude | longitude | addressText | isStation | deliveryFeeType | deliveryFeeAmount |
      | Missing ID | +237699006131 | 4.0500   | 9.7000    | Address     | false     | FIXED           | 1000              |
    Then The last request failed with the http status 'NOT_FOUND' and error code 'PROXIGAZ-404-002'
    When I list the audit log for that vendor
    Then the audit log is empty

  Scenario Outline: Invalid audit log queries are rejected
    Given I am connected with the scopes 'admin'
    When I try to list the audit log with the query '<query>'
    Then The last request failed with the http status 'BAD_REQUEST'

    Examples:
      | query                                         |
      | targetId=0b9d5f0e-6f1f-4f43-9d3c-0f3a6c7a1b11 |
      | targetType=ORDER                              |
      | targetType=VENDOR&targetId=not-a-uuid         |
      | size=0                                        |

  Scenario: The audit log requires an authenticated admin
    When I try to list the audit log with the query ''
    Then The last request failed with the http status 'UNAUTHORIZED'
    Given I am connected with the scopes 'demo:create'
    When I try to list the audit log with the query ''
    Then The last request failed with the http status 'FORBIDDEN'
