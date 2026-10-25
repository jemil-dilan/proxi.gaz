Feature: Admin authentication

  Scenario: Admin can sign in and access the protected profile
    Given I am connected with the scopes ''
    When I log in as admin with email 'pxg010-admin@example.test' and password 'PXG010-test-password' from bootstrapped class
    Then the login succeeds and returns an access token
    When I access the protected admin profile
    Then the admin profile is returned for 'pxg010-admin@example.test'

  Scenario: Admin can sign in and access the protected profile
    Given I am connected with the scopes ''
    And I assume the admin with the following data are in the database
      | id                                   | email             | password | created_at          |
      | f75c66ed-8cb4-4afd-91d2-d0a10ea92548 | admin@proxigaz.cm | password | 2026-10-24T17:06:00 |
    When I log in as admin with email 'admin@proxigaz.cm' and password 'password'
    Then the login succeeds and returns auth token
    When I access the protected admin profile
    Then the admin profile is returned for 'admin@proxigaz.cm'

  Scenario: Invalid credentials are rejected without disclosing which value was wrong
    Given I am connected with the scopes ''
    When I try to log in as admin with email 'pxg010-admin@example.test' and password 'wrong-password'
    Then The last request failed with the http status 'UNAUTHORIZED' and error code 'PROXIGAZ-401-000'

  Scenario: Unknown admin email is rejected with the same generic response
    Given I am connected with the scopes ''
    When I try to log in as admin with email 'unknown-pxg010@example.test' and password 'PXG010-test-password'
    Then The last request failed with the http status 'UNAUTHORIZED' and error code 'PROXIGAZ-401-000'

  Scenario: Protected admin profile rejects an invalid token
    Given I am connected with the scopes ''
    When I try to access the protected admin profile with an invalid token
    Then The last request failed with the http status 'UNAUTHORIZED' and error code ''

  Scenario: Protected admin profile rejects an authenticated token with a malformed subject
    Given I am connected with an admin token with subject 'not-a-user-id'
    When I access the protected admin profile
    Then The last request failed with the http status 'UNAUTHORIZED' and error code ''
