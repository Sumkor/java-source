package com.sumkor.proxy.proxy;

import com.sumkor.proxy.entity.Landlord;

/**
 * 静态代理
 *
 * @author Sumkor
 * @since 2021/6/22
 */
public class LandlordProxy implements Landlord {

    private final Landlord landlord;

    public LandlordProxy(Landlord landlord) {
        this.landlord = landlord;
    }

    /**
     * 中介代理老板，出租行为
     */
    @Override
    public boolean rent() {
        beforeRent();
        if (landlord.rent()) {
            afterRent();
            return true;
        }
        return false;
    }

    private void afterRent() {
        System.out.println("中介收取佣金");
    }

    private void beforeRent() {
        System.out.println("中介带人看房子");
    }
}
