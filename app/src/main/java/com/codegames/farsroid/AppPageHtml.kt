package com.codegames.farsroid

private val style
    get() = """
@font-face {
    font-family: 'Samim';
    src: url('font/samim_normal.ttf');
    font-weight: normal;
}
@font-face {
    font-family: 'Samim';
    src: url('font/samim_bold.ttf');
    font-weight: bold;
}

* {
    text-decoration: none;
    font-family: Samim, Arial, serif !important;
}

body {
    background-color: #ffffff;
    -webkit-touch-callout: none;
    -webkit-user-select: none;
    -khtml-user-select: none;
    -moz-user-select: none;
    -ms-user-select: none;
    user-select: none;
}
a {
    color: inherit;
    pointer-events: none;
}
img.aligncenter {
    display: block;
    margin: 5px auto;
}
img {
    height: auto;
    max-width: 100% !important;
    border: 0;
    vertical-align: middle;
}
""".trimIndent()

fun AppPageActivity.detailHtml(content: String) = """
<html>
    <head>
        <style>$style</style>
    </head>
    <body dir="rtl">$content</body>
</html>
""".trimIndent()