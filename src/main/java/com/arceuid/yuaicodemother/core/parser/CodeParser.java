package com.arceuid.yuaicodemother.core.parser;

/**
 * 代码解析器的接口
 */
public interface CodeParser<T> {
    /**
     * 解析代码
     *
     * @param codeContent 代码
     * @return 解析后的结果对象
     */
    T parseCode(String codeContent);
}
