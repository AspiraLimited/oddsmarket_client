package com.aspiralimited.oddsmarket.client.demo.feedreader.diff;

import java.util.ArrayList;
import java.util.List;

public class DiffList {
    private final List<Diff> diffs = new ArrayList<>();

    public void addDiff(Diff diff) {
        diffs.add(diff);
    }

    public boolean isEmpty() {
        return diffs.isEmpty();
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        boolean appendSeparator = false;
        for (Diff diff : diffs) {
            if (appendSeparator) {
                result.append("; ");
            }
            result.append(diff.toString());
            appendSeparator = true;
        }
        return result.toString();
    }
}
