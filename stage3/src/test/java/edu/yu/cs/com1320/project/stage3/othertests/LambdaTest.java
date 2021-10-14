package edu.yu.cs.com1320.project.stage3.othertests;
import org.junit.*;

interface TestFunc {
    void testF(String x);
}

public class LambdaTest {
    public void testFunction(String x) {
        System.out.println("hello, " + x);
    }

    TestFunc[] tfArray = new TestFunc[3];

    TestFunc testf1 = (String x) -> {
        System.out.println("hey1");
    };
    TestFunc testf2 = (String x) -> {
        System.out.println("hey2");
    };
    TestFunc testf3 = (String x) -> {
        System.out.println("hey3");
    };

    // void aFunc(String x);

    @Before
    public void setup() {
        tfArray[0] = testf1;
        tfArray[1] = testf2;
        tfArray[2] = testf3;
    }

    @Test
    public void lTest() {
        // TestFunc lf = tfArray[0];
        for (int i = 0; i < tfArray.length; i++) {
            tfArray[i].testF("random string");
        }
        
    }
}