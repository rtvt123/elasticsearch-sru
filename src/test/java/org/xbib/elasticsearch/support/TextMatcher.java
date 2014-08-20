package org.xbib.elasticsearch.support;

import org.hamcrest.CoreMatchers;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import java.io.BufferedReader;
import java.io.IOException;

import static org.hamcrest.Description.NONE;

public class TextMatcher {

    public static Matcher<BufferedReader> matchesTextLines(BufferedReader expected){
        return new TypeSafeMatcher<BufferedReader>() {
            public boolean matchesSafely(BufferedReader actual) {
                try {
                    String line;
                    while ((line = expected.readLine()) != null) {
                        line = line.trim();
                        Matcher<?> equalsMatcher = CoreMatchers.equalTo(line);
                        String actualLine = actual.readLine();
                        if (actualLine != null) {
                            actualLine = actualLine.trim();
                            if (!equalsMatcher.matches(actualLine)) {
                                equalsMatcher.describeMismatch(actual, NONE);
                                return false;
                            }
                        } else {
                            return false;
                        }
                    }
                } catch (IOException e) {
                    return false;
                }
                return true;
            }

            @Override
            public void describeTo(Description description) {

            }
        };
    }

    /*
        protected static boolean isEqual(InputStream i1, InputStream i2) throws IOException {
        ReadableByteChannel ch1 = Channels.newChannel(i1);
        ReadableByteChannel ch2 = Channels.newChannel(i2);
        ByteBuffer buf1 = ByteBuffer.allocateDirect(1024);
        ByteBuffer buf2 = ByteBuffer.allocateDirect(1024);
        try {
            while (true) {
                int n1 = ch1.read(buf1);
                int n2 = ch2.read(buf2);
                if (n1 == -1 || n2 == -1) {
                    return n1 == n2;
                }
                buf1.flip();
                buf2.flip();
                for (int i = 0; i < Math.min(n1, n2); i++) {
                    byte b1 = buf1.get();
                    byte b2 = buf2.get();
                    if (b1 != b2) {
                        return false;
                    }
                }
                buf1.compact();
                buf2.compact();
            }
        } finally {
            i1.close();
            i2.close();
        }
    }
    * */

}
