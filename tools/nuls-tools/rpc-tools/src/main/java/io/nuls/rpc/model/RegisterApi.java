/*
 * MIT License
 *
 * Copyright (c) 2017-2018 nuls.io
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */
package io.nuls.rpc.model;

import lombok.*;

import java.util.List;
import java.util.Map;

/**
 * 一个模块对外提供的所有方法的合集
 * A collection of all methods provided by a module
 *
 * @author tangyi
 * @date 2018/11/19
 * @description
 */
@ToString
@NoArgsConstructor
public class RegisterApi {
    @Getter
    @Setter
    private List<CmdDetail> apiMethods;
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
