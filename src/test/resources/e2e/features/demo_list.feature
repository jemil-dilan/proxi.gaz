Feature: Demo - Fetch All

  Scenario: Fetch all Demos
    Given I am connected with the scopes 'demo:read:all'
    And I assume that the demos with the following data are inside the database
      | id                                   | name            | reference | description                 | deleted |
      | c03b8308-edb8-4800-aa7b-29cb9d5ee4a7 | FetchDemo_Name1 | DEMO-001  | FetchDemo_Name1_Description | false   |
      | b5899b94-b8ed-4c26-897c-d26644d32b69 | FetchDemo_Name2 | DEMO-002  | FetchDemo_Name2_Description | false   |
      | 47929b74-9498-435e-be6b-6b10348676e8 | FetchDemo_Name3 | DEMO-003  | FetchDemo_Name3_Description | true    |
    When I fetch all demos using the extraction code 'fieldsToExtract_1'
    Then I should see that the following demos are among the fetched ones
      | id                                   | name            | reference |
      | c03b8308-edb8-4800-aa7b-29cb9d5ee4a7 | FetchDemo_Name1 | DEMO-001  |
      | b5899b94-b8ed-4c26-897c-d26644d32b69 | FetchDemo_Name2 | DEMO-002  |
    And I should see that the following data are not among the fetched ones
      | id                                   | name            | reference |
      | 47929b74-9498-435e-be6b-6b10348676e8 | FetchDemo_Name3 | DEMO-003  |

  Scenario: Fetch all demos without the necessary permission
    Given I am connected with the scopes ''
    And I assume that the demos with the following data are inside the database
      | id                                   | name            | reference | description                 | deleted |
      | c03b8308-edb8-4800-aa7b-29cb9d5ee4a7 | FetchDemo_Name1 | DEMO-001  | FetchDemo_Name1_Description | false   |
      | b5899b94-b8ed-4c26-897c-d26644d32b69 | FetchDemo_Name2 | DEMO-002  | FetchDemo_Name2_Description | false   |
    When I try to fetch all demos using the extraction code 'fieldsToExtract_1'
    Then The last request failed with the http status 'FORBIDDEN' and error code ''
