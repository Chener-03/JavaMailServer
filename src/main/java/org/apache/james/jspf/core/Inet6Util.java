/****************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one   *
 * or more contributor license agreements.  See the NOTICE file *
 * distributed with this work for additional information        *
 * regarding copyright ownership.  The ASF licenses this file   *
 * to you under the Apache License, Version 2.0 (the            *
 * "License"); you may not use this file except in compliance   *
 * with the License.  You may obtain a copy of the License at   *
 *                                                              *
 *   http://www.apache.org/licenses/LICENSE-2.0                 *
 *                                                              *
 * Unless required by applicable law or agreed to in writing,   *
 * software distributed under the License is distributed on an  *
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY       *
 * KIND, either express or implied.  See the License for the    *
 * specific language governing permissions and limitations      *
 * under the License.                                           *
 ****************************************************************/

package org.apache.james.jspf.core;

import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * Utility functions for IPV6 operations.
 * 
 * see Inet6Util from the Apache Harmony project
 * 
 * see org.apache.harmony.util.Inet6Util
 */
public class Inet6Util {
    
    private Inet6Util() {
        // make this class a an utility class non-instantiable
    }

    /**
     * Creates an byte[] based on an ipAddressString. No error handling is
     * performed here.
     */
    public static byte[] createByteArrayFromIPAddressString(
            String ipAddressString) {

        if (isValidIPV4Address(ipAddressString)) {
            StringTokenizer tokenizer = new StringTokenizer(ipAddressString,
                    ".");
            String token = "";
            int tempInt = 0;
            byte[] byteAddress = new byte[4];
            for (int i = 0; i < 4; i++) {
                token = tokenizer.nextToken();
                tempInt = Integer.parseInt(token);
                byteAddress[i] = (byte) tempInt;
            }

            return byteAddress;
        }

        if (ipAddressString.charAt(0) == '[') {
            ipAddressString = ipAddressString.substring(1, ipAddressString
                    .length() - 1);
        }

        StringTokenizer tokenizer = new StringTokenizer(ipAddressString, ":.",
                true);
        ArrayList hexStrings = new ArrayList();
        ArrayList decStrings = new ArrayList();
        String token = "";
        String prevToken = "";
        int doubleColonIndex = -1; // If a double colon exists, we need to
        // insert 0s.

        // Go through the tokens, including the seperators ':' and '.'
        // When we hit a : or . the previous token will be added to either
        // the hex list or decimal list. In the case where we hit a ::
        // we will save the index of the hexStrings so we can add zeros
        // in to fill out the string
        while (tokenizer.hasMoreTokens()) {
            prevToken = token;
            token = tokenizer.nextToken();

            if (token.equals(":")) {
                if (prevToken.equals(":")) {
                    doubleColonIndex = hexStrings.size();
                } else if (!prevToken.equals("")) {
                    hexStrings.add(prevToken);
                }
            } else if (token.equals(".")) {
                decStrings.add(prevToken);
            }
        }

        if (prevToken.equals(":")) {
            if (token.equals(":")) {
                doubleColonIndex = hexStrings.size();
            } else {
                hexStrings.add(token);
            }
        } else if (prevToken.equals(".")) {
            decStrings.add(token);
        }

        // figure out how many hexStrings we should have
        // also check if it is a IPv4 address
        int hexStringsLength = 8;

        // If we have an IPv4 address tagged on at the end, subtract
        // 4 bytes, or 2 hex words from the total
        if (decStrings.size() > 0) {
            hexStringsLength -= 2;
        }

        // if we hit a double Colon add the appropriate hex strings
        if (doubleColonIndex != -1) {
            int numberToInsert = hexStringsLength - hexStrings.size();
            for (int i = 0; i < numberToInsert; i++) {
                hexStrings.add(doubleColonIndex, "0");
            }
        }

        byte ipByteArray[] = new byte[16];

        // Finally convert these strings to bytes...
        for (int i = 0; i < hexStrings.size(); i++) {
            convertToBytes((String) hexStrings.get(i), ipByteArray, i * 2);
        }

        // Now if there are any decimal values, we know where they go...
        for (int i = 0; i < decStrings.size(); i++) {
            ipByteArray[i + 12] = (byte) (Integer.parseInt((String) decStrings
                    .get(i)) & 255);
        }

        // now check to see if this guy is actually and IPv4 address
        // an ipV4 address is ::FFFF:d.d.d.d
        boolean ipV4 = true;
        for (int i = 0; i < 10; i++) {
            if (ipByteArray[i] != 0) {
                ipV4 = false;
                break;
            }
        }

        if (ipByteArray[10] != -1 || ipByteArray[11] != -1) {
            ipV4 = false;
        }

        if (ipV4) {
            byte ipv4ByteArray[] = new byte[4];
            for (int i = 0; i < 4; i++) {
                ipv4ByteArray[i] = ipByteArray[i + 12];
            }
            return ipv4ByteArray;
        }

        return ipByteArray;

    }

    /** Converts a 4 character hex word into a 2 byte word equivalent */
    public static void convertToBytes(String hexWord, byte ipByteArray[],
            int byteIndex) {

        int hexWordLength = hexWord.length();
        int hexWordIndex = 0;
        ipByteArray[byteIndex] = 0;
        ipByteArray[byteIndex + 1] = 0;
        int charValue;

        // high order 4 bits of first byte
        if (hexWordLength > 3) {
            charValue = getIntValue(hexWord.charAt(hexWordIndex++));
            ipByteArray[byteIndex] = (byte) (ipByteArray[byteIndex] | (charValue << 4));
        }

        // low order 4 bits of the first byte
        if (hexWordLength > 2) {
            charValue = getIntValue(hexWord.charAt(hexWordIndex++));
            ipByteArray[byteIndex] = (byte) (ipByteArray[byteIndex] | charValue);
        }

        // high order 4 bits of second byte
        if (hexWordLength > 1) {
            charValue = getIntValue(hexWord.charAt(hexWordIndex++));
            ipByteArray[byteIndex + 1] = (byte) (ipByteArray[byteIndex + 1] | (charValue << 4));
        }

        // low order 4 bits of the first byte
        charValue = getIntValue(hexWord.charAt(hexWordIndex));
        ipByteArray[byteIndex + 1] = (byte) (ipByteArray[byteIndex + 1] | charValue & 15);
    }

    static int getIntValue(char c) {

        switch (c) {
        case '0':
            return 0;
        case '1':
            return 1;
        case '2':
            return 2;
        case '3':
            return 3;
        case '4':
            return 4;
        case '5':
            return 5;
        case '6':
            return 6;
        case '7':
            return 7;
        case '8':
            return 8;
        case '9':
            return 9;
        }

        c = Character.toLowerCase(c);
        switch (c) {
        case 'a':
            return 10;
        case 'b':
            return 11;
        case 'c':
            return 12;
        case 'd':
            return 13;
        case 'e':
            return 14;
        case 'f':
            return 15;
        }
        return 0;
    }

    public static boolean isValidIP6Address(String ipAddress) {
        int length = ipAddress.length();
        boolean doubleColon = false;
        int numberOfColons = 0;
        int numberOfPeriods = 0;
        int numberOfPercent = 0;
        String word = "";
        char c = 0;
        char prevChar = 0;
        int offset = 0; // offset for [] ip addresses

        if (length < 2)
            return false;

        for (int i = 0; i < length; i++) {
            prevChar = c;
            c = ipAddress.charAt(i);
            switch (c) {

            // case for an open bracket [x:x:x:...x]
            case '[':
                if (i != 0)
                    return false; // must be first character
                if (ipAddress.charAt(length - 1) != ']')
                    return false; // must have a close ]
                offset = 1;
                if (length < 4)
                    return false;
                break;

            // case for a closed bracket at end of IP [x:x:x:...x]
            case ']':
                if (i != length - 1)
                    return false; // must be last charcter
                if (ipAddress.charAt(0) != '[')
                    return false; // must have a open [
                break;

            // case for the last 32-bits represented as IPv4 x:x:x:x:x:x:d.d.d.d
            case '.':
                numberOfPeriods++;
                if (numberOfPeriods > 3)
                    return false;
                if (!isValidIP4Word(word))
                    return false;
                if (numberOfColons != 6 && !doubleColon)
                    return false;
                // a special case ::1:2:3:4:5:d.d.d.d allows 7 colons with an
                // IPv4 ending, otherwise 7 :'s is bad
                if (numberOfColons == 7 && ipAddress.charAt(0 + offset) != ':'
                        && ipAddress.charAt(1 + offset) != ':')
                    return false;
                word = "";
                break;

            case ':':
                // FIX "IP6 mechanism syntax #ip6-bad1"
                // An IPV6 address cannot start with a single ":".
                // Either it can starti with "::" or with a number.
                if (i == offset && (ipAddress.length() <= i || ipAddress.charAt(i+1) != ':')) {
                    return false;
                }
                // END FIX "IP6 mechanism syntax #ip6-bad1"
                numberOfColons++;
                if (numberOfColons > 7)
                    return false;
                if (numberOfPeriods > 0)
                    return false;
                if (prevChar == ':') {
                    if (doubleColon)
                        return false;
                    doubleColon = true;
                }
                word = "";
                break;
            case '%':
                if (numberOfColons == 0)
                    return false;
                numberOfPercent++;

                // validate that the stuff after the % is valid
                if ((i + 1) >= length) {
                    // in this case the percent is there but no number is
                    // available
                    return false;
                }
                try {
                    Integer.parseInt(ipAddress.substring(i + 1));
                } catch (NumberFormatException e) {
                    // right now we just support an integer after the % so if
                    // this is not
                    // what is there then return
                    return false;
                }
                break;

            default:
                if (numberOfPercent == 0) {
                    if (word.length() > 3)
                        return false;
                    if (!isValidHexChar(c))
                        return false;
                }
                word += c;
            }
        }

        // Check if we have an IPv4 ending
        if (numberOfPeriods > 0) {
            if (numberOfPeriods != 3 || !isValidIP4Word(word))
                return false;
        } else {
            // If we're at then end and we haven't had 7 colons then there is a
            // problem unless we encountered a doubleColon
            if (numberOfColons != 7 && !doubleColon) {
                return false;
            }

            // If we have an empty word at the end, it means we ended in either
            // a : or a .
            // If we did not end in :: then this is invalid
            if (numberOfPercent == 0) {
                if (word == "" && ipAddress.charAt(length - 1 - offset) == ':'
                        && ipAddress.charAt(length - 2 - offset) != ':') {
                    return false;
                }
            }
        }

        return true;
    }

    public static boolean isValidIP4Word(String word) {
        char c;
        if (word.length() < 1 || word.length() > 3)
            return false;
        for (int i = 0; i < word.length(); i++) {
            c = word.charAt(i);
            if (!(c >= '0' && c <= '9'))
                return false;
        }
        if (Integer.parseInt(word) > 255)
            return false;
        return true;
    }

    static boolean isValidHexChar(char c) {

        return (c >= '0' && c <= '9') || (c >= 'A' && c <= 'F')
                || (c >= 'a' && c <= 'f');
    }

    /**
     * Takes a string and parses it to see if it is a valid IPV4 address.
     * 
     * @return true, if the string represents an IPV4 address in dotted
     *         notation, false otherwise
     */
    public static boolean isValidIPV4Address(String value) {

        int periods = 0;
        int i = 0;
        int length = value.length();

        if (length > 15)
            return false;
        char c = 0;
        String word = "";
        for (i = 0; i < length; i++) {
            c = value.charAt(i);
            if (c == '.') {
                periods++;
                if (periods > 3)
                    return false;
                if (word == "")
                    return false;
                if (Integer.parseInt(word) > 255)
                    return false;
                word = "";
            } else if (!(Character.isDigit(c)))
                return false;
            else {
                if (word.length() > 2)
                    return false;
                word += c;
            }
        }

        if (word == "" || Integer.parseInt(word) > 255)
            return false;
        if (periods != 3)
            return false;
        return true;
    }

}
