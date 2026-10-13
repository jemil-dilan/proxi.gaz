Feature: Demo Deleting

  Scenario: Delete an existing Demo
    Given I am connected with the scopes 'demo:delete'
    And I assume that the demos with the following data are inside the database
      | id                                   | reference | name                     | description                          | deleted |
      | c28b426d-2b31-403f-ad1c-db5b8f8143b3 | DEMO-300  | DeleteTarget_InitialDemo | DeleteTarget_InitialDemo_Description | false   |
    When I delete the Demo identify by 'c28b426d-2b31-403f-ad1c-db5b8f8143b3'
    Then After max 5 seconds, I should see that the demo with the following data is persisted in the database
      | id                                   | reference | name                     | description                          | deleted |
      | c28b426d-2b31-403f-ad1c-db5b8f8143b3 | DEMO-300  | DeleteTarget_InitialDemo | DeleteTarget_InitialDemo_Description | true    |

  Scenario: Try to delete a non existing Demo
    Given I am connected with the scopes 'demo:delete'
    And I assume that the demos with the following data are not inside the database
      | id                                   |
      | f7f617c7-2916-4822-a287-216f8eb77602 |
    When I try to delete the Demo identify by 'f7f617c7-2916-4822-a287-216f8eb77602'
    Then The last request failed with the http status 'NOT_FOUND' and error code 'PROXIGAZ-404-000'

  Scenario: Try to delete a Demo without permission
    Given I am connected with the scopes ''
    And I assume that the demos with the following data are inside the database
      | id                                   | reference | name                       | description                            |
      | 7316ab6f-3ce2-497a-90db-5e98e5428f1e | DEMO-301  | ExistingDemo_ConflictName1 | ExistingDemo_ConflictName1_Description |
    When I try to delete the Demo identify by '7316ab6f-3ce2-497a-90db-5e98e5428f1e'
    Then The last request failed with the http status 'FORBIDDEN' and error code ''
