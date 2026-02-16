package com.harshwarghade.project.service;

import com.harshwarghade.project.entity.Transaction;
import com.harshwarghade.project.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountTransactionExportService {

    private final TransactionRepository transactionRepository;
    private final S3Service s3Service;

    private static final int PAGE_SIZE = 5000; // Optimal for large datasets

    public String exportSingleAccountTransactions(Long accountId) throws Exception {

        String fileName = "account-" + accountId + "-transactions-" + UUID.randomUUID() + ".xlsx";
        File file = new File(fileName);

        try (SXSSFWorkbook workbook = new SXSSFWorkbook(100);
             FileOutputStream fos = new FileOutputStream(file)) {

            var sheet = workbook.createSheet("Transactions");

            // Header Row
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Transaction ID");
            header.createCell(1).setCellValue("Account ID");
            header.createCell(2).setCellValue("Amount");
            header.createCell(3).setCellValue("Type");
            header.createCell(4).setCellValue("Timestamp");

            int rowNum = 1;
            int page = 0;

            while (true) {
                Page<Transaction> txnPage =
                        transactionRepository.findByAccountId(
                                accountId,
                                PageRequest.of(page, PAGE_SIZE)
                        );

                if (txnPage.isEmpty()) break;

                for (Transaction txn : txnPage.getContent()) {
                    Row row = sheet.createRow(rowNum++);
                    row.createCell(0).setCellValue(txn.getId());
                    row.createCell(1).setCellValue(txn.getAccount().getId());
                    row.createCell(2).setCellValue(txn.getAmount());
                    row.createCell(3).setCellValue(txn.getType().name());
                    row.createCell(4).setCellValue(txn.getTimestamp().toString());
                }

                System.out.println("Exported page: " + page + " for account: " + accountId);
                page++;
            }

            workbook.write(fos);
        }

        // Upload to S3
        String s3Key = "exports/account-" + accountId + "/" + fileName;
        s3Service.uploadFile(file, s3Key);

        // Generate Pre-Signed URL (1 hour)
        String url = s3Service.generatePresignedUrl(s3Key);

        // Delete temp local file
        file.delete();

        return url;
    }
}
