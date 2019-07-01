package io.nuls.core.rpc.util;

import io.nuls.core.basic.Result;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.core.annotation.Value;
import io.nuls.core.core.ioc.SpringLiteContext;
import io.nuls.core.log.Log;
import io.nuls.core.rpc.cmd.BaseCmd;
import io.nuls.core.rpc.model.*;
import net.steppschuh.markdowngenerator.table.Table;
import net.steppschuh.markdowngenerator.text.Text;
import net.steppschuh.markdowngenerator.text.heading.Heading;

import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author: zhoulijun
 * @Time: 2019-06-19 14:26
 * @Description: 生成rpc接口文档
 */
public class DocTool {

    static Set<String> exclusion = Set.of("io.nuls.base.protocol.cmd");

    static Set<Class> baseType = new HashSet<>();

    static {
        baseType.add(Integer.class);
        baseType.add(int.class);
        baseType.add(Long.class);
        baseType.add(long.class);
        baseType.add(Float.class);
        baseType.add(float.class);
        baseType.add(Double.class);
        baseType.add(double.class);
        baseType.add(Character.class);
        baseType.add(char.class);
        baseType.add(Short.class);
        baseType.add(short.class);
        baseType.add(Boolean.class);
        baseType.add(boolean.class);
        baseType.add(Byte.class);
        baseType.add(byte.class);
        baseType.add(String.class);
    }

    public static class ResultDes {
        String name;
        String des;
        String type;
        List<ResultDes> list;
        boolean canNull;
    }

    public static class CmdDes {
        String cmdName;
        String des;
        String scope;
        double version;
        List<ResultDes> parameters;
        List<ResultDes> result;
    }

    public static void main(String[] args) throws IOException {
        SpringLiteContext.init("io.nuls");
        Gen gen = SpringLiteContext.getBean(Gen.class);
        gen.start();
    }


    @Component
    public static class Gen {

        private static final String NA = "N/A";

        @Value("APP_NAME")
        private String appName;

        public void start() throws IOException {
            List<BaseCmd> cmdList = SpringLiteContext.getBeanList(BaseCmd.class);
            List<CmdDes> cmdDesList = new ArrayList<>();
            cmdList.forEach(cmd -> {
                Class<?> clazs = cmd.getClass();
                if(exclusion.contains(clazs.getPackageName())){
                    Log.info("跳过{}生成文档，所在包{}在排除范围",clazs.getSimpleName(),clazs.getPackageName());
                    return ;
                }
                Method[] methods = clazs.getMethods();
                Arrays.stream(methods).forEach(method -> {
                    Annotation annotation = method.getAnnotation(CmdAnnotation.class);
                    if (annotation == null) {
                        return;
                    }
                    CmdAnnotation cmdAnnotation = (CmdAnnotation) annotation;
                    CmdDes cmdDes = new CmdDes();
                    cmdDes.cmdName = cmdAnnotation.cmd().replaceAll("_", "\\\\_");
                    cmdDes.des = cmdAnnotation.description();
                    cmdDes.scope = cmdAnnotation.scope();
                    cmdDes.version = cmdAnnotation.version();
//                    annotation = method.getAnnotation(Parameters.class);
//                    List<ResultDes> parameters = new ArrayList<>();
//
//                    if (annotation != null) {
//                        cmdDes.parameters = Arrays.asList(((Parameters) annotation).value()).forEach(parameter -> {
//                            ResultDes res = new ResultDes();
//                            res.type = parameter.parameterType();
//                            res.name = parameter.parameterName();
//                            res.des = parameter.parameterDes();
//                            if(parameter.requestType().value() != String.class ){
//                                res.list = buildResultDes(parameter.requestType(),res.des);
//                            }
//                            parameters.add(res);
//                        });
//                    } else {
//                        Parameter[] parameters = method.getAnnotationsByType(Parameter.class);
//                        cmdDes.parameters = Arrays.asList(parameters);
//                    }
                    cmdDes.parameters = buildParam(method);
                    annotation = method.getAnnotation(ResponseData.class);
                    if (annotation != null) {
                        ResponseData responseData = (ResponseData) annotation;
                        cmdDes.result = buildResultDes(responseData.responseType(), responseData.description(),responseData.name());
                    }
                    cmdDesList.add(cmdDes);
                });
            });
            System.out.println("生成文档成功："+createMarketDownDoc(cmdDesList,"./readme.md"));
            System.exit(0);
        }

        public List<ResultDes> buildParam(Method method){
            Annotation annotation = method.getAnnotation(Parameters.class);
            List<Parameter> parameters;
            if (annotation != null) {
                parameters = Arrays.asList(((Parameters) annotation).value());
            } else {
                Parameter[] parameterAry = method.getAnnotationsByType(Parameter.class);
                parameters = Arrays.asList(parameterAry);
            }
            List<ResultDes> param = new ArrayList<>();
            parameters.stream().forEach(parameter -> {
                ResultDes res = new ResultDes();
                res.type = parameter.parameterType();
                res.name = parameter.parameterName();
                res.des = parameter.parameterDes();
                res.canNull = parameter.canNull();
                param.addAll(buildResultDes(parameter.requestType(),res.des,res.name));
            });
            return param;
        }

        public List<ResultDes> buildResultDes(TypeDescriptor typeDescriptor, String des,String name) {
            ResultDes resultDes = new ResultDes();
            if (typeDescriptor.value() == Void.class){
                resultDes.type = "void";
                resultDes.des = des;
                resultDes.name = NA;
                return List.of(resultDes);
            }
            if (baseType.contains(typeDescriptor.value())) {
                resultDes.des = des;
                resultDes.name = name;
                resultDes.type = typeDescriptor.value().getSimpleName().toLowerCase();
                return List.of(resultDes);
            } else if (typeDescriptor.value() == Map.class) {
                return mapToResultDes(typeDescriptor);
            } else if (List.class == typeDescriptor.value()) {
                if(baseType.contains(typeDescriptor.collectionElement())){
                    resultDes.type = "list&lt;" + typeDescriptor.collectionElement().getSimpleName() + ">";
                    resultDes.des = des;
                    resultDes.name = name;
                    return List.of(resultDes);
                }
                if(typeDescriptor.collectionElement() == Map.class){
                    return mapToResultDes(typeDescriptor);
                }else{
                    return classToResultDes(typeDescriptor.collectionElement());
                }
            } else if (Object[].class == typeDescriptor.value()){
                resultDes.des = des;
                resultDes.name = name;
                resultDes.type = "object[]";
                return List.of(resultDes);
            } else {
                return classToResultDes(typeDescriptor.value());
            }
        }

        public List<ResultDes> mapToResultDes(TypeDescriptor typeDescriptor){
            Key[] keys = typeDescriptor.mapKeys();
            List<ResultDes> res = new ArrayList<>();
            Arrays.stream(keys).forEach(key -> {
                ResultDes rd = new ResultDes();
                if (baseType.contains(key.valueType())) {
                    rd.type = key.valueType().getSimpleName().toLowerCase();
                    rd.name = key.name();
                    rd.des = key.description();
                } else if (List.class == key.valueType()) {
                    rd.name = key.name();
                    rd.des = key.description();
                    if(baseType.contains(key.valueElement()) || key.valueElement() == Map.class){
                        rd.type = "list&lt;" + key.valueElement().getSimpleName() + ">";
                    }else{
                        rd.list = classToResultDes(key.valueElement());
                        rd.type = "list&lt;object>";
                    }
                } else if (Map.class == key.valueType()) {
                    rd.type = "map";
                    rd.name = key.name();
                    rd.des = key.description();
                } else {
                    Annotation annotation = key.valueType().getAnnotation(ApiModel.class);
                    if(annotation == null){
                        rd.type = key.valueType().getSimpleName().toLowerCase();
                        rd.name = key.name();
                        rd.des = key.description();
                    }else{
                        rd.type = "object";
                        rd.name = key.name();
                        rd.des = key.description();
                        rd.list = classToResultDes(key.valueType());
                    }

                }
                res.add(rd);
            });
            return res;
        }

        public List<ResultDes> classToResultDes(Class<?> clzs) {
            Annotation annotation = clzs.getAnnotation(ApiModel.class);
            if (annotation == null) {
                throw new IllegalArgumentException("返回值是复杂对象时必须声明ApiModule注解 + " + clzs.getSimpleName());
            }
            List<ResultDes> filedList = new ArrayList<>();
            Field[] fileds = clzs.getDeclaredFields();
            Arrays.stream(fileds).forEach(filed -> {
                Annotation ann = filed.getAnnotation(ApiModelProperty.class);
                ApiModelProperty apiModelProperty = (ApiModelProperty) ann;
                if(apiModelProperty == null){
                    Log.warn("发现未配置ApiModelProperty注解的filed:{}",clzs.getSimpleName() + "#" + filed.getName());
                    return ;
                }
                ResultDes filedDes = new ResultDes();
                filedDes.des = apiModelProperty.description();
                filedDes.name = filed.getName();
                if(apiModelProperty.type().value() != Void.class ){
                    if(baseType.contains(apiModelProperty.type().collectionElement())){
                        filedDes.type = "list&lt;" + apiModelProperty.type().collectionElement().getSimpleName() + ">";
                    }else{
                        filedDes.list = buildResultDes(apiModelProperty.type(),filedDes.des,filedDes.name);
                        if(apiModelProperty.type().value() == List.class){
                            filedDes.type = "list&lt;object>";
                        }else if (apiModelProperty.type().value() == Map.class){
                            filedDes.type = "map";
                        }
                    }
                }else{
                    if(baseType.contains(filed.getType())){
                        filedDes.type = filed.getType().getSimpleName().toLowerCase();
                    }else{
                        Annotation filedAnn = filed.getType().getAnnotation(ApiModel.class);
                        if(filedAnn == null){
                            Log.warn("发现ApiModelProperty注解的filed类型为复杂对象，但对象并未注解ApiModel，filed:{}",clzs.getSimpleName() + "#" + filed.getName());
                            filedDes.type = filed.getType().getSimpleName().toLowerCase();
                        }else{
                            if(clzs == filed.getType()){
                                Log.warn("发现循环引用：{}",clzs);
                                filedDes.type = "object&lt;" + clzs.getSimpleName().toLowerCase() + ">";
                            }else{
                                filedDes.list =  classToResultDes(filed.getType());
                                filedDes.type = "object";
                            }
                        }
                    }
                }
                filedList.add(filedDes);
            });
            return filedList;
        }

        public String createMarketDownDoc(List<CmdDes> cmdDesList, String tempFile) throws IOException {
            File file = new File(tempFile);
            if (!file.exists()) {
                throw new RuntimeException("模板文件不存在");
            }

            File mdFile = new File(file.getParentFile().getAbsolutePath() + File.separator + "documents" + File.separator + appName + ".md");
            if(mdFile.exists()){
                mdFile.delete();
            }
            mdFile.createNewFile();
            try(OutputStream outputStream = new FileOutputStream(mdFile);InputStream inputStream = new FileInputStream(file)){
                inputStream.transferTo(outputStream);
                outputStream.flush();
            }
            try(BufferedWriter writer = new BufferedWriter(new FileWriter(mdFile,true))){
                writer.newLine();
                cmdDesList.forEach(cmd->{
                    try {
                        writer.write(new Heading(cmd.cmdName, 1).toString());
                        writer.newLine();
                        writer.write(new Heading("scope:" + cmd.scope, 3).toString());
                        writer.newLine();
                        writer.write(new Heading("version:" + cmd.version, 3).toString());
                        writer.newLine();
                        writer.write(new Text(cmd.des).toString());
                        writer.newLine();
                        buildParam(writer,cmd.parameters);
                        buildResult(writer,cmd.result);
                        writer.newLine();
                        writer.newLine();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }
            return mdFile.getAbsolutePath();
        }

        private void buildResult(BufferedWriter writer, List<ResultDes> result) throws IOException {
            writer.newLine();
            writer.newLine();
            writer.write(new Heading("返回值",2).toString());
            if(result == null){
                writer.newLine();
                writer.write("无返回值");
                return;
            }
            Table.Builder tableBuilder = new Table.Builder()
                    .withAlignments(Table.ALIGN_LEFT, Table.ALIGN_CENTER,Table.ALIGN_LEFT)
                    .addRow("字段名", "字段类型","参数描述");
            buildResult(tableBuilder,result,0);
            writer.newLine();
            writer.write(tableBuilder.build().toString());
        }

        private void buildResult(Table.Builder tableBuilder, List<ResultDes> result,int depth) {
            result.forEach(r->{
                tableBuilder.addRow("&nbsp;".repeat(depth*8) + r.name,r.type.toLowerCase(),r.des);
                if(r.list != null){
                    buildResult(tableBuilder,r.list,depth+1);
                }
            });
        }

        private void buildParam(BufferedWriter writer, List<ResultDes> parameters) throws IOException {
            writer.newLine();
            writer.write(new Heading("参数列表",2).toString());
            if(parameters == null || parameters.isEmpty()){
                writer.newLine();
                writer.write("无参数");
                return;
            }
            Table.Builder tableBuilder = new Table.Builder()
                    .withAlignments(Table.ALIGN_LEFT, Table.ALIGN_CENTER,Table.ALIGN_LEFT,Table.ALIGN_CENTER)
                    .addRow("参数名", "参数类型","参数描述","是否非空");
//            parameters.forEach(p->{
//                tableBuilder.addRow(p.parameterName(),p.parameterType().toLowerCase(),p.parameterDes(),!p.canNull() ? "是" : "否");
//            });
            buildParam(tableBuilder,parameters,0);
            writer.newLine();
            writer.write(tableBuilder.build().toString());
        }

        private void buildParam(Table.Builder tableBuilder, List<ResultDes> result,int depth) {
            result.forEach(r->{
                tableBuilder.addRow("&nbsp;".repeat(depth*8) + r.name,r.type.toLowerCase(),r.des,!r.canNull ? "是" : "否");
                if(r.list != null){
                    buildParam(tableBuilder,r.list,depth+1);
                }
            });
        }
    }

}
