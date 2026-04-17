package com.zcxt.system.service;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

@Service
public class SystemDataService {
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    private static final Path DATA_DIR = Path.of("./data");
    private static final Path BACKUP_DIR = Path.of("./data/backups");

    public BackupResult backup() {
        String fileName = "backup_" + LocalDateTime.now().format(FMT) + ".zip";
        Path target = BACKUP_DIR.resolve(fileName);
        try {
            Files.createDirectories(BACKUP_DIR);
            try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(target))) {
                zipDirIfExists(zos, DATA_DIR.resolve("h2"), "h2");
                zipDirIfExists(zos, DATA_DIR.resolve("qrcodes"), "qrcodes");
                zipDirIfExists(zos, DATA_DIR.resolve("uploads"), "uploads");
            }
        } catch (Exception e) {
            throw new IllegalStateException("备份失败");
        }
        return new BackupResult(fileName, target.toAbsolutePath().toString());
    }

    public List<String> listBackups() {
        try {
            if (!Files.exists(BACKUP_DIR)) return List.of();
            return Files.list(BACKUP_DIR)
                    .filter(p -> p.getFileName().toString().endsWith(".zip"))
                    .sorted(Comparator.comparing((Path p) -> p.getFileName().toString()).reversed())
                    .map(p -> p.getFileName().toString())
                    .toList();
        } catch (Exception e) {
            return List.of();
        }
    }

    public Path getBackupFile(String fileName) {
        if (fileName == null || fileName.isBlank()) throw new IllegalStateException("文件名为空");
        if (fileName.contains("..") || fileName.contains("/") || fileName.contains("\\")) throw new IllegalStateException("非法文件名");
        Path f = BACKUP_DIR.resolve(fileName).normalize();
        if (!Files.exists(f)) throw new IllegalStateException("备份文件不存在");
        return f;
    }

    public void restore(String fileName) {
        Path zip = getBackupFile(fileName);
        try (InputStream in = Files.newInputStream(zip); ZipInputStream zis = new ZipInputStream(in)) {
            Files.createDirectories(DATA_DIR);
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    zis.closeEntry();
                    continue;
                }
                String name = entry.getName();
                if (name.contains("..")) {
                    zis.closeEntry();
                    continue;
                }
                Path out = DATA_DIR.resolve(name).normalize();
                if (!out.startsWith(DATA_DIR)) {
                    zis.closeEntry();
                    continue;
                }
                Files.createDirectories(out.getParent());
                Files.copy(zis, out, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                zis.closeEntry();
            }
        } catch (Exception e) {
            throw new IllegalStateException("恢复失败");
        }
    }

    public int cleanOldBackups(int keep) {
        int k = Math.min(Math.max(keep, 1), 50);
        List<String> all = listBackups();
        if (all.size() <= k) return 0;
        int deleted = 0;
        for (int i = k; i < all.size(); i++) {
            try {
                Files.deleteIfExists(getBackupFile(all.get(i)));
                deleted++;
            } catch (Exception ignored) {
            }
        }
        return deleted;
    }

    private void zipDirIfExists(ZipOutputStream zos, Path dir, String prefix) throws Exception {
        if (!Files.exists(dir)) return;
        if (!Files.isDirectory(dir)) return;
        Files.walk(dir).filter(Files::isRegularFile).forEach(p -> {
            String rel = dir.relativize(p).toString().replace("\\", "/");
            String entryName = prefix + "/" + rel;
            try {
                zos.putNextEntry(new ZipEntry(entryName));
                Files.copy(p, zos);
                zos.closeEntry();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    public record BackupResult(String fileName, String path) {
    }
}
