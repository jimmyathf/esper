package com.espertech.esper.type;

import java.util.regex.Pattern;
import java.io.Serializable;

/**
 * Regular expression matcher.
 */
public class StringPatternSetRegex implements StringPatternSet
{
    private final String patternText;
    private final Pattern pattern;

    /**
     * Ctor.
     * @param patternText regex to match
     */
    public StringPatternSetRegex(String patternText)
    {
        this.patternText = patternText;
        this.pattern = Pattern.compile(patternText);
    }

    /**
     * Match the string returning true for a match, using regular expression semantics.
     * @param stringToMatch string to match
     * @return true for match
     */
    public boolean match(String stringToMatch)
    {
        return pattern.matcher(stringToMatch).matches();
    }

    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StringPatternSetRegex that = (StringPatternSetRegex) o;

        if (!patternText.equals(that.patternText)) return false;

        return true;
    }

    public int hashCode()
    {
        return patternText.hashCode();
    }
}
