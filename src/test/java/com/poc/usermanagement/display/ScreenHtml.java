package com.poc.usermanagement.display;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class ScreenHtml {

    private ScreenHtml() {
    }

    static String form(String html, String action) {
        Matcher matcher = Pattern.compile(
                        "<form\\b[^>]*\\baction=\"" + Pattern.quote(action) + "\"[^>]*>.*?</form>",
                        Pattern.DOTALL)
                .matcher(html);
        assertThat(matcher.find()).as("form %s", action).isTrue();
        return matcher.group();
    }

    static String selectNamed(String html, String name) {
        Matcher matcher = Pattern.compile(
                        "<select\\b[^>]*\\bname=\"" + Pattern.quote(name) + "\"[^>]*>.*?</select>",
                        Pattern.DOTALL)
                .matcher(html);
        assertThat(matcher.find()).as("select %s", name).isTrue();
        return matcher.group();
    }

    static String rowContaining(String html, String marker) {
        Matcher matcher = Pattern.compile("<tr\\b[^>]*>(.*?)</tr>", Pattern.DOTALL).matcher(html);
        while (matcher.find()) {
            if (matcher.group().contains(marker)) {
                return matcher.group();
            }
        }
        throw new AssertionError("row containing " + marker);
    }

    static List<String> cells(String row) {
        List<String> values = new ArrayList<>();
        Matcher matcher = Pattern.compile("<td\\b[^>]*>(.*?)</td>", Pattern.DOTALL).matcher(row);
        while (matcher.find()) {
            values.add(matcher.group(1).replaceAll("<[^>]+>", "").trim());
        }
        return values;
    }

    static void order(String html, String... markers) {
        int from = 0;
        for (String marker : markers) {
            int found = html.indexOf(marker, from);
            assertThat(found).as("marker '%s'", marker).isGreaterThanOrEqualTo(from == 0 ? 0 : from);
            assertThat(found).as("marker '%s' missing", marker).isGreaterThanOrEqualTo(0);
            from = found + marker.length();
        }
    }

    static void eachFieldHasOneControl(String html) {
        Matcher matcher = Pattern.compile("<div class=\"field\">(.*?)</div>", Pattern.DOTALL).matcher(html);
        int count = 0;
        while (matcher.find()) {
            count++;
            String body = matcher.group(1);
            int controls = countOf(body, "<input") + countOf(body, "<select");
            assertThat(controls).as(body).isEqualTo(1);
        }
        assertThat(count).isPositive();
    }

    static void buttonAfterControls(String form) {
        int button = form.indexOf("<button");
        int lastControl = Math.max(form.lastIndexOf("<input"), form.lastIndexOf("<select"));
        assertThat(button).isGreaterThan(lastControl);
    }

    static int countOf(String html, String token) {
        int count = 0;
        int from = 0;
        while (true) {
            int found = html.indexOf(token, from);
            if (found < 0) {
                return count;
            }
            count++;
            from = found + token.length();
        }
    }

    static String inputTag(String html, String name) {
        Matcher matcher = Pattern.compile("<input\\b[^>]*\\bname=\"" + Pattern.quote(name) + "\"[^>]*>")
                .matcher(html);
        assertThat(matcher.find()).as("input %s", name).isTrue();
        return matcher.group();
    }
}
