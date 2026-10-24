Feature: Admin authentication

  Scenario: Admin can sign in and access the protected profile
    When I log in as admin with email 'pxg010-admin@example.test' and password 'PXG010-test-password'
    Then the login succeeds and returns an access token
    When I access the protected admin profile
    Then the admin profile is returned for 'pxg010-admin@example.test'

  Scenario: Invalid credentials are rejected without disclosing which value was wrong
    When I log in as admin with email 'pxg010-admin@example.test' and password 'wrong-password'
    Then the login is rejected with a generic unauthorized response

  Scenario: Protected admin profile rejects a missing token
    When I access the protected admin profile without a token
    Then The last request failed with the http status 'UNAUTHORIZED' and error code ''

  Scenario: Protected admin profile rejects an invalid token
    When I access the protected admin profile with an invalid token
    Then The last request failed with the http status 'UNAUTHORIZED' and error code ''
