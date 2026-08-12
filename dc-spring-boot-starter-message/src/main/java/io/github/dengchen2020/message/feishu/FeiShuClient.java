package io.github.dengchen2020.message.feishu;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.node.ArrayNode;
import io.github.dengchen2020.core.utils.JsonUtils;
import org.jspecify.annotations.NullMarked;

import java.util.Objects;

/**
 * 飞书消息接口
 * @author xiaochen
 * @since 2023/7/4
 */
public interface FeiShuClient {

    /**
     * 发送消息
     * @param message 消息
     */
    @NullMarked
    void send(Message message);

    /**
     * 发送消息
     * @param message 消息
     * @param webhook webhook地址
     */
    @NullMarked
    void send(Message message, String webhook);

    /**
     * 发送消息
     * @param message 消息
     * @param webhook 地址
     * @param secret 密钥
     */
    @NullMarked
    void send(Message message, String webhook, String secret);

    interface Message {
        String type();
    }

    /**
     * 详见：<a href="https://open.feishu.cn/document/client-docs/bot-v3/add-custom-bot#756b882f">自定义机器人使用指南</a>
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    class TextMessage implements Message {

        @Override
        public String type() {
            return "text";
        }

        public TextMessage(String text) {
            this.text = text;
        }

        private String text;

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public TextMessage addAt(String... userId) {
            StringBuilder builder = new StringBuilder();
            for (String s : userId) {
                builder.append(" <at user_id=\"").append(s).append("\">").append(s).append("</at>");
            }
            this.text = this.text + builder;
            return this;
        }

        public TextMessage addAtAll() {
            this.text = this.text + "<at user_id=\"all\">所有人</at>";
            return this;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            TextMessage that = (TextMessage) o;
            return Objects.equals(text, that.text);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(text);
        }

        @Override
        public String toString() {
            return "TextMessage{" +
                    "text='" + text + '\'' +
                    '}';
        }
    }

    /**
     * 详见：<a href="https://open.feishu.cn/document/client-docs/bot-v3/add-custom-bot#756b882f">自定义机器人使用指南</a>
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    class PostMessage implements Message {

        @Override
        public String type() {
            return "post";
        }

        public PostMessage(Post post) {
            this.post = post;
        }

        private Post post;

        @JsonInclude(JsonInclude.Include.NON_NULL)
        public static class Post {

            public Post(Zh_cn zh_cn) {
                this.zh_cn = zh_cn;
            }

            public Post(En_us en_us) {
                this.en_us = en_us;
            }

            public Post(Zh_cn zh_cn, En_us en_us) {
                this.zh_cn = zh_cn;
                this.en_us = en_us;
            }

            private Zh_cn zh_cn;

            private En_us en_us;

            @JsonInclude(JsonInclude.Include.NON_NULL)
            public static class Zh_cn {
                private String title;
                private final ArrayNode content = JsonUtils.createArrayNode();

                public Zh_cn() {
                }

                public Zh_cn(String title) {
                    this.title = title;
                }

                public ArrayNode createNode() {
                    return JsonUtils.createArrayNode();
                }

                public void addNode(ArrayNode node) {
                    content.add(node);
                }

                public Zh_cn addText(String text, Boolean un_escape) {
                    ArrayNode node = createNode();
                    addText(node, text, un_escape);
                    addNode(node);
                    return this;
                }

                public Zh_cn addText(ArrayNode node, String text, boolean un_escape) {
                    node.add(JsonUtils.createObjectNode().put("tag", "text").put("text", text).put("unescape", un_escape));
                    return this;
                }

                public Zh_cn addA(String text, String href) {
                    ArrayNode node = createNode();
                    addA(node, text, href);
                    addNode(node);
                    return this;
                }

                public Zh_cn addA(ArrayNode node, String text, String href) {
                    node.add(JsonUtils.createObjectNode().put("tag", "a").put("text", text).put("href", href));
                    return this;
                }

                public Zh_cn addAt(String user_id) {
                    ArrayNode node = createNode();
                    addAt(node, user_id);
                    addNode(node);
                    return this;
                }

                public Zh_cn addAtAll() {
                    addAt("all");
                    return this;
                }

                public Zh_cn addAt(ArrayNode arrayNode, String user_id) {
                    arrayNode.add(JsonUtils.createObjectNode().put("tag", "at").put("user_id", user_id));
                    return this;
                }

                public String getTitle() {
                    return title;
                }

                public void setTitle(String title) {
                    this.title = title;
                }

                public ArrayNode getContent() {
                    return content;
                }

                @Override
                public boolean equals(Object o) {
                    if (o == null || getClass() != o.getClass()) return false;
                    Zh_cn zhCn = (Zh_cn) o;
                    return Objects.equals(title, zhCn.title) && Objects.equals(content, zhCn.content);
                }

                @Override
                public int hashCode() {
                    return Objects.hash(title, content);
                }

                @Override
                public String toString() {
                    return "Zh_cn{" +
                            "title='" + title + '\'' +
                            ", content=" + content +
                            '}';
                }
            }

            @JsonInclude(JsonInclude.Include.NON_NULL)
            public static class En_us {
                private String title;
                private final ArrayNode content = JsonUtils.createArrayNode();

                public En_us() {
                }

                public En_us(String title) {
                    this.title = title;
                }

                public ArrayNode createNode() {
                    return JsonUtils.createArrayNode();
                }

                public void addNode(ArrayNode node) {
                    content.add(node);
                }

                public En_us addText(String text, Boolean un_escape) {
                    ArrayNode node = createNode();
                    addText(node, text, un_escape);
                    addNode(node);
                    return this;
                }

                public En_us addText(ArrayNode node, String text, boolean un_escape) {
                    node.add(JsonUtils.createObjectNode().put("tag", "text").put("text", text).put("unescape", un_escape));
                    return this;
                }

                public En_us addA(String text, String href) {
                    ArrayNode node = createNode();
                    addA(node, text, href);
                    addNode(node);
                    return this;
                }

                public En_us addA(ArrayNode node, String text, String href) {
                    node.add(JsonUtils.createObjectNode().put("tag", "a").put("text", text).put("href", href));
                    return this;
                }

                public En_us addAt(String user_id) {
                    ArrayNode node = createNode();
                    addAt(node, user_id);
                    addNode(node);
                    return this;
                }

                public En_us addAtAll() {
                    addAt("all");
                    return this;
                }

                public En_us addAt(ArrayNode arrayNode, String user_id) {
                    arrayNode.add(JsonUtils.createObjectNode().put("tag", "at").put("user_id", user_id));
                    return this;
                }

                public String getTitle() {
                    return title;
                }

                public void setTitle(String title) {
                    this.title = title;
                }

                public ArrayNode getContent() {
                    return content;
                }

                @Override
                public boolean equals(Object o) {
                    if (o == null || getClass() != o.getClass()) return false;
                    En_us enUs = (En_us) o;
                    return Objects.equals(title, enUs.title) && Objects.equals(content, enUs.content);
                }

                @Override
                public int hashCode() {
                    return Objects.hash(title, content);
                }

                @Override
                public String toString() {
                    return "En_us{" +
                            "title='" + title + '\'' +
                            ", content=" + content +
                            '}';
                }
            }

            public Zh_cn getZh_cn() {
                return zh_cn;
            }

            public void setZh_cn(Zh_cn zh_cn) {
                this.zh_cn = zh_cn;
            }

            public En_us getEn_us() {
                return en_us;
            }

            public void setEn_us(En_us en_us) {
                this.en_us = en_us;
            }

            @Override
            public boolean equals(Object o) {
                if (o == null || getClass() != o.getClass()) return false;
                Post post = (Post) o;
                return Objects.equals(zh_cn, post.zh_cn) && Objects.equals(en_us, post.en_us);
            }

            @Override
            public int hashCode() {
                return Objects.hash(zh_cn, en_us);
            }

            @Override
            public String toString() {
                return "Post{" +
                        "zh_cn=" + zh_cn +
                        ", en_us=" + en_us +
                        '}';
            }
        }

        public Post getPost() {
            return post;
        }

        public void setPost(Post post) {
            this.post = post;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            PostMessage that = (PostMessage) o;
            return Objects.equals(post, that.post);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(post);
        }

        @Override
        public String toString() {
            return "PostMessage{" +
                    "post=" + post +
                    '}';
        }
    }

}
