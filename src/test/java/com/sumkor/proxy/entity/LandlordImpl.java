package com.sumkor.proxy.entity;

/**
 * 原始对象
 *
 * @author Sumkor
 * @since 2021/6/22
 */
public class LandlordImpl implements Landlord {

    @Override
    public boolean rent() {
        System.out.println("房东的房子出租啦");
        return true;
    }
}
