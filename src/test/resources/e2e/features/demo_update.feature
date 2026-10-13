Feature: Demo Updating

  Scenario: Update an existing Demo
    Given I am connected with the scopes 'demo:update'
    And I assume that the demos with the following data are inside the database
      | id                                   | reference | name                     | description                          | deleted |
      | 393896b9-4f50-46bb-8a72-d0bf0cd3b228 | DEMO-102  | UpdateTarget_InitialDemo | UpdateTarget_InitialDemo_Description | false   |
    When I update the Demo identify by '393896b9-4f50-46bb-8a72-d0bf0cd3b228' with the following data
      | name                     | description                          |
      | UpdateTarget_UpdatedDemo | UpdateTarget_UpdatedDemo_Description |
    Then After max 5 seconds, I should see that the demo with the following data is persisted in the database
      | name                     | reference | description                          | deleted |
      | UpdateTarget_UpdatedDemo | DEMO-102  | UpdateTarget_UpdatedDemo_Description | false   |
    And I should see that the Demo's immutable fields have not been modified
      | id                                   | reference |
      | 393896b9-4f50-46bb-8a72-d0bf0cd3b228 | DEMO-102  |

  Scenario: Try to update a non existing Demo
    Given I am connected with the scopes 'demo:update'
    And I assume that the demos with the following data are not inside the database
      | id                                   |
      | f7f617c7-2916-4822-a287-216f8eb77602 |
    When I try to update the Demo identify by 'f7f617c7-2916-4822-a287-216f8eb77602' with the following data
      | name                     | description                          |
      | UpdateTarget_UpdatedDemo | UpdateTarget_UpdatedDemo_Description |
    Then The last request failed with the http status 'NOT_FOUND' and error code 'PROXIGAZ-404-000'

  Scenario: Try to update a Demo using a name already used by another Demo
    Given I am connected with the scopes 'demo:update'
    And I assume that the demos with the following data are inside the database
      | id                                   | reference | name                      | description                           |
      | 840a6ef0-494b-4a98-959b-aaf94472e35c | DEMO-110  | ExistingDemo_ConflictName | ExistingDemo_ConflictName_Description |
    When I try to update the Demo identify by '393896b9-4f50-46bb-8a72-d0bf0cd3b228' with the following data
      | name                      | description                           |
      | ExistingDemo_ConflictName | ExistingDemo_ConflictName_Description |
    Then The last request failed with the http status 'CONFLICT' and error code 'PROXIGAZ-409-000'

  Scenario: Try to update a Demo without permission
    Given I am connected with the scopes ''
    And I assume that the demos with the following data are inside the database
      | id                                   | reference | name                      | description                           |
      | 840a6ef0-494b-4a98-959b-aaf94472e35c | DEMO-110  | ExistingDemo_ConflictName | ExistingDemo_ConflictName_Description |
    When I try to update the Demo identify by '840a6ef0-494b-4a98-959b-aaf94472e35c' with the following data
      | name                      | description                           |
      | ExistingDemo_ConflictName | ExistingDemo_ConflictName_Description |
    Then The last request failed with the http status 'FORBIDDEN' and error code ''
