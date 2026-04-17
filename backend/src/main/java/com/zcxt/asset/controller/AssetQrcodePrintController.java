package com.zcxt.asset.controller;

import com.zcxt.asset.entity.AssetBase;
import com.zcxt.asset.mapper.AssetBaseMapper;
import com.zcxt.asset.service.QrcodeService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/assets/qrcodes")
public class AssetQrcodePrintController {
    private final AssetBaseMapper assetBaseMapper;
    private final QrcodeService qrcodeService;

    public AssetQrcodePrintController(AssetBaseMapper assetBaseMapper, QrcodeService qrcodeService) {
        this.assetBaseMapper = assetBaseMapper;
        this.qrcodeService = qrcodeService;
    }

    @PostMapping(value = "/print", produces = MediaType.APPLICATION_PDF_VALUE)
    @PreAuthorize("hasAuthority('asset:write')")
    public byte[] print(@Valid @RequestBody PrintRequest req) {
        int cols = Math.min(Math.max(req.cols(), 1), 4);
        int rows = Math.min(Math.max(req.rows(), 1), 8);

        List<AssetBase> assets = new ArrayList<>();
        for (String id : req.assetIds()) {
            AssetBase a = assetBaseMapper.selectById(id);
            if (a != null && a.getQrcodeContent() != null && !a.getQrcodeContent().isBlank()) {
                assets.add(a);
            }
        }
        if (assets.isEmpty()) {
            throw new IllegalStateException("未找到可打印的资产二维码");
        }

        try (PDDocument doc = new PDDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PDType1Font font = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
            int index = 0;
            while (index < assets.size()) {
                PDPage page = new PDPage(PDRectangle.A4);
                doc.addPage(page);
                PDRectangle box = page.getMediaBox();

                float margin = 24f;
                float cellW = (box.getWidth() - margin * 2) / cols;
                float cellH = (box.getHeight() - margin * 2) / rows;

                try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                    for (int r = 0; r < rows && index < assets.size(); r++) {
                        for (int c = 0; c < cols && index < assets.size(); c++) {
                            AssetBase a = assets.get(index++);
                            float x = margin + c * cellW;
                            float yTop = box.getHeight() - margin - r * cellH;

                            float qrSize = Math.min(cellW, cellH) - 24f;
                            qrSize = Math.min(qrSize, 140f);
                            byte[] png = qrcodeService.generatePngBytes(a.getQrcodeContent(), 320);
                            PDImageXObject img = PDImageXObject.createFromByteArray(doc, png, a.getAssetId());

                            float imgX = x + 10f;
                            float imgY = yTop - 10f - qrSize;
                            cs.drawImage(img, imgX, imgY, qrSize, qrSize);

                            cs.beginText();
                            cs.setFont(font, 9);
                            cs.newLineAtOffset(imgX, imgY - 12f);
                            cs.showText(safeText(a.getAssetId()));
                            cs.endText();

                            cs.beginText();
                            cs.setFont(font, 9);
                            cs.newLineAtOffset(imgX, imgY - 24f);
                            cs.showText(safeText(a.getAssetName()));
                            cs.endText();
                        }
                    }
                }
            }

            doc.save(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("二维码打印生成失败");
        }
    }

    private String safeText(String s) {
        if (s == null) return "";
        String t = s.replaceAll("[\\r\\n\\t]", " ").trim();
        if (t.length() > 40) return t.substring(0, 40);
        return t;
    }

    public record PrintRequest(@NotEmpty List<String> assetIds, int cols, int rows) {
    }
}
