package io.nuls.cmd.client;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-28 15:24
 * @Description:
 *     参数异常
 */
public class ParameterException extends RuntimeException{

    private ParameterException(String msg){
        super(msg);
    }

    public static ParameterException throwParameterException() throws ParameterException{
        throw new ParameterException("parameter error");
    }

    public static ParameterException throwParameterException(String msg) throws ParameterException{
        throw  new ParameterException(msg);
    }

    public static ParameterException throwParameterExceptionForFieldName(String fieldName) throws ParameterException{
        throw  new ParameterException(fieldName + " format error ");
    }

}
