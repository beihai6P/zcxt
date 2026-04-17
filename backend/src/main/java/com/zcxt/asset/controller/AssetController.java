package com.zcxt.asset.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zcxt.asset.entity.AssetBase;
import com.zcxt.asset.entity.AssetChangeHistory;
import com.zcxt.asset.service.AssetService;
import com.zcxt.common.web.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import com.alibaba.excel.EasyExcel;
import com.zcxt.asset.dto.AssetExcelRow;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import com.zcxt.asset.service.AssetApprovalService;
import com.zcxt.security.UserPrincipal;
import com.zcxt.system.annotation.LogOperation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@RestController
@RequestMapping("/api/assets")
public class AssetController {
    private final AssetService assetService;
    private final AssetApprovalService assetApprovalService;

    public AssetController(AssetService assetService, AssetApprovalService assetApprovalService) {
        this.assetService = assetService;
        this.assetApprovalService = assetApprovalService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('asset:read')")
    public ApiResponse<Page<AssetBase>> page(
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "10") @Min(1) int size,
            @RequestParam(required = false) String assetType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String deptId,
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String keyword
    ) {
        return ApiResponse.ok(assetService.page(page, size, assetType, status, deptId, userId, keyword));
    }

    @GetMapping("/{assetId}")
    @PreAuthorize("hasAuthority('asset:read')")
    public ApiResponse<AssetBase> get(@PathVariable String assetId) {
        var asset = assetService.getById(assetId);
        if (asset == null) {
            return ApiResponse.fail("资产不存在");
        }
        return ApiResponse.ok(asset);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('asset:write')")
    public ApiResponse<AssetBase> create(@Valid @RequestBody AssetCreateRequest req) {
        AssetBase a = new AssetBase();
        a.setAssetId(req.assetId());
        a.setAssetType(req.assetType());
        a.setAssetName(req.assetName());
        a.setModel(req.model());
        a.setSpecification(req.specification());
        a.setPurchaseDate(req.purchaseDate());
        a.setPurchasePrice(req.purchasePrice());
        a.setSupplier(req.supplier());
        a.setDeptId(req.deptId());
        a.setUserId(req.userId());
        a.setStatus(req.status());
        a.setImageUrl(req.imageUrl());
        a.setRemark(req.remark());
        return ApiResponse.ok(assetService.create(a));
    }

    @PutMapping("/{assetId}")
    @PreAuthorize("hasAuthority('asset:write')")
    public ApiResponse<AssetBase> update(@PathVariable String assetId, @RequestBody AssetUpdateRequest req) {
        AssetBase patch = new AssetBase();
        patch.setAssetType(req.assetType());
        patch.setAssetName(req.assetName());
        patch.setModel(req.model());
        patch.setSpecification(req.specification());
        patch.setPurchaseDate(req.purchaseDate());
        patch.setPurchasePrice(req.purchasePrice());
        patch.setSupplier(req.supplier());
        patch.setDeptId(req.deptId());
        patch.setUserId(req.userId());
        patch.setStatus(req.status());
        patch.setImageUrl(req.imageUrl());
        patch.setRemark(req.remark());
        return ApiResponse.ok(assetService.update(assetId, patch));
    }

    @PostMapping("/{assetId}/apply")
    @LogOperation("提交资产变更申请")
    @PreAuthorize("hasAuthority('asset:read')")
    public ApiResponse<Void> apply(@PathVariable String assetId, @Valid @RequestBody ApplyRequest req) {
        String applicantId = "system";
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserPrincipal p) {
            applicantId = p.getUserId();
        }
        assetApprovalService.createRequest(assetId, applicantId, req.applyType(), req.applyReason(), req.targetStatus(), req.targetDeptId(), req.targetUserId());
        return ApiResponse.ok(null);
    }

    @PostMapping("/{assetId}/change")
    @PreAuthorize("hasRole('role-super')")
    public ApiResponse<Void> change(@PathVariable String assetId, @Valid @RequestBody ChangeRequest req) {
        assetService.changeStatus(assetId, new AssetService.ChangeRequest(req.changeType(), req.newStatus(), req.deptId(), req.userId(), req.approverId(), req.reason()));
        return ApiResponse.ok(null);
    }

    @GetMapping("/{assetId}/history")
    @PreAuthorize("hasAuthority('asset:read')")
    public ApiResponse<Page<AssetChangeHistory>> history(
            @PathVariable String assetId,
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "10") @Min(1) int size
    ) {
        return ApiResponse.ok(assetService.history(assetId, page, size));
    }

    @GetMapping("/export")
    public void exportAssets(HttpServletResponse response) throws IOException {
        // 设置响应头
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = "资产明细.xlsx";
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename=" + encodedFileName);
        
        System.out.println("开始导出Excel文件...");
        
        try (OutputStream outputStream = response.getOutputStream()) {
            // 使用Apache POI直接创建Excel文件
            org.apache.poi.ss.usermodel.Workbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook();
            org.apache.poi.ss.usermodel.Sheet sheet = workbook.createSheet("资产列表");
            
            // 创建表头
            org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(0);
            String[] headers = {"资产编号", "资产类型", "资产名称", "型号", "规格", "采购日期", "采购价格", "供应商", "部门ID", "使用人ID", "状态", "备注"};
            for (int i = 0; i < headers.length; i++) {
                org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }
            
            // 写入工作簿到输出流
            workbook.write(outputStream);
            workbook.close();
            
            outputStream.flush();
            System.out.println("Excel文件导出成功");
        } catch (Exception e) {
            System.err.println("Excel导出失败: " + e.getMessage());
            e.printStackTrace();
            // 确保响应状态设置为500
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("Excel导出失败: " + e.getMessage());
        }
    }

    @PostMapping("/import")
    @PreAuthorize("hasAuthority('asset:write')")
    public ApiResponse<ImportResult> importAssets(@RequestParam("file") MultipartFile file) {
        List<AssetExcelRow> rows = new java.util.ArrayList<>();
        try {
            // 检查文件是否为空
            if (file.isEmpty()) {
                return ApiResponse.fail("文件不能为空");
            }
            
            // 检查文件类型
            String fileName = file.getOriginalFilename();
            if (fileName == null || (!fileName.endsWith(".xlsx") && !fileName.endsWith(".xls"))) {
                return ApiResponse.fail("只支持 .xlsx 和 .xls 格式的 Excel 文件");
            }
            
            // 输出文件信息
            System.out.println("导入文件: " + fileName);
            System.out.println("文件大小: " + file.getSize() + " bytes");
            
            // 使用Apache POI读取Excel文件
            org.apache.poi.ss.usermodel.Workbook workbook = null;
            try {
                workbook = org.apache.poi.ss.usermodel.WorkbookFactory.create(file.getInputStream());
                System.out.println("成功创建Workbook");
            } catch (Exception e) {
                System.out.println("Excel文件格式错误: " + e.getMessage());
                e.printStackTrace();
                return ApiResponse.fail("Excel文件格式错误: " + e.getMessage());
            }
            
            if (workbook == null) {
                return ApiResponse.fail("无法读取Excel文件");
            }
            
            org.apache.poi.ss.usermodel.Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) {
                workbook.close();
                return ApiResponse.fail("Excel文件中没有工作表");
            }
            
            System.out.println("工作表名称: " + sheet.getSheetName());
            System.out.println("总行数: " + sheet.getLastRowNum());
            
            // 跳过表头，从第二行开始读取数据
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                org.apache.poi.ss.usermodel.Row row = sheet.getRow(i);
                if (row == null) continue;
                
                AssetExcelRow excelRow = new AssetExcelRow();
                
                // 读取各列数据
                if (row.getCell(0) != null) excelRow.setAssetId(row.getCell(0).getStringCellValue());
                if (row.getCell(1) != null) excelRow.setAssetType(row.getCell(1).getStringCellValue());
                if (row.getCell(2) != null) excelRow.setAssetName(row.getCell(2).getStringCellValue());
                if (row.getCell(3) != null) excelRow.setModel(row.getCell(3).getStringCellValue());
                if (row.getCell(4) != null) excelRow.setSpecification(row.getCell(4).getStringCellValue());
                if (row.getCell(5) != null) {
                    if (row.getCell(5).getCellType() == org.apache.poi.ss.usermodel.CellType.NUMERIC) {
                        // 处理日期类型
                        if (org.apache.poi.ss.usermodel.DateUtil.isCellDateFormatted(row.getCell(5))) {
                            java.util.Date date = row.getCell(5).getDateCellValue();
                            excelRow.setPurchaseDate(java.time.LocalDate.ofInstant(date.toInstant(), java.time.ZoneId.systemDefault()));
                        }
                    } else if (row.getCell(5).getCellType() == org.apache.poi.ss.usermodel.CellType.STRING) {
                        // 处理字符串类型的日期
                        try {
                            excelRow.setPurchaseDate(java.time.LocalDate.parse(row.getCell(5).getStringCellValue()));
                        } catch (Exception e) {
                            // 忽略日期解析错误
                        }
                    }
                }
                if (row.getCell(6) != null) {
                    if (row.getCell(6).getCellType() == org.apache.poi.ss.usermodel.CellType.NUMERIC) {
                        excelRow.setPurchasePrice(java.math.BigDecimal.valueOf(row.getCell(6).getNumericCellValue()));
                    } else if (row.getCell(6).getCellType() == org.apache.poi.ss.usermodel.CellType.STRING) {
                        try {
                            excelRow.setPurchasePrice(new java.math.BigDecimal(row.getCell(6).getStringCellValue()));
                        } catch (Exception e) {
                            // 忽略价格解析错误
                        }
                    }
                }
                if (row.getCell(7) != null) excelRow.setSupplier(row.getCell(7).getStringCellValue());
                if (row.getCell(8) != null) excelRow.setDeptId(row.getCell(8).getStringCellValue());
                if (row.getCell(9) != null) excelRow.setUserId(row.getCell(9).getStringCellValue());
                if (row.getCell(10) != null) excelRow.setStatus(row.getCell(10).getStringCellValue());
                if (row.getCell(11) != null) excelRow.setRemark(row.getCell(11).getStringCellValue());
                
                rows.add(excelRow);
            }
            
            workbook.close();
        } catch (Exception e) {
            // 记录详细错误信息
            System.err.println("Excel解析失败: " + e.getMessage());
            e.printStackTrace();
            return ApiResponse.fail("Excel解析失败: " + (e.getMessage() != null ? e.getMessage() : "未知错误"));
        }

        int success = 0;
        int fail = 0;
        java.util.List<ImportFailure> failures = new java.util.ArrayList<>();
        for (int i = 0; i < rows.size(); i++) {
            int rowNum = i + 2;
            AssetExcelRow r = rows.get(i);
            try {
                // 只做最基本的校验，允许字段为空
                if (r.getStatus() == null || r.getStatus().isBlank()) {
                    r.setStatus("闲置");
                }
                
                AssetBase patch = new AssetBase();
                // 自动生成资产ID（如果为空）
                if (r.getAssetId() == null || r.getAssetId().isBlank()) {
                    patch.setAssetId("asset-" + java.util.UUID.randomUUID().toString().substring(0, 8));
                } else {
                    patch.setAssetId(r.getAssetId());
                }
                patch.setAssetType(r.getAssetType());
                patch.setAssetName(r.getAssetName());
                patch.setModel(r.getModel());
                patch.setSpecification(r.getSpecification());
                // 如果 purchase_date 为空，设置默认值为当前日期
                if (r.getPurchaseDate() == null) {
                    patch.setPurchaseDate(java.time.LocalDate.now());
                } else {
                    patch.setPurchaseDate(r.getPurchaseDate());
                }
                patch.setPurchasePrice(r.getPurchasePrice());
                patch.setSupplier(r.getSupplier());
                patch.setDeptId(r.getDeptId());
                patch.setUserId(r.getUserId());
                patch.setStatus(r.getStatus());
                patch.setRemark(r.getRemark());

                if (assetService.getById(patch.getAssetId()) != null) {
                    // 更新现有资产
                    assetService.update(patch.getAssetId(), patch);
                } else {
                    // 创建新资产
                    assetService.create(patch);
                }
                success++;
            } catch (Exception ex) {
                fail++;
                if (failures.size() < 50) {
                    failures.add(new ImportFailure(rowNum, ex.getMessage() == null ? "导入失败" : ex.getMessage()));
                }
            }
        }

        return ApiResponse.ok(new ImportResult(success, fail, failures));
    }

    public record ImportFailure(int row, String message) {
    }

    public record ImportResult(int successCount, int failCount, List<ImportFailure> failures) {
    }

    public record AssetCreateRequest(
            String assetId,
            @NotBlank String assetType,
            @NotBlank String assetName,
            @NotBlank String model,
            String specification,
            @NotNull java.time.LocalDate purchaseDate,
            java.math.BigDecimal purchasePrice,
            String supplier,
            @NotBlank String deptId,
            String userId,
            String status,
            String imageUrl,
            String remark
    ) {
    }

    public record AssetUpdateRequest(
            String assetType,
            String assetName,
            String model,
            String specification,
            java.time.LocalDate purchaseDate,
            java.math.BigDecimal purchasePrice,
            String supplier,
            String deptId,
            String userId,
            String status,
            String imageUrl,
            String remark
    ) {
    }

    public record ChangeRequest(
            @NotBlank String changeType,
            @NotBlank String newStatus,
            String deptId,
            String userId,
            String approverId,
            String reason
    ) {
    }

    public record ApplyRequest(
            @NotBlank String applyType,
            String applyReason,
            @NotBlank String targetStatus,
            String targetDeptId,
            String targetUserId
    ) {
    }
}
