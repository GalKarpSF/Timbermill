package com.datorama.timbermill;

import org.junit.BeforeClass;
import org.junit.Test;

public class TimberLogAdvancedOrphansLocalTest extends TimberLogAdvancedOrphansTest{

    @BeforeClass
    public static void init() {
        TimberLogLocalTest.init();
    }

      @Test
    public void testOrphanIncorrectOrder() {
        super.testOrphanIncorrectOrder();
    }

    @Test
    public void testOrphanWithAdoption(){
        super.testOrphanWithAdoption();
    }

    @Test
    public void testOutOfOrderComplexOrphanWithAdoption(){
        super.testOutOfOrderComplexOrphanWithAdoption();
    }

    @Test
    public void testInOrderComplexOrphanWithAdoption(){
        super.testInOrderComplexOrphanWithAdoption1();
    }
}
