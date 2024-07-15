package net.primal.android.thread.articles.rendering

private val ARTICLE_BASE_CSS = """
.sunset {
  --text-primary: #ffffff;
  --text-tertiary: #757575;
  --subtile-devider: #444444;
  --accent-links: #f800c1;
  --background-input: #222222;
}

.midnight {
  --text-primary: #ffffff;
  --text-tertiary: #757575;
  --subtile-devider: #444444;
  --accent-links: #2394EF;
  --background-input: #222222;
}

.sunrise {
  --text-primary: #111111;
  --text-tertiary: #808080;
  --subtile-devider: #c8c8c8;
  --accent-links: #CA077C;
  --background-input: #e5e5e5;
}

.ice {
  --text-primary: #111111;
  --text-tertiary: #808080;
  --subtile-devider: #c8c8c8;
  --accent-links: #2394EF;
  --background-input: #e5e5e5;
}
    
.small {
  --p-font-size: 15px;
  --p-font-weight: 400;
  --p-font-weight-bold:700;
  --p-line-height: 23px;

  --h1-font-size: 30px;
  --h1-font-weight: 700;
  --h1-line-height: 38px;

  --h2-font-size: 26px;
  --h2-font-weight: 700;
  --h2-line-height: 34px;

  --h3-font-size: 22px;
  --h3-font-weight: 700;
  --h3-line-height: 30px;

  --h4-font-size: 20px;
  --h4-font-weight: 700;
  --h4-line-height: 28px;

  --h5-font-size: 18px;
  --h5-font-weight: 700;
  --h5-line-height: 26px;

  --h6-font-size: 16px;
  --h6-font-weight: 700;
  --h6-line-height: 24px;
}

.regular {
  --p-font-size: 16px;
  --p-font-weight: 400;
  --p-font-weight-bold:700;
  --p-line-height: 24px;

  --h1-font-size: 32px;
  --h1-font-weight: 700;
  --h1-line-height: 40px;

  --h2-font-size: 28px;
  --h2-font-weight: 700;
  --h2-line-height: 36px;

  --h3-font-size: 24px;
  --h3-font-weight: 700;
  --h3-line-height: 32px;

  --h4-font-size: 22px;
  --h4-font-weight: 700;
  --h4-line-height: 30px;

  --h5-font-size: 20px;
  --h5-font-weight: 700;
  --h5-line-height: 28px;

  --h6-font-size: 18px;
  --h6-font-weight: 700;
  --h6-line-height: 26px;
}

.large {
  --p-font-size: 18px;
  --p-font-weight: 400;
  --p-font-weight-bold:700;
  --p-line-height: 26px;

  --h1-font-size: 36px;
  --h1-font-weight: 700;
  --h1-line-height: 44px;

  --h2-font-size: 32px;
  --h2-font-weight: 700;
  --h2-line-height: 40px;

  --h3-font-size: 28px;
  --h3-font-weight: 700;
  --h3-line-height: 36px;

  --h4-font-size: 26px;
  --h4-font-weight: 700;
  --h4-line-height: 34px;

  --h5-font-size: 24px;
  --h5-font-weight: 700;
  --h5-line-height: 32px;

  --h6-font-size: 22px;
  --h6-font-weight: 700;
  --h6-line-height: 30px;
}

.huge {
  --p-font-size: 20px;
  --p-font-weight: 400;
  --p-font-weight-bold:700;
  --p-line-height: 28px;

  --h1-font-size: 40px;
  --h1-font-weight: 700;
  --h1-line-height: 48px;

  --h2-font-size: 36px;
  --h2-font-weight: 700;
  --h2-line-height: 44px;

  --h3-font-size: 32px;
  --h3-font-weight: 700;
  --h3-line-height: 40px;

  --h4-font-size: 30px;
  --h4-font-weight: 700;
  --h4-line-height: 38px;

  --h5-font-size: 28px;
  --h5-font-weight: 700;
  --h5-line-height: 36px;

  --h6-font-size: 24px;
  --h6-font-weight: 700;
  --h6-line-height: 32px;
}


p {
  color: var(--text-primary);
  font-family: Nacelle, sans-serif;
  font-size: var(--p-font-size);
  font-weight: var(--p-font-weight);
  line-height: var(--p-line-height);
  margin-top: 0;
  margin-bottom: 20px;
}

h1 {
  color: var(--text-primary);
  font-family: Nacelle, sans-serif;
  font-size: var(--h1-font-size);
  font-weight: var(--h1-font-weight);
  line-height: var(--h1-line-height);
  margin-top: 10px;
  margin-bottom: 10px;
}

h2 {
  color: var(--text-primary);
  font-family: Nacelle, sans-serif;
  font-size: var(--h2-font-size);
  font-weight: var(--h2-font-weight);
  line-height: var(--h2-line-height);
  margin-top: 10px;
  margin-bottom: 10px;
}

h3 {
  color: var(--text-primary);
  font-family: Nacelle, sans-serif;
  font-size: var(--h3-font-size);
  font-weight: var(--h3-font-weight);
  line-height: var(--h3-line-height);
  margin-top: 10px;
  margin-bottom: 10px;
}

h4 {
  color: var(--text-primary);
  font-family: Nacelle, sans-serif;
  font-size: var(--h4-font-size);
  font-weight: var(--h4-font-weight);
  line-height: var(--h4-line-height);
  margin-top: 10px;
  margin-bottom: 10px;
}

h5 {
  color: var(--text-primary);
  font-family: Nacelle, sans-serif;
  font-size: var(--h5-font-size);
  font-weight: var(--h5-font-weight);
  line-height: var(--h5-line-height);
  margin-top: 10px;
  margin-bottom: 10px;
}

h6 {
  color: var(--text-primary);
  font-family: Nacelle, sans-serif;
  font-size: var(--h6-font-size);
  font-weight: var(--h6-font-weight);
  line-height: var(--h6-line-height);
  margin-top: 10px;
  margin-bottom: 10px;
}

hr {
  border-top:1px solid var(--subtile-devider);
  margin-top: 10px;
  margin-bottom: 20px;
}

ul {
  margin-left: 0px;
  padding-left: 14px;
  margin-bottom: 20px;
}

li {
  padding-left: 8px;
  color: var(--text-primary);
  font-family: Nacelle, sans-serif;
  font-size: var(--p-font-size);
  font-weight: var(--p-font-weight);
  line-height: var(--p-line-height);
  margin-top: 0;
  margin-bottom: 12px;
}

li::marker {
  content: '•';
}

li > ul {
  padding-left: 30px;
}

ol {
  margin-left: 0px;
  padding-left: var(--p-line-height);
  margin-bottom: 20px;
}

li {
  padding-left: 8px;
  color: var(--text-primary);
  font-family: Nacelle, sans-serif;
  font-size: var(--p-font-size);
  font-weight: var(--p-font-weight);
  line-height: var(--p-line-height);
  margin-top: 0;
  margin-bottom: 12px;
}

li > ol {
  padding-left: 40px;
}


dl {
  margin-left: 0px;
  padding-left: 14px;
  margin-bottom: 20px;
}

dt {
  color: var(--text-primary);
  font-family: Nacelle, sans-serif;
  font-size: var(--p-font-size);
  font-weight: var(--p-font-weight);
  line-height: var(--p-line-height);
}

dd {
  padding-left: 8px;
  color: var(--text-primary);
  font-family: Nacelle, sans-serif;
  font-size: var(--p-font-size);
  font-weight: var(--p-font-weight);
  line-height: var(--p-line-height);
  margin-top: 0;
  margin-bottom: 12px;

}

dd > dl {
  padding-left: 30px;
}

img {
  margin-top: 0;
  margin-bottom: 20px;
  border-radius: 4px;
  overflow: hidden;
  max-width: 100%;
}

img + sup {
  display: block;
  margin-top: -6px;
}


blockquote {
  color: var(--text-primary);
  font-family: Nacelle, sans-serif;
  font-size: var(--p-font-size);
  font-weight: var(--p-font-weight);
  line-height: var(--p-line-height);
  padding-left: 12px;
  padding-bottom: 0;
  margin-top: 0;
  margin-bottom: 20px;
  border-left: 4px solid var(--text-tertiary);
}

a {
  color: var(--accent-links);
  font-family: Nacelle, sans-serif;
  font-size: var(--p-font-size);
  font-weight: var(--p-font-weight);
  line-height: var(--p-line-height);
}

pre, code, mark {
  background-color: var(--background-input);
}

code {
  color: var(--text-primary);
  font-family: "Fira Mono";
  font-size: var(--p-font-size);
  font-weight: var(--p-font-weight);
  line-height: var(--p-line-height);
}

del {
  color: inherit;
}

th {
  color: var(--text-primary);
  font-size: var(--p-font-size);
  font-weight: var(--p-font-weight-bold);
  line-height: var(--p-line-height);
  border-bottom: 1px solid var(--text-secondary);
  padding: 12px 8px;
}

td {
  color: var(--text-primary);
  font-size: var(--p-font-size);
  font-weight: var(--p-font-weight);
  line-height: var(--p-line-height);
  border-bottom: 1px solid var(--subtile-devider);
  padding: 12px 8px;
}

td > *, th > * {
  margin-bottom: 0;
  font-size: inherit;
  font-weight: inherit;
  line-height: inherit;
}

""".trimIndent()

val ARTICLE_BASE_HTML = """
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
    <style>
        body {
            width: 95vw;
            width: 95dvw;
            overflow-x: hidden;
            word-wrap: break-word;
        }

        $ARTICLE_BASE_CSS

    </style>
</head>
<body class="{{ THEME }}">
    {{ CONTENT }}
</body>
</html>
""".trimIndent()
