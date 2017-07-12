package com.xtt.log.trace;

import org.junit.Assert;
import org.junit.Test;

/**
 * systoon-parent
 *
 * @author liqiang
 * @date 2016-12-23
 */
public class TraceTest {
    @Test
    public void urlMatch(){
        String url="/open/querySwitchInfo";
        Assert.assertTrue(url.matches("^/(user|open|org|inner)/.*"));
    }

    @Test
    public void gsonEncrypt(){

    }
}
