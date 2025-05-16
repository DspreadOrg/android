package com.dspread.pos.common.http.model;

public class DingTalkRequest {
    private String msgtype = "text";
    private TextContent text;

    public DingTalkRequest(String content) {
        this.text = new TextContent(content);
    }

    public static class TextContent {
        private String content;

        public TextContent(String content) {
            this.content = content;
        }
    }
}