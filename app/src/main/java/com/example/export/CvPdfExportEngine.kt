package com.example.export

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.content.FileProvider
import com.example.data.model.CoverLetterDocument
import com.example.data.model.CvCustomization
import com.example.data.model.CvLayoutArchitecture
import com.example.data.model.CvTemplate
import com.example.data.model.KungaTemplateCatalog
import com.example.data.model.ResumeDocument
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

object CvPdfExportEngine {

    private const val PAGE_WIDTH = 595 // Standard A4 width in points (72 dpi)
    private const val PAGE_HEIGHT = 842 // Standard A4 height in points
    private const val MARGIN_H = 40f
    private const val MARGIN_V = 40f

    suspend fun exportAndShareResumePdf(
        context: Context,
        resume: ResumeDocument,
        accentHex: Long? = null,
        customization: CvCustomization? = null
    ): Result<File> = withContext(Dispatchers.IO) {
        runCatching {
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas

            val template = CvTemplate.fromId(resume.templateId)
            val preset = customization?.let { KungaTemplateCatalog.fromPresetId(it.presetId) }
                ?: KungaTemplateCatalog.fromCvTemplate(template)
            val resolvedArchitecture = when {
                customization?.columns == 2 && customization.direction == "left" ->
                    CvLayoutArchitecture.ARCHITECTURAL_LEFT_SIDEBAR
                customization?.columns == 2 && customization.direction == "right" ->
                    CvLayoutArchitecture.EDITORIAL_RIGHT_SIDEBAR
                preset.columns == 2 && customization?.columns == 1 ->
                    CvLayoutArchitecture.HARVARD_EXECUTIVE
                else -> preset.architecture
            }

            drawResumeOnCanvas(
                canvas = canvas,
                resume = resume,
                template = template,
                accentHex = accentHex ?: preset.accentHex,
                architecture = resolvedArchitecture,
                presetName = preset.name
            )

            pdfDocument.finishPage(page)

            val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
            val sanitizedTitle = resume.fullName.ifBlank { resume.title }
                .replace(Regex("[^a-zA-Z0-9_\\-]"), "_")
                .take(40)
            val file = File(exportDir, "${sanitizedTitle}_CV.pdf")
            FileOutputStream(file).use { out ->
                pdfDocument.writeTo(out)
            }
            pdfDocument.close()

            sharePdfFile(context, file, "${resume.fullName.ifBlank { "Candidate" }} — KungaCV Resume PDF")
            file
        }
    }

    suspend fun exportAndShareCoverLetterPdf(
        context: Context,
        letter: CoverLetterDocument,
        candidateName: String,
        candidateContact: String
    ): Result<File> = withContext(Dispatchers.IO) {
        runCatching {
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas

            drawCoverLetterOnCanvas(canvas, letter, candidateName, candidateContact)

            pdfDocument.finishPage(page)

            val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
            val sanitizedCompany = letter.companyName.ifBlank { "Company" }
                .replace(Regex("[^a-zA-Z0-9_\\-]"), "_")
                .take(30)
            val file = File(exportDir, "Cover_Letter_${sanitizedCompany}.pdf")
            FileOutputStream(file).use { out ->
                pdfDocument.writeTo(out)
            }
            pdfDocument.close()

            sharePdfFile(context, file, "Cover Letter — ${letter.companyName}")
            file
        }
    }

    private fun sharePdfFile(context: Context, file: File, subject: String) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, subject)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val chooser = Intent.createChooser(shareIntent, subject).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }

    private fun drawResumeOnCanvas(
        canvas: Canvas,
        resume: ResumeDocument,
        template: CvTemplate,
        accentHex: Long?,
        architecture: CvLayoutArchitecture,
        presetName: String
    ) {
        val resolvedAccent = accentHex?.toInt() ?: when (template) {
            CvTemplate.EXECUTIVE_NAVY -> Color.parseColor("#386FA4")
            CvTemplate.EDITORIAL_SERIF -> Color.parseColor("#547980")
            CvTemplate.MODERN_MINIMAL -> Color.parseColor("#348AA7")
        }
        val inkColor = Color.parseColor("#0A0A0B")
        val slateColor = Color.parseColor("#6B6B76")
        val bodyColor = Color.parseColor("#1E1E22")
        val ruleColor = Color.parseColor("#DEDDD6")

        // Paper background
        val bgPaint = Paint().apply { color = Color.parseColor("#FFFFFF") }
        canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), PAGE_HEIGHT.toFloat(), bgPaint)

        when (architecture) {
            CvLayoutArchitecture.ARCHITECTURAL_LEFT_SIDEBAR -> {
                drawLeftSidebarDossierPdf(
                    canvas = canvas,
                    resume = resume,
                    resolvedAccent = resolvedAccent,
                    inkColor = inkColor,
                    slateColor = slateColor,
                    bodyColor = bodyColor,
                    ruleColor = ruleColor,
                    presetName = presetName
                )
            }

            CvLayoutArchitecture.SWISS_INDEX_RAIL -> {
                drawSwissIndexRailPdf(
                    canvas = canvas,
                    resume = resume,
                    resolvedAccent = resolvedAccent,
                    inkColor = inkColor,
                    slateColor = slateColor,
                    bodyColor = bodyColor,
                    ruleColor = ruleColor,
                    presetName = presetName
                )
            }

            else -> {
                drawHarvardAndEditorialPdf(
                    canvas = canvas,
                    resume = resume,
                    resolvedAccent = resolvedAccent,
                    inkColor = inkColor,
                    slateColor = slateColor,
                    bodyColor = bodyColor,
                    ruleColor = ruleColor,
                    architecture = architecture,
                    presetName = presetName
                )
            }
        }
    }

    private fun drawHarvardAndEditorialPdf(
        canvas: Canvas,
        resume: ResumeDocument,
        resolvedAccent: Int,
        inkColor: Int,
        slateColor: Int,
        bodyColor: Int,
        ruleColor: Int,
        architecture: CvLayoutArchitecture,
        presetName: String
    ) {
        val contentWidth = (PAGE_WIDTH - MARGIN_H * 2).toInt()
        var currentY = MARGIN_V

        val isFramed = architecture == CvLayoutArchitecture.FRAMED_MONOGRAPH
        if (isFramed) {
            val framePaint = Paint().apply {
                color = inkColor
                style = Paint.Style.STROKE
                strokeWidth = 1.4f
            }
            canvas.drawRect(22f, 22f, PAGE_WIDTH - 22f, PAGE_HEIGHT - 22f, framePaint)
        }

        val headerTypeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        val namePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = inkColor
            textSize = 24f
            typeface = headerTypeface
            textAlign = Paint.Align.CENTER
        }
        val centerX = PAGE_WIDTH / 2f
        canvas.drawText(resume.fullName.ifBlank { "Candidate Name" }, centerX, currentY + 18f, namePaint)
        currentY += 32f

        val rolePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = resolvedAccent
            textSize = 10.5f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            letterSpacing = 0.14f
        }
        canvas.drawText(resume.jobTitle.ifBlank { "Professional" }.uppercase(), centerX, currentY, rolePaint)
        currentY += 15f

        val contactLine = listOf(resume.location, resume.email, resume.phone, resume.website)
            .filter { it.isNotBlank() }
            .joinToString("   |   ")
        val contactPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = slateColor
            textSize = 9.2f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(contactLine, centerX, currentY, contactPaint)
        currentY += 12f

        // Classic Executive Double-Rule
        val primaryRulePaint = Paint().apply {
            color = inkColor
            strokeWidth = 1.8f
        }
        val secondaryRulePaint = Paint().apply {
            color = resolvedAccent
            strokeWidth = 0.8f
        }
        canvas.drawLine(MARGIN_H, currentY, PAGE_WIDTH - MARGIN_H, currentY, primaryRulePaint)
        canvas.drawLine(MARGIN_H, currentY + 3f, PAGE_WIDTH - MARGIN_H, currentY + 3f, secondaryRulePaint)
        currentY += 18f

        fun drawSectionHeader(title: String) {
            if (currentY > PAGE_HEIGHT - 80f) return
            val sectionPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = inkColor
                textSize = 9.8f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                letterSpacing = 0.14f
            }
            canvas.drawText(title.uppercase(), MARGIN_H, currentY, sectionPaint)
            currentY += 4f
            val rulePaint = Paint().apply {
                color = resolvedAccent
                strokeWidth = 1.1f
            }
            canvas.drawLine(MARGIN_H, currentY, PAGE_WIDTH - MARGIN_H, currentY, rulePaint)
            currentY += 11f
        }

        fun drawMultilineText(text: String, paint: TextPaint, indent: Float = 0f): Float {
            if (text.isBlank() || currentY > PAGE_HEIGHT - 50f) return 0f
            val availableWidth = (contentWidth - indent).toInt().coerceAtLeast(100)
            val layout = StaticLayout.Builder
                .obtain(text, 0, text.length, paint, availableWidth)
                .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                .setLineSpacing(1.5f, 1.15f)
                .setIncludePad(false)
                .build()
            canvas.save()
            canvas.translate(MARGIN_H + indent, currentY)
            layout.draw(canvas)
            canvas.restore()
            val height = layout.height.toFloat()
            currentY += height + 5f
            return height
        }

        val bodyPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = bodyColor
            textSize = 9.6f
        }
        val boldSubPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = inkColor
            textSize = 10.5f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }
        val italicRolePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = resolvedAccent
            textSize = 9.8f
            typeface = Typeface.create(Typeface.SERIF, Typeface.ITALIC)
        }
        val metaPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = slateColor
            textSize = 9f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
        }

        if (resume.summary.isNotBlank()) {
            drawSectionHeader("Executive Summary")
            drawMultilineText(resume.summary, bodyPaint)
            currentY += 6f
        }

        val experiences = resume.parsedExperiences()
        if (experiences.isNotEmpty()) {
            drawSectionHeader("Professional Experience")
            experiences.forEach { exp ->
                if (currentY > PAGE_HEIGHT - 75f) return@forEach
                // Line 1: Company (left) + Location (right)
                canvas.drawText(exp.company.ifBlank { exp.role }, MARGIN_H, currentY + 9f, boldSubPaint)
                if (exp.location.isNotBlank()) {
                    val locWidth = metaPaint.measureText(exp.location)
                    canvas.drawText(exp.location, PAGE_WIDTH - MARGIN_H - locWidth, currentY + 9f, metaPaint)
                }
                currentY += 13f
                // Line 2: Role (italic left) + Period (tabular right)
                canvas.drawText(exp.role, MARGIN_H, currentY + 8f, italicRolePaint)
                if (exp.period.isNotBlank()) {
                    val perWidth = metaPaint.measureText(exp.period)
                    canvas.drawText(exp.period, PAGE_WIDTH - MARGIN_H - perWidth, currentY + 8f, metaPaint)
                }
                currentY += 13f
                exp.highlights.filter { it.isNotBlank() }.forEach { bullet ->
                    drawMultilineText("▪  $bullet", bodyPaint, indent = 8f)
                }
                currentY += 5f
            }
        }

        val educations = resume.parsedEducations()
        if (educations.isNotEmpty()) {
            drawSectionHeader("Education")
            educations.forEach { edu ->
                if (currentY > PAGE_HEIGHT - 60f) return@forEach
                canvas.drawText(edu.institution.ifBlank { edu.degree }, MARGIN_H, currentY + 9f, boldSubPaint)
                if (edu.year.isNotBlank()) {
                    val yWidth = metaPaint.measureText(edu.year)
                    canvas.drawText(edu.year, PAGE_WIDTH - MARGIN_H - yWidth, currentY + 9f, metaPaint)
                }
                currentY += 13f
                canvas.drawText(edu.degree, MARGIN_H, currentY + 8f, italicRolePaint)
                currentY += 12f
                if (edu.details.isNotBlank()) {
                    drawMultilineText("•  ${edu.details}", bodyPaint, indent = 6f)
                }
                currentY += 4f
            }
        }

        val typedSkills = resume.parsedTypedSkills()
        if (typedSkills.isNotEmpty()) {
            drawSectionHeader("Core Competencies")
            val skillsLine = typedSkills.joinToString("   •   ") { "${it.skill} (${it.skillLevel.label})" }
            drawMultilineText(skillsLine, bodyPaint)
            currentY += 6f
        }

        val projects = resume.parsedProjects()
        if (projects.isNotEmpty()) {
            drawSectionHeader("Selected Projects")
            projects.forEach { proj ->
                if (currentY > PAGE_HEIGHT - 60f) return@forEach
                val titleLine = listOf(proj.name, proj.role).filter { it.isNotBlank() }.joinToString(" — ")
                canvas.drawText(titleLine, MARGIN_H, currentY + 9f, boldSubPaint)
                currentY += 14f
                if (proj.description.isNotBlank()) {
                    drawMultilineText(proj.description, bodyPaint, indent = 6f)
                }
            }
        }

        drawFooter(canvas, presetName, ruleColor)
    }

    private fun drawSwissIndexRailPdf(
        canvas: Canvas,
        resume: ResumeDocument,
        resolvedAccent: Int,
        inkColor: Int,
        slateColor: Int,
        bodyColor: Int,
        ruleColor: Int,
        presetName: String
    ) {
        val leftRailX = MARGIN_H
        val rightColX = MARGIN_H + 135f
        val rightColWidth = (PAGE_WIDTH - MARGIN_H - rightColX).toInt()
        var currentY = MARGIN_V

        val accentBarPaint = Paint().apply { color = resolvedAccent }
        canvas.drawRect(leftRailX, currentY, leftRailX + 26f, currentY + 4f, accentBarPaint)

        val monoIndexPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = resolvedAccent
            textSize = 8.8f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        }
        val monoLabelPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = slateColor
            textSize = 8.5f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
        }
        canvas.drawText("CURRICULUM VITAE", leftRailX, currentY + 18f, monoIndexPaint)
        if (resume.location.isNotBlank()) {
            canvas.drawText(resume.location, leftRailX, currentY + 30f, monoLabelPaint)
        }

        val namePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = inkColor
            textSize = 22f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }
        canvas.drawText(resume.fullName.ifBlank { "Candidate Name" }, rightColX, currentY + 18f, namePaint)

        val rolePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = resolvedAccent
            textSize = 11.5f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }
        canvas.drawText(resume.jobTitle.ifBlank { "Professional" }, rightColX, currentY + 34f, rolePaint)

        val contactLine = listOf(resume.email, resume.phone, resume.website)
            .filter { it.isNotBlank() }
            .joinToString("  ·  ")
        canvas.drawText(contactLine, rightColX, currentY + 48f, monoLabelPaint)
        currentY += 64f

        val rulePaint = Paint().apply {
            color = ruleColor
            strokeWidth = 1f
        }
        val bodyPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = bodyColor
            textSize = 9.5f
        }
        val boldTitlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = inkColor
            textSize = 10.2f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }

        fun drawRightColumnText(text: String, paint: TextPaint, indent: Float = 0f) {
            if (text.isBlank() || currentY > PAGE_HEIGHT - 50f) return
            val width = (rightColWidth - indent).toInt().coerceAtLeast(80)
            val layout = StaticLayout.Builder
                .obtain(text, 0, text.length, paint, width)
                .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                .setLineSpacing(1.5f, 1.15f)
                .setIncludePad(false)
                .build()
            canvas.save()
            canvas.translate(rightColX + indent, currentY)
            layout.draw(canvas)
            canvas.restore()
            currentY += layout.height + 5f
        }

        var secNum = 1
        fun startSwissSection(label: String) {
            if (currentY > PAGE_HEIGHT - 75f) return
            canvas.drawLine(MARGIN_H, currentY, PAGE_WIDTH - MARGIN_H, currentY, rulePaint)
            currentY += 12f
            canvas.drawText("0${secNum++}", leftRailX, currentY + 8f, monoIndexPaint)
            canvas.drawText(label.uppercase(), leftRailX, currentY + 20f, monoLabelPaint)
        }

        if (resume.summary.isNotBlank()) {
            startSwissSection("About")
            drawRightColumnText(resume.summary, bodyPaint)
            currentY += 8f
        }

        val experiences = resume.parsedExperiences()
        if (experiences.isNotEmpty()) {
            startSwissSection("Experience")
            experiences.forEach { exp ->
                if (currentY > PAGE_HEIGHT - 70f) return@forEach
                canvas.drawText("${exp.role} · ${exp.company}", rightColX, currentY + 9f, boldTitlePaint)
                if (exp.period.isNotBlank()) {
                    val pWidth = monoIndexPaint.measureText(exp.period)
                    canvas.drawText(exp.period, PAGE_WIDTH - MARGIN_H - pWidth, currentY + 9f, monoIndexPaint)
                }
                currentY += 14f
                exp.highlights.filter { it.isNotBlank() }.forEach { bullet ->
                    drawRightColumnText("—  $bullet", bodyPaint, indent = 4f)
                }
                currentY += 6f
            }
        }

        val educations = resume.parsedEducations()
        if (educations.isNotEmpty()) {
            startSwissSection("Education")
            educations.forEach { edu ->
                if (currentY > PAGE_HEIGHT - 60f) return@forEach
                canvas.drawText(edu.degree, rightColX, currentY + 9f, boldTitlePaint)
                if (edu.year.isNotBlank()) {
                    val yWidth = monoLabelPaint.measureText(edu.year)
                    canvas.drawText(edu.year, PAGE_WIDTH - MARGIN_H - yWidth, currentY + 9f, monoLabelPaint)
                }
                currentY += 13f
                drawRightColumnText(edu.institution, monoLabelPaint)
                currentY += 4f
            }
        }

        val typedSkills = resume.parsedTypedSkills()
        if (typedSkills.isNotEmpty()) {
            startSwissSection("Capabilities")
            val skillsLine = typedSkills.joinToString("   ▪   ") { "${it.skill} (${it.skillLevel.label})" }
            drawRightColumnText(skillsLine, bodyPaint)
            currentY += 6f
        }

        drawFooter(canvas, presetName, ruleColor)
    }

    private fun drawLeftSidebarDossierPdf(
        canvas: Canvas,
        resume: ResumeDocument,
        resolvedAccent: Int,
        inkColor: Int,
        slateColor: Int,
        bodyColor: Int,
        ruleColor: Int,
        presetName: String
    ) {
        val sidebarWidth = 195f
        val sidebarTintPaint = Paint().apply {
            color = Color.argb(
                18,
                Color.red(resolvedAccent),
                Color.green(resolvedAccent),
                Color.blue(resolvedAccent)
            )
        }
        canvas.drawRect(0f, 0f, sidebarWidth, PAGE_HEIGHT.toFloat(), sidebarTintPaint)

        val borderPaint = Paint().apply {
            color = ruleColor
            strokeWidth = 1f
        }
        canvas.drawLine(sidebarWidth, 0f, sidebarWidth, PAGE_HEIGHT.toFloat(), borderPaint)

        // Left Sidebar Content
        var leftY = MARGIN_V
        val leftX = 26f
        val leftContentWidth = (sidebarWidth - 44f).toInt()

        val sideHeadingPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = resolvedAccent
            textSize = 8.8f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            letterSpacing = 0.12f
        }
        val sideBodyPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = inkColor
            textSize = 9.2f
        }
        val sideSubtlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = slateColor
            textSize = 8.5f
        }

        fun drawLeftMultiline(text: String, paint: TextPaint) {
            if (text.isBlank() || leftY > PAGE_HEIGHT - 50f) return
            val layout = StaticLayout.Builder
                .obtain(text, 0, text.length, paint, leftContentWidth)
                .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                .setLineSpacing(1.2f, 1.1f)
                .setIncludePad(false)
                .build()
            canvas.save()
            canvas.translate(leftX, leftY)
            layout.draw(canvas)
            canvas.restore()
            leftY += layout.height + 5f
        }

        canvas.drawText("CONTACT DOSSIER", leftX, leftY + 8f, sideHeadingPaint)
        leftY += 16f
        listOf(resume.location, resume.email, resume.phone, resume.website)
            .filter { it.isNotBlank() }
            .forEach { item ->
                drawLeftMultiline(item, sideBodyPaint)
            }
        leftY += 12f

        val typedSkills = resume.parsedTypedSkills()
        if (typedSkills.isNotEmpty()) {
            canvas.drawText("COMPETENCIES", leftX, leftY + 8f, sideHeadingPaint)
            leftY += 16f
            typedSkills.forEach { skill ->
                drawLeftMultiline("• ${skill.skill} (${skill.skillLevel.label})", sideBodyPaint)
            }
            leftY += 12f
        }

        val educations = resume.parsedEducations()
        if (educations.isNotEmpty()) {
            canvas.drawText("EDUCATION", leftX, leftY + 8f, sideHeadingPaint)
            leftY += 16f
            educations.forEach { edu ->
                drawLeftMultiline(edu.degree, sideBodyPaint)
                drawLeftMultiline("${edu.institution} (${edu.year})", sideSubtlePaint)
                leftY += 4f
            }
        }

        // Right Main Column
        val rightX = sidebarWidth + 24f
        val rightWidth = (PAGE_WIDTH - MARGIN_H - rightX).toInt()
        var rightY = MARGIN_V

        val namePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = inkColor
            textSize = 23f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        }
        canvas.drawText(resume.fullName.ifBlank { "Candidate Name" }, rightX, rightY + 18f, namePaint)
        rightY += 32f

        val rolePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = resolvedAccent
            textSize = 11.5f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }
        canvas.drawText(resume.jobTitle.ifBlank { "Professional" }, rightX, rightY, rolePaint)
        rightY += 12f

        val accentRulePaint = Paint().apply {
            color = resolvedAccent
            strokeWidth = 1.5f
        }
        canvas.drawLine(rightX, rightY, PAGE_WIDTH - MARGIN_H, rightY, accentRulePaint)
        rightY += 18f

        val bodyPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = bodyColor
            textSize = 9.5f
        }
        val boldTitlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = inkColor
            textSize = 10.5f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }

        fun drawRightMultiline(text: String, paint: TextPaint, indent: Float = 0f) {
            if (text.isBlank() || rightY > PAGE_HEIGHT - 50f) return
            val w = (rightWidth - indent).toInt().coerceAtLeast(80)
            val layout = StaticLayout.Builder
                .obtain(text, 0, text.length, paint, w)
                .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                .setLineSpacing(1.4f, 1.15f)
                .setIncludePad(false)
                .build()
            canvas.save()
            canvas.translate(rightX + indent, rightY)
            layout.draw(canvas)
            canvas.restore()
            rightY += layout.height + 5f
        }

        if (resume.summary.isNotBlank()) {
            canvas.drawText("EXECUTIVE PROFILE", rightX, rightY, sideHeadingPaint)
            rightY += 10f
            drawRightMultiline(resume.summary, bodyPaint)
            rightY += 10f
        }

        val experiences = resume.parsedExperiences()
        if (experiences.isNotEmpty()) {
            canvas.drawText("WORK EXPERIENCE", rightX, rightY, sideHeadingPaint)
            rightY += 12f
            experiences.forEach { exp ->
                if (rightY > PAGE_HEIGHT - 70f) return@forEach
                canvas.drawText("${exp.role} — ${exp.company}", rightX, rightY + 8f, boldTitlePaint)
                rightY += 13f
                drawRightMultiline(
                    listOf(exp.period, exp.location).filter { it.isNotBlank() }.joinToString(" · "),
                    sideSubtlePaint
                )
                exp.highlights.filter { it.isNotBlank() }.forEach { bullet ->
                    drawRightMultiline("•  $bullet", bodyPaint, indent = 6f)
                }
                rightY += 6f
            }
        }

        drawFooter(canvas, presetName, ruleColor)
    }

    private fun drawFooter(canvas: Canvas, presetName: String, ruleColor: Int) {
        val rulePaint = Paint().apply {
            color = ruleColor
            strokeWidth = 0.75f
        }
        canvas.drawLine(MARGIN_H, PAGE_HEIGHT - 32f, PAGE_WIDTH - MARGIN_H, PAGE_HEIGHT - 32f, rulePaint)
        val footerPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#8A8A93")
            textSize = 8f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
        }
        canvas.drawText("KUNGACV STUDIO · A4 ARCHIVAL", MARGIN_H, PAGE_HEIGHT - 18f, footerPaint)
        val rightLabel = presetName.uppercase()
        val rw = footerPaint.measureText(rightLabel)
        canvas.drawText(rightLabel, PAGE_WIDTH - MARGIN_H - rw, PAGE_HEIGHT - 18f, footerPaint)
    }

    private fun drawCoverLetterOnCanvas(
        canvas: Canvas,
        letter: CoverLetterDocument,
        candidateName: String,
        candidateContact: String
    ) {
        val contentWidth = (PAGE_WIDTH - MARGIN_H * 2).toInt()
        val inkColor = Color.parseColor("#0A0A0B")
        val brandBlue = Color.parseColor("#386FA4")
        val bodyColor = Color.parseColor("#1E1E22")

        val bgPaint = Paint().apply { color = Color.parseColor("#FFFFFF") }
        canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), PAGE_HEIGHT.toFloat(), bgPaint)

        val namePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = inkColor
            textSize = 22f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        }
        canvas.drawText(candidateName.ifBlank { "Candidate Name" }, MARGIN_H, 54f, namePaint)

        val contactPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#6B6B76")
            textSize = 9.5f
        }
        canvas.drawText(candidateContact, MARGIN_H, 72f, contactPaint)

        val linePaint = Paint().apply {
            color = brandBlue
            strokeWidth = 2f
        }
        canvas.drawLine(MARGIN_H, 86f, PAGE_WIDTH - MARGIN_H, 86f, linePaint)

        val roleBannerPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = brandBlue
            textSize = 11f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        }
        canvas.drawText(
            "RE: ${letter.targetRole.uppercase()} — ${letter.companyName.uppercase()}",
            MARGIN_H,
            112f,
            roleBannerPaint
        )

        val bodyPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = bodyColor
            textSize = 10.5f
        }

        val text = letter.content.ifBlank { "No content generated." }
        val layout = StaticLayout.Builder
            .obtain(text, 0, text.length, bodyPaint, contentWidth)
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setLineSpacing(3f, 1.25f)
            .setIncludePad(false)
            .build()

        canvas.save()
        canvas.translate(MARGIN_H, 136f)
        layout.draw(canvas)
        canvas.restore()

        drawFooter(canvas, "COVER LETTER", Color.parseColor("#DEDDD6"))
    }
}
