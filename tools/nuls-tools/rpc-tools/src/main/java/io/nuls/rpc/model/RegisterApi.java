package io.nuls.rpc.model;

import java.util.List;

/**
 * @author tangyi
 * @date 2018/11/19
 * @description
 */
public class RegisterApi {
    private List<CmdDetail> methods;
    private List<String> serviceSupportedAPIVersions;
    private String serviceDomain;
    private String serviceName;
    private String serviceRole;
    private String serviceVersion;

    public List<CmdDetail> getMethods() {
        return methods;
    }

    public void setMethods(List<CmdDetail> methods) {
        this.methods = methods;
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
}
