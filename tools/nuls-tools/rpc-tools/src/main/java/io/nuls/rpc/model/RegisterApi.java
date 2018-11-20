package io.nuls.rpc.model;

import java.util.List;

/**
 * @author tangyi
 * @date 2018/11/19
 * @description
 */
public class RegisterApi {
    private List<CmdDetail> apiMethods;
    private List<String> serviceSupportedAPIVersions;
    private String serviceDomain;
    private String serviceName;
    private String serviceRole;
    private String serviceVersion;
    private String abbr;
    private String name;
    private String address;
    private int port;

    public List<CmdDetail> getApiMethods() {
        return apiMethods;
    }

    public void setApiMethods(List<CmdDetail> apiMethods) {
        this.apiMethods = apiMethods;
    }

    public List<String> getServiceSupportedAPIVersions() {
        return serviceSupportedAPIVersions;
    }

    public void setServiceSupportedAPIVersions(List<String> serviceSupportedAPIVersions) {
        this.serviceSupportedAPIVersions = serviceSupportedAPIVersions;
    }

    public String getServiceDomain() {
        return serviceDomain;
    }

    public void setServiceDomain(String serviceDomain) {
        this.serviceDomain = serviceDomain;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getServiceRole() {
        return serviceRole;
    }

    public void setServiceRole(String serviceRole) {
        this.serviceRole = serviceRole;
    }

    public String getServiceVersion() {
        return serviceVersion;
    }

    public void setServiceVersion(String serviceVersion) {
        this.serviceVersion = serviceVersion;
    }

    public String getAbbr() {
        return abbr;
    }

    public void setAbbr(String abbr) {
        this.abbr = abbr;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
