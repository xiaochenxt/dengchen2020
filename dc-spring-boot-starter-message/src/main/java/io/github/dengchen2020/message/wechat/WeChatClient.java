package io.github.dengchen2020.message.wechat;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.github.dengchen2020.core.utils.Base64Utils;
import io.github.dengchen2020.core.utils.digest.DigestUtils;
import org.springframework.core.io.Resource;

import java.util.Arrays;
import java.util.Objects;

/**
 * 微信消息接口
 * @author xiaochen
 * @since 2025/4/7
 */
public interface WeChatClient {

    /**
     * 发送消息
     * @param message 消息
     */
    void send(Message message);

    /**
     * 发送消息
     * @param message 消息
     * @param webhook 地址
     */
    void send(Message message, String webhook);

    /**
     * 上传文件
     * @param resource 文件资源
     * @param type 类型，例如：file,voice
     * @param key webhook中的key
     */
    String upload(Resource resource, String type, String key);

    interface Message {
        String type();
    }

    /**
     * 详见：<a href="https://developer.work.weixin.qq.com/document/path/91770#%E6%96%87%E6%9C%AC%E7%B1%BB%E5%9E%8B">文本类型</a>
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    class TextMessage implements Message {

        @Override
        public String type() {
            return "text";
        }

        public TextMessage(String content, String... mentioned_mobile_list) {
            this.content = content;
            this.mentioned_mobile_list = mentioned_mobile_list;
        }

        public TextMessage(String content, boolean atAll) {
            this(content, atAll ? new String[]{"@all"} : null);
        }

        /**
         * 文本内容，最长不超过2048个字节，必须是utf8编码
         */
        private String content;

        /**
         * 手机号列表，提醒手机号对应的群成员(@某个成员)，@all表示提醒所有人
         */
        private String[] mentioned_mobile_list;

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public String[] getMentioned_mobile_list() {
            return mentioned_mobile_list;
        }

        public void setMentioned_mobile_list(String[] mentioned_mobile_list) {
            this.mentioned_mobile_list = mentioned_mobile_list;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            TextMessage that = (TextMessage) o;
            return Objects.equals(content, that.content) && Objects.deepEquals(mentioned_mobile_list, that.mentioned_mobile_list);
        }

        @Override
        public int hashCode() {
            return Objects.hash(content, Arrays.hashCode(mentioned_mobile_list));
        }

        @Override
        public String toString() {
            return "TextMessage{" +
                    "content='" + content + '\'' +
                    ", mentioned_mobile_list=" + Arrays.toString(mentioned_mobile_list) +
                    '}';
        }
    }

    /**
     * 详见：<a href="https://developer.work.weixin.qq.com/document/path/91770#markdown%E7%B1%BB%E5%9E%8B">markdown类型</a>
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    class MarkdownMessage implements Message {

        @Override
        public String type() {
            return "markdown";
        }

        public MarkdownMessage(String content) {
            this.content = content;
        }

        /**
         * markdown内容，最长不超过4096个字节，必须是utf8编码
         */
        private String content;

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            MarkdownMessage that = (MarkdownMessage) o;
            return Objects.equals(content, that.content);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(content);
        }

        @Override
        public String toString() {
            return "MarkdownMessage{" +
                    "content='" + content + '\'' +
                    '}';
        }
    }

    /**
     * 详见：<a href="https://developer.work.weixin.qq.com/document/path/91770#%E5%9B%BE%E7%89%87%E7%B1%BB%E5%9E%8B">图片类型</a>
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    class ImageMessage implements Message {

        @Override
        public String type() {
            return "image";
        }

        public ImageMessage(String base64, String md5) {
            this.base64 = base64;
            this.md5 = md5;
        }

        public ImageMessage(byte[] imageData) {
            this.base64 = Base64Utils.encodeToString(imageData);
            this.md5 = DigestUtils.md5Hex(imageData);
        }

        /**
         * 图片内容的base64编码
         */
        private String base64;

        /**
         * 图片内容（base64编码前）的md5值
         */
        private String md5;

        public String getBase64() {
            return base64;
        }

        public void setBase64(String base64) {
            this.base64 = base64;
        }

        public String getMd5() {
            return md5;
        }

        public void setMd5(String md5) {
            this.md5 = md5;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            ImageMessage that = (ImageMessage) o;
            return Objects.equals(base64, that.base64) && Objects.equals(md5, that.md5);
        }

        @Override
        public int hashCode() {
            return Objects.hash(base64, md5);
        }

        @Override
        public String toString() {
            return "ImageMessage{" +
                    "base64='" + base64 + '\'' +
                    ", md5='" + md5 + '\'' +
                    '}';
        }
    }

    /**
     * 详见：<a href="https://developer.work.weixin.qq.com/document/path/91770#%E5%9B%BE%E6%96%87%E7%B1%BB%E5%9E%8B">图文类型</a>
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    class NewsMessage implements Message {

        @Override
        public String type() {
            return "news";
        }

        public NewsMessage(String articles, String title, String url) {
            this.articles = articles;
            this.title = title;
            this.url = url;
        }

        /**
         * 图文消息，一个图文消息支持1到8条图文
         */
        private String articles;

        /**
         * 标题，不超过128个字节，超过会自动截断
         */
        private String title;

        /**
         * 点击后跳转的链接
         */
        private String url;

        /**
         * 描述，不超过512个字节，超过会自动截断
         */
        private String description;

        /**
         * 图文消息的图片链接，支持JPG、PNG格式，较好的效果为大图 1068*455，小图150*150
         */
        private String picurl;

        public String getArticles() {
            return articles;
        }

        public void setArticles(String articles) {
            this.articles = articles;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getPicurl() {
            return picurl;
        }

        public void setPicurl(String picurl) {
            this.picurl = picurl;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            NewsMessage that = (NewsMessage) o;
            return Objects.equals(articles, that.articles) && Objects.equals(title, that.title) && Objects.equals(url, that.url) && Objects.equals(description, that.description) && Objects.equals(picurl, that.picurl);
        }

        @Override
        public int hashCode() {
            return Objects.hash(articles, title, url, description, picurl);
        }

        @Override
        public String toString() {
            return "NewsMessage{" +
                    "articles='" + articles + '\'' +
                    ", title='" + title + '\'' +
                    ", url='" + url + '\'' +
                    ", description='" + description + '\'' +
                    ", picurl='" + picurl + '\'' +
                    '}';
        }
    }

    /**
     * 详见：<a href="https://developer.work.weixin.qq.com/document/path/91770#%E6%96%87%E4%BB%B6%E7%B1%BB%E5%9E%8B">文件类型</a>
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    class FileMessage implements Message {

        @Override
        public String type() {
            return "file";
        }

        public FileMessage(String media_id) {
            this.media_id = media_id;
        }

        /**
         * 文件id，通过{@link WeChatClient#upload(Resource, String, String)}上传后获取
         */
        private String media_id;

        public String getMedia_id() {
            return media_id;
        }

        public void setMedia_id(String media_id) {
            this.media_id = media_id;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            FileMessage that = (FileMessage) o;
            return Objects.equals(media_id, that.media_id);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(media_id);
        }

        @Override
        public String toString() {
            return "FileMessage{" +
                    "media_id='" + media_id + '\'' +
                    '}';
        }
    }

    /**
     * 详见：<a href="https://developer.work.weixin.qq.com/document/path/91770#%E8%AF%AD%E9%9F%B3%E7%B1%BB%E5%9E%8B">语音类型</a>
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    class VoiceMessage implements Message {

        @Override
        public String type() {
            return "voice";
        }

        public VoiceMessage(String media_id) {
            this.media_id = media_id;
        }

        /**
         * 语音文件id，通过{@link WeChatClient#upload(Resource, String, String)}上传后获取
         */
        private String media_id;

        public String getMedia_id() {
            return media_id;
        }

        public void setMedia_id(String media_id) {
            this.media_id = media_id;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            VoiceMessage that = (VoiceMessage) o;
            return Objects.equals(media_id, that.media_id);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(media_id);
        }

        @Override
        public String toString() {
            return "VoiceMessage{" +
                    "media_id='" + media_id + '\'' +
                    '}';
        }
    }

    /**
     * 详见：<a href="https://developer.work.weixin.qq.com/document/path/91770#%E6%96%87%E6%9C%AC%E9%80%9A%E7%9F%A5%E6%A8%A1%E7%89%88%E5%8D%A1%E7%89%87">文本通知模版卡片</a>
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    class TemplateCardMessage implements Message {

        @Override
        public String type() {
            return "template_card";
        }

        public TemplateCardMessage(String card_type, MainTitle main_title, CardAction card_action) {
            this.card_type = card_type;
            this.main_title = main_title;
            this.card_action = card_action;
        }

        private String card_type;

        /**
         * 模版卡片的主要内容，包括一级标题和标题辅助信息
         */
        private MainTitle main_title;

        public static class MainTitle {
            public MainTitle(String title) {
                this.title = title;
            }

            /**
             * 一级标题，建议不超过26个字。模版卡片主要内容的一级标题main_title.title和二级普通文本sub_title_text必须有一项填写
             */
            private String title;
            /**
             * 标题辅助信息，建议不超过30个字
             */
            private String desc;

            public String getTitle() {
                return title;
            }

            public void setTitle(String title) {
                this.title = title;
            }

            public String getDesc() {
                return desc;
            }

            public void setDesc(String desc) {
                this.desc = desc;
            }

            @Override
            public boolean equals(Object o) {
                if (o == null || getClass() != o.getClass()) return false;
                MainTitle mainTitle = (MainTitle) o;
                return Objects.equals(title, mainTitle.title) && Objects.equals(desc, mainTitle.desc);
            }

            @Override
            public int hashCode() {
                return Objects.hash(title, desc);
            }

            @Override
            public String toString() {
                return "MainTitle{" +
                        "title='" + title + '\'' +
                        ", desc='" + desc + '\'' +
                        '}';
            }
        }

        /**
         * 整体卡片的点击跳转事件，text_notice模版卡片中该字段为必填项
         */
        private CardAction card_action;

        public static class CardAction {
            public CardAction(String url) {
                this.url = url;
                this.type = 1;
            }
            public CardAction(String appid, String pagepath) {
                this.appid = appid;
                this.pagepath = pagepath;
                this.type = 2;
            }

            /**
             * 卡片跳转类型，1 代表跳转url，2 代表打开小程序。text_notice模版卡片中该字段取值范围为[1,2]
             */
            private Integer type;

            /**
             * 跳转事件的url，card_action.type是1时必填
             */
            private String url;

            /**
             * 跳转事件的小程序的appid，card_action.type是2时必填
             */
            private String appid;

            /**
             * 跳转事件的小程序的pagepath，card_action.type是2时选填
             */
            private String pagepath;

            public Integer getType() {
                return type;
            }

            public void setType(Integer type) {
                this.type = type;
            }

            public String getUrl() {
                return url;
            }

            public void setUrl(String url) {
                this.url = url;
            }

            public String getAppid() {
                return appid;
            }

            public void setAppid(String appid) {
                this.appid = appid;
            }

            public String getPagepath() {
                return pagepath;
            }

            public void setPagepath(String pagepath) {
                this.pagepath = pagepath;
            }

            @Override
            public boolean equals(Object o) {
                if (o == null || getClass() != o.getClass()) return false;
                CardAction that = (CardAction) o;
                return Objects.equals(type, that.type) && Objects.equals(url, that.url) && Objects.equals(appid, that.appid) && Objects.equals(pagepath, that.pagepath);
            }

            @Override
            public int hashCode() {
                return Objects.hash(type, url, appid, pagepath);
            }

            @Override
            public String toString() {
                return "CardAction{" +
                        "type=" + type +
                        ", url='" + url + '\'' +
                        ", appid='" + appid + '\'' +
                        ", pagepath='" + pagepath + '\'' +
                        '}';
            }
        }

        public String getCard_type() {
            return card_type;
        }

        public void setCard_type(String card_type) {
            this.card_type = card_type;
        }

        public MainTitle getMain_title() {
            return main_title;
        }

        public void setMain_title(MainTitle main_title) {
            this.main_title = main_title;
        }

        public CardAction getCard_action() {
            return card_action;
        }

        public void setCard_action(CardAction card_action) {
            this.card_action = card_action;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            TemplateCardMessage that = (TemplateCardMessage) o;
            return Objects.equals(card_type, that.card_type) && Objects.equals(main_title, that.main_title) && Objects.equals(card_action, that.card_action);
        }

        @Override
        public int hashCode() {
            return Objects.hash(card_type, main_title, card_action);
        }

        @Override
        public String toString() {
            return "TemplateCardMessage{" +
                    "card_type='" + card_type + '\'' +
                    ", main_title=" + main_title +
                    ", card_action=" + card_action +
                    '}';
        }
    }

}
