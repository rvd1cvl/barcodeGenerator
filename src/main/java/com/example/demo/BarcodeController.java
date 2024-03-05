package com.example.demo;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.oned.Code128Writer;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
public class BarcodeController {

    @GetMapping(value = "/generateBarcode", produces = MediaType.IMAGE_PNG_VALUE)
    public byte[] generateBarcode(@RequestParam String data) throws WriterException, IOException {
        int barcodeWidth = 300;
        int barcodeHeight = 100;
        int dataPaddingTop = 10;

        // Настройки штрих-кода
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");

        Code128Writer code128Writer = new Code128Writer();
        BitMatrix bitMatrix = code128Writer.encode(data, BarcodeFormat.CODE_128, barcodeWidth, barcodeHeight, hints);

        // Создаем изображение для штрих-кода
        BufferedImage barcodeImage = new BufferedImage(barcodeWidth, barcodeHeight, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < barcodeWidth; x++) {
            for (int y = 0; y < barcodeHeight; y++) {
                barcodeImage.setRGB(x, y, bitMatrix.get(x, y) ? Color.BLACK.getRGB() : Color.WHITE.getRGB());
            }
        }

        // Создаем изображение для текста с данными
        BufferedImage textImage = new BufferedImage(barcodeWidth, dataPaddingTop + 30, BufferedImage.TYPE_INT_RGB);
        Graphics2D textGraphics = textImage.createGraphics();
        textGraphics.setColor(Color.WHITE);
        textGraphics.fillRect(0, 0, barcodeWidth, dataPaddingTop + 30);
        textGraphics.setColor(Color.BLACK);
        textGraphics.setFont(new Font("Arial", Font.PLAIN, 12));
        FontMetrics fontMetrics = textGraphics.getFontMetrics();
        int textWidth = fontMetrics.stringWidth(data);
        int textHeight = fontMetrics.getHeight();
        int x = (barcodeWidth - textWidth) / 2;
        int y = dataPaddingTop + (30 - textHeight) / 2;
        textGraphics.drawString(data, x, y);
        textGraphics.dispose();

        // Объединяем штрих-код и данные
        BufferedImage combinedImage = new BufferedImage(barcodeWidth, barcodeHeight + dataPaddingTop + 30, BufferedImage.TYPE_INT_RGB);
        Graphics2D combinedGraphics = combinedImage.createGraphics();
        combinedGraphics.drawImage(barcodeImage, 0, 0, null);
        combinedGraphics.drawImage(textImage, 0, barcodeHeight, null);
        combinedGraphics.dispose();

        // Конвертируем изображение в массив байтов и возвращаем
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(combinedImage, "png", baos);
        return baos.toByteArray();
    }
}
