Feature: Demo - Fetch Demo By Id

  Scenario: Fetch Demo by id
    Given I am connected with the scopes 'demo:read'
    And I assume that the demos with the following data are inside the database
      | id                                   | name            | reference | description                 | deleted |
      | c03b8308-edb8-4800-aa7b-29cb9d5ee4a7 | FetchDemo_Name1 | DEMO-001  | FetchDemo_Name1_Description | false   |
    When I fetch demo identify by 'c03b8308-edb8-4800-aa7b-29cb9d5ee4a7' using the extraction code 'fieldsToExtract_2'
    Then I should see that demo with the following data have been fetched
      | id                                   | name            | reference | description                 | deleted |
      | c03b8308-edb8-4800-aa7b-29cb9d5ee4a7 | FetchDemo_Name1 | DEMO-001  | FetchDemo_Name1_Description | false   |

  Scenario: Fetch Demo By Id without the necessary permission
    Given I am connected with the scopes ''
    And I assume that the demos with the following data are inside the database
      | id                                   | name            | reference | description                 | deleted |
      | b5899b94-b8ed-4c26-897c-d26644d32b69 | FetchDemo_Name2 | DEMO-002  | FetchDemo_Name2_Description | false   |
    When I try to fetch demo identify by 'b5899b94-b8ed-4c26-897c-d26644d32b69' using the extraction code 'fieldsToExtract_2'
    Then The last request failed with the http status 'FORBIDDEN' and error code ''

  Scenario: Fetch Non Existent Demo
    Given I am connected with the scopes 'demo:read'
    And I assume that the demos with the following data are not inside the database
      | id                                   |
      | f7f617c7-2916-4822-a287-216f8eb77602 |
    When I try to fetch demo identify by 'f7f617c7-2916-4822-a287-216f8eb77602' using the extraction code 'fieldsToExtract_1'
    Then The last request failed with the http status 'NOT_FOUND' and error code 'PROXIGAZ-404-000'
