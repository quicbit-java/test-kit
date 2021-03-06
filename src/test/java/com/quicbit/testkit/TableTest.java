package com.quicbit.testkit;

import org.junit.Test;

import static com.quicbit.testkit.TestKit.*;

public class TableTest {
    @Test
    public void testSum() {
        table(
            a( "a",             "exp" ),
            a( a(),                  0),
            a( a(0, 1, 2),           3),
            a( a(1, 2, 3, 4),        10)
        ).test("sum",
            (r) -> {
                int ret = 0;
                for (Object v: r.arr("a")){ret += (Integer) v; }
                return ret;
            }
        );
    }

    @Test
    public void testDesc() {
        table(
            a("label", "input", "output", "exp"),
            a("msg", a("a"), 0, "msg: (\"a\") -expect-> (0)"),
            a("msg", a(1, 2), 3, "msg: (1,2) -expect-> (3)")
        ).test("desc", (r) -> desc(r.str("label"), r.arr("input"), r.ival("output")));
    }
}
