Feature: Admin brand catalogue

  Scenario: An admin creates a brand and finds it in the active catalogue
    Given I am connected with the scopes 'admin'
    When I create the brand 'PXG064 Tradex'
    Then the created brand is named 'PXG064 Tradex' and is active
    When I list the brands with isActive 'true'
    Then the brand list contains 'PXG064 Tradex'
    And the brand list is sorted by name
    When I list the brands with isActive 'false'
    Then the brand list does not contain 'PXG064 Tradex'

  Scenario: Brand names are unique regardless of case and spacing
    Given I am connected with the scopes 'admin'
    When I create the brand 'PXG064 Camgaz'
    And I try to create the brand '  pxg064   CAMGAZ '
    Then The last request failed with the http status 'CONFLICT' and error code 'PROXIGAZ-409-003'

  Scenario: A created brand name is stored trimmed with single spaces
    Given I am connected with the scopes 'admin'
    When I create the brand '  PXG064   Green   Gas '
    Then the created brand is named 'PXG064 Green Gas' and is active

  Scenario Outline: Invalid brand names are rejected
    Given I am connected with the scopes 'admin'
    When I try to create the brand '<name>'
    Then The last request failed with the http status 'BAD_REQUEST' and error code 'PROXIGAZ-400-002'

    Examples:
      | name                                                              |
      |                                                                   |
      | AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA |

  Scenario: Creating a brand is recorded in the audit log
    Given I am connected with the scopes 'admin'
    When I create the brand 'PXG064 Audited Gas'
    And I list the audit log for that brand
    Then the brand audit log has one 'BRAND_CREATED' entry by the admin 'pxg010-admin@example.test'

  Scenario: The brand catalogue is admin-only
    When I try to list the brands
    Then The last request failed with the http status 'UNAUTHORIZED'
    Given I am connected with the scopes 'demo:create'
    When I try to list the brands
    Then The last request failed with the http status 'FORBIDDEN'
    When I try to create the brand 'PXG064 Forbidden Gas'
    Then The last request failed with the http status 'FORBIDDEN'
