package com.daemon.markvii.utils

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.print.PrintAttributes
import android.print.PrintManager
import android.text.Html
import android.text.StaticLayout
import android.text.TextPaint
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.core.content.FileProvider
import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer
import java.io.File
import java.io.FileOutputStream

object PdfGenerator {

    fun exportToPdf(context: Context, markdown: String, brandName: String, modelId: String, userPrompt: String) {
        val html = convertMarkdownToHtml(markdown, brandName, modelId, userPrompt)
        val webView = WebView(context)
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
                val jobName = "MarkVII_Response_${System.currentTimeMillis()}"
                printManager.print(jobName, view.createPrintDocumentAdapter(jobName), null)
            }
        }
        webView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)
    }

    fun sharePdf(context: Context, markdown: String, brandName: String, modelId: String, userPrompt: String) {
        try {
            val html = convertMarkdownToHtml(markdown, brandName, modelId, userPrompt)
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas
            val paint = TextPaint()
            paint.color = Color.BLACK
            paint.textSize = 12f
            paint.isAntiAlias = true

            val spannedText = Html.fromHtml(html, Html.FROM_HTML_MODE_COMPACT)
            val textLayout = StaticLayout.Builder.obtain(spannedText, 0, spannedText.length, paint, 555) // 595 - 20 padding * 2
                .setAlignment(android.text.Layout.Alignment.ALIGN_NORMAL)
                .setLineSpacing(1.0f, 1.0f)
                .setIncludePad(false)
                .build()

            canvas.save()
            canvas.translate(20f, 20f) // Padding
            textLayout.draw(canvas)
            canvas.restore()

            pdfDocument.finishPage(page)

            val file = File(context.cacheDir, "response.pdf")
            val fileOutputStream = FileOutputStream(file)
            pdfDocument.writeTo(fileOutputStream)
            pdfDocument.close()
            fileOutputStream.close()

            val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share PDF"))

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Failed to generate PDF: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun convertMarkdownToHtml(markdown: String, brandName: String, modelId: String, userPrompt: String): String {
        val parser = Parser.builder().build()
        val document = parser.parse(markdown)
        val renderer = HtmlRenderer.builder().build()
        val body = renderer.render(document)
        
        return """
            <html>
            <head>
            <style>
                body { font-family: sans-serif; padding: 16px; color: #000000; }
                pre { background-color: #f0f0f0; padding: 8px; border-radius: 4px; overflow-x: auto; white-space: pre-wrap; }
                code { font-family: monospace; background-color: #f0f0f0; padding: 2px 4px; border-radius: 2px; }
                h1, h2, h3 { color: #333333; }
                blockquote { border-left: 4px solid #ccc; margin-left: 0; padding-left: 16px; color: #666; }
                p { line-height: 1.5; }
                .header { margin-bottom: 24px; padding-bottom: 10px; }
                .brand-title { font-size: 24px; font-weight: bold; color: #000; margin-bottom: 4px; }
                .model-id { font-size: 14px; color: #666; font-family: monospace; }
                .prompt-box { background-color: #f5f5f5; padding: 12px; border-radius: 8px; margin-bottom: 24px; color: #333; font-size: 16px; line-height: 1.4; }
                .prompt-label { font-size: 12px; color: #888; margin-bottom: 4px; font-weight: bold; text-transform: uppercase; }
            </style>
            </head>
            <body>
            <div class="header">
                <div class="brand-title">Mark VII  x  $brandName</div>
                <div class="model-id">$modelId</div>
            </div>
            
            <div class="prompt-box">
                <div class="prompt-label">Your Prompt</div>
                $userPrompt
            </div>
            
            $body
            </body>
            </html>
        """.trimIndent()
    }
}
