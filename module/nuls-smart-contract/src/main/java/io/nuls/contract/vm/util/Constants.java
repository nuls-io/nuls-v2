/*
 * MIT License
 *
 * Copyright (c) 2017-2019 nuls.io
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */
package io.nuls.contract.vm.util;

public class Constants {

    public static final String DOLLAR = "$";

    public static final String CLASS_SEPARATOR = "/";

    public static final String CLASS_SUFFIX = ".class";

    public static final String CLINIT_NAME = "<clinit>";

    public static final String CLINIT_DESC = "()V";

    public static final String CONSTRUCTOR_NAME = "<init>";

    public static final String ARRAY_START = "[";

    public static final String ARRAY_PREFIX = "[L";

    public static final String ARRAY_SUFFIX = ";";

    public static final String OBJECT_CLASS_NAME = "java/lang/Object";

    public static final String OBJECT_CLASS_DESC = "Ljava/lang/Object;";

    public static final String TO_STRING_METHOD_NAME = "toString";

    public static final String TO_STRING_METHOD_DESC = "()Ljava/lang/String;";

    public static final String CLONE_METHOD_NAME = "clone";

    public static final String CLONE_METHOD_DESC = "()Ljava/lang/Object;";

    public static final String HASH = "hash";

    public static final String VALUE = "value";

    public static final String CHAR_CONSTRUCTOR_DESC = "(C)V";

    public static final String BYTES_CONSTRUCTOR_DESC = "([B)V";

    public static final String CONSTRUCTOR_DESC = "()V";

    public static final String CONSTRUCTOR_STRING_DESC = "(Ljava/lang/String;)V";

    public static final String CLASS_NAME = "java/lang/Class";

    public static final String CLASS_DESC = "Ljava/lang/Class;";

    public static final String ARRAYLIST_ADD_METHOD_NAME = "add";

    public static final String ARRAYLIST_ADD_METHOD_DESC = "(Ljava/lang/Object;)Z";

    public static final String COLLECTION_TOARRAY_METHOD_NAME = "toArray";

    public static final String COLLECTION_TOARRAY_METHOD_DESC = "()[Ljava/lang/Object;";

    public static final String MAP_ENTRYSET_METHOD_NAME = "entrySet";

    public static final String MAP_ENTRYSET_METHOD_DESC = null;

    public static final String MAP_ENTRY_KEY_METHOD_NAME = "getKey";

    public static final String MAP_ENTRY_KEY_METHOD_DESC = null;

    public static final String MAP_ENTRY_VALUE_METHOD_NAME = "getValue";

    public static final String MAP_ENTRY_VALUE_METHOD_DESC = null;

    public static final String SIZE = "size";

    public static final String SIZE_DESC = "()I";

    public static final int LIST_MAX_CAPACITY = 71140;
    public static final int MAP_MAX_CAPACITY = 65536;
    public static final int MAP_MIN_TRIGGER_RESIZE_CAPACITY = 3072;

}
