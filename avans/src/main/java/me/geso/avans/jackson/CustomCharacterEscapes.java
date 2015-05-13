package me.geso.avans.jackson;

import com.fasterxml.jackson.core.SerializableString;
import com.fasterxml.jackson.core.io.CharacterEscapes;

public class CustomCharacterEscapes extends CharacterEscapes {

    private static final long serialVersionUID = 1L;

    private final int[] asciiEscapes;

    public CustomCharacterEscapes() {
        asciiEscapes = standardAsciiEscapesForJSON();
        // Escape characters for preventing XSS
        // see http://www.cowtowncoder.com/blog/archives/2012/08/entry_476.html
        asciiEscapes['/'] = CharacterEscapes.ESCAPE_STANDARD;
        asciiEscapes['<'] = CharacterEscapes.ESCAPE_STANDARD;
        asciiEscapes['>'] = CharacterEscapes.ESCAPE_STANDARD;
        asciiEscapes['+'] = CharacterEscapes.ESCAPE_STANDARD;
    }

    @Override
    public int[] getEscapeCodesForAscii() {
        return asciiEscapes.clone();
    }

    @Override
    public SerializableString getEscapeSequence(int i) {
        return null;
    }
}
