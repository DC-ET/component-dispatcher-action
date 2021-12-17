package com.zoi7.component.dispatcher.action.annotation;

/**
 *
 * 请求路由 由 module 与 cmd 产生一个路由
 * @author yjy
 * 2018-05-28 11:24
 */
public class ReqNames {

    private String module; // 模块 类似于 Controller类上的 RequestMapping值
    private String cmd; // 命令 类似于 方法上的 RequestMapping值

    public ReqNames(String module, String cmd) {
        this.module = module;
        this.cmd = cmd;
    }

    // 重写equals, 保证只要 module 与 cmd 相同, Map.get(key) 就能找到指定val
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ReqNames) {
            ReqNames reqNames = (ReqNames) obj;
            return this.module.equals(reqNames.getModule()) && this.cmd.equals(reqNames.getCmd());
        }
        return super.equals(obj);
    }

    // 重写hashCode, 保证只要 module 与 cmd 相同, hashcode值就一样
    @Override
    public int hashCode() {
        return this.module.hashCode() + this.cmd.hashCode();
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public String getCmd() {
        return cmd;
    }

    public void setCmd(String cmd) {
        this.cmd = cmd;
    }

    @Override
    public String toString() {
        return "ReqNames{" +
                "module='" + module + '\'' +
                ", cmd='" + cmd + '\'' +
                '}';
    }
}
