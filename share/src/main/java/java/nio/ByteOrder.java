/*
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */

package java.nio;


/**
 * A typesafe enumeration for byte orders.
 *
 * @author Mark Reinhold
 * @author JSR-51 Expert Group
 * @since 1.4
 */

public final class ByteOrder {

    private String name;

    private ByteOrder(String name) {
        this.name = name;
    }

    /**
     * Constant denoting big-endian byte order.  In this order, the bytes of a  // 大端模式：高位字节排放在内存的低地址端，低位字节排放在内存的高地址端
     * multibyte value are ordered from most significant to least significant.  // 比如数字 0x12345678 在内存中的表示形式： 低地址 ---> 高地址  0x12  |  0x34  |  0x56  |  0x78
     */
    public static final ByteOrder BIG_ENDIAN
        = new ByteOrder("BIG_ENDIAN");

    /**
     * Constant denoting little-endian byte order.  In this order, the bytes of // 小端模式：低位字节排放在内存的低地址端，高位字节排放在内存的高地址端
     * a multibyte value are ordered from least significant to most             // 比如数字 0x12345678 在内存中的表示形式： 低地址 ---> 高地址  0x78  |  0x56  |  0x34  |  0x12
     * significant.
     */
    public static final ByteOrder LITTLE_ENDIAN
        = new ByteOrder("LITTLE_ENDIAN");

    /**
     * Retrieves the native byte order of the underlying platform.
     *
     * <p> This method is defined so that performance-sensitive Java code can
     * allocate direct buffers with the same byte order as the hardware.
     * Native code libraries are often more efficient when such buffers are
     * used.  </p>
     *
     * @return  The native byte order of the hardware upon which this Java
     *          virtual machine is running
     */
    public static ByteOrder nativeOrder() {
        return Bits.byteOrder();
    }

    /**
     * Constructs a string describing this object.
     *
     * <p> This method returns the string <tt>"BIG_ENDIAN"</tt> for {@link
     * #BIG_ENDIAN} and <tt>"LITTLE_ENDIAN"</tt> for {@link #LITTLE_ENDIAN}.
     * </p>
     *
     * @return  The specified string
     */
    public String toString() {
        return name;
    }

}
