package org.example.util;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.model.TrainingApplication;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public class ExcelExporter {

    private static final String[] HEADERS = {
            "ID",
            "ФИО",
            "Email",
            "Телефон",
            "Курс",
            "Дата",
            "Статус",
            "Форма обучения",
            "Комментарий"
    };

    private ExcelExporter() {
    }

    public static void export(
            List<TrainingApplication> applications,
            String fileName
    ) throws IOException {
        export(applications, new File(fileName));
    }

    public static void export(
            List<TrainingApplication> applications,
            File file
    ) throws IOException {

        try (Workbook workbook = new XSSFWorkbook()) {

            Sheet sheet = workbook.createSheet("Заявки");

            CellStyle headerStyle = workbook.createCellStyle();

            Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);

            Row header = sheet.createRow(0);

            for (int i = 0; i < HEADERS.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(HEADERS[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowNumber = 1;

            for (TrainingApplication application : applications) {

                Row row = sheet.createRow(rowNumber++);

                row.createCell(0)
                        .setCellValue(application.getId());

                row.createCell(1)
                        .setCellValue(
                                application.getUser() == null
                                        ? ""
                                        : application.getUser().getFullName()
                        );

                row.createCell(2)
                        .setCellValue(
                                application.getUser() == null
                                        ? ""
                                        : application.getUser().getEmail()
                        );

                row.createCell(3)
                        .setCellValue(
                                application.getUser() == null
                                        ? ""
                                        : application.getUser().getPhone()
                        );

                row.createCell(4)
                        .setCellValue(
                                application.getCourseName() == null
                                        ? ""
                                        : application.getCourseName()
                        );

                row.createCell(5)
                        .setCellValue(
                                application.getApplicationDate() == null
                                        ? ""
                                        : application.getApplicationDate().toString()
                        );

                row.createCell(6)
                        .setCellValue(
                                application.getStatus() == null
                                        ? ""
                                        : application.getStatus().getDisplayName()
                        );

                row.createCell(7)
                        .setCellValue(
                                application.getEducationType() == null
                                        ? ""
                                        : application.getEducationType().getDisplayName()
                        );

                row.createCell(8)
                        .setCellValue(
                                application.getComment() == null
                                        ? ""
                                        : application.getComment()
                        );
            }

            for (int i = 0; i < HEADERS.length; i++) {
                sheet.autoSizeColumn(i);
            }

            try (OutputStream outputStream =
                         new FileOutputStream(file)) {

                workbook.write(outputStream);
            }
        }
    }
}
