//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//
package com.leedoo.printerdemo;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class HexUtil {
    public HexUtil() {
    }

    public static void AddArrayToList(List<Byte> list, byte[] bytes) {
        byte[] var2 = bytes;
        int var3 = bytes.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            byte _byte = var2[var4];
            list.add(_byte);
        }

    }

    public static byte[] GetByteArrayWithList(List<Byte> byteList) {
        byte[] bytes = new byte[byteList.size()];
        int index = 0;

        byte b;
        for(Iterator var3 = byteList.iterator(); var3.hasNext(); bytes[index++] = b) {
            b = (Byte)var3.next();
        }

        return bytes;
    }

    public static String GetHexString(byte[] input) {
        List<String> hexStringList = new ArrayList();
        byte[] var2 = input;
        int var3 = input.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            byte b = var2[var4];
            hexStringList.add(GetHexString(b));
        }

        return hexStringList.toString();
    }

    public static String GetHexString(byte input) {
        int i = input & 255;
        return i < 16 ? "0x0" + Integer.toHexString(i).toUpperCase() : "0x" + Integer.toHexString(i).toUpperCase();
    }

    public static byte[] GetBytesFormHexString(String hexString) {
        int bytesLength = hexString.length() / 2;
        if (hexString.length() % 2 != 0) {
            return null;
        } else {
            byte[] bytes = new byte[bytesLength];

            for(int index = 0; index < hexString.length(); index += 2) {
                int higher = GetByteFromHexChar(hexString.charAt(index)) << 4;
                int lower = GetByteFromHexChar(hexString.charAt(index + 1));
                byte b = (byte)(higher + lower);
                bytes[index / 2] = b;
            }

            return bytes;
        }
    }

    public static byte GetByteFromHexChar(char hexChar) {
        hexChar = Character.toUpperCase(hexChar);
        String allHexStrings = "0123456789ABCDEF";
        int index = allHexStrings.indexOf(hexChar);
        return (byte)index;
    }

    public static Byte[] GetOriginalByteArray(byte[] arr) {
        int length = arr.length;
        Byte[] _arr = new Byte[length];

        for(int index = 0; index < length; ++index) {
            _arr[index] = arr[index];
        }

        return _arr;
    }

    public static byte[] GetPackagedByteArray(Byte[] arr) {
        int length = arr.length;
        byte[] _arr = new byte[length];

        for(int index = 0; index < length; ++index) {
            _arr[index] = arr[index];
        }

        return _arr;
    }
}

