// Software License Agreement (ISC License)
//
// Copyright (c) 2019, Matthew Voss
//
// Permission to use, copy, modify, and/or distribute this software for
// any purpose with or without fee is hereby granted, provided that the
// above copyright notice and this permission notice appear in all copies.
//
// THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
// WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
// MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
// ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
// WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
// ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
// OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.

package com.quicbit.testkit;

import org.junit.Assert;

import java.io.PrintStream;
import java.lang.reflect.Array;
import java.util.*;
import java.lang.reflect.*;

public class TestKit {
    PrintStream out = System.out;
    int num_tests = 0;
    int num_ok = 0;

    @FunctionalInterface
    public interface RowFn {
        Object apply(Row r);
    }

    public static class Options {
        int max_tests = 0;
        String assertion = "same";      // "none", "same", "contains" or "throws"
    }

    public static class Row {
        Object[] values;
        Table table;

        public Row(Table table, Object[] vals) {
            this.table = table;
            this.values = vals;
        }

        public String str(String n) {
            return (String) obj(n);
        }

        public int ival(String n) {
            return (int) obj(n);
        }

        public Object obj(String n) {
            return values[table.indexOf(n)];
        }

        public Object expected () {
            return values[values.length-1];
        }

        public Object[] inputs() {
            return Arrays.copyOfRange(values, 0, values.length-1);
        }

        public String[] strarr(String n) {
            Object[] a = (Object[]) values[table.indexOf(n)];
            return Arrays.copyOf(a, a.length, String[].class);
        }

        public Object[] arr (String n) {
            return (Object[]) values[table.indexOf(n)];
        }

        public int[] intarr (String n) {
            Object[] a = (Object[]) values[table.indexOf(n)];
            int[] ret = new int[a.length];
            for (int i=0; i<a.length; i++) {
                ret[i] = (int) a[i];
            }
            return ret;
        }

        public List<Integer> intlist(String n) {
            Object[] a = (Object[]) values[table.indexOf(n)];
            List<Integer> ret = new ArrayList<>(a.length);
            for (Object v : a) { ret.add((Integer) v); }
            return ret;
        }

        public List<Object> list (String n) {
            Object[] a = (Object[]) values[table.indexOf(n)];
            List<Object> ret = new ArrayList<>(a.length);
            ret.addAll(Arrays.asList(a));
            return ret;
        }
    }

    public static class Table {
        TestKit context;
        String[] header;
        Row[] rows;
        Map<String, Integer> colsByName;

        public Table(TestKit context, Object[]... all_rows) {
            this.context = context;
            String[] header = Arrays.copyOf(all_rows[0], all_rows[0].length, String[].class);
            colsByName = new HashMap<>();
            for (int i = 0; i < header.length; i++) {
                colsByName.put(header[i], i);
            }
            this.header = header;

            Row[] rows = new Row[all_rows.length-1];
            for (int i=0; i<all_rows.length-1; i++) {
                rows[i] = new Row(this, all_rows[i+1]);
            }
            this.rows = rows;
        }

        int indexOf(String n) {
            Integer idx = colsByName.get(n);
            if (idx == null) {
                throw new IllegalArgumentException("unknown column: " + n);
            }
            return idx;
        }

        public void test1 (String name, RowFn fn) {
            Options opt = new Options();
            opt.max_tests = 1;
            _test(name, fn, opt);
        }

        public void test (String name, RowFn fn) {
            _test(name, fn, new Options());
        }
        public void teste (String name, RowFn fn) { Options opt = new Options(); opt.assertion = "throws"; _test(name, fn, opt); }

        public void _test(String name, RowFn fn, Options opt) {
            if (opt.assertion.equals("none")) {
                return;
            }
            context.out.println("# " + name);
            boolean ok = true;
            int rlim = rows.length;
            if (opt.max_tests > 0 && opt.max_tests < rlim) {
                rlim = opt.max_tests;
            }
            for (int ri=0; ri < rlim; ri++) {
                context.num_tests++;
                Row row = rows[ri];
                // test
                Object actual = null;
                String assertion = opt.assertion;
                if (assertion.equals("throws")) {
                    try {
                        fn.apply(row);
                    } catch (Exception e) {
                        actual = e.getMessage();
                    }
                } else {
                    actual = fn.apply(row);
                }
                String msg = format(row.inputs()) + " -expect-> " + format(row.expected());
                try {
                    Object expected = row.expected();
                    if (expected != null && expected.getClass().isArray()) {
                        Object[] reta = arrayOf(actual);
                        Object[] expa = arrayOf(expected);
                        Assert.assertArrayEquals(expa, reta);
                    } else {
                        switch (assertion) {
                            case "throws":
                                if (actual == null) {
                                    Assert.fail("Expected throw, but no error was thrown");
                                } else if (!String.valueOf(actual).contains(String.valueOf(expected))) {
                                    Assert.fail("Error did not contain expected string: \"" + expected + "\"");
                                }
                                break;
                            case "same":
                                Assert.assertEquals(expected, actual);
                                break;
                            default:
                                Assert.fail("unknown assertion type: " + assertion);
                        }
                    }
                    context.num_ok++;
                    context.out.println("ok " + context.num_tests + " : " + msg);
                } catch (Throwable e) {
                    ok = false;
                    context.out.println("not ok " + context.num_tests + " : " + msg);
                    context.out.println("  ---");
                    context.out.println("    expected: " + format(row.expected()));
                    context.out.println("    actual:   " + format(actual));
                    context.out.println("    " + e.getMessage());
                    e.printStackTrace(context.out);
                    context.out.println();
                }
            }
            if (!ok) {
                throw new AssertionError("one or more assertion failures");
            }
        }
    }

    public static String desc (String msg, Object[] input, Object expected) {
        return msg + " " + format(input) + " -expect-> " + format(expected);
    }

    // format() was copied from org.junit.Assert to replicate look and feel of assertion messages
    public static String format(Object obj) {
        String ret;
        if (obj != null && obj.getClass().isArray()) {
            ret = Arrays.deepToString(arrayOf(obj));
        } else {
            ret = String.valueOf(obj);
        }
        ret = ret.replaceAll("\\r", "");
        ret = ret.replaceAll("\\n", " ");
        return "(" + ret + ")";
    }

    public static Object[] arrayOf (Object a) {
        int len = Array.getLength(a);
        Object[] ret = new Object[len];
        for (int i=0; i<len; i++) {
            ret[i] = Array.get(a, i);
        }
        return ret;
    }

    public static class JSObj extends LinkedHashMap<String,Object> {
    }
    public static JSObj o (Object... kv) {
        if (kv.length % 2 != 0) {
            throw new IllegalArgumentException("uneven key/value set");
        }
        JSObj ret = new JSObj();
        for (int i=0; i<kv.length; i+=2) {
            ret.put((String)kv[i], kv[i+1]);
        }
        return ret;
    }

    public static String join(Object[] a, String delim) {
        StringBuilder sb = new StringBuilder();
        for (int i=0; i<a.length-1; i++) {
            sb.append(a[i]);
            sb.append(delim);
        }
        sb.append(a[a.length-1]);
        return sb.toString();
    }

    // invoke a method on an object using the given arguments
    public static Object call (Object obj, String method, Object[] args) {
        Method[] methods = obj.getClass().getMethods();
        for (Method m: methods) {
            if (m.getName().equals(method) && m.getParameterCount() == args.length) {
                try {
                    return m.invoke(obj, args);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
        throw new RuntimeException("method " + method + " with " + args.length + " parameters not found in class " + obj.getClass());
    }

    public static Object field (Object obj, String field) {
        try {
            Field f = obj.getClass().getField(field);
            return f.get(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Table table(Object[]... rows) { return new Table(new TestKit(), rows); }
    public static Object[] a (Object... a) { return a; }
    public static String[] sa (Object... a) { return Arrays.copyOf(a, a.length, String[].class); }
}
