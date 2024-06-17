package io.nuls.account;

/**
 * @author EdwardChan
 *
 * Oct.8th 2018
 *
 * Initialize the Service before test
 *
 * Unit testing needs to run automatically and is not dependent on other processes, so subsequent attempts will be made to manually start itWsKernal,AcccountBootStrapPut it here for unified initialization
 *
 *
 * **/
public class ServiceInitializer {

    public static boolean isInitialized = Boolean.FALSE;

    public static void initialize() throws Exception {
        if (!isInitialized) {
            //NoUse.mockKernel();
//            AccountBootstrap.main(null);
            isInitialized = Boolean.TRUE;
        }
    }
}
