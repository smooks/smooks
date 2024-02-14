Feature: XPath

  Scenario: Select text and attribute predicate

    Given a fragment
    """
    <x>
      <y d="2" h="rr">dd</y>
    </x>
    """
    And the current node is "/x/y"
    When the selector "x/y[(@d = 23 or text() = 'ddd') and @h = 'rrr']" is applied
    Then the current node does not match the selector

    Given a fragment
    """
    <x>
      <y d="23" h="rr">dd</y>
    </x>
    """
    And the current node is "/x/y"
    When the selector "x/y[(@d = 23 or text() = 'ddd') and @h = 'rrr']" is applied
    Then the current node does not match the selector

    Given a fragment
    """
    <x>
      <y d="23" h="rrr">dd</y>
    </x>
    """
    And the current node is "/x/y"
    When the selector "x/y[(@d = 23 or text() = 'ddd') and @h = 'rrr']" is applied
    Then the current node matches the selector

    Given a fragment
    """
    <x>
      <y d="2" h="rrr">dd</y>
    </x>
    """
    And the current node is "/x/y"
    When the selector "x/y[(@d = 23 or text() = 'ddd') and @h = 'rrr']" is applied
    Then the current node does not match the selector

    Given a fragment
    """
    <x>
      <y d="2" h="rrr">ddd</y>
    </x>
    """
    And the current node is "/x/y"
    When the selector "x/y[(@d = 23 or text() = 'ddd') and @h = 'rrr']" is applied
    Then the current node matches the selector

  Scenario: Select text attribute equality predicate

    Given a fragment
    """
    <x>
      <y d="ddd" h="rrr">ddd</y>
    </x>
    """
    And the current node is "/x/y"
    When the selector "x/y[@d = text()]" is applied
    Then the current node matches the selector

    Given a fragment
    """
    <x>
      <y d="rrr" h="ddd">ddd</y>
    </x>
    """
    And the current node is "/x/y"
    When the selector "x/y[@d = text()]" is applied
    Then the current node does not match the selector

  Scenario: Select attribute in path

    Given a fragment
    """
    <x>
      <y c="dd"></y>
    </x>
    """
    And the current node is "/x/y"
    When the selector "x/y/@c" is applied
    Then the current node matches the selector

  Scenario: Select not predicate

    Given a fragment
    """
    <x>
      <y c="dd"></y>
    </x>
    """
    And the current node is "/x"
    When the selector "not(self::y)" is applied
    Then the current node matches the selector

    Given a fragment
    """
    <x>
      <y c="dd"></y>
    </x>
    """
    And the current node is "/x"
    When the selector "not(self::x)" is applied
    Then the current node does not match the selector

  Scenario: Select any-descendant-or-self selector

    Given a fragment
    """
    <a>
      <b>
        <c></c>
      </b>
    </a>
    """
    And the current node is "/a/b/c"
    When the selector "//*" is applied
    Then the current node matches the selector

    Given a fragment
    """
    <a>
      <b>
        <c></c>
      </b>
    </a>
    """
    And the current node is "/a/b"
    When the selector "//*" is applied
    Then the current node matches the selector

    Given a fragment
    """
    <a>
      <b>
        <c></c>
      </b>
    </a>
    """
    And the current node is "/a"
    When the selector "//*" is applied
    Then the current node does not match the selector

  Scenario: Select any-descendant-or-self selector with not predicate

    Given a fragment
    """
    <a>
      <b>
        <c></c>
      </b>
    </a>
    """
    And the current node is "/a/b/c"
    When the selector "//*[not(self::b)]" is applied
    Then the current node matches the selector

    Given a fragment
    """
    <a>
      <b>
        <c></c>
      </b>
    </a>
    """
    And the current node is "/a/b"
    When the selector "//*[not(self::b)]" is applied
    Then the current node does not match the selector

    Given a fragment
    """
    <a>
      <b>
        <c></c>
      </b>
    </a>
    """
    And the current node is "/a"
    When the selector "//*[not(self::b)]" is applied
    Then the current node does not match the selector

  Scenario: Select prefixed attribute in path

    Given a fragment
    """
    <x>
      <y xmlns:ns0="http://a" ns0:c="dd"></y>
    </x>
    """
    And the current node is "/x/y"
    And the Smooks namespaces are
      | prefix | uri      |
      | a      | http://a |
    When the selector "x/y/@a:c" is applied
    Then the current node matches the selector

  Scenario: Select prefixed attribute in path and attribute predicate

    Given a fragment
    """
    <x>
      <y xmlns:ns0="http://a" ns0:c="dd" xxx="123"></y>
    </x>
    """
    And the current node is "/x/y"
    And the Smooks namespaces are
      | prefix | uri      |
      | a      | http://a |
    When the selector "x/y[@xxx = 123]/@a:c" is applied
    Then the current node matches the selector

  Scenario: Select attribute in path and attribute predicates

    Given a fragment
    """
    <x>
      <y c="dd" g="987" n="1"></y>
    </x>
    """
    And the current node is "/x/y"
    When the selector "x/y[@n = 1 and @g = '987']/@c" is applied
    Then the current node matches the selector

  Scenario: Select attribute in path and prefixed attribute predicates

    Given a fragment
    """
    <x>
      <y c="dd" xmlns:ns0="http://c" ns0:g="987" ns0:n="1"></y>
    </x>
    """
    And the current node is "/x/y"
    And the Smooks namespaces are
      | prefix | uri      |
      | c      | http://c |
    When the selector "x/y[@c:n = 1 and @c:g = '987']/@c" is applied
    Then the current node matches the selector

  Scenario: Select prefixed attribute predicate

    Given a fragment
    """
    <x>
      <y h="rrr" xmlns:ns0="http://c" ns0:z="78">ddd</y>
    </x>
    """
    And the current node is "/x/y"
    And the Smooks namespaces are
      | prefix | uri      |
      | c      | http://c |
    When the selector "x/y[@c:z = 78]" is applied
    Then the current node matches the selector

    Given a fragment
    """
    <x>
      <y h="rrr" xmlns:ns0="http://d" ns0:z="78">ddd</y>
    </x>
    """
    And the current node is "/x/y"
    And the Smooks namespaces are
      | prefix | uri      |
      | c      | http://c |
      | d      | http://d |
    When the selector "x/y[@c:z = 78]" is applied
    Then the current node does not match the selector

  Scenario: Select prefixed nodes

    Given a fragment
    """
    <x xmlns="http://a">
      <y d="ddd" h="rrr" xmlns="http://b">ddd</y>
    </x>
    """
    And the current node is "/*[local-name()='x']/*[local-name()='y']"
    And the Smooks namespaces are
      | prefix | uri      |
      | a      | http://a |
      | b      | http://b |
    When the selector "a:x/b:y" is applied
    Then the current node matches the selector

    Given a fragment
    """
    <x xmlns="http://a">
      <y d="ddd" h="rrr" xmlns="http://d">ddd</y>
    </x>
    """
    And the current node is "/*[local-name()='x']/*[local-name()='y']"
    And the Smooks namespaces are
      | prefix | uri      |
      | a      | http://a |
      | b      | http://b |
    When the selector "a:x/b:y" is applied
    Then the current node does not match the selector

    Given a fragment
    """
    <a:x xmlns:a="http://a"></a:x>
    """
    And the current node is "/*[local-name()='x']"
    And the Smooks namespaces are
      | prefix | uri      |
      | a      | http://aa |
    When the selector "a:x" is applied
    Then the current node does not match the selector