package telomemore.java;

/*
 * Copyright (c) 2014. Real Time Genomics Limited.
 *
 * Use of this source code is bound by the Real Time Genomics Limited Software Licence Agreement
 * for Academic Non-commercial Research Purposes only.
 *
 * If you did not receive a license accompanying this file, a copy must first be obtained by email
 * from support@realtimegenomics.com.  On downloading, using and/or continuing to use this source
 * code you accept the terms of that license agreement and any amendments to those terms that may
 * be made from time to time by Real Time Genomics Limited.
 */
public class SeqUtil {
	
	
	/**
     * Take reverse complement of a string.
     * @param seq to be reverse complemented.
     * @return the reversed sequence.
     */
    public static String reverseComplement(final String seq) {
        final StringBuilder sb = new StringBuilder();
        for (int i = seq.length() - 1; i >= 0; i--) {
            final char c = seq.charAt(i);
            switch (c) {
            case 'a':
                sb.append('t');
                break;
            case 'A':
                sb.append('T');
                break;
            case 'c':
                sb.append('g');
                break;
            case 'C':
                sb.append('G');
                break;
            case 'g':
                sb.append('c');
                break;
            case 'G':
                sb.append('C');
                break;
            case 't':
                sb.append('a');
                break;
            case 'T':
                sb.append('A');
                break;
            case '-':
                break;
            default: // n
                sb.append(c);
                break;
            }
        }
        return sb.toString();
    }

    /**
     * Transform a human-readable DNA sequence into the reverse complemented version, uppercase.
     * @param src Eg. {'a','c','g','t','n'} will become {'N', 'A', 'C', 'G', 'T'}.
     * @param dest destination byte array
     * @param length length to convert
     */
    public static void reverseComplement(byte[] src, byte[] dest, int length) {
        for (int k = 0; k < length; k++) {
            switch (src[k]) {
            case (byte) 'a':
            case (byte) 'A':
                dest[length - 1 - k] = 'T';
                break;
            case (byte) 'c':
            case (byte) 'C':
                dest[length - 1 - k] = 'G';
                break;
            case (byte) 'g':
            case (byte) 'G':
                dest[length - 1 - k] = 'C';
                break;
            case (byte) 't':
            case (byte) 'T':
                dest[length - 1 - k] = 'A';
                break;
            default:
                dest[length - 1 - k] = 'N';
                break;
            }
        }
    }
}