package com.daemon.markvii.utils

import android.content.Context
import android.content.Intent
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.core.content.FileProvider
import com.itextpdf.html2pdf.HtmlConverter
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.layout.Document
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
                val attributes = PrintAttributes.Builder()
                    .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                    .setResolution(PrintAttributes.Resolution("pdf", "pdf", 600, 600))
                    .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
                    .build()
                printManager.print(jobName, view.createPrintDocumentAdapter(jobName), attributes)
            }
        }
        webView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)
    }

    fun sharePdf(context: Context, markdown: String, brandName: String, modelId: String, userPrompt: String) {
        try {
            val html = convertMarkdownToHtml(markdown, brandName, modelId, userPrompt)
            val file = File(context.cacheDir, "MarkVII_Response_${System.currentTimeMillis()}.pdf")
            
            // Create PDF using iText
            val writer = PdfWriter(FileOutputStream(file))
            val pdfDoc = PdfDocument(writer)
            pdfDoc.defaultPageSize = PageSize.A4
            
            // Convert HTML to PDF
            HtmlConverter.convertToPdf(html, pdfDoc, null)
            pdfDoc.close()
            
            // Share the PDF
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
                @page { margin: 16px; }
                body { font-family: sans-serif; padding: 0; margin: 0; color: #000000; }
                pre { background-color: #f0f0f0; padding: 8px; border-radius: 4px; overflow-x: auto; white-space: pre-wrap; page-break-inside: avoid; }
                code { font-family: monospace; background-color: #f0f0f0; padding: 2px 4px; border-radius: 2px; }
                h1, h2, h3 { color: #333333; page-break-after: avoid; }
                blockquote { border-left: 4px solid #ccc; margin-left: 0; padding-left: 16px; color: #666; }
                p { line-height: 1.5; }
                .header { margin-bottom: 24px; padding-bottom: 10px; page-break-after: avoid; }
                .brand-title { font-size: 24px; font-weight: bold; color: #000; margin-bottom: 4px; }
                .model-id { font-size: 14px; color: #666; font-family: monospace; }
                .prompt-box { background-color: #f5f5f5; padding: 12px; border-radius: 8px; margin-bottom: 24px; color: #333; font-size: 16px; line-height: 1.4; page-break-inside: avoid; }
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
