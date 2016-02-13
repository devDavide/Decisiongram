package org.decisiongram.service;

import java.nio.charset.Charset;

/**
 * Created by davide on 23/12/15.
 */
class EmojiUtils {

    public static String getEmojiAsString(byte... emojiBytes){
        return new String(emojiBytes, Charset.forName("UTF-8"));
    }
}
