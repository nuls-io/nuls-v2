package io.nuls.poc.model.bo.config;

/**
 * 配置信息类
 * Configuration information class
 *
 * @author tag
 * 2018/11/8
 * */
public class ConfigItem {
    private String key;
    private Object value;
    private boolean readOnly;

    public  ConfigItem(){

    }
    public ConfigItem( Object value, boolean readOnly) {
        this.value = value;
        this.readOnly = readOnly;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }
}
