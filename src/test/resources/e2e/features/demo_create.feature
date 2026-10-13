Feature: Demo Creating

  Scenario: Create Demo
    Given I am connected with the scopes 'demo:create'
    And I assume that no demo with the following name is inside the database
      | name           |
      | CreateDemoName |
    When I create the Demo with the following data
      | name           | description                |
      | CreateDemoName | CreateDemoName_Description |
    Then After max 5 seconds, I should see that the demo with the following data is persisted in the database
      | name           | reference | description                | deleted |
      | CreateDemoName | DEMO-101  | CreateDemoName_Description | false   |

  Scenario: Try to create a Demo when one with the same name already exists
    Given I am connected with the scopes 'demo:create'
    And I assume that the demos with the following data are inside the database
      | id                                   | name            | reference | description                 | deleted |
      | c03b8308-edb8-4800-aa7b-29cb9d5ee4a7 | FetchDemo_Name1 | DEMO-001  | FetchDemo_Name1_Description | false   |
    When I try to create a new Demo with the following data
      | name            | description                 |
      | FetchDemo_Name1 | FetchDemo_Name1_Description |
    Then The last request failed with the http status 'CONFLICT' and error code 'PROXIGAZ-409-000'

  Scenario: Try to create a Demo without permission
    Given I am connected with the scopes ''
    When I try to create a new Demo named 'abc'
    Then The last request failed with the http status 'FORBIDDEN' and error code ''
