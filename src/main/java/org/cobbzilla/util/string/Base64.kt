package org.cobbzilla.util.string

/**
 *
 * Encodes and decodes to and from Base64 notation.
 *
 * Homepage: [http://iharder.net/base64](http://iharder.net/base64).
 *
 *
 * Example:
 *
 * `String encoded = Base64.encode( myByteArray );`
 * <br></br>
 * `byte[] myByteArray = Base64.decode( encoded );`
 *
 *
 * The <tt>options</tt> parameter, which appears in a few places, is used to pass
 * several pieces of information to the encoder. In the "higher level" methods such as
 * encodeBytes( bytes, options ) the options parameter can be used to indicate such
 * things as first gzipping the bytes before encoding them, not inserting linefeeds,
 * and encoding using the URL-safe and Ordered dialects.
 *
 *
 * Note, according to [RFC3548](http://www.faqs.org/rfcs/rfc3548.html),
 * Section 2.1, implementations should not add line feeds unless explicitly told
 * to do so. I've got Base64 set to this behavior now, although earlier versions
 * broke lines by default.
 *
 *
 * The constants defined in Base64 can be OR-ed together to combine options, so you
 * might make a call like this:
 *
 * `String encoded = Base64.encodeBytes( mybytes, Base64.GZIP | Base64.DO_BREAK_LINES );`
 *
 * to compress the data before encoding it and then making the output have newline characters.
 *
 * Also...
 * `String encoded = Base64.encodeBytes( crazyString.getBytes() );`
 *
 *
 *
 *
 *
 * Change Log:
 *
 *
 *  * v2.3.7 - Fixed subtle bug when base 64 input stream contained the
 * value 01111111, which is an invalid base 64 character but should not
 * throw an ArrayIndexOutOfBoundsException either. Led to discovery of
 * mishandling (or potential for better handling) of other bad input
 * characters. You should now get an IOException if you try decoding
 * something that has bad characters in it.
 *  * v2.3.6 - Fixed bug when breaking lines and the final byte of the encoded
 * string ended in the last column; the buffer was not properly shrunk and
 * contained an extra (null) byte that made it into the string.
 *  * v2.3.5 - Fixed bug in [.encodeFromFile] where estimated buffer size
 * was wrong for files of size 31, 34, and 37 bytes.
 *  * v2.3.4 - Fixed bug when working with gzipped streams whereby flushing
 * the Base64.OutputStream closed the Base64 encoding (by padding with equals
 * signs) too soon. Also added an option to suppress the automatic decoding
 * of gzipped streams. Also added experimental support for specifying a
 * class loader when using the
 * [.decodeToObject]
 * method.
 *  * v2.3.3 - Changed default char encoding to US-ASCII which reduces the internal Java
 * footprint with its CharEncoders and so forth. Fixed some javadocs that were
 * inconsistent. Removed imports and specified things like java.io.IOException
 * explicitly inline.
 *  * v2.3.2 - Reduced memory footprint! Finally refined the "guessing" of how big the
 * final encoded data will be so that the code doesn't have to create two output
 * arrays: an oversized initial one and then a final, exact-sized one. Big win
 * when using the [.encodeBytesToBytes] family of methods (and not
 * using the gzip options which uses a different mechanism with streams and stuff).
 *  * v2.3.1 - Added [.encodeBytesToBytes] and some
 * similar helper methods to be more efficient with memory by not returning a
 * String but just a byte array.
 *  * v2.3 - **This is not a drop-in replacement!** This is two years of comments
 * and bug fixes queued up and finally executed. Thanks to everyone who sent
 * me stuff, and I'm sorry I wasn't able to distribute your fixes to everyone else.
 * Much bad coding was cleaned up including throwing exceptions where necessary
 * instead of returning null values or something similar. Here are some changes
 * that may affect you:
 *
 *  * *Does not break lines, by default.* This is to keep in compliance with
 * [RFC3548](http://www.faqs.org/rfcs/rfc3548.html).
 *  * *Throws exceptions instead of returning null values.* Because some operations
 * (especially those that may permit the GZIP option) use IO streams, there
 * is a possiblity of an java.io.IOException being thrown. After some discussion and
 * thought, I've changed the behavior of the methods to throw java.io.IOExceptions
 * rather than return null if ever there's an error. I think this is more
 * appropriate, though it will require some changes to your code. Sorry,
 * it should have been done this way to begin with.
 *  * *Removed all references to System.out, System.err, and the like.*
 * Shame on me. All I can say is sorry they were ever there.
 *  * *Throws NullPointerExceptions and IllegalArgumentExceptions* as needed
 * such as when passed arrays are null or offsets are invalid.
 *  * Cleaned up as much javadoc as I could to avoid any javadoc warnings.
 * This was especially annoying before for people who were thorough in their
 * own projects and then had gobs of javadoc warnings on this file.
 *
 *  * v2.2.1 - Fixed bug using URL_SAFE and ORDERED encodings. Fixed bug
 * when using very small files (~&lt; 40 bytes).
 *  * v2.2 - Added some helper methods for encoding/decoding directly from
 * one file to the next. Also added a main() method to support command line
 * encoding/decoding from one file to the next. Also added these Base64 dialects:
 *
 *  1. The default is RFC3548 format.
 *  1. Calling Base64.setFormat(Base64.BASE64_FORMAT.URLSAFE_FORMAT) generates
 * URL and file name friendly format as described in Section 4 of RFC3548.
 * http://www.faqs.org/rfcs/rfc3548.html
 *  1. Calling Base64.setFormat(Base64.BASE64_FORMAT.ORDERED_FORMAT) generates
 * URL and file name friendly format that preserves lexical ordering as described
 * in http://www.faqs.org/qa/rfcc-1940.html
 *
 * Special thanks to Jim Kellerman at [http://www.powerset.com/](http://www.powerset.com/)
 * for contributing the new Base64 dialects.
 *
 *
 *  * v2.1 - Cleaned up javadoc comments and unused variables and methods. Added
 * some convenience methods for reading and writing to and from files.
 *  * v2.0.2 - Now specifies UTF-8 encoding in places where the code fails on systems
 * with other encodings (like EBCDIC).
 *  * v2.0.1 - Fixed an error when decoding a single byte, that is, when the
 * encoded data was a single byte.
 *  * v2.0 - I got rid of methods that used booleans to set options.
 * Now everything is more consolidated and cleaner. The code now detects
 * when data that's being decoded is gzip-compressed and will decompress it
 * automatically. Generally things are cleaner. You'll probably have to
 * change some method calls that you were making to support the new
 * options format (<tt>int</tt>s that you "OR" together).
 *  * v1.5.1 - Fixed bug when decompressing and decoding to a
 * byte[] using <tt>decode( String s, boolean gzipCompressed )</tt>.
 * Added the ability to "suspend" encoding in the Output Stream so
 * you can turn on and off the encoding if you need to embed base64
 * data in an otherwise "normal" stream (like an XML file).
 *  * v1.5 - Output stream pases on flush() command but doesn't do anything itself.
 * This helps when using GZIP streams.
 * Added the ability to GZip-compress objects before encoding them.
 *  * v1.4 - Added helper methods to read/write files.
 *  * v1.3.6 - Fixed OutputStream.flush() so that 'position' is reset.
 *  * v1.3.5 - Added flag to turn on and off line breaks. Fixed bug in input stream
 * where last buffer being read, if not completely full, was not returned.
 *  * v1.3.4 - Fixed when "improperly padded stream" error was thrown at the wrong time.
 *  * v1.3.3 - Fixed I/O streams which were totally messed up.
 *
 *
 *
 *
 * I am placing this code in the Public Domain. Do with it as you will.
 * This software comes with no guarantees or warranties but with
 * plenty of well-wishing instead!
 * Please visit [http://iharder.net/base64](http://iharder.net/base64)
 * periodically to check for updates or to contribute improvements.
 *
 *
 * @author Robert Harder
 * @author rob@iharder.net
 * @version 2.3.7
 */
object Base64 {

    /* ********  P U B L I C   F I E L D S  ******** */


    /** No options specified. Value is zero.  */
    val NO_OPTIONS = 0

    /** Specify encoding in first bit. Value is one.  */
    val ENCODE = 1


    /** Specify decoding in first bit. Value is zero.  */
    val DECODE = 0


    /** Specify that data should be gzip-compressed in second bit. Value is two.  */
    val GZIP = 2

    /** Specify that gzipped data should *not* be automatically gunzipped.  */
    val DONT_GUNZIP = 4


    /** Do break lines when encoding. Value is 8.  */
    val DO_BREAK_LINES = 8

    /**
     * Encode using Base64-like encoding that is URL- and Filename-safe as described
     * in Section 4 of RFC3548:
     * [http://www.faqs.org/rfcs/rfc3548.html](http://www.faqs.org/rfcs/rfc3548.html).
     * It is important to note that data encoded this way is *not* officially valid Base64,
     * or at the very least should not be called Base64 without also specifying that is
     * was encoded using the URL- and Filename-safe dialect.
     */
    val URL_SAFE = 16


    /**
     * Encode using the special "ordered" dialect of Base64 described here:
     * [http://www.faqs.org/qa/rfcc-1940.html](http://www.faqs.org/qa/rfcc-1940.html).
     */
    val ORDERED = 32


    /* ********  P R I V A T E   F I E L D S  ******** */


    /** Maximum line length (76) of Base64 output.  */
    private val MAX_LINE_LENGTH = 76


    /** The equals sign (=) as a byte.  */
    private val EQUALS_SIGN = '='.toByte()


    /** The new line character (\n) as a byte.  */
    private val NEW_LINE = '\n'.toByte()


    /** Preferred encoding.  */
    private val PREFERRED_ENCODING = "US-ASCII"


    private val WHITE_SPACE_ENC: Byte = -5 // Indicates white space in encoding
    private val EQUALS_SIGN_ENC: Byte = -1 // Indicates equals sign in encoding


    /* ********  S T A N D A R D   B A S E 6 4   A L P H A B E T  ******** */

    /** The 64 valid Base64 values.  */
    /* Host platform me be something funny like EBCDIC, so we hardcode these values. */
    private val _STANDARD_ALPHABET = byteArrayOf('A'.toByte(), 'B'.toByte(), 'C'.toByte(), 'D'.toByte(), 'E'.toByte(), 'F'.toByte(), 'G'.toByte(), 'H'.toByte(), 'I'.toByte(), 'J'.toByte(), 'K'.toByte(), 'L'.toByte(), 'M'.toByte(), 'N'.toByte(), 'O'.toByte(), 'P'.toByte(), 'Q'.toByte(), 'R'.toByte(), 'S'.toByte(), 'T'.toByte(), 'U'.toByte(), 'V'.toByte(), 'W'.toByte(), 'X'.toByte(), 'Y'.toByte(), 'Z'.toByte(), 'a'.toByte(), 'b'.toByte(), 'c'.toByte(), 'd'.toByte(), 'e'.toByte(), 'f'.toByte(), 'g'.toByte(), 'h'.toByte(), 'i'.toByte(), 'j'.toByte(), 'k'.toByte(), 'l'.toByte(), 'm'.toByte(), 'n'.toByte(), 'o'.toByte(), 'p'.toByte(), 'q'.toByte(), 'r'.toByte(), 's'.toByte(), 't'.toByte(), 'u'.toByte(), 'v'.toByte(), 'w'.toByte(), 'x'.toByte(), 'y'.toByte(), 'z'.toByte(), '0'.toByte(), '1'.toByte(), '2'.toByte(), '3'.toByte(), '4'.toByte(), '5'.toByte(), '6'.toByte(), '7'.toByte(), '8'.toByte(), '9'.toByte(), '+'.toByte(), '/'.toByte())


    /**
     * Translates a Base64 value to either its 6-bit reconstruction value
     * or a negative number indicating some other meaning.
     */
    private val _STANDARD_DECODABET = byteArrayOf(-9, -9, -9, -9, -9, -9, -9, -9, -9, // Decimal  0 -  8
            -5, -5, // Whitespace: Tab and Linefeed
            -9, -9, // Decimal 11 - 12
            -5, // Whitespace: Carriage Return
            -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, // Decimal 14 - 26
            -9, -9, -9, -9, -9, // Decimal 27 - 31
            -5, // Whitespace: Space
            -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, // Decimal 33 - 42
            62, // Plus sign at decimal 43
            -9, -9, -9, // Decimal 44 - 46
            63, // Slash at decimal 47
            52, 53, 54, 55, 56, 57, 58, 59, 60, 61, // Numbers zero through nine
            -9, -9, -9, // Decimal 58 - 60
            -1, // Equals sign at decimal 61
            -9, -9, -9, // Decimal 62 - 64
            0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, // Letters 'A' through 'N'
            14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, // Letters 'O' through 'Z'
            -9, -9, -9, -9, -9, -9, // Decimal 91 - 96
            26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, // Letters 'a' through 'm'
            39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, // Letters 'n' through 'z'
            -9, -9, -9, -9, -9                              // Decimal 123 - 127
            , -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, // Decimal 128 - 139
            -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, // Decimal 140 - 152
            -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, // Decimal 153 - 165
            -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, // Decimal 166 - 178
            -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, // Decimal 179 - 191
            -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, // Decimal 192 - 204
            -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, // Decimal 205 - 217
            -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, // Decimal 218 - 230
            -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, // Decimal 231 - 243
            -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9         // Decimal 244 - 255
    )


    /* ********  U R L   S A F E   B A S E 6 4   A L P H A B E T  ******** */

    /**
     * Used in the URL- and Filename-safe dialect described in Section 4 of RFC3548:
     * [http://www.faqs.org/rfcs/rfc3548.html](http://www.faqs.org/rfcs/rfc3548.html).
     * Notice that the last two bytes become "hyphen" and "underscore" instead of "plus" and "slash."
     */
    private val _URL_SAFE_ALPHABET = byteArrayOf('A'.toByte(), 'B'.toByte(), 'C'.toByte(), 'D'.toByte(), 'E'.toByte(), 'F'.toByte(), 'G'.toByte(), 'H'.toByte(), 'I'.toByte(), 'J'.toByte(), 'K'.toByte(), 'L'.toByte(), 'M'.toByte(), 'N'.toByte(), 'O'.toByte(), 'P'.toByte(), 'Q'.toByte(), 'R'.toByte(), 'S'.toByte(), 'T'.toByte(), 'U'.toByte(), 'V'.toByte(), 'W'.toByte(), 'X'.toByte(), 'Y'.toByte(), 'Z'.toByte(), 'a'.toByte(), 'b'.toByte(), 'c'.toByte(), 'd'.toByte(), 'e'.toByte(), 'f'.toByte(), 'g'.toByte(), 'h'.toByte(), 'i'.toByte(), 'j'.toByte(), 'k'.toByte(), 'l'.toByte(), 'm'.toByte(), 'n'.toByte(), 'o'.toByte(), 'p'.toByte(), 'q'.toByte(), 'r'.toByte(), 's'.toByte(), 't'.toByte(), 'u'.toByte(), 'v'.toByte(), 'w'.toByte(), 'x'.toByte(), 'y'.toByte(), 'z'.toByte(), '0'.toByte(), '1'.toByte(), '2'.toByte(), '3'.toByte(), '4'.toByte(), '5'.toByte(), '6'.toByte(), '7'.toByte(), '8'.toByte(), '9'.toByte(), '-'.toByte(), '_'.toByte())

    /**
     * Used in decoding URL- and Filename-safe dialects of Base64.
     */
    private val _URL_SAFE_DECODABET = byteArrayOf(-9, -9, -9, -9, -9, -9, -9, -9, -9, // Decimal  0 -  8
            -5, -5, // Whitespace: Tab and Linefeed
            -9, -9, // Decimal 11 - 12
            -5, // Whitespace: Carriage Return
            -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, // Decimal 14 - 26
            -9, -9, -9, -9, -9, // Decimal 27 - 31
            -5, // Whitespace: Space
            -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, // Decimal 33 - 42
            -9, // Plus sign at decimal 43
            -9, // Decimal 44
            62, // Minus sign at decimal 45
            -9, // Decimal 46
            -9, // Slash at decimal 47
            52, 53, 54, 55, 56, 57, 58, 59, 60, 61, // Numbers zero through nine
            -9, -9, -9, // Decimal 58 - 60
            -1, // Equals sign at decimal 61
            -9, -9, -9, // Decimal 62 - 64
            0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, // Letters 'A' through 'N'
            14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, // Letters 'O' through 'Z'
            -9, -9, -9, -9, // Decimal 91 - 94
            63, // Underscore at decimal 95
            -9, // Decimal 96
            26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, // Letters 'a' through 'm'
            39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, // Letters 'n' through 'z'
            -9, -9, -9, -9, -9                              // Decimal 123 - 127
            , -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, // Decimal 128 - 139
            -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, // Decimal 140 - 152
            -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, // Decimal 153 - 165
            -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, // Decimal 166 - 178
            -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, // Decimal 179 - 191
            -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, // Decimal 192 - 204
            -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, // Decimal 205 - 217
            -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, // Decimal 218 - 230
            -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, // Decimal 231 - 243
            -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9         // Decimal 244 - 255
    )


    /* ********  O R D E R E D   B A S E 6 4   A L P H A B E T  ******** */

    /**
     * I don't get the point of this technique, but someone requested it,
     * and it is described here:
     * [http://www.faqs.org/qa/rfcc-1940.html](http://www.faqs.org/qa/rfcc-1940.html).
     */
    private val _ORDERED_ALPHABET = byteArrayOf('-'.toByte(), '0'.toByte(), '1'.toByte(), '2'.toByte(), '3'.toByte(), '4'.toByte(), '5'.toByte(), '6'.toByte(), '7'.toByte(), '8'.toByte(), '9'.toByte(), 'A'.toByte(), 'B'.toByte(), 'C'.toByte(), 'D'.toByte(), 'E'.toByte(), 'F'.toByte(), 'G'.toByte(), 'H'.toByte(), 'I'.toByte(), 'J'.toByte(), 'K'.toByte(), 'L'.toByte(), 'M'.toByte(), 'N'.toByte(), 'O'.toByte(), 'P'.toByte(), 'Q'.toByte(), 'R'.toByte(), 'S'.toByte(), 'T'.toByte(), 'U'.toByte(), 'V'.toByte(), 'W'.toByte(), 'X'.toByte(), 'Y'.toByte(), 'Z'.toByte(), '_'.toByte(), 'a'.toByte(), 'b'.toByte(), 'c'.toByte(), 'd'.toByte(), 'e'.toByte(), 'f'.toByte(), 'g'.toByte(), 'h'.toByte(), 'i'.toByte(), 'j'.toByte(), 'k'.toByte(), 'l'.toByte(), 'm'.toByte(), 'n'.toByte(), 'o'.toByte(), 'p'.toByte(), 'q'.toByte(), 'r'.toByte(), 's'.toByte(), 't'.toByte(), 'u'.toByte(), 'v'.toByte(), 'w'.toByte(), 'x'.toByte(), 'y'.toByte(), 'z'.toByte())

    /**
     * Used in decoding the "ordered" dialect of Base64.
     */
    private val _ORDERED_DECODABET = byteArrayOf(-9, -9, -9, -9, -9, -9, -9, -9, -9, // Decimal  0 -  8
            -5, -5, // Whitespace: Tab and Linefeed
            -9, -9, // Decimal 11 - 12
            -5, // Whitespace: Carriage Return
            -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, // Decimal 14 - 26
            -9, -9, -9, -9, -9, // Decimal 27 - 31
            -5, // Whitespace: Space
            -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, // Decimal 33 - 42
            -9, // Plus sign at decimal 43
            -9, // Decimal 44
            0, // Minus sign at decimal 45
            -9, // Decimal 46
            -9, // Slash at decimal 47
            1, 2, 3, 4, 5, 6, 7, 8, 9, 10, // Numbers zero through nine
            -9, -9, -9, // Decimal 58 - 60
            -1, // Equals sign at decimal 61
            -9, -9, -9, // Decimal 62 - 64
            11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, // Letters 'A' through 'M'
            24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, // Letters 'N' through 'Z'
            -9, -9, -9, -9, // Decimal 91 - 94
            37, // Underscore at decimal 95
            -9, // Decimal 96
            38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, // Letters 'a' through 'm'
            51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, // Letters 'n' through 'z'
            -9, -9, -9, -9, -9                                 // Decimal 123 - 127
            , -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, // Decimal 128 - 139
            -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, // Decimal 140 - 152
            -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, // Decimal 153 - 165
            -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, // Decimal 166 - 178
            -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, // Decimal 179 - 191
            -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, // Decimal 192 - 204
            -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, // Decimal 205 - 217
            -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, // Decimal 218 - 230
            -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, // Decimal 231 - 243
            -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9         // Decimal 244 - 255
    )


    /* ********  D E T E R M I N E   W H I C H   A L H A B E T  ******** */


    /**
     * Returns one of the _SOMETHING_ALPHABET byte arrays depending on
     * the options specified.
     * It's possible, though silly, to specify ORDERED **and** URLSAFE
     * in which case one of them will be picked, though there is
     * no guarantee as to which one will be picked.
     */
    private fun getAlphabet(options: Int): ByteArray {
        return if (options and URL_SAFE == URL_SAFE) {
            _URL_SAFE_ALPHABET
        } else if (options and ORDERED == ORDERED) {
            _ORDERED_ALPHABET
        } else {
            _STANDARD_ALPHABET
        }
    }    // end getAlphabet


    /**
     * Returns one of the _SOMETHING_DECODABET byte arrays depending on
     * the options specified.
     * It's possible, though silly, to specify ORDERED and URL_SAFE
     * in which case one of them will be picked, though there is
     * no guarantee as to which one will be picked.
     */
    private fun getDecodabet(options: Int): ByteArray {
        return if (options and URL_SAFE == URL_SAFE) {
            _URL_SAFE_DECODABET
        } else if (options and ORDERED == ORDERED) {
            _ORDERED_DECODABET
        } else {
            _STANDARD_DECODABET
        }
    }    // end getAlphabet


    /* ********  E N C O D I N G   M E T H O D S  ******** */


    /**
     * Encodes up to the first three bytes of array <var>threeBytes</var>
     * and returns a four-byte array in Base64 notation.
     * The actual number of significant bytes in your array is
     * given by <var>numSigBytes</var>.
     * The array <var>threeBytes</var> needs only be as big as
     * <var>numSigBytes</var>.
     * Code can reuse a byte array by passing a four-byte array as <var>b4</var>.
     *
     * @param b4 A reusable byte array to reduce array instantiation
     * @param threeBytes the array to convert
     * @param numSigBytes the number of significant bytes in your array
     * @return four byte array in Base64 notation.
     * @since 1.5.1
     */
    private fun encode3to4(b4: ByteArray, threeBytes: ByteArray?, numSigBytes: Int, options: Int): ByteArray {
        encode3to4(threeBytes, 0, numSigBytes, b4, 0, options)
        return b4
    }   // end encode3to4


    /**
     *
     * Encodes up to three bytes of the array <var>source</var>
     * and writes the resulting four Base64 bytes to <var>destination</var>.
     * The source and destination arrays can be manipulated
     * anywhere along their length by specifying
     * <var>srcOffset</var> and <var>destOffset</var>.
     * This method does not check to make sure your arrays
     * are large enough to accomodate <var>srcOffset</var> + 3 for
     * the <var>source</var> array or <var>destOffset</var> + 4 for
     * the <var>destination</var> array.
     * The actual number of significant bytes in your array is
     * given by <var>numSigBytes</var>.
     *
     * This is the lowest level of the encoding methods with
     * all possible parameters.
     *
     * @param source the array to convert
     * @param srcOffset the index where conversion begins
     * @param numSigBytes the number of significant bytes in your array
     * @param destination the array to hold the conversion
     * @param destOffset the index where output will be put
     * @return the <var>destination</var> array
     * @since 1.3
     */
    private fun encode3to4(
            source: ByteArray?, srcOffset: Int, numSigBytes: Int,
            destination: ByteArray, destOffset: Int, options: Int): ByteArray {

        val ALPHABET = getAlphabet(options)

        //           1         2         3
        // 01234567890123456789012345678901 Bit position
        // --------000000001111111122222222 Array position from threeBytes
        // --------|    ||    ||    ||    | Six bit groups to index ALPHABET
        //          >>18  >>12  >> 6  >> 0  Right shift necessary
        //                0x3f  0x3f  0x3f  Additional AND

        // Create buffer with zero-padding if there are only one or two
        // significant bytes passed in the array.
        // We have to shift left 24 in order to flush out the 1's that appear
        // when Java treats a value as negative that is cast from a byte to an int.
        val inBuff = ((if (numSigBytes > 0) (source!![srcOffset] shl 24).ushr(8) else 0)
                or (if (numSigBytes > 1) (source!![srcOffset + 1] shl 24).ushr(16) else 0)
                or if (numSigBytes > 2) (source!![srcOffset + 2] shl 24).ushr(24) else 0)

        when (numSigBytes) {
            3 -> {
                destination[destOffset] = ALPHABET[inBuff.ushr(18)]
                destination[destOffset + 1] = ALPHABET[inBuff.ushr(12) and 0x3f]
                destination[destOffset + 2] = ALPHABET[inBuff.ushr(6) and 0x3f]
                destination[destOffset + 3] = ALPHABET[inBuff and 0x3f]
                return destination
            }

            2 -> {
                destination[destOffset] = ALPHABET[inBuff.ushr(18)]
                destination[destOffset + 1] = ALPHABET[inBuff.ushr(12) and 0x3f]
                destination[destOffset + 2] = ALPHABET[inBuff.ushr(6) and 0x3f]
                destination[destOffset + 3] = EQUALS_SIGN
                return destination
            }

            1 -> {
                destination[destOffset] = ALPHABET[inBuff.ushr(18)]
                destination[destOffset + 1] = ALPHABET[inBuff.ushr(12) and 0x3f]
                destination[destOffset + 2] = EQUALS_SIGN
                destination[destOffset + 3] = EQUALS_SIGN
                return destination
            }

            else -> return destination
        }   // end switch
    }   // end encode3to4


    /**
     * Performs Base64 encoding on the `raw` ByteBuffer,
     * writing it to the `encoded` ByteBuffer.
     * This is an experimental feature. Currently it does not
     * pass along any options (such as [.DO_BREAK_LINES]
     * or [.GZIP].
     *
     * @param raw input buffer
     * @param encoded output buffer
     * @since 2.3
     */
    fun encode(raw: java.nio.ByteBuffer, encoded: java.nio.ByteBuffer) {
        val raw3 = ByteArray(3)
        val enc4 = ByteArray(4)

        while (raw.hasRemaining()) {
            val rem = Math.min(3, raw.remaining())
            raw.get(raw3, 0, rem)
            Base64.encode3to4(enc4, raw3, rem, Base64.NO_OPTIONS)
            encoded.put(enc4)
        }   // end input remaining
    }


    /**
     * Performs Base64 encoding on the `raw` ByteBuffer,
     * writing it to the `encoded` CharBuffer.
     * This is an experimental feature. Currently it does not
     * pass along any options (such as [.DO_BREAK_LINES]
     * or [.GZIP].
     *
     * @param raw input buffer
     * @param encoded output buffer
     * @since 2.3
     */
    fun encode(raw: java.nio.ByteBuffer, encoded: java.nio.CharBuffer) {
        val raw3 = ByteArray(3)
        val enc4 = ByteArray(4)

        while (raw.hasRemaining()) {
            val rem = Math.min(3, raw.remaining())
            raw.get(raw3, 0, rem)
            Base64.encode3to4(enc4, raw3, rem, Base64.NO_OPTIONS)
            for (i in 0..3) {
                encoded.put((enc4[i] and 0xFF).toChar())
            }
        }   // end input remaining
    }


    /**
     * Serializes an object and returns the Base64-encoded
     * version of that serialized object.
     *
     *
     * As of v 2.3, if the object
     * cannot be serialized or there is another error,
     * the method will throw an java.io.IOException. **This is new to v2.3!**
     * In earlier versions, it just returned a null value, but
     * in retrospect that's a pretty poor way to handle it.
     *
     * The object is not GZip-compressed before being encoded.
     *
     *
     * Example options:<pre>
     * GZIP: gzip-compresses object before encoding it.
     * DO_BREAK_LINES: break lines at 76 characters
    </pre> *
     *
     *
     * Example: `encodeObject( myObj, Base64.GZIP )` or
     *
     *
     * Example: `encodeObject( myObj, Base64.GZIP | Base64.DO_BREAK_LINES )`
     *
     * @param serializableObject The object to encode
     * @param options Specified options
     * @return The Base64-encoded object
     * @see Base64.GZIP
     *
     * @see Base64.DO_BREAK_LINES
     *
     * @throws java.io.IOException if there is an error
     * @since 2.0
     */
    @Throws(java.io.IOException::class)
    @JvmOverloads
    fun encodeObject(serializableObject: java.io.Serializable?, options: Int = NO_OPTIONS): String {

        if (serializableObject == null) {
            throw NullPointerException("Cannot serialize a null object.")
        }   // end if: null

        // Streams
        var baos: java.io.ByteArrayOutputStream? = null
        var b64os: java.io.OutputStream? = null
        var gzos: java.util.zip.GZIPOutputStream? = null
        var oos: java.io.ObjectOutputStream? = null


        try {
            // ObjectOutputStream -> (GZIP) -> Base64 -> ByteArrayOutputStream
            baos = java.io.ByteArrayOutputStream()
            b64os = Base64.OutputStream(baos, ENCODE or options)
            if (options and GZIP != 0) {
                // Gzip
                gzos = java.util.zip.GZIPOutputStream(b64os)
                oos = java.io.ObjectOutputStream(gzos)
            } else {
                // Not gzipped
                oos = java.io.ObjectOutputStream(b64os)
            }
            oos.writeObject(serializableObject)
        }   // end try
        catch (e: java.io.IOException) {
            // Catch it and then throw it immediately so that
            // the finally{} block is called for cleanup.
            throw e
        }   // end catch
        finally {
            try {
                oos!!.close()
            } catch (e: Exception) {
            }

            try {
                gzos!!.close()
            } catch (e: Exception) {
            }

            try {
                b64os!!.close()
            } catch (e: Exception) {
            }

            try {
                baos!!.close()
            } catch (e: Exception) {
            }

        }   // end finally

        // Return value according to relevant encoding.
        try {
            return String(baos!!.toByteArray(), PREFERRED_ENCODING)
        }   // end try
        catch (uue: java.io.UnsupportedEncodingException) {
            // Fall back to some Java default
            return String(baos!!.toByteArray())
        }
        // end catch

    }   // end encode


    /**
     * Encodes a byte array into Base64 notation.
     * Does not GZip-compress data.
     *
     * @param source The data to convert
     * @return The data in Base64-encoded form
     * @throws NullPointerException if source array is null
     * @since 1.4
     */
    fun encodeBytes(source: ByteArray): String {
        // Since we're not going to have the GZIP encoding turned on,
        // we're not going to have an java.io.IOException thrown, so
        // we should not force the user to have to catch it.
        var encoded: String? = null
        try {
            encoded = encodeBytes(source, 0, source.size, NO_OPTIONS)
        } catch (ex: java.io.IOException) {
            assert(false) { ex.message }
        }
        // end catch
        assert(encoded != null)
        return encoded
    }   // end encodeBytes


    /**
     * Encodes a byte array into Base64 notation.
     *
     *
     * Example options:<pre>
     * GZIP: gzip-compresses object before encoding it.
     * DO_BREAK_LINES: break lines at 76 characters
     * *Note: Technically, this makes your encoding non-compliant.*
    </pre> *
     *
     *
     * Example: `encodeBytes( myData, Base64.GZIP )` or
     *
     *
     * Example: `encodeBytes( myData, Base64.GZIP | Base64.DO_BREAK_LINES )`
     *
     *
     *
     * As of v 2.3, if there is an error with the GZIP stream,
     * the method will throw an java.io.IOException. **This is new to v2.3!**
     * In earlier versions, it just returned a null value, but
     * in retrospect that's a pretty poor way to handle it.
     *
     *
     * @param source The data to convert
     * @param options Specified options
     * @return The Base64-encoded data as a String
     * @see Base64.GZIP
     *
     * @see Base64.DO_BREAK_LINES
     *
     * @throws java.io.IOException if there is an error
     * @throws NullPointerException if source array is null
     * @since 2.0
     */
    @Throws(java.io.IOException::class)
    fun encodeBytes(source: ByteArray, options: Int): String {
        return encodeBytes(source, 0, source.size, options)
    }   // end encodeBytes


    /**
     * Encodes a byte array into Base64 notation.
     * Does not GZip-compress data.
     *
     *
     * As of v 2.3, if there is an error,
     * the method will throw an java.io.IOException. **This is new to v2.3!**
     * In earlier versions, it just returned a null value, but
     * in retrospect that's a pretty poor way to handle it.
     *
     *
     * @param source The data to convert
     * @param off Offset in array where conversion should begin
     * @param len Length of data to convert
     * @return The Base64-encoded data as a String
     * @throws NullPointerException if source array is null
     * @throws IllegalArgumentException if source array, offset, or length are invalid
     * @since 1.4
     */
    fun encodeBytes(source: ByteArray, off: Int, len: Int): String {
        // Since we're not going to have the GZIP encoding turned on,
        // we're not going to have an java.io.IOException thrown, so
        // we should not force the user to have to catch it.
        var encoded: String? = null
        try {
            encoded = encodeBytes(source, off, len, NO_OPTIONS)
        } catch (ex: java.io.IOException) {
            assert(false) { ex.message }
        }
        // end catch
        assert(encoded != null)
        return encoded
    }   // end encodeBytes


    /**
     * Encodes a byte array into Base64 notation.
     *
     *
     * Example options:<pre>
     * GZIP: gzip-compresses object before encoding it.
     * DO_BREAK_LINES: break lines at 76 characters
     * *Note: Technically, this makes your encoding non-compliant.*
    </pre> *
     *
     *
     * Example: `encodeBytes( myData, Base64.GZIP )` or
     *
     *
     * Example: `encodeBytes( myData, Base64.GZIP | Base64.DO_BREAK_LINES )`
     *
     *
     *
     * As of v 2.3, if there is an error with the GZIP stream,
     * the method will throw an java.io.IOException. **This is new to v2.3!**
     * In earlier versions, it just returned a null value, but
     * in retrospect that's a pretty poor way to handle it.
     *
     *
     * @param source The data to convert
     * @param off Offset in array where conversion should begin
     * @param len Length of data to convert
     * @param options Specified options
     * @return The Base64-encoded data as a String
     * @see Base64.GZIP
     *
     * @see Base64.DO_BREAK_LINES
     *
     * @throws java.io.IOException if there is an error
     * @throws NullPointerException if source array is null
     * @throws IllegalArgumentException if source array, offset, or length are invalid
     * @since 2.0
     */
    @Throws(java.io.IOException::class)
    fun encodeBytes(source: ByteArray, off: Int, len: Int, options: Int): String {
        val encoded = encodeBytesToBytes(source, off, len, options)

        // Return value according to relevant encoding.
        try {
            return String(encoded, PREFERRED_ENCODING)
        }   // end try
        catch (uue: java.io.UnsupportedEncodingException) {
            return String(encoded)
        }
        // end catch

    }   // end encodeBytes


    /**
     * Similar to [.encodeBytes] but returns
     * a byte array instead of instantiating a String. This is more efficient
     * if you're working with I/O streams and have large data sets to encode.
     *
     *
     * @param source The data to convert
     * @return The Base64-encoded data as a byte[] (of ASCII characters)
     * @throws NullPointerException if source array is null
     * @since 2.3.1
     */
    fun encodeBytesToBytes(source: ByteArray): ByteArray? {
        var encoded: ByteArray? = null
        try {
            encoded = encodeBytesToBytes(source, 0, source.size, Base64.NO_OPTIONS)
        } catch (ex: java.io.IOException) {
            assert(false) { "IOExceptions only come from GZipping, which is turned off: " + ex.message }
        }

        return encoded
    }


    /**
     * Similar to [.encodeBytes] but returns
     * a byte array instead of instantiating a String. This is more efficient
     * if you're working with I/O streams and have large data sets to encode.
     *
     *
     * @param source The data to convert
     * @param off Offset in array where conversion should begin
     * @param len Length of data to convert
     * @param options Specified options
     * @return The Base64-encoded data as a String
     * @see Base64.GZIP
     *
     * @see Base64.DO_BREAK_LINES
     *
     * @throws java.io.IOException if there is an error
     * @throws NullPointerException if source array is null
     * @throws IllegalArgumentException if source array, offset, or length are invalid
     * @since 2.3.1
     */
    @Throws(java.io.IOException::class)
    fun encodeBytesToBytes(source: ByteArray?, off: Int, len: Int, options: Int): ByteArray {

        if (source == null) {
            throw NullPointerException("Cannot serialize a null array.")
        }   // end if: null

        if (off < 0) {
            throw IllegalArgumentException("Cannot have negative offset: $off")
        }   // end if: off < 0

        if (len < 0) {
            throw IllegalArgumentException("Cannot have length offset: $len")
        }   // end if: len < 0

        if (off + len > source.size) {
            throw IllegalArgumentException(
                    String.format("Cannot have offset of %d and length of %d with array of length %d", off, len, source.size))
        }   // end if: off < 0


        // Compress?
        if (options and GZIP != 0) {
            var baos: java.io.ByteArrayOutputStream? = null
            var gzos: java.util.zip.GZIPOutputStream? = null
            var b64os: Base64.OutputStream? = null

            try {
                // GZip -> Base64 -> ByteArray
                baos = java.io.ByteArrayOutputStream()
                b64os = Base64.OutputStream(baos, ENCODE or options)
                gzos = java.util.zip.GZIPOutputStream(b64os)

                gzos.write(source, off, len)
                gzos.close()
            }   // end try
            catch (e: java.io.IOException) {
                // Catch it and then throw it immediately so that
                // the finally{} block is called for cleanup.
                throw e
            }   // end catch
            finally {
                try {
                    gzos!!.close()
                } catch (e: Exception) {
                }

                try {
                    b64os!!.close()
                } catch (e: Exception) {
                }

                try {
                    baos!!.close()
                } catch (e: Exception) {
                }

            }   // end finally

            return baos!!.toByteArray()
        }   // end if: compress
        else {
            val breakLines = options and DO_BREAK_LINES != 0

            //int    len43   = len * 4 / 3;
            //byte[] outBuff = new byte[   ( len43 )                      // Main 4:3
            //                           + ( (len % 3) > 0 ? 4 : 0 )      // Account for padding
            //                           + (breakLines ? ( len43 / MAX_LINE_LENGTH ) : 0) ]; // New lines
            // Try to determine more precisely how big the array needs to be.
            // If we get it right, we don't have to do an array copy, and
            // we save a bunch of memory.
            var encLen = len / 3 * 4 + if (len % 3 > 0) 4 else 0 // Bytes needed for actual encoding
            if (breakLines) {
                encLen += encLen / MAX_LINE_LENGTH // Plus extra newline characters
            }
            val outBuff = ByteArray(encLen)


            var d = 0
            var e = 0
            val len2 = len - 2
            var lineLength = 0
            while (d < len2) {
                encode3to4(source, d + off, 3, outBuff, e, options)

                lineLength += 4
                if (breakLines && lineLength >= MAX_LINE_LENGTH) {
                    outBuff[e + 4] = NEW_LINE
                    e++
                    lineLength = 0
                }   // end if: end of line
                d += 3
                e += 4
            }   // en dfor: each piece of array

            if (d < len) {
                encode3to4(source, d + off, len - d, outBuff, e, options)
                e += 4
            }   // end if: some padding needed


            // Only resize array if we didn't guess it right.
            if (e <= outBuff.size - 1) {
                // If breaking lines and the last byte falls right at
                // the line length (76 bytes per line), there will be
                // one extra byte, and the array will need to be resized.
                // Not too bad of an estimate on array size, I'd say.
                val finalOut = ByteArray(e)
                System.arraycopy(outBuff, 0, finalOut, 0, e)
                //System.err.println("Having to resize array from " + outBuff.length + " to " + e );
                return finalOut
            } else {
                //System.err.println("No need to resize array.");
                return outBuff
            }

        }// Else, don't compress. Better not to use streams at all then.
        // end else: don't compress

    }   // end encodeBytesToBytes


    /* ********  D E C O D I N G   M E T H O D S  ******** */


    /**
     * Decodes four bytes from array <var>source</var>
     * and writes the resulting bytes (up to three of them)
     * to <var>destination</var>.
     * The source and destination arrays can be manipulated
     * anywhere along their length by specifying
     * <var>srcOffset</var> and <var>destOffset</var>.
     * This method does not check to make sure your arrays
     * are large enough to accomodate <var>srcOffset</var> + 4 for
     * the <var>source</var> array or <var>destOffset</var> + 3 for
     * the <var>destination</var> array.
     * This method returns the actual number of bytes that
     * were converted from the Base64 encoding.
     *
     * This is the lowest level of the decoding methods with
     * all possible parameters.
     *
     *
     * @param source the array to convert
     * @param srcOffset the index where conversion begins
     * @param destination the array to hold the conversion
     * @param destOffset the index where output will be put
     * @param options alphabet type is pulled from this (standard, url-safe, ordered)
     * @return the number of decoded bytes converted
     * @throws NullPointerException if source or destination arrays are null
     * @throws IllegalArgumentException if srcOffset or destOffset are invalid
     * or there is not enough room in the array.
     * @since 1.3
     */
    private fun decode4to3(
            source: ByteArray?, srcOffset: Int,
            destination: ByteArray?, destOffset: Int, options: Int): Int {

        // Lots of error checking and exception throwing
        if (source == null) {
            throw NullPointerException("Source array was null.")
        }   // end if
        if (destination == null) {
            throw NullPointerException("Destination array was null.")
        }   // end if
        if (srcOffset < 0 || srcOffset + 3 >= source.size) {
            throw IllegalArgumentException(String.format(
                    "Source array with length %d cannot have offset of %d and still process four bytes.", source.size, srcOffset))
        }   // end if
        if (destOffset < 0 || destOffset + 2 >= destination.size) {
            throw IllegalArgumentException(String.format(
                    "Destination array with length %d cannot have offset of %d and still store three bytes.", destination.size, destOffset))
        }   // end if


        val DECODABET = getDecodabet(options)

        // Example: Dk==
        if (source[srcOffset + 2] == EQUALS_SIGN) {
            // Two ways to do the same thing. Don't know which way I like best.
            //int outBuff =   ( ( DECODABET[ source[ srcOffset    ] ] << 24 ) >>>  6 )
            //              | ( ( DECODABET[ source[ srcOffset + 1] ] << 24 ) >>> 12 );
            val outBuff = DECODABET[source[srcOffset]] and 0xFF shl 18 or (DECODABET[source[srcOffset + 1]] and 0xFF shl 12)

            destination[destOffset] = outBuff.ushr(16).toByte()
            return 1
        } else if (source[srcOffset + 3] == EQUALS_SIGN) {
            // Two ways to do the same thing. Don't know which way I like best.
            //int outBuff =   ( ( DECODABET[ source[ srcOffset     ] ] << 24 ) >>>  6 )
            //              | ( ( DECODABET[ source[ srcOffset + 1 ] ] << 24 ) >>> 12 )
            //              | ( ( DECODABET[ source[ srcOffset + 2 ] ] << 24 ) >>> 18 );
            val outBuff = (DECODABET[source[srcOffset]] and 0xFF shl 18
                    or (DECODABET[source[srcOffset + 1]] and 0xFF shl 12)
                    or (DECODABET[source[srcOffset + 2]] and 0xFF shl 6))

            destination[destOffset] = outBuff.ushr(16).toByte()
            destination[destOffset + 1] = outBuff.ushr(8).toByte()
            return 2
        } else {
            // Two ways to do the same thing. Don't know which way I like best.
            //int outBuff =   ( ( DECODABET[ source[ srcOffset     ] ] << 24 ) >>>  6 )
            //              | ( ( DECODABET[ source[ srcOffset + 1 ] ] << 24 ) >>> 12 )
            //              | ( ( DECODABET[ source[ srcOffset + 2 ] ] << 24 ) >>> 18 )
            //              | ( ( DECODABET[ source[ srcOffset + 3 ] ] << 24 ) >>> 24 );
            val outBuff = (DECODABET[source[srcOffset]] and 0xFF shl 18
                    or (DECODABET[source[srcOffset + 1]] and 0xFF shl 12)
                    or (DECODABET[source[srcOffset + 2]] and 0xFF shl 6)
                    or (DECODABET[source[srcOffset + 3]] and 0xFF))


            destination[destOffset] = (outBuff shr 16).toByte()
            destination[destOffset + 1] = (outBuff shr 8).toByte()
            destination[destOffset + 2] = outBuff.toByte()

            return 3
        }// Example: DkLE
        // Example: DkL=
    }   // end decodeToBytes


    /**
     * Low-level access to decoding ASCII characters in
     * the form of a byte array. **Ignores GUNZIP option, if
     * it's set.** This is not generally a recommended method,
     * although it is used internally as part of the decoding process.
     * Special case: if len = 0, an empty array is returned. Still,
     * if you need more speed and reduced memory footprint (and aren't
     * gzipping), consider this method.
     *
     * @param source The Base64 encoded data
     * @return decoded data
     * @since 2.3.1
     */
    @Throws(java.io.IOException::class)
    fun decode(source: ByteArray): ByteArray? {
        var decoded: ByteArray? = null
        //        try {
        decoded = decode(source, 0, source.size, Base64.NO_OPTIONS)
        //        } catch( java.io.IOException ex ) {
        //            assert false : "IOExceptions only come from GZipping, which is turned off: " + ex.getMessage();
        //        }
        return decoded
    }


    /**
     * Low-level access to decoding ASCII characters in
     * the form of a byte array. **Ignores GUNZIP option, if
     * it's set.** This is not generally a recommended method,
     * although it is used internally as part of the decoding process.
     * Special case: if len = 0, an empty array is returned. Still,
     * if you need more speed and reduced memory footprint (and aren't
     * gzipping), consider this method.
     *
     * @param source The Base64 encoded data
     * @param off    The offset of where to begin decoding
     * @param len    The length of characters to decode
     * @param options Can specify options such as alphabet type to use
     * @return decoded data
     * @throws java.io.IOException If bogus characters exist in source data
     * @since 1.3
     */
    @Throws(java.io.IOException::class)
    fun decode(source: ByteArray?, off: Int, len: Int, options: Int): ByteArray {

        // Lots of error checking and exception throwing
        if (source == null) {
            throw NullPointerException("Cannot decode null source array.")
        }   // end if
        if (off < 0 || off + len > source.size) {
            throw IllegalArgumentException(String.format(
                    "Source array with length %d cannot have offset of %d and process %d bytes.", source.size, off, len))
        }   // end if

        if (len == 0) {
            return ByteArray(0)
        } else if (len < 4) {
            throw IllegalArgumentException(
                    "Base64-encoded string must have at least four characters, but length specified was $len")
        }   // end if

        val DECODABET = getDecodabet(options)

        val len34 = len * 3 / 4       // Estimate on array size
        val outBuff = ByteArray(len34) // Upper limit on size of output
        var outBuffPosn = 0             // Keep track of where we're writing

        val b4 = ByteArray(4)     // Four byte buffer from source, eliminating white space
        var b4Posn = 0               // Keep track of four byte input buffer
        var i = 0               // Source array counter
        var sbiDecode: Byte = 0               // Special value from DECODABET

        i = off
        while (i < off + len) {  // Loop through source

            sbiDecode = DECODABET[source[i] and 0xFF]

            // White space, Equals sign, or legit Base64 character
            // Note the values such as -5 and -9 in the
            // DECODABETs at the top of the file.
            if (sbiDecode >= WHITE_SPACE_ENC) {
                if (sbiDecode >= EQUALS_SIGN_ENC) {
                    b4[b4Posn++] = source[i]         // Save non-whitespace
                    if (b4Posn > 3) {                  // Time to decode?
                        outBuffPosn += decode4to3(b4, 0, outBuff, outBuffPosn, options)
                        b4Posn = 0

                        // If that was the equals sign, break out of 'for' loop
                        if (source[i] == EQUALS_SIGN) {
                            break
                        }   // end if: equals sign
                    }   // end if: quartet built
                }   // end if: equals sign or better
            }   // end if: white space, equals sign or better
            else {
                // There's a bad input character in the Base64 stream.
                throw java.io.IOException(String.format(
                        "Bad Base64 input character decimal %d in array position %d", source[i].toInt() and 0xFF, i))
            }   // end else:
            i++
        }   // each input character

        val out = ByteArray(outBuffPosn)
        System.arraycopy(outBuff, 0, out, 0, outBuffPosn)
        return out
    }   // end decode


    /**
     * Decodes data from Base64 notation, automatically
     * detecting gzip-compressed data and decompressing it.
     *
     * @param s the string to decode
     * @param options encode options such as URL_SAFE
     * @return the decoded data
     * @throws java.io.IOException if there is an error
     * @throws NullPointerException if <tt>s</tt> is null
     * @since 1.4
     */
    @Throws(java.io.IOException::class)
    @JvmOverloads
    fun decode(s: String?, options: Int = NO_OPTIONS): ByteArray? {

        if (s == null) {
            throw NullPointerException("Input string was null.")
        }   // end if

        var bytes: ByteArray?
        try {
            bytes = s.toByteArray(charset(PREFERRED_ENCODING))
        }   // end try
        catch (uee: java.io.UnsupportedEncodingException) {
            bytes = s.toByteArray()
        }
        // end catch
        //</change>

        // Decode
        bytes = decode(bytes, 0, bytes!!.size, options)

        // Check to see if it's gzip-compressed
        // GZIP Magic Two-Byte Number: 0x8b1f (35615)
        val dontGunzip = options and DONT_GUNZIP != 0
        if (bytes != null && bytes.size >= 4 && !dontGunzip) {

            val head = bytes[0].toInt() and 0xff or (bytes[1] shl 8 and 0xff00)
            if (java.util.zip.GZIPInputStream.GZIP_MAGIC == head) {
                var bais: java.io.ByteArrayInputStream? = null
                var gzis: java.util.zip.GZIPInputStream? = null
                var baos: java.io.ByteArrayOutputStream? = null
                val buffer = ByteArray(2048)
                var length = 0

                try {
                    baos = java.io.ByteArrayOutputStream()
                    bais = java.io.ByteArrayInputStream(bytes)
                    gzis = java.util.zip.GZIPInputStream(bais)

                    while ((length = gzis.read(buffer)) >= 0) {
                        baos.write(buffer, 0, length)
                    }   // end while: reading input

                    // No error? Get new bytes.
                    bytes = baos.toByteArray()

                }   // end try
                catch (e: java.io.IOException) {
                    e.printStackTrace()
                    // Just return originally-decoded bytes
                }   // end catch
                finally {
                    try {
                        baos!!.close()
                    } catch (e: Exception) {
                    }

                    try {
                        gzis!!.close()
                    } catch (e: Exception) {
                    }

                    try {
                        bais!!.close()
                    } catch (e: Exception) {
                    }

                }   // end finally

            }   // end if: gzipped
        }   // end if: bytes.length >= 2

        return bytes
    }   // end decode


    /**
     * Attempts to decode Base64 data and deserialize a Java
     * Object within. Returns <tt>null</tt> if there was an error.
     * If <tt>loader</tt> is not null, it will be the class loader
     * used when deserializing.
     *
     * @param encodedObject The Base64 data to decode
     * @param options Various parameters related to decoding
     * @param loader Optional class loader to use in deserializing classes.
     * @return The decoded and deserialized object
     * @throws NullPointerException if encodedObject is null
     * @throws java.io.IOException if there is a general error
     * @throws ClassNotFoundException if the decoded object is of a
     * class that cannot be found by the JVM
     * @since 2.3.4
     */
    @Throws(java.io.IOException::class, java.lang.ClassNotFoundException::class)
    @JvmOverloads
    fun decodeToObject(
            encodedObject: String, options: Int = NO_OPTIONS, loader: ClassLoader? = null): Any? {

        // Decode and gunzip if necessary
        val objBytes = decode(encodedObject, options)

        var bais: java.io.ByteArrayInputStream? = null
        var ois: java.io.ObjectInputStream? = null
        var obj: Any? = null

        try {
            bais = java.io.ByteArrayInputStream(objBytes!!)

            // If no custom class loader is provided, use Java's builtin OIS.
            if (loader == null) {
                ois = java.io.ObjectInputStream(bais)
            }   // end if: no loader provided
            else {
                ois = object : java.io.ObjectInputStream(bais) {
                    @Throws(java.io.IOException::class, ClassNotFoundException::class)
                    public override fun resolveClass(streamClass: java.io.ObjectStreamClass): Class<*>? {
                        val c = Class.forName(streamClass.name, false, loader)
                        return c   // Class loader knows of this class.
                                ?: super.resolveClass(streamClass)   // end else: not null
                    }   // end resolveClass
                }  // end ois
            }// Else make a customized object input stream that uses
            // the provided class loader.
            // end else: no custom class loader

            obj = ois.readObject()
        }   // end try
        catch (e: java.io.IOException) {
            throw e    // Catch and throw in order to execute finally{}
        }   // end catch
        catch (e: java.lang.ClassNotFoundException) {
            throw e    // Catch and throw in order to execute finally{}
        }   // end catch
        finally {
            try {
                bais!!.close()
            } catch (e: Exception) {
            }

            try {
                ois!!.close()
            } catch (e: Exception) {
            }

        }   // end finally

        return obj
    }   // end decodeObject


    /**
     * Convenience method for encoding data to a file.
     *
     *
     * As of v 2.3, if there is a error,
     * the method will throw an java.io.IOException. **This is new to v2.3!**
     * In earlier versions, it just returned false, but
     * in retrospect that's a pretty poor way to handle it.
     *
     * @param dataToEncode byte array of data to encode in base64 form
     * @param filename Filename for saving encoded data
     * @throws java.io.IOException if there is an error
     * @throws NullPointerException if dataToEncode is null
     * @since 2.1
     */
    @Throws(java.io.IOException::class)
    fun encodeToFile(dataToEncode: ByteArray?, filename: String) {

        if (dataToEncode == null) {
            throw NullPointerException("Data to encode was null.")
        }   // end iff

        var bos: Base64.OutputStream? = null
        try {
            bos = Base64.OutputStream(
                    java.io.FileOutputStream(filename), Base64.ENCODE)
            bos.write(dataToEncode)
        }   // end try
        catch (e: java.io.IOException) {
            throw e // Catch and throw to execute finally{} block
        }   // end catch: java.io.IOException
        finally {
            try {
                bos!!.close()
            } catch (e: Exception) {
            }

        }   // end finally

    }   // end encodeToFile


    /**
     * Convenience method for decoding data to a file.
     *
     *
     * As of v 2.3, if there is a error,
     * the method will throw an java.io.IOException. **This is new to v2.3!**
     * In earlier versions, it just returned false, but
     * in retrospect that's a pretty poor way to handle it.
     *
     * @param dataToDecode Base64-encoded data as a string
     * @param filename Filename for saving decoded data
     * @throws java.io.IOException if there is an error
     * @since 2.1
     */
    @Throws(java.io.IOException::class)
    fun decodeToFile(dataToDecode: String, filename: String) {

        var bos: Base64.OutputStream? = null
        try {
            bos = Base64.OutputStream(
                    java.io.FileOutputStream(filename), Base64.DECODE)
            bos.write(dataToDecode.toByteArray(charset(PREFERRED_ENCODING)))
        }   // end try
        catch (e: java.io.IOException) {
            throw e // Catch and throw to execute finally{} block
        }   // end catch: java.io.IOException
        finally {
            try {
                bos!!.close()
            } catch (e: Exception) {
            }

        }   // end finally

    }   // end decodeToFile


    /**
     * Convenience method for reading a base64-encoded
     * file and decoding it.
     *
     *
     * As of v 2.3, if there is a error,
     * the method will throw an java.io.IOException. **This is new to v2.3!**
     * In earlier versions, it just returned false, but
     * in retrospect that's a pretty poor way to handle it.
     *
     * @param filename Filename for reading encoded data
     * @return decoded byte array
     * @throws java.io.IOException if there is an error
     * @since 2.1
     */
    @Throws(java.io.IOException::class)
    fun decodeFromFile(filename: String): ByteArray {

        var decodedData: ByteArray? = null
        var bis: Base64.InputStream? = null
        try {
            // Set up some useful variables
            val file = java.io.File(filename)
            var buffer: ByteArray? = null
            var length = 0
            var numBytes = 0

            // Check for size of file
            if (file.length() > Integer.MAX_VALUE) {
                throw java.io.IOException("File is too big for this convenience method (" + file.length() + " bytes).")
            }   // end if: file too big for int index
            buffer = ByteArray(file.length().toInt())

            // Open a stream
            bis = Base64.InputStream(
                    java.io.BufferedInputStream(
                            java.io.FileInputStream(file)), Base64.DECODE)

            // Read until done
            while ((numBytes = bis.read(buffer, length, 4096)) >= 0) {
                length += numBytes
            }   // end while

            // Save in a variable to return
            decodedData = ByteArray(length)
            System.arraycopy(buffer, 0, decodedData, 0, length)

        }   // end try
        catch (e: java.io.IOException) {
            throw e // Catch and release to execute finally{}
        }   // end catch: java.io.IOException
        finally {
            try {
                bis!!.close()
            } catch (e: Exception) {
            }

        }   // end finally

        return decodedData
    }   // end decodeFromFile


    /**
     * See [.encodeFromFile].
     *
     * @param filename Filename for reading binary data
     * @return base64-encoded string
     * @throws java.io.IOException if there is an error
     * @since 2.1
     */
    @Throws(java.io.IOException::class)
    fun encodeFromFile(filename: String): String {
        return encodeFromFile(java.io.File(filename))
    }

    /**
     * Convenience method for reading a binary file
     * and base64-encoding it.
     *
     *
     * As of v 2.3, if there is a error,
     * the method will throw an java.io.IOException. **This is new to v2.3!**
     * In earlier versions, it just returned false, but
     * in retrospect that's a pretty poor way to handle it.
     *
     * @param file File for reading binary data
     * @return base64-encoded string
     * @throws java.io.IOException if there is an error
     */
    @Throws(java.io.IOException::class)
    fun encodeFromFile(file: java.io.File): String {

        var encodedData: String? = null
        var bis: Base64.InputStream? = null
        try {
            // Set up some useful variables
            val buffer = ByteArray(Math.max((file.length() * 1.4 + 1).toInt(), 40)) // Need max() for math on small files (v2.2.1); Need +1 for a few corner cases (v2.3.5)
            var length = 0
            var numBytes = 0

            // Open a stream
            bis = Base64.InputStream(
                    java.io.BufferedInputStream(
                            java.io.FileInputStream(file)), Base64.ENCODE)

            // Read until done
            while ((numBytes = bis.read(buffer, length, 4096)) >= 0) {
                length += numBytes
            }   // end while

            // Save in a variable to return
            encodedData = String(buffer, 0, length, Base64.PREFERRED_ENCODING)

        }   // end try
        catch (e: java.io.IOException) {
            throw e // Catch and release to execute finally{}
        }   // end catch: java.io.IOException
        finally {
            try {
                bis!!.close()
            } catch (e: Exception) {
            }

        }   // end finally

        return encodedData
    }   // end encodeFromFile

    /**
     * Reads <tt>infile</tt> and encodes it to <tt>outfile</tt>.
     *
     * @param infile Input file
     * @param outfile Output file
     * @throws java.io.IOException if there is an error
     * @since 2.2
     */
    @Throws(java.io.IOException::class)
    fun encodeFileToFile(infile: String, outfile: String) {

        val encoded = Base64.encodeFromFile(infile)
        var out: java.io.OutputStream? = null
        try {
            out = java.io.BufferedOutputStream(
                    java.io.FileOutputStream(outfile))
            out.write(encoded.toByteArray(charset("US-ASCII"))) // Strict, 7-bit output.
        }   // end try
        catch (e: java.io.IOException) {
            throw e // Catch and release to execute finally{}
        }   // end catch
        finally {
            try {
                out!!.close()
            } catch (ex: Exception) {
            }

        }   // end finally
    }   // end encodeFileToFile


    /**
     * Reads <tt>infile</tt> and decodes it to <tt>outfile</tt>.
     *
     * @param infile Input file
     * @param outfile Output file
     * @throws java.io.IOException if there is an error
     * @since 2.2
     */
    @Throws(java.io.IOException::class)
    fun decodeFileToFile(infile: String, outfile: String) {

        val decoded = Base64.decodeFromFile(infile)
        var out: java.io.OutputStream? = null
        try {
            out = java.io.BufferedOutputStream(
                    java.io.FileOutputStream(outfile))
            out.write(decoded)
        }   // end try
        catch (e: java.io.IOException) {
            throw e // Catch and release to execute finally{}
        }   // end catch
        finally {
            try {
                out!!.close()
            } catch (ex: Exception) {
            }

        }   // end finally
    }   // end decodeFileToFile


    /* ********  I N N E R   C L A S S   I N P U T S T R E A M  ******** */


    /**
     * A [Base64.InputStream] will read data from another
     * <tt>java.io.InputStream</tt>, given in the constructor,
     * and encode/decode to/from Base64 notation on the fly.
     *
     * @see Base64
     *
     * @since 1.3
     */
    class InputStream
    /**
     * Constructs a [Base64.InputStream] in
     * either ENCODE or DECODE mode.
     *
     *
     * Valid options:<pre>
     * ENCODE or DECODE: Encode or Decode as data is read.
     * DO_BREAK_LINES: break lines at 76 characters
     * (only meaningful when encoding)
    </pre> *
     *
     *
     * Example: `new Base64.InputStream( in, Base64.DECODE )`
     *
     *
     * @param in the <tt>java.io.InputStream</tt> from which to read data.
     * @param options Specified options
     * @see Base64.ENCODE
     *
     * @see Base64.DECODE
     *
     * @see Base64.DO_BREAK_LINES
     *
     * @since 2.0
     */
    @JvmOverloads constructor(`in`: java.io.InputStream, private val options: Int = DECODE        // Record options used to create the stream.
    ) : java.io.FilterInputStream(`in`) {

        private val encode: Boolean         // Encoding or decoding
        private var position: Int = 0       // Current position in the buffer
        private val buffer: ByteArray         // Small buffer holding converted data
        private val bufferLength: Int   // Length of buffer (3 or 4)
        private var numSigBytes: Int = 0    // Number of meaningful bytes in the buffer
        private var lineLength: Int = 0
        private val breakLines: Boolean     // Break lines at less than 80 characters
        private val decodabet: ByteArray      // Local copies to avoid extra method calls


        init {
            this.breakLines = options and DO_BREAK_LINES > 0
            this.encode = options and ENCODE > 0
            this.bufferLength = if (encode) 4 else 3
            this.buffer = ByteArray(bufferLength)
            this.position = -1
            this.lineLength = 0
            this.decodabet = getDecodabet(options)
        }// Record for later
        // end constructor

        /**
         * Reads enough of the input stream to convert
         * to/from Base64 and returns the next byte.
         *
         * @return next byte
         * @since 1.3
         */
        @Throws(java.io.IOException::class)
        override fun read(): Int {

            // Do we need to get data?
            if (position < 0) {
                if (encode) {
                    val b3 = ByteArray(3)
                    var numBinaryBytes = 0
                    for (i in 0..2) {
                        val b = `in`.read()

                        // If end of stream, b is -1.
                        if (b >= 0) {
                            b3[i] = b.toByte()
                            numBinaryBytes++
                        } else {
                            break // out of for loop
                        }   // end else: end of stream

                    }   // end for: each needed input byte

                    if (numBinaryBytes > 0) {
                        encode3to4(b3, 0, numBinaryBytes, buffer, 0, options)
                        position = 0
                        numSigBytes = 4
                    }   // end if: got data
                    else {
                        return -1  // Must be end of stream
                    }   // end else
                }   // end if: encoding
                else {
                    val b4 = ByteArray(4)
                    var i = 0
                    i = 0
                    while (i < 4) {
                        // Read four "meaningful" bytes:
                        var b = 0
                        do {
                            b = `in`.read()
                        } while (b >= 0 && decodabet[b and 0x7f] <= WHITE_SPACE_ENC)

                        if (b < 0) {
                            break // Reads a -1 if end of stream
                        }   // end if: end of stream

                        b4[i] = b.toByte()
                        i++
                    }   // end for: each needed input byte

                    if (i == 4) {
                        numSigBytes = decode4to3(b4, 0, buffer, 0, options)
                        position = 0
                    }   // end if: got four characters
                    else return if (i == 0) {
                        -1
                    }   // end else if: also padded correctly
                    else {
                        // Must have broken out from above.
                        throw java.io.IOException("Improperly padded Base64 input.")
                    }   // end

                }// Else decoding
                // end else: decode
            }   // end else: get data

            // Got data?
            if (position >= 0) {
                // End of relevant data?
                if (/*!encode &&*/ position >= numSigBytes) {
                    return -1
                }   // end if: got data

                if (encode && breakLines && lineLength >= MAX_LINE_LENGTH) {
                    lineLength = 0
                    return '\n'.toInt()
                }   // end if
                else {
                    lineLength++   // This isn't important when decoding
                    // but throwing an extra "if" seems
                    // just as wasteful.

                    val b = buffer[position++].toInt()

                    if (position >= bufferLength) {
                        position = -1
                    }   // end if: end

                    return b and 0xFF // This is how you "cast" a byte that's
                    // intended to be unsigned.
                }   // end else
            }   // end if: position >= 0
            else {
                throw java.io.IOException("Error in Base64 code reading stream.")
            }// Else error
            // end else
        }   // end read


        /**
         * Calls [.read] repeatedly until the end of stream
         * is reached or <var>len</var> bytes are read.
         * Returns number of bytes read into array or -1 if
         * end of stream is encountered.
         *
         * @param dest array to hold values
         * @param off offset for array
         * @param len max number of bytes to read into array
         * @return bytes read into array or -1 if end of stream is encountered.
         * @since 1.3
         */
        @Throws(java.io.IOException::class)
        override fun read(dest: ByteArray, off: Int, len: Int): Int {
            var i: Int
            var b: Int
            i = 0
            while (i < len) {
                b = read()

                if (b >= 0) {
                    dest[off + i] = b.toByte()
                } else return if (i == 0) {
                    -1
                } else {
                    break // Out of 'for' loop
                } // Out of 'for' loop
                i++
            }   // end for: each byte read
            return i
        }   // end read

    }
    /**
     * Constructs a [Base64.InputStream] in DECODE mode.
     *
     * @param in the <tt>java.io.InputStream</tt> from which to read data.
     * @since 1.3
     */// end constructor
    // end inner class InputStream


    /* ********  I N N E R   C L A S S   O U T P U T S T R E A M  ******** */


    /**
     * A [Base64.OutputStream] will write data to another
     * <tt>java.io.OutputStream</tt>, given in the constructor,
     * and encode/decode to/from Base64 notation on the fly.
     *
     * @see Base64
     *
     * @since 1.3
     */
    class OutputStream
    /**
     * Constructs a [Base64.OutputStream] in
     * either ENCODE or DECODE mode.
     *
     *
     * Valid options:<pre>
     * ENCODE or DECODE: Encode or Decode as data is read.
     * DO_BREAK_LINES: don't break lines at 76 characters
     * (only meaningful when encoding)
    </pre> *
     *
     *
     * Example: `new Base64.OutputStream( out, Base64.ENCODE )`
     *
     * @param out the <tt>java.io.OutputStream</tt> to which data will be written.
     * @param options Specified options.
     * @see Base64.ENCODE
     *
     * @see Base64.DECODE
     *
     * @see Base64.DO_BREAK_LINES
     *
     * @since 1.3
     */
    @JvmOverloads constructor(out: java.io.OutputStream, private val options: Int = ENCODE    // Record for later
    ) : java.io.FilterOutputStream(out) {

        private val encode: Boolean
        private var position: Int = 0
        private var buffer: ByteArray? = null
        private val bufferLength: Int
        private var lineLength: Int = 0
        private val breakLines: Boolean
        private val b4: ByteArray         // Scratch used in a few places
        private var suspendEncoding: Boolean = false
        private val decodabet: ByteArray  // Local copies to avoid extra method calls


        init {
            this.breakLines = options and DO_BREAK_LINES != 0
            this.encode = options and ENCODE != 0
            this.bufferLength = if (encode) 3 else 4
            this.buffer = ByteArray(bufferLength)
            this.position = 0
            this.lineLength = 0
            this.suspendEncoding = false
            this.b4 = ByteArray(4)
            this.decodabet = getDecodabet(options)
        }   // end constructor


        /**
         * Writes the byte to the output stream after
         * converting to/from Base64 notation.
         * When encoding, bytes are buffered three
         * at a time before the output stream actually
         * gets a write() call.
         * When decoding, bytes are buffered four
         * at a time.
         *
         * @param theByte the byte to write
         * @since 1.3
         */
        @Throws(java.io.IOException::class)
        override fun write(theByte: Int) {
            // Encoding suspended?
            if (suspendEncoding) {
                this.out.write(theByte)
                return
            }   // end if: supsended

            // Encode?
            if (encode) {
                buffer[position++] = theByte.toByte()
                if (position >= bufferLength) { // Enough to encode.

                    this.out.write(encode3to4(b4, buffer, bufferLength, options))

                    lineLength += 4
                    if (breakLines && lineLength >= MAX_LINE_LENGTH) {
                        this.out.write(NEW_LINE.toInt())
                        lineLength = 0
                    }   // end if: end of line

                    position = 0
                }   // end if: enough to output
            }   // end if: encoding
            else {
                // Meaningful Base64 character?
                if (decodabet[theByte and 0x7f] > WHITE_SPACE_ENC) {
                    buffer[position++] = theByte.toByte()
                    if (position >= bufferLength) { // Enough to output.

                        val len = Base64.decode4to3(buffer, 0, b4, 0, options)
                        out.write(b4, 0, len)
                        position = 0
                    }   // end if: enough to output
                }   // end if: meaningful base64 character
                else if (decodabet[theByte and 0x7f] != WHITE_SPACE_ENC) {
                    throw java.io.IOException("Invalid character in Base64 data.")
                }   // end else: not white space either
            }// Else, Decoding
            // end else: decoding
        }   // end write


        /**
         * Calls [.write] repeatedly until <var>len</var>
         * bytes are written.
         *
         * @param theBytes array from which to read bytes
         * @param off offset for array
         * @param len max number of bytes to read into array
         * @since 1.3
         */
        @Throws(java.io.IOException::class)
        override fun write(theBytes: ByteArray, off: Int, len: Int) {
            // Encoding suspended?
            if (suspendEncoding) {
                this.out.write(theBytes, off, len)
                return
            }   // end if: supsended

            for (i in 0 until len) {
                write(theBytes[off + i].toInt())
            }   // end for: each byte written

        }   // end write


        /**
         * Method added by PHIL. [Thanks, PHIL. -Rob]
         * This pads the buffer without closing the stream.
         * @throws java.io.IOException  if there's an error.
         */
        @Throws(java.io.IOException::class)
        fun flushBase64() {
            if (position > 0) {
                if (encode) {
                    out.write(encode3to4(b4, buffer, position, options))
                    position = 0
                }   // end if: encoding
                else {
                    throw java.io.IOException("Base64 input not properly padded.")
                }   // end else: decoding
            }   // end if: buffer partially full

        }   // end flush


        /**
         * Flushes and closes (I think, in the superclass) the stream.
         *
         * @since 1.3
         */
        @Throws(java.io.IOException::class)
        override fun close() {
            // 1. Ensure that pending characters are written
            flushBase64()

            // 2. Actually close the stream
            // Base class both flushes and closes.
            super.close()

            buffer = null
            out = null
        }   // end close


        /**
         * Suspends encoding of the stream.
         * May be helpful if you need to embed a piece of
         * base64-encoded data in a stream.
         *
         * @throws java.io.IOException  if there's an error flushing
         * @since 1.5.1
         */
        @Throws(java.io.IOException::class)
        fun suspendEncoding() {
            flushBase64()
            this.suspendEncoding = true
        }   // end suspendEncoding


        /**
         * Resumes encoding of the stream.
         * May be helpful if you need to embed a piece of
         * base64-encoded data in a stream.
         *
         * @since 1.5.1
         */
        fun resumeEncoding() {
            this.suspendEncoding = false
        }   // end resumeEncoding


    }
    /**
     * Constructs a [Base64.OutputStream] in ENCODE mode.
     *
     * @param out the <tt>java.io.OutputStream</tt> to which data will be written.
     * @since 1.3
     */// end constructor
    // end inner class OutputStream


}
/** Defeats instantiation.  */
/**
 * Serializes an object and returns the Base64-encoded
 * version of that serialized object.
 *
 *
 * As of v 2.3, if the object
 * cannot be serialized or there is another error,
 * the method will throw an java.io.IOException. **This is new to v2.3!**
 * In earlier versions, it just returned a null value, but
 * in retrospect that's a pretty poor way to handle it.
 *
 * The object is not GZip-compressed before being encoded.
 *
 * @param serializableObject The object to encode
 * @return The Base64-encoded object
 * @throws java.io.IOException if there is an error
 * @throws NullPointerException if serializedObject is null
 * @since 1.4
 */// end encodeObject
/**
 * Decodes data from Base64 notation, automatically
 * detecting gzip-compressed data and decompressing it.
 *
 * @param s the string to decode
 * @return the decoded data
 * @throws java.io.IOException If there is a problem
 * @since 1.4
 */
/**
 * Attempts to decode Base64 data and deserialize a Java
 * Object within. Returns <tt>null</tt> if there was an error.
 *
 * @param encodedObject The Base64 data to decode
 * @return The decoded and deserialized object
 * @throws NullPointerException if encodedObject is null
 * @throws java.io.IOException if there is a general error
 * @throws ClassNotFoundException if the decoded object is of a
 * class that cannot be found by the JVM
 * @since 1.5
 */   // end class Base64
