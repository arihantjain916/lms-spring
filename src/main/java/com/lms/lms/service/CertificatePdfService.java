package com.lms.lms.service;

import com.lms.lms.modals.Certificate;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Renders the certificate PDF.
 *
 * <p>The web certificate page (education-portal
 * {@code app/certificates/[id]/page.tsx}) is the canonical design, and the
 * mobile app mirrors it too. Everything below — palette, the Lucide award mark,
 * the "Verified by EduPortal" pill, wording, date format and the two-column
 * footer — reproduces that card so one credential looks identical wherever it
 * is rendered.
 *
 * <p>The layout is positioned absolutely rather than flowed, because the pill
 * and the award mark have to sit at exact coordinates relative to the text.
 */
@Service
public class CertificatePdfService {

    /** Tailwind blue-700 — the eyebrow. */
    private static final Color EYEBROW = new Color(29, 78, 216);
    /** Tailwind blue-950 — the course title. */
    private static final Color COURSE_TITLE = new Color(23, 37, 84);
    /** Tailwind amber-300 — the card's double border. */
    private static final Color FRAME = new Color(252, 211, 77);
    /** Tailwind amber-500 — the award mark. */
    private static final Color AWARD = new Color(245, 158, 11);
    /** Tailwind green-700 / green-50 — the verified pill. */
    private static final Color VERIFIED_TEXT = new Color(21, 128, 61);
    private static final Color VERIFIED_TINT = new Color(240, 253, 244);
    /** Tailwind gray-500 / gray-200 — muted labels and the footer rule. */
    private static final Color MUTED = new Color(107, 114, 128);
    private static final Color RULE = new Color(229, 231, 235);

    /** Baselines, measured down from the top of an A4 landscape page. */
    private static final float AWARD_TOP = 464f;
    private static final float AWARD_SIZE = 58f;
    private static final float EYEBROW_BASELINE = 380f;
    private static final float NAME_BASELINE = 330f;
    private static final float COMPLETED_BASELINE = 300f;
    private static final float COURSE_BASELINE = 266f;
    private static final float PILL_BOTTOM = 216f;
    private static final float PILL_HEIGHT = 28f;
    private static final float RULE_Y = 182f;
    private static final float FOOTER_LABEL_BASELINE = 160f;
    private static final float FOOTER_VALUE_BASELINE = 140f;
    /** Width of the footer block, centred on the page. */
    private static final float FOOTER_WIDTH = 460f;

    public byte[] generate(Certificate certificate) {
        Document document = new Document(PageSize.A4.rotate(), 48, 48, 48, 48);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        PdfWriter writer = PdfWriter.getInstance(document, out);
        document.open();
        // Nothing is added through the document's flow, so the page would
        // otherwise be discarded as empty.
        writer.setPageEmpty(false);

        PdfContentByte canvas = writer.getDirectContent();
        Rectangle page = document.getPageSize();
        float centerX = page.getWidth() / 2f;

        drawBorder(canvas, page);
        drawAward(canvas, centerX, AWARD_TOP, AWARD_SIZE);

        // Letter-spaced to match the eyebrow's tracking on the web card.
        centered(canvas, spaced("CERTIFICATE OF COMPLETION"),
                font(FontFactory.HELVETICA, 14, Font.NORMAL, EYEBROW), centerX, EYEBROW_BASELINE);
        // The name is a serif on the web card.
        centered(canvas, certificate.getUser().getName(),
                font(FontFactory.TIMES_BOLD, 34, Font.BOLD, Color.BLACK), centerX, NAME_BASELINE);
        centered(canvas, "has successfully completed",
                font(FontFactory.HELVETICA, 13, Font.NORMAL, MUTED), centerX, COMPLETED_BASELINE);
        centered(canvas, certificate.getCourse().getTitle(),
                font(FontFactory.HELVETICA_BOLD, 22, Font.BOLD, COURSE_TITLE), centerX, COURSE_BASELINE);

        drawVerifiedPill(canvas, centerX);
        drawFooter(canvas, centerX, certificate);

        document.close();
        return out.toByteArray();
    }

    /** The two-column "Certificate number" / "Issued" block, above a rule. */
    private void drawFooter(PdfContentByte canvas, float centerX, Certificate certificate) {
        float left = centerX - FOOTER_WIDTH / 2f;
        float right = left + FOOTER_WIDTH;
        float columnGap = 16f;
        float secondColumn = left + (FOOTER_WIDTH + columnGap) / 2f;

        canvas.setColorStroke(RULE);
        canvas.setLineWidth(1f);
        canvas.moveTo(left, RULE_Y);
        canvas.lineTo(right, RULE_Y);
        canvas.stroke();

        Font labelFont = font(FontFactory.HELVETICA, 10, Font.NORMAL, MUTED);
        leftAligned(canvas, "Certificate number", labelFont, left, FOOTER_LABEL_BASELINE);
        leftAligned(canvas, "Issued", labelFont, secondColumn, FOOTER_LABEL_BASELINE);

        // Monospaced, as the web card renders the number in font-mono.
        leftAligned(canvas, certificate.getCertificateNumber(),
                font(FontFactory.COURIER_BOLD, 13, Font.BOLD, Color.BLACK), left, FOOTER_VALUE_BASELINE);
        // "July 16, 2026" — the Intl "long" style both clients render.
        SimpleDateFormat formatter = new SimpleDateFormat("MMMM d, yyyy", Locale.ENGLISH);
        leftAligned(canvas, formatter.format(certificate.getIssuedAt()),
                font(FontFactory.HELVETICA_BOLD, 13, Font.BOLD, Color.BLACK), secondColumn,
                FOOTER_VALUE_BASELINE);
    }

    /**
     * The green "Verified by EduPortal" badge: a rounded tint behind a check
     * mark and its label, sized to the text so it hugs the content like the
     * web pill does.
     */
    private void drawVerifiedPill(PdfContentByte canvas, float centerX) {
        String label = "Verified by EduPortal";
        Font labelFont = font(FontFactory.HELVETICA, 12, Font.NORMAL, VERIFIED_TEXT);
        float textWidth = labelFont.getBaseFont().getWidthPoint(label, labelFont.getSize());

        float iconSize = 14f;
        float iconGap = 6f;
        float paddingX = 14f;
        float width = paddingX * 2 + iconSize + iconGap + textWidth;
        float left = centerX - width / 2f;

        canvas.setColorFill(VERIFIED_TINT);
        canvas.roundRectangle(left, PILL_BOTTOM, width, PILL_HEIGHT, PILL_HEIGHT / 2f);
        canvas.fill();

        float iconTop = PILL_BOTTOM + (PILL_HEIGHT + iconSize) / 2f;
        drawCheckCircle(canvas, left + paddingX, iconTop, iconSize);

        // Roughly optical centring for a 12pt face in a 28pt pill.
        float baseline = PILL_BOTTOM + (PILL_HEIGHT - labelFont.getSize()) / 2f + 2.5f;
        leftAligned(canvas, label, labelFont, left + paddingX + iconSize + iconGap, baseline);
    }

    /**
     * The Lucide "award" mark the web and mobile cards both show: a ring with
     * two ribbon tails. Drawn from the icon's 24x24 viewBox, with SVG's
     * top-down y axis flipped to the PDF's bottom-up one.
     */
    private void drawAward(PdfContentByte canvas, float centerX, float top, float size) {
        float scale = size / 24f;
        float originX = centerX - size / 2f;
        float originY = top - size;

        canvas.saveState();
        canvas.setColorStroke(AWARD);
        canvas.setLineWidth(2f * scale);
        canvas.setLineCap(PdfContentByte.LINE_CAP_ROUND);
        canvas.setLineJoin(PdfContentByte.LINE_JOIN_ROUND);

        // <circle cx="12" cy="8" r="6" />
        canvas.circle(originX + 12f * scale, originY + (24f - 8f) * scale, 6f * scale);
        canvas.stroke();

        // <path d="M15.477 12.89 17 22l-5-3-5 3 1.523-9.11" />
        canvas.moveTo(originX + 15.477f * scale, originY + (24f - 12.89f) * scale);
        canvas.lineTo(originX + 17f * scale, originY + (24f - 22f) * scale);
        canvas.lineTo(originX + 12f * scale, originY + (24f - 19f) * scale);
        canvas.lineTo(originX + 7f * scale, originY + (24f - 22f) * scale);
        canvas.lineTo(originX + 8.523f * scale, originY + (24f - 12.89f) * scale);
        canvas.stroke();
        canvas.restoreState();
    }

    /** Lucide "check-circle-2", used inside the verified pill. */
    private void drawCheckCircle(PdfContentByte canvas, float left, float top, float size) {
        float scale = size / 24f;
        float originY = top - size;

        canvas.saveState();
        canvas.setColorStroke(VERIFIED_TEXT);
        canvas.setLineWidth(2.2f * scale);
        canvas.setLineCap(PdfContentByte.LINE_CAP_ROUND);
        canvas.setLineJoin(PdfContentByte.LINE_JOIN_ROUND);

        // <circle cx="12" cy="12" r="10" />
        canvas.circle(left + 12f * scale, originY + 12f * scale, 10f * scale);
        canvas.stroke();

        // <path d="m9 12 2 2 4-4" />
        canvas.moveTo(left + 9f * scale, originY + (24f - 12f) * scale);
        canvas.lineTo(left + 11f * scale, originY + (24f - 14f) * scale);
        canvas.lineTo(left + 15f * scale, originY + (24f - 10f) * scale);
        canvas.stroke();
        canvas.restoreState();
    }

    /** The card's double border, in the web card's amber. */
    private void drawBorder(PdfContentByte canvas, Rectangle page) {
        canvas.saveState();
        canvas.setColorStroke(FRAME);
        canvas.setLineWidth(3f);
        canvas.rectangle(28, 28, page.getWidth() - 56, page.getHeight() - 56);
        canvas.stroke();

        canvas.setLineWidth(1f);
        canvas.rectangle(38, 38, page.getWidth() - 76, page.getHeight() - 76);
        canvas.stroke();
        canvas.restoreState();
    }

    /** Approximates the eyebrow's wide letter-spacing. */
    private String spaced(String text) {
        StringBuilder builder = new StringBuilder();
        for (char character : text.toCharArray()) {
            builder.append(character).append(' ');
        }
        return builder.toString().trim();
    }

    private void centered(PdfContentByte canvas, String text, Font font, float x, float baseline) {
        ColumnText.showTextAligned(canvas, Element.ALIGN_CENTER, new Phrase(text, font), x, baseline, 0);
    }

    private void leftAligned(PdfContentByte canvas, String text, Font font, float x, float baseline) {
        ColumnText.showTextAligned(canvas, Element.ALIGN_LEFT, new Phrase(text, font), x, baseline, 0);
    }

    private Font font(String family, float size, int style, Color color) {
        return FontFactory.getFont(family, size, style, color);
    }
}
