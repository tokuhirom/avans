package me.geso.avans.jackson;

import com.fasterxml.jackson.core.SerializableString;
import com.fasterxml.jackson.core.io.CharacterEscapes;

/**
 * Jacksonでシリアライズする際にescapeする文字を定義する。
 * non asciiな文字はJsonGenerator.Feature.ESCAPE_NON_ASCIIでescapeされるので、
 * asciiな文字を定義する。
 * @see http://qiita.com/kanemu@github/items/5476b6acc10dba3cff8b
 * @see http://www.cowtowncoder.com/blog/archives/2012/08/entry_476.html
 */
public class CustomCharacterEscapes extends CharacterEscapes {

    private static final long serialVersionUID = 1L;

    private final int[] asciiEscapes;

    public CustomCharacterEscapes() {
        asciiEscapes = standardAsciiEscapesForJSON();
        //XSS対策としてhtmlのtagを構成する文字をescapeする。
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
