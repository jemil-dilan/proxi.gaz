Feature: Local development seed data

  Scenario: The seeded admin can sign in and sample vendors have Douala locations
    Given I am connected with the scopes ''
    And I assume the admin with the following data are in the database
      | id                                   | email                | password | created_at          |
      | f75c66ed-8cb4-4afd-91d2-d0a10ea92548 | admin@proxigaz.cm    | password | 2026-10-24T17:06:00 |
      | 01100000-0000-0000-0000-000000000001 | admin@proxigaz.local | password | 2026-10-24T17:06:00 |
    When I log in as admin with email 'admin@proxigaz.local' and password 'password'
    Then the login succeeds and returns auth token
    When I access the protected admin profile
    Then the admin profile is returned for 'admin@proxigaz.local'
    And I assume the following vendors are present in the database
      | name               | phone         | latitude | longitude | addressText              | isStation | deliveryFeeType | deliveryFeeAmount | isActive |
      | Demo Gas Akwa      | +237699001001 | 4.0483   | 9.7048    | Demo location, Akwa      | false     | flat            | 1000              | true     |
      | Demo Gas Bonanjo   | +237699001002 | 4.0178   | 9.6906    | Demo location, Bonanjo   | false     | flat            | 1000              | true     |
      | Demo Gas Deido     | +237699001003 | 4.0718   | 9.7053    | Demo location, Deido     | false     | flat            | 1000              | true     |
      | Demo Gas Bali      | +237699001004 | 4.0356   | 9.7007    | Demo location, Bali      | false     | flat            | 1000              | true     |
      | Demo Gas Bonapriso | +237699001005 | 4.0239   | 9.6971    | Demo location, Bonapriso | false     | flat            | 1000              | true     |
