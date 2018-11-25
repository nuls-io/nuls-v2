package io.nuls.rpc.model;

import lombok.*;

import java.util.List;
import java.util.Map;

/**
 * @author tangyi
 * @date 2018/11/19
 * @description
 */
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class RegisterApi {
    @Getter
    @Setter
    private List<CmdDetail> apiMethods;
    @Getter
    @Setter
    private String[] supportedAPIVersions;
    @Getter
    @Setter
    private Map<String, String> dependencies;
    @Getter
    @Setter
    private Map<String, String> connectionInformation;
    @Getter
    @Setter
    private String moduleDomain;
    @Getter
    @Setter
    private Map<String, String[]> moduleRoles;
    @Getter
    @Setter
    private String moduleVersion;
    @Getter
    @Setter
    private String moduleAbbreviation;
    @Getter
    @Setter
    private String moduleName;
}
