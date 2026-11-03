Feature: Error Responses - RFC 7807 Problem Details

  Background:
    Given I am connected with an admin token with subject "proxigaz-e2e-admin"

  Scenario: Error responses contain RFC 7807 Problem structure
    Given I try create a vendor with invalid data
    Then The last response body should contain RFC 7807 Problem structure
    And The last response should have error code 'PROXIGAZ-400-002'

  Scenario: Domain exception responses contain RFC 7807 Problem structure
    Given I assume that the vendors with the following data are not inside the database
      | id                                   |
      | 00000000-0000-0000-0000-000000000000 |
    When I try to get vendor by id '00000000-0000-0000-0000-000000000000'
    Then The last response body should contain RFC 7807 Problem structure
    And The last response should have a valid error code
