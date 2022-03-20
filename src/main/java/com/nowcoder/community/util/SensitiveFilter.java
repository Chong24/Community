package com.nowcoder.community.util;

import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * 一个敏感词汇的过滤器，当出现了敏感词汇就会替代它。
 * 用到的数据结构是前缀树：
 */
@Component
public class SensitiveFilter {
    //需要打印日志信息的
    private static final Logger logger = LoggerFactory.getLogger(SensitiveFilter.class);
    //需要一个常量字符串，当出现敏感词汇的时候，用这个来替代它
    private static final String REPLACEMENT = "***";
    //我们是用前缀树来实现的敏感词汇过滤，所以我们需要一个树的根节点
    private TrieNode rootNode = new TrieNode();

    /**
     * 首先，敏感词汇都是先定好在一个文件中的，我们是去读取这个文件，获取到有那些敏感词汇，所以我们希望一调用这个方法，就能自动读取文件
     *
     * @PostConstruct 该注解被用来修饰一个非静态的void（）方法。被@PostConstruct修饰的方法会在服务器加载Servlet的时候运行，
     * 并且只会被服务器执行一次。PostConstruct在构造函数之后执行。
     * 即只要我们用构造器new了SensitiveFilter对象，那么后面就会执行被@PostConstruct修饰的init方法，完成对文件的读取
     */
    @PostConstruct
    public void init() {
        try (
                //1、获取到输入流，读取文件；
                InputStream is = this.getClass().getClassLoader().getResourceAsStream("sensitive-words.txt");
                //2、InputStream是字节流，我们希望的是字符流，来读取字符。并且用缓冲流来包裹
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        ) {
            //3、开始读取
            String keyword;
            while ((keyword = reader.readLine()) != null) {
                //4、读到一个敏感词汇，就把他加入到前缀树中
                this.addKeyWord(keyword);
            }
        } catch (IOException e) {
            logger.error("加载敏感词汇文件失败：" + e.getMessage());
        }

    }

    /**
     * 实现的是将一个敏感词添加到前缀树中
     * @param keyword
     */
    private void addKeyWord(String keyword) {
        //1、获取根节点，前缀树的根节点是不存取数据的
        TrieNode tempNode = rootNode;
        //2、然后将keyword的字符一个个添加到每一层中，一个节点存一个字符
        for (int i = 0; i < keyword.length(); i++) {
            //首先查询该层是否已经存过这个节点了，一层一个字符只能出现一次，所以前缀树还可以查询字符串的公共前缀
            char c = keyword.charAt(i);
            TrieNode subNode = tempNode.getSubNode(c);
            if (subNode == null){
                //没存过，就初始化子节点，将该字符存进去
                subNode = new TrieNode();
                tempNode.addSubNode(c,subNode);
            }
            //然后往下移一层
            tempNode = subNode;
            //设置结束标记，即该字符串存完了
            if (i == keyword.length() - 1){
                tempNode.setKeywordEnd(true);
            }
        }
    }

    /**
     * 过滤敏感词汇：需要用到三个指针，一个指到用户输入字符串的开始，一个指针开始遍历用户输入字符串，另一个值得是树对应的节点。
     * @param text 用户输入的文本
     * @return
     */
    public String filter(String text){
        //用户没输入
        if (StringUtils.isBlank(text)){
            return null;
        }
        
        //指针一
        TrieNode tempNode = rootNode;
        //指针二
        int begin = 0;
        //指针三
        int position = 0;
        //存取替换后的用户输入内容
        StringBuilder sb = new StringBuilder();
        
        //我们用指针三来遍历，当指针二遍历完了，也就代表结束了。
        while (position < text.length()){
            //取出指针三的字符
            char c = text.charAt(position);

            //如果取出的字符是特殊符号，例如五角星，我们就跳过这个符号，因为有的会在敏感词汇间隔中输入一些特殊符号，以便算法检测不到
            if (isSymbol(c)){
                //如果是特殊符号，且指针一还在根节点，那么指针二、三需要后移，不考虑这个节点，
                if (tempNode == rootNode){
                    sb.append(c);
                    begin++;
                }
                //如果指针一不是根节点，说明前面已经匹配上了敏感词汇的前几个字符，这个时候出现特殊符号，就只让指针三后移
                //正是因为指针三哪种情况都需后移，于是写在外面
                position++;
                continue;
            }

            //如果取出的不是特殊字符，那么需要检测下级节点
            tempNode = tempNode.getSubNode(c);
            if (tempNode == null){
                //说明下级节点没有字符c，那么指针二指的字符就不是敏感词汇的开头
                sb.append(text.charAt(begin));
                //进入下一个text字符
                position = ++begin;
                // 重新指向根节点
                tempNode = rootNode;
            }else if (tempNode.isKeywordEnd()){
                //代表一路匹配成功，找到了敏感词，则需替换它
                sb.append(REPLACEMENT);
                //直接在text中跳过这个敏感词的索引
                begin = ++position;
                //重新回到根节点，检查下一个敏感词汇
                tempNode = rootNode;
            }else{
                //还在匹配，且没匹配玩敏感词汇
                // 检查下一个字符，因为可能fabcd abc是敏感词汇，用户输入fabc,那就检测到c的时候跳出循环，将fabc都输出了，包含了敏感的abc
                if (position < text.length() - 1) {
                    position++;
                }
            }
        }
        //因为是用指针三遍历的，指针三肯定是比指针二更快遍历完text的。
        // 就可能出现指针三没遍历完，但指针二还没遍历完，这意味着用户输入的text会缺失一部分
        //所以当指针三遍历完，意味着指针二到指针三的位置不可能出现敏感词汇
        sb.append(text.substring(begin));
        return sb.toString();
    }

    private boolean isSymbol(char c) {
        // 0x2E80~0x9FFF 是东亚文字范围; CharUtils.isAsciiAlphanumeric(c)是判断c是否为合法普通的字符，是返回true。
        return !CharUtils.isAsciiAlphanumeric(c) && (c < 0x2E80 || c > 0x9FFF);
    }

    //因为这个前缀树，只有在替代敏感词汇这个模块可能被使用，所以我们用内部类的形式定义，不让外部调用
    private class TrieNode {
        //敏感词汇结束的标识，
        private boolean isKeywordEnd = false;
        //由于前缀树不是二叉树，可以有多个子节点，且为了查询更加方便，我们用map存取，子节点的字符为key, 子节点为value
        private Map<Character, TrieNode> subNodes = new HashMap<>();

        public boolean isKeywordEnd(){
            return isKeywordEnd;
        }

        public void setKeywordEnd(boolean keywordEnd) {
            isKeywordEnd = keywordEnd;
        }

        //添加子节点
        public void addSubNode(Character c, TrieNode node){
            subNodes.put(c,node);
        }

        //获取子节点
        public TrieNode getSubNode(Character c){
            return subNodes.get(c);
        }
    }
}
