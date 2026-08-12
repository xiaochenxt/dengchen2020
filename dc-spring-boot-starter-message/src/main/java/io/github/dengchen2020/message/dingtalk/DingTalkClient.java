package io.github.dengchen2020.message.dingtalk;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.jspecify.annotations.NullMarked;

import java.util.Arrays;
import java.util.Objects;

/**
 * 钉钉消息接口
 * @author xiaochen
 * @since 2023/3/11
 */

public interface DingTalkClient {

    /**
     * 发送消息
     * @param message 消息
     */
    @NullMarked
    void send(Message message);

    /**
     * 发送消息
     * @param message 消息
     * @param webhook 地址
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
    interface At {
        String[] atMobiles();
        Boolean isAtAll();
    }

    /**
     * 详见：<a href="https://open.dingtalk.com/document/isvapp/message-type#title-2as-54h-wmc">钉钉开放平台</a>
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    class TextMessage implements Message, At {

        @Override
        public String type() {
            return "text";
        }

        @Override
        public String[] atMobiles() {
            return atMobiles;
        }

        @Override
        public Boolean isAtAll() {
            return isAtAll;
        }

        public TextMessage(String content) {
            this.content = content;
        }
        public TextMessage(String content, String... atMobiles) {
            this.content = content;
            this.atMobiles = atMobiles;
        }
        public TextMessage(String content, Boolean isAtAll) {
            this.content = content;
            this.isAtAll = isAtAll;
        }

        /**
         * 消息文本
         */
        private String content;

        /**
         * 被@人的手机号
         */
        private String[] atMobiles;

        /**
         * @所有人是true，否则为false
         */
        private Boolean isAtAll;

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public void setAtMobiles(String[] atMobiles) {
            this.atMobiles = atMobiles;
        }

        public void setAtAll(Boolean atAll) {
            isAtAll = atAll;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            TextMessage that = (TextMessage) o;
            return Objects.equals(content, that.content) && Objects.deepEquals(atMobiles, that.atMobiles) && Objects.equals(isAtAll, that.isAtAll);
        }

        @Override
        public int hashCode() {
            return Objects.hash(content, Arrays.hashCode(atMobiles), isAtAll);
        }

        @Override
        public String toString() {
            return "TextMessage{" +
                    "content='" + content + '\'' +
                    ", atMobiles=" + Arrays.toString(atMobiles) +
                    ", isAtAll=" + isAtAll +
                    '}';
        }
    }

    /**
     * 详见：<a href="https://open.dingtalk.com/document/isvapp/message-type#title-2as-54h-wmc">钉钉开放平台</a>
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    class LinkMessage implements Message {
        @Override
        public String type() {
            return "link";
        }

        public LinkMessage(String title, String text, String messageUrl) {
            this.title = title;
            this.text = text;
            this.messageUrl = messageUrl;
        }

        /**
         *
         * 消息标题
         */
        private String title;
        /**
         * 消息内容。如果太长只会部分展示
         */
        private String text;
        /**
         * 点击消息跳转的URL
         */
        private String messageUrl;
        /**
         * 图片URL
         */
        private String picUrl;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public String getMessageUrl() {
            return messageUrl;
        }

        public void setMessageUrl(String messageUrl) {
            this.messageUrl = messageUrl;
        }

        public String getPicUrl() {
            return picUrl;
        }

        public void setPicUrl(String picUrl) {
            this.picUrl = picUrl;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            LinkMessage message = (LinkMessage) o;
            return Objects.equals(title, message.title) && Objects.equals(text, message.text) && Objects.equals(messageUrl, message.messageUrl) && Objects.equals(picUrl, message.picUrl);
        }

        @Override
        public int hashCode() {
            return Objects.hash(title, text, messageUrl, picUrl);
        }

        @Override
        public String toString() {
            return "LinkMessage{" +
                    "title='" + title + '\'' +
                    ", text='" + text + '\'' +
                    ", messageUrl='" + messageUrl + '\'' +
                    ", picUrl='" + picUrl + '\'' +
                    '}';
        }
    }

    /**
     * 详见：<a href="https://open.dingtalk.com/document/isvapp/message-type#title-2as-54h-wmc">钉钉开放平台</a>
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    class MarkdownMessage implements Message, At {

        @Override
        public String type() {
            return "markdown";
        }

        @Override
        public String[] atMobiles() {
            return atMobiles;
        }

        @Override
        public Boolean isAtAll() {
            return isAtAll;
        }

        public MarkdownMessage(String title, String text) {
            this.title = title;
            this.text = text;
        }
        public MarkdownMessage(String title, String text, String... atMobiles) {
            this.title = title;
            this.text = text;
            this.atMobiles = atMobiles;
        }
        public MarkdownMessage(String title, String text, Boolean isAtAll) {
            this.title = title;
            this.text = text;
            this.isAtAll = isAtAll;
        }

        /**
         * 首屏会话透出的展示内容
         */
        private String title;
        /**
         * Markdown格式的消息内容
         */
        private String text;

        /**
         * 被@人的手机号
         */
        private String[] atMobiles;

        /**
         * @所有人是true，否则为false
         */
        private Boolean isAtAll;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public void setAtMobiles(String[] atMobiles) {
            this.atMobiles = atMobiles;
        }

        public void setAtAll(Boolean atAll) {
            isAtAll = atAll;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            MarkdownMessage that = (MarkdownMessage) o;
            return Objects.equals(title, that.title) && Objects.equals(text, that.text) && Objects.deepEquals(atMobiles, that.atMobiles) && Objects.equals(isAtAll, that.isAtAll);
        }

        @Override
        public int hashCode() {
            return Objects.hash(title, text, Arrays.hashCode(atMobiles), isAtAll);
        }

        @Override
        public String toString() {
            return "MarkdownMessage{" +
                    "title='" + title + '\'' +
                    ", text='" + text + '\'' +
                    ", atMobiles=" + Arrays.toString(atMobiles) +
                    ", isAtAll=" + isAtAll +
                    '}';
        }
    }

    /**
     * 详见：<a href="https://open.dingtalk.com/document/isvapp/message-type#title-2as-54h-wmc">钉钉开放平台</a>
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    class ActionCardMessage implements Message {
        @Override
        public String type() {
            return "actionCard";
        }

        public ActionCardMessage(String title, String text, String singleTitle, String singleURL) {
            this.title = title;
            this.text = text;
            this.singleTitle = singleTitle;
            this.singleURL = singleURL;
        }

        public ActionCardMessage(String title, String text, Btns btns, String btnOrientation) {
            this.title = title;
            this.text = text;
            this.btns = btns;
            this.btnOrientation = btnOrientation;
        }

        /**
         * 首屏会话透出的展示内容
         */
        private String title;
        /**
         * markdown格式的消息内容
         */
        private String text;
        /**
         * 单个按钮的标题
         */
        private String singleTitle;
        /**
         * 单个按钮的跳转链接
         */
        private String singleURL;

        /**
         * 按钮
         */
        private Btns btns;

        /**
         * 按钮排列顺序，0-按钮竖直排列 1-按钮横向排列
         */
        private String btnOrientation;

        public static class Btns {

            /**
             * 按钮标题
             */
            private String title;

            /**
             * 点击按钮触发的URL
             */
            private String actionURL;

            public String getTitle() {
                return title;
            }

            public void setTitle(String title) {
                this.title = title;
            }

            public String getActionURL() {
                return actionURL;
            }

            public void setActionURL(String actionURL) {
                this.actionURL = actionURL;
            }

            @Override
            public boolean equals(Object o) {
                if (o == null || getClass() != o.getClass()) return false;
                Btns btns = (Btns) o;
                return Objects.equals(title, btns.title) && Objects.equals(actionURL, btns.actionURL);
            }

            @Override
            public int hashCode() {
                return Objects.hash(title, actionURL);
            }

            @Override
            public String toString() {
                return "Btns{" +
                        "title='" + title + '\'' +
                        ", actionURL='" + actionURL + '\'' +
                        '}';
            }
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public String getSingleTitle() {
            return singleTitle;
        }

        public void setSingleTitle(String singleTitle) {
            this.singleTitle = singleTitle;
        }

        public String getSingleURL() {
            return singleURL;
        }

        public void setSingleURL(String singleURL) {
            this.singleURL = singleURL;
        }

        public Btns getBtns() {
            return btns;
        }

        public void setBtns(Btns btns) {
            this.btns = btns;
        }

        public String getBtnOrientation() {
            return btnOrientation;
        }

        public void setBtnOrientation(String btnOrientation) {
            this.btnOrientation = btnOrientation;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            ActionCardMessage that = (ActionCardMessage) o;
            return Objects.equals(title, that.title) && Objects.equals(text, that.text) && Objects.equals(singleTitle, that.singleTitle) && Objects.equals(singleURL, that.singleURL) && Objects.equals(btns, that.btns) && Objects.equals(btnOrientation, that.btnOrientation);
        }

        @Override
        public int hashCode() {
            return Objects.hash(title, text, singleTitle, singleURL, btns, btnOrientation);
        }

        @Override
        public String toString() {
            return "ActionCardMessage{" +
                    "title='" + title + '\'' +
                    ", text='" + text + '\'' +
                    ", singleTitle='" + singleTitle + '\'' +
                    ", singleURL='" + singleURL + '\'' +
                    ", btns=" + btns +
                    ", btnOrientation='" + btnOrientation + '\'' +
                    '}';
        }
    }

}
