# test-kit

Concise data-driven testing functions that work with JUnit. This kit can execute
tables of tests exported using the
 [JavaScript version of test-kit.](https://github.com/quicbit-js/test-kit)

Only a small amount of functionality is ported at this point, but enough to save time
porting complex input and output assertions and error handling from nodejs to
Java.

Tables of tests in the nodejs version such as:

    test('incremental', function (t) {
      t.table_assert(
        [
          [ 'input',   'exp' ],
          [ '"abc", ', [ 'B@0,S5@0,E@7', '0.7/-/B_V/null' ] ],
          [ '[',       [ 'B@0,[@0,E@1', '0.1/[/BFV/null' ] ],
          ...
        ]
      ), function_to_assert);
    } 

Can be exported by changing the test() function to test.java():

    test.java('incremental', function (t) {
      t.table_assert(
        [
          [ 'input',   'exp' ],
          [ '"abc", ', [ 'B@0,S5@0,E@7', '0.7/-/B_V/null' ] ],
          [ '[',       [ 'B@0,[@0,E@1', '0.1/[/BFV/null' ] ],
          ...

Which exports the java declarative table format:

    a(
        a( "input",     "exp" ),
        a( "\"abc\", ", a( "B@0,S5@0,E@7", "0.7/-/B_V/null" ) ),
        a( "[",         a( "B@0,[@0,E@1", "0.1/[/BFV/null" ) ),
        ...
    );
    
a(Object v...) is the array construction function that takes any number of row objects. 

The java test can be setup using:

    table(
        a( "input",     "exp" ),
        a( "\"abc\", ", a( "B@0,S5@0,E@7", "0.7/-/B_V/null" ) ),
        a( "[",         a( "B@0,[@0,E@1", "0.1/[/BFV/null" ) ),
        ...
    ).test("name", lambda_to_assert);


So the detailed test assertions themselves are generated.

See [the nodejs test.java() function](https://github.com/quicbit-js/test-kit#testjava) for more info
about exporting tests.