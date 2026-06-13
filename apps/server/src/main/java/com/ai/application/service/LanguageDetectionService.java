package com.ai.application.service;

import java.util.regex.Pattern;

/**
 * Lightweight language detection service using JDK standard library.
 * Detects primary language based on character distribution in input text.
 */
public class LanguageDetectionService {

    private static final Pattern CJK_UNIFIED_IDEOGRAPHS = Pattern.compile("[\\u4e00-\\u9fff]");
    private static final Pattern HIRAGANA = Pattern.compile("[\\u3040-\\u309f]");
    private static final Pattern KATAKANA = Pattern.compile("[\\u30a0-\\u30ff]");
    private static final Pattern KANJI = Pattern.compile("[\\u3400-\\u4dbf\\u4e00-\\u9fff]");
    private static final Pattern LATIN = Pattern.compile("[a-zA-Z]");

    private static final double JAPANESE_KANA_THRESHOLD = 0.05;
    private static final double CHINESE_CJK_THRESHOLD = 0.3;
    private static final double ENGLISH_LATIN_THRESHOLD = 0.5;

    /**
     * Detects the primary language of the given text.
     *
     * @param text the input text to analyze
     * @return language code: "zh" for Chinese, "ja" for Japanese, "en" for English, "default" otherwise
     */
    public String detect(String text) {
        if (text == null || text.isBlank()) {
            return "default";
        }

        int cjkCount = countMatches(text, CJK_UNIFIED_IDEOGRAPHS);
        int hiraganaCount = countMatches(text, HIRAGANA);
        int katakanaCount = countMatches(text, KATAKANA);
        int kanjiCount = countMatches(text, KANJI);
        int latinCount = countMatches(text, LATIN);

        int totalChars = text.length();
        if (totalChars == 0) {
            return "default";
        }

        int japaneseKanaCount = hiraganaCount + katakanaCount;
        boolean hasJapaneseContent = japaneseKanaCount > 0 || kanjiCount > 0;
        boolean isPredominantlyJapanese = hasJapaneseContent && japaneseKanaCount > totalChars * JAPANESE_KANA_THRESHOLD;
        boolean isPredominantlyChinese = cjkCount > totalChars * CHINESE_CJK_THRESHOLD && !isPredominantlyJapanese;
        boolean isPredominantlyEnglish = latinCount > totalChars * ENGLISH_LATIN_THRESHOLD;

        if (isPredominantlyJapanese) {
            return "ja";
        } else if (isPredominantlyChinese) {
            return "zh";
        } else if (isPredominantlyEnglish) {
            return "en";
        }

        return "default";
    }

    private int countMatches(String text, Pattern pattern) {
        java.util.regex.Matcher matcher = pattern.matcher(text);
        int count = 0;
        while (matcher.find()) {
            count++;
        }
        return count;
    }

    /**
     * Builds a prompt based on detected language.
     *
     * @param question the user's question
     * @param context the retrieved context from documents
     * @param languageCode the detected language code
     * @return the formatted prompt in the detected language
     */
    public String buildPrompt(String question, String context, String languageCode) {
        if (context == null || context.isBlank()) {
            return getNoContextMessage(languageCode);
        }

        String template = getPromptTemplate(languageCode);
        return String.format(template, context, question);
    }

    private String getNoContextMessage(String languageCode) {
        return switch (languageCode) {
            case "zh" -> "没有找到相关的文档来回答您的问题。请先上传一些文档。";
            case "ja" -> "関連ドキュメントがありません。まずドキュメントをアップロードしてください。";
            default -> "I don't have relevant documents to answer your question. Please upload some documents first.";
        };
    }

    private String getPromptTemplate(String languageCode) {
        return switch (languageCode) {
            case "zh" -> """
                你是一个有用的助手。请根据以下文档内容，用结构化的 Markdown 格式回答用户的问题。

                # 文档内容
                %s

                # 用户问题
                %s

                ## 格式要求（必须严格遵守）

                - 标题符号后必须加空格：`# 标题` 而不是 `#标题`
                - 粗体符号前后必须加空格：`** 关键词 **` 而不是 `**关键词**`
                - 列表符号后必须加空格：`- 要点` 而不是 `-要点`
                - **段落之间必须用两个换行符分隔（空一行）**：每个标题、每个列表项之间都要有空行
                - **每个段落（标题或正文）的尾部必须加换行符分隔**
                - 回答必须遵循「总-分-总」结构：开头先用 1-2 句话给出总体结论或概述（总）；中间用分点列表或子标题展开详细要点（分）；结尾再用 1-2 句话做总结或给出行动建议（总）。
                - 使用标准 Markdown 语法

                ## 正确示例（严格模仿此格式，每个段落尾部都有空行）

                # 概述

                **人工智能**（AI）是计算机科学的重要分支，正在深刻改变各行各业。

                # 核心要点

                - **机器学习**是 AI 的核心技术，通过数据训练模型
                - **深度学习**基于神经网络，在图像和语音领域表现突出
                - **自然语言处理**让机器理解和生成人类语言

                # 总结

                AI 将持续推动技术创新，建议持续关注其最新发展动态。

                # 回答

                """;
            case "ja" -> """
                あなたは有帮助なアシスタントです。以下のドキュメントの内容に基づいて、構造化されたマークダウン形式でユーザーの質問に回答してください。

                # ドキュメント内容
                %s

                # ユーザーの質問
                %s

                ## フォーマット要件（厳守必須）

                - 見出し記号の後にスペースが必要：`# 見出し` 而不是 `#見出し`
                - 太字記号の前後にスペースが必要：`** キーワード **` 而不是 `**キーワード**`
                - リスト記号の後にスペースが必要：`- 要点` 而不是 `-要点`
                - **段落の間に必ず空行を挿入（2つの改行で区切る）**：各見出し、リスト項目同士は必ず空行で分隔
                - **各段落（見出しまたは本文）の末尾に改行を追加すること**
                - 回答は「総-分-総」構造に従うこと：冒頭で 1〜2 文の全体結論（総）、中段はリストまたはサブ見出しで詳細を展開（分）、末尾で 1〜2 文のまとめまたは次のアクション提案（総）。
                - 標準的な Markdown 構文を使用

                ## 正しい例（この形式を厳密に真似ること、各段落の末尾に空行がある）

                # 概要

                **人工知能**（AI）はコンピュータ科学の重要な分支であり、各業界に深い変化をもたらしています。

                # コアポイント

                - **機械学習**は AI の中核技術であり、データを通じてモデルを訓練する
                - **深層学習**はニューラルネットワークに基づき、画像と音声の分野で顕著な成果を上げる
                - **自然言語処理**により、機械が人間の言語を理解し生成することが可能になる

                # まとめ

                AI は技術革新を継続的に推進するため、最新の動向に注目することをお勧めします。

                # 回答

                """;
            default -> """
                You are a helpful assistant. Answer the user's question using structured Markdown.

                # Context
                %s

                # Question
                %s

                ## Format Requirements (MUST STRICTLY FOLLOW)

                - Space after heading symbols: `# Heading` NOT `#Heading`
                - Space around bold symbols: `** keyword **` NOT `**keyword**`
                - Space after list symbols: `- Bullet point` NOT `-Bullet point`
                - **Blank line between paragraphs (use two line breaks to separate)**: Always add empty line between headings and lists
                - **Add blank line at the end of every paragraph (heading or body text)**
                - Follow a `General-Specific-General` structure: open with a 1-2 sentence overall conclusion (General), expand with bullet points or sub-headings for details (Specific), close with a 1-2 sentence summary or next-step suggestion (General).
                - Use standard Markdown syntax

                ## Correct Example (STRICTLY follow this format, each paragraph ends with blank line)

                # Overview

                **Artificial Intelligence** (AI) is an important branch of computer science, transforming every industry.

                # Core Points

                - **Machine learning** is the core technology of AI, training models through data
                - **Deep learning** is based on neural networks and excels in image and speech domains
                - **Natural language processing** enables machines to understand and generate human language

                # Summary

                AI will continue to drive technological innovation; staying current with its latest developments is recommended.

                # Answer

                """;
        };
    }
}
