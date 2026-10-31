Feature: Admin vendor listing

  Scenario: Admin can filter vendors and navigate stable pages
    Given I am connected with the scopes 'admin'
    When I create a vendor with the following data
      | name                        | phone         | latitude | longitude | addressText | isStation | isActive |
      | PXG014 Directory Active One | +237699002041 | 4.0501   | 9.7001    | Akwa        | false     | true     |
    Then the vendor create request succeeds
    When I create a vendor with the following data
      | name                        | phone         | latitude | longitude | addressText | isStation | isActive |
      | PXG014 Directory Active Two | +237699002042 | 4.0502   | 9.7002    | Akwa        | false     | true     |
    Then the vendor create request succeeds
    When I create a vendor with the following data
      | name                          | phone         | latitude | longitude | addressText | isStation | isActive |
      | PXG014 Directory Inactive One | +237699002043 | 4.0503   | 9.7003    | Akwa        | false     | false    |
    Then the vendor create request succeeds
    When I list vendors at page 0 with size 1, active 'true', and name 'PXG014 Directory'
    Then the vendor page metadata is page 0, size 1, total elements 2, total pages 2
    And the vendor list contains only the name 'PXG014 Directory Active One'
    When I list vendors at page 1 with size 1, active 'true', and name 'PXG014 Directory'
    Then the vendor page metadata is page 1, size 1, total elements 2, total pages 2
    And the vendor list contains only the name 'PXG014 Directory Active Two'

  Scenario: Only admins can list vendors
    Given I am connected with the scopes 'demo:read'
    When I list vendors at page 0 with size 20, active 'any', and name 'any'
    Then the vendor request is rejected with status 403
