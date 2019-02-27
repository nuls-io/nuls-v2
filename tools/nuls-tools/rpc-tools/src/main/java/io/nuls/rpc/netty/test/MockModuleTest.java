package io.nuls.rpc.netty.test;

/**
 * @author tag
 * */
public class MockModuleTest {
    public static void main(String[] args){
        try {
            KernelModule.mockKernel();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
